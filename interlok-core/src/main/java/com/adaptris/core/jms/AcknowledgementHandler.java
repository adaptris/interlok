package com.adaptris.core.jms;

import javax.jms.JMSException;
import javax.jms.Message;

public interface AcknowledgementHandler {

  void acknowledgeMessage(Message message) throws JMSException;
  
  void rollbackMessage(Message message);
  
}
