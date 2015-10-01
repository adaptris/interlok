package com.adaptris.core.services.metadata;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.services.metadata.MetadataToPayloadService.Encoding;
import com.adaptris.core.services.metadata.MetadataToPayloadService.MetadataSource;
import com.adaptris.core.services.metadata.PayloadToMetadataService.MetadataTarget;
import com.adaptris.util.text.Conversion;

public class MetadataToPayloadTest extends MetadataServiceExample {

  private static final String DEFAULT_PAYLOAD = "zzzzzzzz";
  private static final String DEFAULT_METADATA_KEY = "helloMetadataKey";

  public MetadataToPayloadTest(String arg0) {
    super(arg0);
  }

  @Override
  protected void setUp() throws Exception {
    super.setUp();
  }

  private MetadataToPayloadService createService(MetadataSource target) {
    return new MetadataToPayloadService(DEFAULT_METADATA_KEY, target);
  }

  private PayloadToMetadataService toMetadataService(MetadataTarget target) {
    return new PayloadToMetadataService(DEFAULT_METADATA_KEY, target);
  }

  private AdaptrisMessage createMessage(boolean base64) {
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage();
    if (!base64) {
      msg.addMessageHeader(DEFAULT_METADATA_KEY, DEFAULT_PAYLOAD);
      msg.addObjectHeader(DEFAULT_METADATA_KEY, DEFAULT_PAYLOAD.getBytes());
    } else {
      msg.addMessageHeader(DEFAULT_METADATA_KEY, Conversion.byteArrayToBase64String(DEFAULT_PAYLOAD.getBytes()));
      msg.addObjectHeader(DEFAULT_METADATA_KEY, Conversion.byteArrayToBase64String(DEFAULT_PAYLOAD.getBytes()).getBytes());
    }
    return msg;
  }

  public void testService_Metadata() throws Exception {
    MetadataToPayloadService service = createService(MetadataSource.Standard);
    AdaptrisMessage msg = createMessage(false);
    execute(service, msg);
    assertEquals(DEFAULT_PAYLOAD, msg.getContent());
  }

  public void testService_Metadata_Encoded() throws Exception {
    MetadataToPayloadService service = createService(MetadataSource.Standard);
    service.setEncoding(Encoding.Base64);
    AdaptrisMessage msg = createMessage(true);
    execute(service, msg);
    assertEquals(DEFAULT_PAYLOAD, msg.getContent());
  }


  public void testService_ObjectMetadata() throws Exception {
    MetadataToPayloadService service = createService(MetadataSource.Object);
    AdaptrisMessage msg = createMessage(false);
    execute(service, msg);
    assertEquals(DEFAULT_PAYLOAD, msg.getContent());

  }


  public void testService_ObjectMetadata_Encoded() throws Exception {
    MetadataToPayloadService service = createService(MetadataSource.Object);
    service.setEncoding(Encoding.Base64);
    AdaptrisMessage msg = createMessage(true);
    execute(service, msg);
    assertEquals(DEFAULT_PAYLOAD, msg.getContent());

  }


  @Override
  protected MetadataToPayloadService retrieveObjectForSampleConfig() {
    MetadataToPayloadService service = new MetadataToPayloadService("theMetadataKey", MetadataSource.Standard);
    return service;
  }
}
