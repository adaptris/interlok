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

import static com.adaptris.core.AdaptrisMessageFactory.defaultIfNull;
import static com.adaptris.core.jms.JmsConstants.JMS_ASYNC_STATIC_REPLY_TO;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreException;
import com.adaptris.core.ProduceDestination;
import com.adaptris.core.ProduceException;
import com.adaptris.core.util.ExceptionHelper;

/**
 * <p>
 * Contains behaviour common to PTP and PAS JMS message producers.
 * </p>
 */
public abstract class DefinedJmsProducer extends JmsProducerImpl {


  public DefinedJmsProducer() {
    super();
  }

  public DefinedJmsProducer(ProduceDestination d) {
    this();
    setDestination(d);
  }


  /**
   * @see com.adaptris.core.AdaptrisMessageProducer#produce(AdaptrisMessage, ProduceDestination)
   */
  @Override
  public void produce(AdaptrisMessage msg, ProduceDestination destination) throws ProduceException {
    try {
      setupSession(msg);
      Destination replyTo = null;
      if (msg.containsKey(JMS_ASYNC_STATIC_REPLY_TO)) {
        replyTo = createDestination(msg.getMetadataValue(JMS_ASYNC_STATIC_REPLY_TO));
      }
      doProduce(msg, destination, replyTo);
    }
    catch (JMSException e) {
      logLinkedException("Creating Destination", e);
      throw ExceptionHelper.wrapProduceException(e);
    }
  }

  /*
   */
  private void doProduce(AdaptrisMessage msg, ProduceDestination dest, Destination replyTo) throws ProduceException {

    Destination jmsDest = null;

    try {
      if (dest != null) { // overloaded
        // First of all directly try to get a Destination object if available.
        jmsDest = createDestination(dest, msg);
        if (jmsDest == null) {
          String d = dest.getDestination(msg);
          if (d != null) {
            jmsDest = createDestination(d);
          }
        }
        if (jmsDest != null) {
          this.produce(msg, jmsDest, replyTo);
        }
        else {
          throw new ProduceException("No destination available from " + getDestination() + " to produce to");
        }
      }
      else {
        throw new ProduceException("Could not produce to null destination");
      }
      commit();
    }
    catch (JMSException e) {
      log.warn("Error producing to destination [{}]", getDestination());
      logLinkedException("Produce", e);
      rollback();
      throw new ProduceException(e);

    }
    catch (CoreException e) {
      throw ExceptionHelper.wrapProduceException(e);
    }
  }

  protected void produce(AdaptrisMessage msg, Destination destination, Destination replyTo) throws JMSException, CoreException {
    setupSession(msg);
    Message jmsMsg = translate(msg, replyTo);
    if (!perMessageProperties()) {
      producerSession.getProducer().send(destination, jmsMsg);
    }
    else {
      producerSession.getProducer().send(destination, jmsMsg, calculateDeliveryMode(msg, getDeliveryMode()),
          calculatePriority(msg, getPriority()), calculateTimeToLive(msg, timeToLive()));
    }
    if (captureOutgoingMessageDetails()) {
      captureOutgoingMessageDetails(jmsMsg, msg);
    }
    log.info("msg produced to destination [{}]", destination);
  }

  /**
   * <p>
   * Pseudo-synchronous JMS implementation of <code>request</code>. Works by i) setting a temporary queue as the
   * <code>JMSReplyTo</code> header on the message ii) setting up a temporary consumer on this reply-to queue. The message is then
   * sent normally using <code>produce</code>. If the temporary reply listener receives a reply before the timeout, it will create
   * and return a new <code>AdaptrisMessage</code>. Otherwise returns null.
   * </p>
   * 
   * @param msg the request messages
   * @param timeout the period to wait for a reply
   * @param dest the <code>ProduceDestination</code> to use
   * @return the reply or <code>null</code> if the timeout period expires
   * @throws ProduceException wrapping any underlying <code>Exception</code>s
   */
  @Override
  protected AdaptrisMessage doRequest(AdaptrisMessage msg, ProduceDestination dest, long timeout) throws ProduceException {

    AdaptrisMessage translatedReply = defaultIfNull(getMessageFactory()).newMessage();
    Destination replyTo = null;
    MessageConsumer receiver = null;
    try {
      setupSession(msg);
      getMessageTranslator().registerSession(producerSession.getSession());
      if (msg.containsKey(JMS_ASYNC_STATIC_REPLY_TO)) {
        replyTo = createDestination(msg.getMetadataValue(JMS_ASYNC_STATIC_REPLY_TO));
      }
      else {
        replyTo = createTemporaryDestination();
      }
      receiver = currentSession().createConsumer(replyTo);
      doProduce(msg, dest, replyTo);
      Message jmsReply = receiver.receive(timeout);

      if (jmsReply != null) {
        translatedReply = MessageTypeTranslatorImp.translate(getMessageTranslator(), jmsReply);
      }
      else {
        throw new JMSException("No Reply Received within " + timeout + "ms");
      }
      acknowledge(jmsReply);
      // BUG#915
      commit();
    }
    catch (JMSException e) {
      logLinkedException("", e);
      rollback();
      throw new ProduceException(e);
    }
    finally {
      JmsUtils.closeQuietly(receiver);
      JmsUtils.deleteTemporaryDestination(replyTo);
    }
    return translatedReply;
  }



  protected abstract Destination createDestination(String name) throws JMSException;

  protected abstract Destination createTemporaryDestination() throws JMSException;

}
