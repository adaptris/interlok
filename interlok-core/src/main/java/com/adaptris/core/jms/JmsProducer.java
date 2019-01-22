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
import static org.apache.commons.lang.StringUtils.isEmpty;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import org.apache.commons.lang3.BooleanUtils;
import com.adaptris.annotation.AdapterComponent;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.annotation.DisplayOrder;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreException;
import com.adaptris.core.ProduceDestination;
import com.adaptris.core.ProduceException;
import com.adaptris.core.util.ExceptionHelper;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * JMS Producer implementation that can target queues or topics via an RFC6167 style destination.
 * <p>
 * This differs from the standard {@link PtpProducer} and {@link PasProducer} in that it supports a
 * destination that is specified in RFC6167 style. For instance {@code jms:queue:myQueueName} will
 * produce to a queue called {@code myQueueName} and {@code jms:topic:myTopicName} to a topic called
 * {@code myTopicName}
 * </p>
 * <p>
 * While RFC6167 defines the ability to use jndi to lookup the (as part of the 'jndi' variant
 * section); this is not supported. The standard deliveryMode, timeToLive, priority, replyToName
 * properties are supported. If not specified, then they will be inherited from the producers
 * configuration. For instance you could have the following destinations:
 * <ul>
 * <li>jms:queue:MyQueueName</li>
 * <li>jms:topic:MyTopicName</li>
 * <li>jms:queue:MyQueueName?replyToName=StaticReplyTo&amp;priority=1&amp;timeToLive=1234</li>
 * <li>jms:topic:MyTopicName?replyToName=StaticReplyTo</li>
 * </ul>
 * </p>
 * <p>
 * As the RFC6167 string can specify priority, timeToLive and deliveryMode; this producer defaults
 * {@link #getPerMessageProperties()} to be true.
 * </p>
 * 
 * @config jms-producer
 * 
 */
@XStreamAlias("jms-producer")
@AdapterComponent
@ComponentProfile(summary = "Place message on a JMS queue or topic", tag = "producer,jms", recommended = {JmsConnection.class})
@DisplayOrder(order = {"destination", "messageTypeTranslator", "deliveryMode", "priority", "ttl", "acknowledgeMode"})
public class JmsProducer extends JmsProducerImpl {


  public JmsProducer() {
    super();
  }

  public JmsProducer(ProduceDestination d) {
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
      MyJmsDestination target = null;
      // First of all try and get a jms destination directory from the produce destination
      // (JmsReplyToDestination)
      Destination jmsDest = createDestination(destination, msg);
      if (jmsDest != null) {
        target = new MyJmsDestination(jmsDest);
      } else {
        // Otherwise it's a normal producer with nothing special.
        VendorImplementation vendorImp =
            retrieveConnection(JmsConnection.class).configuredVendorImplementation();
        String destString = destination.getDestination(msg);
        target = new MyJmsDestination(vendorImp.createDestination(destString, this));
        target.setReplyTo(createReplyTo(msg, target, false));
      }
      produce(msg, target);
    } catch (Exception e) {
      logLinkedException("", e);
      throw ExceptionHelper.wrapProduceException(e);
    }
  }

  private void produce(AdaptrisMessage msg, JmsDestination jmsDest)
      throws JMSException, CoreException {
    setupSession(msg);
    Message jmsMsg = translate(msg, jmsDest.getReplyToDestination());
    if (!perMessageProperties()) {
      producerSession.getProducer().send(jmsDest.getDestination(), jmsMsg);
    } else {
      producerSession.getProducer().send(jmsDest.getDestination(), jmsMsg,
          calculateDeliveryMode(msg, jmsDest.deliveryMode()),
          calculatePriority(msg, jmsDest.priority()),
          calculateTimeToLive(msg, jmsDest.timeToLive()));
    }
    if (captureOutgoingMessageDetails()) {
      captureOutgoingMessageDetails(jmsMsg, msg);
    }
    log.info("msg produced to destination [{}]", jmsDest);
  }

  @Override
  protected AdaptrisMessage doRequest(AdaptrisMessage msg, ProduceDestination dest, long timeout)
      throws ProduceException {

    AdaptrisMessage translatedReply = defaultIfNull(getMessageFactory()).newMessage();
    Destination replyTo = null;
    MessageConsumer receiver = null;
    try {
      setupSession(msg);
      VendorImplementation vendorImp =
          retrieveConnection(JmsConnection.class).configuredVendorImplementation();
      String destString = dest.getDestination(msg);
      MyJmsDestination target = new MyJmsDestination(vendorImp.createDestination(destString, this));
      replyTo = createReplyTo(msg, target, true);
      target.setReplyTo(replyTo);
      receiver = currentSession().createConsumer(replyTo);
      produce(msg, target);
      Message jmsReply = receiver.receive(timeout);

      if (jmsReply != null) {
        translatedReply = MessageTypeTranslatorImp.translate(getMessageTranslator(), jmsReply);
      } else {
        throw new JMSException("No Reply Received within " + timeout + "ms");
      }
      acknowledge(jmsReply);
      // BUG#915
      commit();
    } catch (Exception e) {
      logLinkedException("", e);
      rollback();
      throw ExceptionHelper.wrapProduceException(e);
    } finally {
      JmsUtils.closeQuietly(receiver);
      JmsUtils.deleteTemporaryDestination(replyTo);
    }
    return translatedReply;
  }


  private Destination createReplyTo(AdaptrisMessage msg, JmsDestination target, boolean alwaysCreate)
      throws JMSException {
    Destination replyTo = null;
    VendorImplementation vendorImp = retrieveConnection(JmsConnection.class).configuredVendorImplementation();
    if (target.getReplyToDestination() == null) {
      if (msg.headersContainsKey(JMS_ASYNC_STATIC_REPLY_TO)) {
        replyTo = target.destinationType().create(vendorImp, this, msg.getMetadataValue(JMS_ASYNC_STATIC_REPLY_TO));
      } else {
        replyTo = alwaysCreate ? target.destinationType().createTemporaryDestination(currentSession()) : null;
      }
    }
    return replyTo;
  }
  
  @Override
  protected boolean perMessageProperties() {
    return BooleanUtils.toBooleanDefaultIfNull(getPerMessageProperties(), true);
  }

  private class MyJmsDestination implements JmsDestination {

    private Destination destination;
    private String deliveryMode;
    private Destination replyTo;
    private Long timeToLive;
    private Integer priority;
    private String subscriptionId;
    private String sharedConsumerId;
    private boolean noLocal;
    private JmsDestination.DestinationType destType;

    MyJmsDestination(JmsDestination orig) {
      destination = orig.getDestination();
      replyTo = orig.getReplyToDestination();
      deliveryMode = isEmpty(orig.deliveryMode()) ? getDeliveryMode() : orig.deliveryMode();
      timeToLive = orig.timeToLive() != null ? orig.timeToLive() : timeToLive();
      priority = orig.priority() != null ? orig.priority() : messagePriority();
      destType = orig.destinationType();
      noLocal = orig.noLocal();
      subscriptionId = orig.subscriptionId();
      sharedConsumerId = orig.sharedConsumerId();
    }

    MyJmsDestination(Destination d) {
      destination = d;
      deliveryMode = getDeliveryMode();
      timeToLive = timeToLive();
      priority = messagePriority();
    }

    @Override
    public Destination getDestination() {
      return destination;
    }

    @Override
    public Destination getReplyToDestination() {
      return replyTo;
    }

    @Override
    public String deliveryMode() {
      return deliveryMode;
    }

    @Override
    public Long timeToLive() {
      return timeToLive;
    }

    @Override
    public Integer priority() {
      return priority;
    }

    private void setReplyTo(Destination replyTo) {
      this.replyTo = replyTo;
    }

    @Override
    public JmsDestination.DestinationType destinationType() {
      return destType;
    }

    @Override
    public String subscriptionId() {
      return subscriptionId;
    }

    @Override
    public boolean noLocal() {
      return noLocal;
    }

    public String toString() {
      return getDestination().toString();
    }

    @Override
    public String sharedConsumerId() {
      return sharedConsumerId;
    }
  }
}
