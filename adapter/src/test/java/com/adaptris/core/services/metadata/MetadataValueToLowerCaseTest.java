package com.adaptris.core.services.metadata;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;

public class MetadataValueToLowerCaseTest extends MetadataServiceExample {

  public MetadataValueToLowerCaseTest(String name) {
    super(name);
  }

  @Override
  protected void setUp() throws Exception {
    super.setUp();
  }


  private AdaptrisMessage createMessage() {
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage("asdfghjk");
    msg.addMetadata("key", "VALUE");
    msg.addMetadata("yetAnotherKey", "");
    return msg;
  }
  public void testToLowerCase() throws Exception {
    MetadataValueToLowerCase service = new MetadataValueToLowerCase();
    service.setMetadataKeyRegexp("key");
    AdaptrisMessage msg = createMessage();
    execute(service, msg);
    String result = msg.getMetadataValue("key");
    assertEquals("value", result);
  }

  @Override
  protected Object retrieveObjectForSampleConfig() {
    MetadataValueToLowerCase service = new MetadataValueToLowerCase();
    service.setMetadataKeyRegexp(".*MetadataKeyRegularExpression.*");
    return service;
  }
}
