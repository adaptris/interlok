package com.adaptris.core.jms;

import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.Session;

@FunctionalInterface
public interface ConsumerCreator {

  public MessageConsumer createConsumer(Session session, JmsDestination destination, String filterExpression) throws JMSException;
  
}
