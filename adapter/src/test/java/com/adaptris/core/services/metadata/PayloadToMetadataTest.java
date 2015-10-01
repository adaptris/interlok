package com.adaptris.core.services.metadata;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.services.metadata.PayloadToMetadataService.Encoding;
import com.adaptris.core.services.metadata.PayloadToMetadataService.MetadataTarget;

public class PayloadToMetadataTest extends MetadataServiceExample {

  private static final String DEFAULT_PAYLOAD = "zzzzzzzz";
  private static final String DEFAULT_METADATA_KEY = "helloMetadataKey";

  public PayloadToMetadataTest(String arg0) {
    super(arg0);
  }

  @Override
  protected void setUp() throws Exception {
    super.setUp();
  }

  private PayloadToMetadataService createService(MetadataTarget target) {
    return new PayloadToMetadataService(DEFAULT_METADATA_KEY, target);
  }

  private AdaptrisMessage createMessage() {
    return AdaptrisMessageFactory.getDefaultInstance().newMessage(DEFAULT_PAYLOAD);
  }

  public void testService_Metadata() throws Exception {
    PayloadToMetadataService service = createService(MetadataTarget.Standard);
    AdaptrisMessage msg = createMessage();
    execute(service, msg);
    assertTrue(msg.containsKey(DEFAULT_METADATA_KEY));
    assertEquals(DEFAULT_PAYLOAD, msg.getMetadataValue(DEFAULT_METADATA_KEY));
    assertFalse(msg.getObjectHeaders().containsKey(DEFAULT_METADATA_KEY));
  }

  public void testService_Metadata_Encoded() throws Exception {
    PayloadToMetadataService service = createService(MetadataTarget.Standard);
    service.setEncoding(Encoding.Base64);
    AdaptrisMessage msg = createMessage();
    execute(service, msg);
    assertTrue(msg.containsKey(DEFAULT_METADATA_KEY));
    assertNotSame(DEFAULT_PAYLOAD, msg.getMetadataValue(DEFAULT_METADATA_KEY));
    assertFalse(msg.getObjectHeaders().containsKey(DEFAULT_METADATA_KEY));
  }


  public void testService_ObjectMetadata() throws Exception {
    PayloadToMetadataService service = createService(MetadataTarget.Object);
    AdaptrisMessage msg = createMessage();
    execute(service, msg);
    assertFalse(msg.containsKey(DEFAULT_METADATA_KEY));
    assertTrue(msg.getObjectHeaders().containsKey(DEFAULT_METADATA_KEY));
  }


  public void testService_ObjectMetadata_Encoded() throws Exception {
    PayloadToMetadataService service = createService(MetadataTarget.Object);
    service.setEncoding(Encoding.Base64);
    AdaptrisMessage msg = createMessage();
    execute(service, msg);
    assertFalse(msg.containsKey(DEFAULT_METADATA_KEY));
    assertTrue(msg.getObjectHeaders().containsKey(DEFAULT_METADATA_KEY));
  }


  @Override
  protected Object retrieveObjectForSampleConfig() {
    PayloadToMetadataService service = new PayloadToMetadataService("theMetadataKey", MetadataTarget.Standard);
    return service;
  }
}
