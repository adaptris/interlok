/*
 * $RCSfile: MetadataExistsBranchingServiceTest.java,v $
 * $Revision: 1.4 $
 * $Date: 2009/05/01 18:04:11 $
 * $Author: lchan $
 */
package com.adaptris.core.services.metadata;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.BranchingServiceCollection;
import com.adaptris.core.services.BranchingServiceExample;
import com.adaptris.core.services.LogMessageService;

public class MetadataExistsBranchingServiceTest extends BranchingServiceExample {

  private MetadataExistsBranchingService service;
  private AdaptrisMessage msg;

  public MetadataExistsBranchingServiceTest(String arg0) {
    super(arg0);
  }

  @Override
  protected void setUp() throws Exception {
    service = new MetadataExistsBranchingService();
    service.addMetadataKey("key1");
    service.setDefaultServiceId("default");
    service.setMetadataExistsServiceId("exists");

    msg = AdaptrisMessageFactory.getDefaultInstance().newMessage("xxxxzzzz");
  }

  public void testSetters() throws Exception {
    MetadataExistsBranchingService service = new MetadataExistsBranchingService();
    try {
      service.addMetadataKey("");
      fail();
    }
    catch (IllegalArgumentException expected) {

    }
    try {
      service.addMetadataKey(null);
      fail();
    }
    catch (IllegalArgumentException expected) {

    }
    try {
      service.setMetadataExistsServiceId(null);
      fail();
    }
    catch (IllegalArgumentException expected) {

    }
    try {
      service.setMetadataExistsServiceId("");
      fail();
    }
    catch (IllegalArgumentException expected) {

    }
  }

  public void testMetadataExists() throws Exception {
    msg.addMetadata("key1", "val1");
    execute(service, msg);
    assertTrue("exists".equals(msg.getNextServiceId()));
  }

  public void testMetadataExistsButIsEmpty() throws Exception {
    msg.addMetadata("key1", "");
    execute(service, msg);
    assertTrue("default".equals(msg.getNextServiceId()));
  }

  public void testMetadataDoesntExist() throws Exception {
    execute(service, msg);

    assertTrue("default".equals(msg.getNextServiceId()));
  }

  public void testMultipleKeysExists() throws Exception {
    msg.clearMetadata();
    msg.addMetadata("key2", "val2");

    service.addMetadataKey("key2");
    execute(service, msg);

    assertTrue("exists".equals(msg.getNextServiceId()));
  }

  public void testMultipleKeysDoesntExist() throws Exception {
    msg.clearMetadata();

    service.addMetadataKey("key2");
    execute(service, msg);

    assertTrue("default".equals(msg.getNextServiceId()));
  }

  @Override
  protected Object retrieveObjectForSampleConfig() {
    BranchingServiceCollection sl = new BranchingServiceCollection();
    service.setUniqueId("CheckMetadataExists");
    sl.addService(service);
    sl.setFirstServiceId(service.getUniqueId());
    sl.addService(new LogMessageService("exists"));
    sl.addService(new LogMessageService("default"));
    return sl;
  }

  @Override
  protected String createBaseFileName(Object object) {
    return MetadataExistsBranchingService.class.getName();
  }
}
