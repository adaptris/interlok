package com.adaptris.core.jms;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageListener;
import com.adaptris.core.CoreException;

/**
 * <p>
 * Behaviour for <code>javax.jms.MessageListener.onMessage</code> is identical for polling and listening implementations. It can't
 * easily be shared using inheritance because polling consumers must extend <code>AdaptrisPollingConsumer</code>. Hence this utility
 * class to be used by both polling and listening imps. This class is not marshalled or configurable.
 * </p>
 */
class OnMessageHandler {

  private transient Logger logR = null;

  private transient JmsActorConfig onMsgConfig;

  private enum AcknowledgeCommitOrRollBack {

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
            c.currentLogger().trace("Rolling back transaction because " + m.getJMSMessageID() + " has failed");
            c.currentSession().rollback();
            try {
              c.currentLogger().trace("Waiting for " + c.rollbackTimeout() + "ms before continuing.");
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
  OnMessageHandler(JmsActorConfig cfg) throws CoreException {
    this();
    if (cfg == null) {
      throw new IllegalArgumentException("Null param");
    }
    logR = cfg.currentLogger() != null ? cfg.currentLogger() : LoggerFactory.getLogger(this.getClass());

    onMsgConfig = cfg;
    verify(onMsgConfig);
  }

  private static void verify(JmsActorConfig cfg) throws CoreException {
    if (cfg.configuredCorrelationIdSource() == null) {
      throw new CoreException("No CorrelationIdSource Configured");
    }
    if (cfg.configuredMessageListener() == null) {
      throw new CoreException("No AdaptrisMessageListener Configured");
    }
    if (cfg.configuredMessageTranslator() == null) {
      throw new CoreException("No MessageTypeTranslator Configured");
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
  void onMessage(Message msg) {
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
      adaptrisMessage.addObjectMetadata(JmsConstants.OBJ_JMS_REPLY_TO_KEY, msg.getJMSReplyTo());
    }
    catch (JMSException ignored) {
      // this might throw an exception, if it does, we don't care.
    }

    try {
      msgListener.onAdaptrisMessage(adaptrisMessage);
      try {
        acknowledge(msg);
      }
      catch (JMSException e) {
        logR.error("Exception acknowledging/committing JMS message", e);
        rollback(msg);
      }
    }
    catch (Throwable e) { // impossible if AML is StandardWorkflow
      logR.error("Unexpected Throwable from AdaptrisMessageListener", e);
      logR.error("logging message " + (adaptrisMessage != null ? adaptrisMessage.toString(true) : "no data available"));
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
        logR.error("Exception processing message [" + msg.getJMSMessageID() + "], attempting rollback");
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
