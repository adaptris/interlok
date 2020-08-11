package com.adaptris.core.jms;

import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.Session;

import com.adaptris.core.CoreException;

public class MockConsumer extends JmsConsumerImpl {
  
  private Session currentSession;;

  @Override
  protected String configuredEndpoint() {
    return null;
  }

  @Override
  protected MessageConsumer createConsumer() throws JMSException, CoreException {
    return null;
  }

  public void setCurrentSession(Session session) {
    this.currentSession = session;
  }
  
  public Session currentSession() {
    return currentSession;
  }
  
}
