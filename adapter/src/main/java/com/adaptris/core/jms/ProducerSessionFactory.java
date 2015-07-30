package com.adaptris.core.jms;

import javax.jms.JMSException;

import com.adaptris.core.AdaptrisComponent;
import com.adaptris.core.AdaptrisMessage;

/**
 * Handles the creation of a JMS Session and MessageProducer for {@link JmsProducerImpl} instances.
 * 
 * @author lchan
 * 
 */
public interface ProducerSessionFactory extends AdaptrisComponent {

  /**
   * Create or reuse an existing session.
   * 
   * @param conn the {@link JmsProducerImpl} instance
   * @param msg the message that the producer is currently handling.
   * @return a {@link ProducerSession}
   * @throws JMSException if there was a problem creating the session.
   */
  ProducerSession createProducerSession(JmsProducerImpl conn, AdaptrisMessage msg)
      throws JMSException;

}
