/*
 * Copyright 2015 Adaptris Ltd.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/

package com.adaptris.core.jms;

import java.util.function.Consumer;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageListener;
import com.adaptris.core.CoreException;
import com.adaptris.core.MessageLoggerImpl;
import com.adaptris.core.util.Args;
import com.adaptris.core.util.ExceptionHelper;

/**
 * <p>
 * Behaviour for <code>javax.jms.MessageListener.onMessage</code> is identical for polling and listening implementations. It can't
 * easily be shared using inheritance because polling consumers must extend <code>AdaptrisPollingConsumer</code>. Hence this utility
 * class to be used by both polling and listening imps. This class is not marshalled or configurable.
 * </p>
 */
public class OnMessageHandler {

  private transient Logger logR = null;

  private transient JmsActorConfig onMsgConfig;

  private enum AcknowledgeCommitOrRollBack {

    ManagedTransaction {
      @Override
      boolean maybe(JmsActorConfig c) throws JMSException {
        return c.isManagedTransaction();
      }

      @Override
      void perform(JmsActorConfig c, Message m) throws JMSException {
        c.currentLogger().trace("Managed transaction, not doing anything");
      }
    },
    CommitPerhapsRollback {
      @Override
      boolean maybe(JmsActorConfig c) throws JMSException {
        return c.currentSession().getTransacted();
      }

      @Override
      void perform(JmsActorConfig c, Message m) throws JMSException {
        AdaptrisMessageListener aml = c.configuredMessageListener();
        if (aml instanceof JmsTransactedWorkflow) {
          if (((JmsTransactedWorkflow) aml).lastMessageFailed()) {
            c.currentLogger().trace("Rolling back transaction because [{}] has failed", m.getJMSMessageID());
            c.currentSession().rollback();
            try {
              c.currentLogger().trace("Waiting for {}ms before continuing", c.rollbackTimeout());
              Thread.sleep(c.rollbackTimeout());
            }
            catch (InterruptedException ignored) {
              ;
            }
          }
          else {
            c.currentSession().commit();
          }
        }
        else {
          throw new IllegalStateException("Cannot be using a transacted session w/o using JmsTransactedWorkflow");
        }
      }
    },
    Acknowledge {
      @Override
      boolean maybe(JmsActorConfig c) throws JMSException {
        return c.configuredAcknowledgeMode() != Session.AUTO_ACKNOWLEDGE && !c.currentSession().getTransacted();
      }

      @Override
      void perform(JmsActorConfig c, Message m) throws JMSException {
        m.acknowledge();
      }

    };
    abstract boolean maybe(JmsActorConfig c) throws JMSException;

    abstract void perform(JmsActorConfig c, Message m) throws JMSException;

  };

  private OnMessageHandler() {
  }

  /**
   * <p>
   * Creates a new instance. Neither param may be null.
   * </p>
   *
   * @param cfg the <code>OnMessageConfig</code> to use
   */
  public OnMessageHandler(JmsActorConfig cfg) throws CoreException {
    this();
    onMsgConfig = Args.notNull(cfg, "onMsgConfig");
    logR = cfg.currentLogger() != null ? cfg.currentLogger() : LoggerFactory.getLogger(this.getClass());
    verify(onMsgConfig);
  }

  private static void verify(JmsActorConfig cfg) throws CoreException {
    try {
      Args.notNull(cfg.configuredCorrelationIdSource(), "correlationIdSource");
      Args.notNull(cfg.configuredMessageListener(), "messageListener");
      Args.notNull(cfg.configuredMessageTranslator(), "messageTranslator");
    } catch (IllegalArgumentException e) {
      throw ExceptionHelper.wrapCoreException(e);
    }
  }

  /**
   * <p>
   * Uses <code>translator</code> to create an <code>AdaptrisMessage</code> from
   * the <code>javax.jms.Message</code>. Adds the <code>JMSReplyTo</code> as
   * transient 'Object' metadata. Passes the <code>AdaptrisMessage</code> to
   * <code>listener</code>. Acknowledges <code>javax.jms.Message</code>.
   * </p>
   * <p>
   * NB if, as is almost always the case, <code>listener</code> is a
   * <code>StandardWorkflow</code>, <code>onAdaptrisMessage</code> will never
   * throw any <code>Throwable</code> and <code>acknowledge</code> will always
   * be called.
   * </p>
   */
  public void onMessage(Message msg) {
    AdaptrisMessage adaptrisMessage = null;
    AdaptrisMessageListener msgListener = onMsgConfig.configuredMessageListener();
    try {
      adaptrisMessage = MessageTypeTranslatorImp.translate(onMsgConfig.configuredMessageTranslator(), msg);
      onMsgConfig.configuredCorrelationIdSource().processCorrelationId(msg, adaptrisMessage);
    }
    catch (JMSException e) {
      logR.error("Failed to translate message into AdaptrisMessage", e);
      if (!rollback(msg)) {
        throw new RuntimeException(e);
      }
      return;
    }

    try {
      // Adaptris message can't be null at this point.
      adaptrisMessage.addObjectHeader(JmsConstants.OBJ_JMS_REPLY_TO_KEY, msg.getJMSReplyTo()); // lgtm
    }
    catch (JMSException ignored) {
      // this might throw an exception, if it does, we don't care.
    }

    Consumer<AdaptrisMessage> successCallback = message -> {
      try {
        logR.trace("Commiting/Ack'ing message with id {}", msg.getJMSMessageID());
        acknowledge(msg);
      }
      catch (JMSException e) {
        logR.error("Exception acknowledging/committing JMS message", e);
        rollback(msg);
      }
    };
    
    Consumer<AdaptrisMessage> failureCallback = message -> {
      try {
        logR.trace("Rolling back/not Ack'ing message with id {}", msg.getJMSMessageID());
        rollback(msg);
      } catch (JMSException e) {
        logR.error("Exception rolling back JMS message", e);
      }
      
    };
    
    try {
      msgListener.onAdaptrisMessage(adaptrisMessage, successCallback, failureCallback);
    }
    catch (Throwable e) { // impossible if AML is StandardWorkflow
      logR.error("Unexpected Throwable from AdaptrisMessageListener", e);
      logR.error("logging message [{}]",
          adaptrisMessage != null ? MessageLoggerImpl.LAST_RESORT_LOGGER.toString(adaptrisMessage)
              : "no data available");
    }

  }

  private void acknowledge(Message msg) throws JMSException {
    for (AcknowledgeCommitOrRollBack ack : AcknowledgeCommitOrRollBack.values()) {
      if (ack.maybe(onMsgConfig)) {
        ack.perform(onMsgConfig, msg);
        break;
      }
    }
  }

  private boolean rollback(Message msg) {
    boolean result = false;
    try {
      if (onMsgConfig.currentSession().getTransacted()) {
        logR.error("Exception processing message [{}], attempting rollback", msg.getJMSMessageID());
        onMsgConfig.currentSession().rollback();
        result = true;
      }
    }
    catch (JMSException f) {
      logR.error("Exception rolling back transacted session", f);
    }
    return result;
  }
}
