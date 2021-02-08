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
import java.util.Optional;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import com.adaptris.annotation.Removal;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreException;
import com.adaptris.core.ProduceDestination;
import com.adaptris.core.ProduceException;
import com.adaptris.core.util.ExceptionHelper;
import com.adaptris.interlok.util.Args;

/**
 * <p>
 * Contains behaviour common to PTP and PAS JMS message producers.
 * </p>
 */
// Note because we have JmsReplyToDestination, we have specialised behaviour here since
// we want to make sure that we actually use the JMSReplyTo if it's available.
// This is why DefinedJmsProducer doesn't extend RequestReplyProducer as normal, and
// this is an edge case that isn't that edgy!
public abstract class DefinedJmsProducer extends JmsProducerImpl {

  public DefinedJmsProducer() {
    super();
  }

  @Override
  public void produce(AdaptrisMessage msg) throws ProduceException {
    try {
      setupSession(msg);
      Destination replyTo = (Destination)msg.resolveObject(JMS_ASYNC_STATIC_REPLY_TO);
      doProduce(msg, replyTo);
    }
    catch (JMSException e) {
      logLinkedException("Creating Destination", e);
      throw ExceptionHelper.wrapProduceException(e);
    }
  }

  protected void doProduce(AdaptrisMessage msg, Destination replyTo)
      throws ProduceException {

    Destination jmsDest = null;
    try {
      // First of all directly try to get a Destination object if available.
      jmsDest = createDestination(msg);

      if (jmsDest == null) {
        String d = endpoint(msg);
        if (d != null) {
          jmsDest = createDestination(d);
        }
      }
      Args.notNull(jmsDest, "destination");
      doProduce(msg, jmsDest, replyTo);
      commit();
    }
    catch (Exception e) {
      log.warn("Error producing to destination [{}]", jmsDest);
      logLinkedException("Produce", e);
      rollback();
      throw ExceptionHelper.wrapProduceException(e);
    }
  }

  protected void doProduce(AdaptrisMessage msg, Destination destination, Destination replyTo)
      throws JMSException, CoreException {
    setupSession(msg);
    Message jmsMsg = translate(msg, replyTo);
    if (!perMessageProperties()) {
      producerSession().getProducer().send(destination, jmsMsg);
    }
    else {
      producerSession().getProducer().send(destination, jmsMsg,
          calculateDeliveryMode(msg, getDeliveryMode()),
          calculatePriority(msg, getPriority()), calculateTimeToLive(msg, timeToLive()));
    }
    captureOutgoingMessageDetails(jmsMsg, msg);
    log.info("msg produced to destination [{}]", destination);
  }

  @Override
  public AdaptrisMessage request(AdaptrisMessage msg, long timeout)
      throws ProduceException {

    AdaptrisMessage translatedReply = defaultIfNull(getMessageFactory()).newMessage();
    Destination replyTo = null;
    MessageConsumer receiver = null;
    try {
      setupSession(msg);
      getMessageTranslator().registerSession(producerSession().getSession());
      if (msg.headersContainsKey(JMS_ASYNC_STATIC_REPLY_TO)) {
        replyTo = createDestination(msg.getMetadataValue(JMS_ASYNC_STATIC_REPLY_TO));
      }
      else {
        replyTo = createTemporaryDestination();
      }
      receiver = currentSession().createConsumer(replyTo);
      doProduce(msg, replyTo);
      Message jmsReply = receiver.receive(timeout);
      translatedReply =
          Optional.ofNullable(MessageTypeTranslatorImp.translate(getMessageTranslator(), jmsReply))
              .orElseThrow(
                  () -> new JMSException("No Reply Received within " + timeout + "ms"));
      acknowledge(jmsReply);
      // BUG#915
      commit();
    }
    catch (Exception e) {
      logLinkedException("", e);
      rollback();
      throw new ProduceException(e);
    }
    finally {
      JmsUtils.closeQuietly(receiver);
      JmsUtils.deleteTemporaryDestination(replyTo);
    }
    return mergeReply(translatedReply, msg);
  }

  protected abstract Destination createDestination(AdaptrisMessage message) throws JMSException;

  protected abstract Destination createDestination(String name) throws JMSException;

  protected abstract Destination createTemporaryDestination() throws JMSException;

}
