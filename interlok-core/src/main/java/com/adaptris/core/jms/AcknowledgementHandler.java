package com.adaptris.core.jms;

import jakarta.jms.JMSException;
import jakarta.jms.Message;

public interface AcknowledgementHandler {

  void acknowledgeMessage(JmsActorConfig actor, Message message) throws JMSException;
  
  void rollbackMessage(JmsActorConfig actor, Message message);
  
}
