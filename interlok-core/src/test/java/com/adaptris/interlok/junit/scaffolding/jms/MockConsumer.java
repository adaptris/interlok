package com.adaptris.interlok.junit.scaffolding.jms;

import jakarta.jms.JMSException;
import jakarta.jms.MessageConsumer;
import jakarta.jms.Session;

import com.adaptris.core.CoreException;
import com.adaptris.core.jms.JmsConsumerImpl;

public class MockConsumer extends JmsConsumerImpl {

  private Session currentSession;;

  @Override
  public String configuredEndpoint() {
    return null;
  }

  @Override
  public MessageConsumer createConsumer() throws JMSException, CoreException {
    return null;
  }

  public void setCurrentSession(Session session) {
    currentSession = session;
  }

  @Override
  public Session currentSession() {
    return currentSession;
  }

}
