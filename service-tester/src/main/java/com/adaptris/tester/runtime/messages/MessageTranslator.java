package com.adaptris.tester.runtime.messages;

import com.adaptris.core.SerializableAdaptrisMessage;
import com.adaptris.interlok.types.SerializableMessage;

public class MessageTranslator {

  public TestMessage translate(SerializableMessage message){
    return new TestMessage(message.getMessageHeaders(), message.getContent());
  }

  public SerializableAdaptrisMessage translate(TestMessage input) {
    SerializableAdaptrisMessage message = new SerializableAdaptrisMessage();
    message.setContent(input.getPayload());
    message.setMessageHeaders(input.getMessageHeaders());
    message.setNextServiceId(null);
    return message;
  }
}
