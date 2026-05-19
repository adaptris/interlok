package com.adaptris.core;

import org.junit.jupiter.api.Test;

import java.nio.charset.Charset;

import static org.junit.jupiter.api.Assertions.*;

public class TruncatePayloadServiceTest
    extends com.adaptris.interlok.junit.scaffolding.services.ExampleServiceCase {
  private final MultiPayloadMessageFactory messageFactory = new MultiPayloadMessageFactory();

  private static final String ENCODING = "UTF-8";

  private static final String CONTENT = "Bacon ipsum dolor amet bresaola ball tip flank, doner pork chop ham hock rump kielbasa pork loin beef burgdoggen short ribs tongue.";


  @Test
  public void testService() throws Exception {
    TruncatePayloadService service = getService();
    int length = CONTENT.length() - 10;
    service.setBytes(length);
    MultiPayloadAdaptrisMessage message = getMessage();
    assertEquals(CONTENT, message.getContent());
    assertEquals(CONTENT.length(), message.getSize());
    // truncate 10
    service.doService(message);
    assertEquals(length, message.getSize());

    // truncate all
    length = 0;
    service.setBytes(length);
    service.doService(message);
    assertEquals(length, message.getSize());
  }

  private MultiPayloadAdaptrisMessage getMessage() {
    MultiPayloadAdaptrisMessage message = (MultiPayloadAdaptrisMessage) messageFactory.newMessage(MultiPayloadAdaptrisMessage.DEFAULT_PAYLOAD_ID, CONTENT,
        ENCODING);
    return message;
  }

  private TruncatePayloadService getService() {
      return new TruncatePayloadService();
  }

  @Override
  protected Object retrieveObjectForSampleConfig() {
    return getService();
  }
}
