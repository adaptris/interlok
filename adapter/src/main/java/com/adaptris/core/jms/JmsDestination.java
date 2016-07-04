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

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Queue;
import javax.jms.Session;
import javax.jms.TemporaryQueue;
import javax.jms.TemporaryTopic;
import javax.jms.Topic;

/**
 * A JMS Destination as specified by a limited parse of an RFC6167 style string.
 * 
 * @author lchan
 * 
 */
public interface JmsDestination {

  public enum DestinationType {
    QUEUE {
      @Override
      public Queue create(VendorImplementationBase vendor, JmsActorConfig c, String name) throws JMSException {
        return vendor.createQueue(name, c);
      }

      @Override
      public TemporaryQueue createTemporaryDestination(Session c) throws JMSException {
        return c.createTemporaryQueue();
      }

    },
    TOPIC {

      @Override
      public Topic create(VendorImplementationBase vendor, JmsActorConfig c, String name) throws JMSException {
        return vendor.createTopic(name, c);
      }

      @Override
      public TemporaryTopic createTemporaryDestination(Session c) throws JMSException {
        return c.createTemporaryTopic();
      }
    };
    // Note that we don't use the session directly here because we need to delegate to
    // the vendor implementation because StandardJndiImplementation can "lookup from jndi" the
    // destination
    // rather than creating it from the session.
    // somewhat obtuse, but there we go.
    public abstract Destination create(VendorImplementationBase vendor, JmsActorConfig c, String name) throws JMSException;

    public abstract Destination createTemporaryDestination(Session c) throws JMSException;
    
  }

  /**
   * The JMS Destination.
   * 
   * @return the destination.
   */
  Destination getDestination();

  /**
   * Get the Jms replyTo Destination.
   * 
   * @return the reply to Name specified in the URI (may be null).
   */
  Destination getReplyToDestination();

  /**
   * Get the delivery mode from the URI.
   * 
   * @return the delivery mode (may be null if not specified).
   */
  String deliveryMode();

  /**
   * Get the time-to-live
   * 
   * @return the time to live (may be null if not specified in the URI).
   */
  Long timeToLive();

  /**
   * Return the priority.
   * 
   * @return the priority (may be null if not specified in the URI).
   */
  Integer priority();

  /**
   * Return the type of destination.
   * 
   * @return the destination type.
   */
  DestinationType destinationType();

  /**
   * Return the subscription ID associated with this destination.
   * 
   * @return the subscription ID (for durable topic subscriptions); may be null.
   */
  String subscriptionId();

  /**
   * Custom parameter that matches the {@code nolocal} setting for
   * {@link javax.jms.Session#createConsumer(Destination, String, boolean)}.
   * 
   * @return the noLocal setting (which only has defined behaviour for topics); defaults to false.
   */
  boolean noLocal();
}
