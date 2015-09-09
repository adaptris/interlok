package com.adaptris.core.services.metadata;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.CoreException;

public class MetadataAppenderServiceTest extends MetadataServiceExample {

  private MetadataAppenderService service;
  private String resultKey;
  private AdaptrisMessage msg;

  public MetadataAppenderServiceTest(String name) {
    super(name);
  }

  @Override
  protected void setUp() throws Exception {
    msg = AdaptrisMessageFactory.getDefaultInstance().newMessage();
    msg.addMetadata("key1", "val1");
    msg.addMetadata("key2", "val2");
    msg.addMetadata("key3", "val3");

    resultKey = "result";

    service = new MetadataAppenderService();
    service.setResultKey(resultKey);
  }

  public void testSetEmptyResultKey() throws Exception {
    MetadataAppenderService service = new MetadataAppenderService();
    try {
      service.setResultKey("");
      fail();
    }
    catch (IllegalArgumentException expected) {

    }

  }

  public void testSetNullResultKey() throws Exception {
    MetadataAppenderService service = new MetadataAppenderService();
    try {
      service.setResultKey(null);
      fail();
    }
    catch (IllegalArgumentException e) {

    }

  }

  public void testTwoKeys() throws CoreException {
    service.addAppendKey("key1");
    service.addAppendKey("key3");

    execute(service, msg);
    assertTrue("val1val3".equals(msg.getMetadataValue(resultKey)));
  }

  public void testTwoKeysOneNotSet() throws CoreException {
    service.addAppendKey("key1");
    service.addAppendKey("key4");

    execute(service, msg);
    assertTrue("val1".equals(msg.getMetadataValue(resultKey)));
  }

  public void testNullKey() throws CoreException {
    try {
      service.addAppendKey(null);
      fail();
    }
    catch (IllegalArgumentException e) {
      // okay
    }
  }

  public void testEmptyKey() throws CoreException {
    try {
      service.addAppendKey("");
      fail();
    }
    catch (IllegalArgumentException e) {
      // okay
    }
  }

  @Override
  protected Object retrieveObjectForSampleConfig() {
    service.addAppendKey("key1");
    service.addAppendKey("key2");

    return service;
  }
}
