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

import static org.apache.commons.lang3.StringUtils.isEmpty;
import java.net.URI;
import java.net.URLDecoder;
import java.util.Map;
import javax.jms.Connection;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.Queue;
import javax.jms.Session;
import javax.jms.Topic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.adaptris.core.ConsumeDestination;
import com.adaptris.core.CoreException;
import com.adaptris.core.jms.JmsDestination.DestinationType;
import com.adaptris.util.URLHelper;

/**
 * <p>
 * Partial implementation with common or default behaviour.
 * </p>
 */
public abstract class VendorImplementationImp implements VendorImplementation {

  private static final String RFC6167_REPLY_TO_NAME = "replyToName";
  private static final String RFC6167_TIME_TO_LIVE = "timeToLive";
  private static final String RFC6167_PRIORITY = "priority";
  private static final String RFC6167_DELIVERY_MODE = "deliveryMode";
  private static final String RFC6167_SUBSCRIPTION_ID = "subscriptionId";
  private static final String RFC6167_NO_LOCAL = "noLocal";
  private static final String JMS20_SHARED_CONSUMER_ID = "sharedConsumerId";

  private static final String URI_CHARSET = "UTF-8";
  protected transient Logger log = LoggerFactory.getLogger(this.getClass());

  public VendorImplementationImp() {}

  @Override
  public void prepare() throws CoreException {}
  /**
   * @see com.adaptris.core.jms.VendorImplementation #retrieveBrokerDetailsForLogging()
   */
  @Override
  public String retrieveBrokerDetailsForLogging() {
    return null;
  }

  /**
   * 
   * @see VendorImplementation#createQueue(java.lang.String, JmsActorConfig)
   */
  @Override
  public Queue createQueue(String name, JmsActorConfig c) throws JMSException {
    return c.currentSession().createQueue(name);
  }

  /**
   * 
   * @see VendorImplementation#createTopic(java.lang.String, JmsActorConfig)
   */
  @Override
  public Topic createTopic(String name, JmsActorConfig c) throws JMSException {
    return c.currentSession().createTopic(name);
  }

  @Override
  public MessageConsumer createQueueReceiver(ConsumeDestination cd, JmsActorConfig c)
      throws JMSException {
    Session s = c.currentSession();
    Queue q = createQueue(cd.getDestination(), c);
    return s.createConsumer(q, cd.getFilterExpression());
  }

  @Override
  public MessageConsumer createConsumer(JmsDestination d, String selector, JmsActorConfig c) throws JMSException {
    MessageConsumer consumer = null;
    if (d.destinationType().equals(DestinationType.TOPIC) && !isEmpty(d.subscriptionId())) {
      consumer = c.currentSession().createDurableSubscriber((Topic) d.getDestination(), d.subscriptionId(), selector, d.noLocal());
    } else {
      consumer = c.currentSession().createConsumer(d.getDestination(), selector, d.noLocal());
    }
    return consumer;
  }

  @Override
  public MessageConsumer createTopicSubscriber(ConsumeDestination cd, String subscriptionId,
      JmsActorConfig c) throws JMSException {
    Session s = c.currentSession();
    Topic t = createTopic(cd.getDestination(), c);
    MessageConsumer result = null;
    if (!isEmpty(subscriptionId)) {
      result = s.createDurableSubscriber(t, subscriptionId, cd.getFilterExpression(), false);
    } else {
      result = s.createConsumer(t, cd.getFilterExpression());
    }
    return result;
  }

  @Override
  public Session createSession(Connection c, boolean transacted, int acknowledgeMode)
      throws JMSException {
    Session s = c.createSession(transacted, acknowledgeMode);
    applyVendorSessionProperties(s);
    return s;
  }

  /**
   * Empty implementation that does not apply any session properties. Concrete sub-classes should
   * override this method.
   * 
   */
  public void applyVendorSessionProperties(Session s) throws JMSException {}


  @Override
  public JmsDestination createDestination(String destination, JmsActorConfig c) throws JMSException {
    JmsDestinationImpl jmsDest = new JmsDestinationImpl();
    try {
      URI uri = new URI(destination);
      if (!uri.getScheme().equals("jms")) {
        throw new JMSException("failed to parse [" + destination + "]; doesn't start with 'jms'");
      }
      String[] parts = uri.getRawSchemeSpecificPart().split("\\?");
      String[] typeAndName = parts[0].split(":");
      String type = URLDecoder.decode(typeAndName[0], URI_CHARSET);
      String name = URLDecoder.decode(typeAndName[1], URI_CHARSET);
      jmsDest.destType = JmsDestination.DestinationType.valueOf(type.toUpperCase());

      jmsDest.setDestination(jmsDest.destType.create(this, c, name));

      if (parts.length > 1) {
        Map<String, String> params = URLHelper.queryStringToMap(parts[1], URI_CHARSET);
        jmsDest.setDeliveryMode(params.get(RFC6167_DELIVERY_MODE));
        jmsDest.setPriority(params.get(RFC6167_PRIORITY));
        jmsDest.setTimeToLive(params.get(RFC6167_TIME_TO_LIVE));
        jmsDest.setSubscriptionId(params.get(RFC6167_SUBSCRIPTION_ID));
        jmsDest.setSharedConsumerId(params.get(JMS20_SHARED_CONSUMER_ID));
        jmsDest.setNoLocal(params.get(RFC6167_NO_LOCAL));
        String replyToName = params.get(RFC6167_REPLY_TO_NAME);
        if (!isEmpty(replyToName)) {
          jmsDest.setReplyTo(jmsDest.destType.create(this, c, replyToName));
        }
      }
    } catch (NullPointerException e) {
      throw new JMSException("failed to parse [" + destination + "]; NullPointerException");
    } catch (Exception e) {
      JmsUtils.rethrowJMSException(e);
    }
    return jmsDest;
  }

  private class JmsDestinationImpl implements JmsDestination {

    private Destination destination;
    private String deliveryMode;
    private String subscriptionId;
    private String sharedConsumerId;
    private Destination replyTo;
    private Long timeToLive;
    private Integer priority;
    private JmsDestination.DestinationType destType;
    private boolean noLocal;

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

    private void setDeliveryMode(String deliveryMode) {
      this.deliveryMode = deliveryMode;
    }

    private void setReplyTo(Destination replyTo) {
      this.replyTo = replyTo;
    }

    private void setTimeToLive(String ttlString) {
      if (!isEmpty(ttlString)) {
        this.timeToLive = Long.valueOf(ttlString);
      }
    }

    private void setPriority(String priorityString) {
      if (!isEmpty(priorityString)) {
        this.priority = Integer.valueOf(priorityString);
      }
    }

    private void setDestination(Destination destination) {
      this.destination = destination;
    }

    @Override
    public JmsDestination.DestinationType destinationType() {
      return destType;
    }

    @Override
    public String subscriptionId() {
      return subscriptionId;
    }

    private void setSubscriptionId(String subscriptionId) {
      this.subscriptionId = subscriptionId;
    }

    @Override
    public boolean noLocal() {
      return noLocal;
    }

    private void setNoLocal(String b) {
      noLocal = Boolean.valueOf(b);
    }

    @Override
    public String sharedConsumerId() {
      return sharedConsumerId;
    }
    
    private void setSharedConsumerId(String sharedConsumerId) {
      this.sharedConsumerId = sharedConsumerId;
    }
  }


}
