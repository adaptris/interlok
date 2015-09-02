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
      producer.setPriority(jmsP.getPriority());
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

    public Session getSession() {
      return session;
    }

    private void setSession(Session session) {
      this.session = session;
    }

    public MessageProducer getProducer() {
      return producer;
    }

    private void setProducer(MessageProducer producer) {
      this.producer = producer;
    }
  }
}
