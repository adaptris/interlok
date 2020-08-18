package com.adaptris.interlok.junit.scaffolding.jms;

import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.Session;
import com.adaptris.core.CoreException;
import com.adaptris.core.jms.JmsConsumerImpl;

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
    currentSession = session;
  }

  @Override
  public Session currentSession() {
    return currentSession;
  }

}
