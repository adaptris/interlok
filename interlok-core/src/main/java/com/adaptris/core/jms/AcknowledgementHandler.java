package com.adaptris.core.jms;

import javax.jms.JMSException;
import javax.jms.Message;

public interface AcknowledgementHandler {

  void acknowledgeMessage(JmsActorConfig actor, Message message) throws JMSException;
  
  void rollbackMessage(JmsActorConfig actor, Message message);
  
}
