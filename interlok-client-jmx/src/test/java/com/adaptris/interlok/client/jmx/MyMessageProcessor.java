package com.adaptris.interlok.client.jmx;

import com.adaptris.interlok.InterlokException;
import com.adaptris.interlok.types.DefaultSerializableMessage;
import com.adaptris.interlok.types.SerializableMessage;



public class MyMessageProcessor implements MyMessageProcessorMBean {

  private SerializableMessage message;

  @Override
  public SerializableMessage process(SerializableMessage msg) throws InterlokException {
    message = msg;
    DefaultSerializableMessage reply = new DefaultSerializableMessage().withMessageHeaders(msg.getMessageHeaders())
        .withPayload(msg.getContent()).withPayloadEncoding(msg.getContentEncoding());
    return reply;
  }

  @Override
  public void processAsync(SerializableMessage msg) throws InterlokException {
    message = msg;
  }

  public SerializableMessage getMessage() {
    return message;
  }
}
