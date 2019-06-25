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

import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.adaptris.core.CoreException;

/**
 * Partial implementation of {@link ProducerSessionFactory}
 * 
 * @author lchan
 * 
 */
public abstract class ProducerSessionFactoryImpl implements ProducerSessionFactory {

  protected transient Logger log = LoggerFactory.getLogger(this.getClass());
  protected transient ProducerSession session = null;
  
  @Override
  public void init() throws CoreException {
    // Reset the session.
    session = null;
  }

  @Override
  public void start() throws CoreException {
  }

  @Override
  public void stop() {
  }

  @Override
  public void close() {
    closeQuietly(session);
  }

  protected ProducerSession createProducerSession(JmsProducerImpl producer) throws JMSException {
    Connection conn = producer.retrieveConnection(JmsConnection.class).currentConnection();
    JmsConnection jmsConnection = producer.retrieveConnection(JmsConnection.class);
    VendorImplementation vendor = jmsConnection.configuredVendorImplementation();
    Session s = vendor.createSession(conn, producer.transactedSession(), AcknowledgeMode.getMode(producer.getAcknowledgeMode()));
    // Session s = producer.retrieveConnection(JmsConnection.class).configuredVendorImplementation()
    // .createSession(conn, producer.getTransacted(), AcknowledgeMode.getMode(producer.getAcknowledgeMode()));
    MessageProducer p = configureMessageProducer(producer, s.createProducer(null));
    ProducerSession ps = new ProducerSessionImpl(s, p);
    log.trace("Created new JMS MessageProducer / Session");
    return ps;
  }

  private MessageProducer configureMessageProducer(JmsProducerImpl jmsP, MessageProducer producer)
      throws JMSException {
    if (!jmsP.perMessageProperties()) {
      producer.setDeliveryMode(com.adaptris.core.jms.DeliveryMode.getMode(jmsP.getDeliveryMode()));
      producer.setPriority(jmsP.messagePriority());
      producer.setTimeToLive(jmsP.timeToLive());
    }
    return producer;
  }

  protected static void closeQuietly(ProducerSession s) {
    if (s != null) {
      JmsUtils.closeQuietly(s.getProducer());
      JmsUtils.closeQuietly(s.getSession());
    }
  }

  private static class ProducerSessionImpl extends ProducerSession {
    private Session session;
    private MessageProducer producer;

    private ProducerSessionImpl(Session s, MessageProducer p) {
      setSession(s);
      setProducer(p);
    }

    @Override
    public Session getSession() {
      return session;
    }

    private void setSession(Session session) {
      this.session = session;
    }

    @Override
    public MessageProducer getProducer() {
      return producer;
    }

    private void setProducer(MessageProducer producer) {
      this.producer = producer;
    }
  }

}
