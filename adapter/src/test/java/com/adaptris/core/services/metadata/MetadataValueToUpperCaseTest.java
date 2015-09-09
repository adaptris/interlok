package com.adaptris.core.services.metadata;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;

public class MetadataValueToUpperCaseTest extends MetadataServiceExample {

  public MetadataValueToUpperCaseTest(String name) {
    super(name);
  }

  private AdaptrisMessage createMessage() {
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage("asdfghjk");
    msg.addMetadata("key", "value");
    msg.addMetadata("yetAnotherKey", "");
    return msg;
  }

  public void testToUpperCase() throws Exception {
    MetadataValueToUpperCase service = new MetadataValueToUpperCase();
    service.setMetadataKeyRegexp("key");
    AdaptrisMessage msg = createMessage();
    execute(service, msg);
    String result = msg.getMetadataValue("key");
    assertEquals("VALUE", result);
  }

  @Override
  protected Object retrieveObjectForSampleConfig() {
    MetadataValueToUpperCase service = new MetadataValueToUpperCase();
    service.setMetadataKeyRegexp(".*MetadataKeyRegularExpression.*");
    return service;
  }
}
