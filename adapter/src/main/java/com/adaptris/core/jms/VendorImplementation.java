package com.adaptris.core.jms;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.Queue;
import javax.jms.Session;
import javax.jms.Topic;

import com.adaptris.core.ConsumeDestination;
import com.adaptris.core.LicensedComponent;

/**
 * <p>
 * Abstract factory that insulates vendor-specific code from the rest of the <code>com.adaptris.core.jms</code> package.
 * </p>
 */
public interface VendorImplementation extends LicensedComponent, ConnectionComparator<VendorImplementation> {

  /**
   * <p>
   * Returns a <code>ConnectionFactory</code>.
   * 
   * @return an instance of <code>ConnectionFactory</code>
   * @throws JMSException if any occurs
   */
  ConnectionFactory createConnectionFactory() throws JMSException;

  /**
   * <p>
   * Returns the broker details used to create the underlying <code>ConnectionFactory</code>. This is delegated to the
   * <code>VendorImplementation</code> because it may over-ride the broker details configured in <code>JmsConnection</code>.
   * </p>
   * 
   * @return the broker details used to create the underlying <code>ConnectionFactory</code>
   */
  String retrieveBrokerDetailsForLogging();

  /**
   * <p>
   * Create or otherwise obtain a <code>Queue</code>.
   * </p>
   * 
   * @param name the name of the queue
   * @param c the Configuration
   * @return a <code>Queue</code> object
   * @throws JMSException if any occur
   */
  Queue createQueue(String name, JmsActorConfig c) throws JMSException;

  /**
   * <p>
   * Create or otherwise obtain a <code>Topic</code>.
   * </p>
   * 
   * @param name the name of the topic
   * @param c the Configuration
   * @return a <code>Topic</code> object
   * @throws JMSException if any occur
   */
  Topic createTopic(String name, JmsActorConfig c) throws JMSException;

  /**
   * Create either a {@code Topic} or {@code Queue} based on a RFC6167 style destination.
   * 
   * <p>
   * While RFC6167 defines the ability to use jndi to lookup the (as part of the 'jndi' variant
   * section); this is not supported. The standard deliveryMode, timeToLive, priority, replyToName
   * properties are supported. If not specified, then they will be inherited from the producers
   * configuration. For instance you could have the following destinations:
   * <ul>
   * <li>jms:queue:MyQueueName</li>
   * <li>jms:topic:MyTopicName</li>
   * <li>jms:queue:MyQueueName?replyToName=StaticReplyTo</li>
   * <li>jms:topic:MyTopicName?replyToName=StaticReplyTo</li>
   * </ul>
   * </p>
   * <p>
   * In addition to the standard deliveryMode, timeToLive, priority, replyToName, there are also
   * some custom parameters when dealing with topics.
   * <ul>
   * <li>{@code subscriptionId} - which indicates the subscriptionId that should be used when
   * attaching a subscriber to a topic; {@code jms:topic:MyTopicName?subscriptionId=myId} would
   * return a {@link JmsDestination#subscriptionId()} of {@code myId}</li>
   * <li>{@code noLocal} - which corresponds to the
   * {@link javax.jms.Session#createConsumer(javax.jms.Destination, String, boolean)} noLocal
   * setting. This defaults to false, if not specified.</li>
   * <ul>
   * </p>
   * 
   * @param destination a RFC6167 style destination.
   * @param c configuration
   * @return a {@link JmsDestination}.
   * @throws JMSException wrapping other exceptions.
   * @since 3.0.4
   */
  JmsDestination createDestination(String destination, JmsActorConfig c) throws JMSException;

  /**
   * Create a message consumer for the given destination and filter expression.
   * <p>
   * If the {@link JmsDestination#destinationType()} is a
   * {@link JmsDestination.DestinationType#TOPIC}, and {@link JmsDestination#subscriptionId()} is
   * not blank, then a durable subscriber is created otherwise a standard consumer is created. Also
   * {@link JmsDestination#noLocal()} is passed through to the appropriate {@link javax.jms.Session}
   * methods.
   * </p>
   * 
   * @param dest the destination
   * @param msgSelector the message selector
   * @param c configuration
   * @return a {@link MessageConsumer}
   * @throws JMSException wrapping other exceptions.
   * @since 3.0.4
   */
  MessageConsumer createConsumer(JmsDestination dest, String msgSelector, JmsActorConfig c) throws JMSException;

  /**
   * Create or otherwise get a MessageConsumer
   * 
   * @param cd the consume destination
   * @param c the Configuration
   * @return a MessageConsumer
   * @throws JMSException if there were any JMS related exceptions
   */
  MessageConsumer createQueueReceiver(ConsumeDestination cd, JmsActorConfig c) throws JMSException;

  /**
   * Create or otherwise get a TopicSubscriber.
   * 
   * @param cd the consume destination
   * @param subscriptionId A subscription ID to create a durable subscriber.
   * @param c the Configuration
   * @return a TopicSubscriber
   * @throws JMSException if there were any JMS related exceptions
   */
  MessageConsumer createTopicSubscriber(ConsumeDestination cd, String subscriptionId, JmsActorConfig c) throws JMSException;

  /**
   * Create or otherwise get a Session
   * 
   * @param c the Connection
   * @param transacted whether or not the session is transacted
   * @return acknowledgeMode the acknowledgement mode
   * @throws JMSException if there were any JMS related exceptions
   */
  Session createSession(Connection c, boolean transacted, int acknowledgeMode) throws JMSException;

}
