package com.adaptris.core.jms;

import javax.jms.CompletionListener;
import javax.jms.JMSException;
import javax.jms.Message;

public class AsyncAcknowledgementHandler implements AcknowledgementHandler, CompletionListener {

  @Override
  public void acknowledgeMessage(JmsActorConfig actor, Message message) throws JMSException {
    // do nothing
  }

  @Override
  public void rollbackMessage(JmsActorConfig actor, Message message) {
    // do nothing
  }

  @Override
  public void onCompletion(Message message) {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void onException(Message message, Exception exception) {
    // TODO Auto-generated method stub
    
  }

}
