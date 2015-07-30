/*
 * $RCSfile: AddMetadataServiceTest.java,v $
 * $Revision: 1.5 $
 * $Date: 2009/05/15 11:06:19 $
 * $Author: lchan $
 */
package com.adaptris.core.services.metadata;

import java.security.MessageDigest;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.CoreException;
import com.adaptris.core.ServiceException;
import com.adaptris.core.stubs.DefectiveMessageFactory;
import com.adaptris.core.util.LifecycleHelper;
import com.adaptris.util.text.Base64ByteTranslator;

public class PayloadHashingServiceTest extends MetadataServiceExample {

  private static final String METADATA_KEY = "metadata-key";
  private static final String SHA256 = "SHA256";
  private static final String PAYLOAD = "Glib jocks quiz nymph to vex dwarf";

  public PayloadHashingServiceTest(String name) {
    super(name);
  }

  @Override
  public void setUp() {
  }

  public void testInit() throws Exception {
    PayloadHashingService service = new PayloadHashingService();
    try {
      LifecycleHelper.init(service);
      fail();
    }
    catch (CoreException expected) {
    }
    service.setHashAlgorithm(SHA256);
    try {
      LifecycleHelper.init(service);
      fail();
    }
    catch (CoreException expected) {
    }
    service.setHashAlgorithm("");
    try {
      LifecycleHelper.init(service);
      fail();
    }
    catch (CoreException expected) {
    }
    service.setHashAlgorithm(SHA256);
    service.setMetadataKey("");
    try {
      LifecycleHelper.init(service);
      fail();
    }
    catch (CoreException expected) {
    }
    service.setMetadataKey(METADATA_KEY);
    service.setHashAlgorithm("BLAHBLAH");
    try {
      LifecycleHelper.init(service);
      fail();
    }
    catch (CoreException expected) {
    }
    service.setMetadataKey(METADATA_KEY);
    service.setHashAlgorithm(SHA256);
    LifecycleHelper.init(service);
  }

  public void testService() throws Exception {
    PayloadHashingService service = new PayloadHashingService(SHA256, METADATA_KEY);
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(PAYLOAD);
    execute(service, msg);
    assertTrue(msg.containsKey(METADATA_KEY));
    assertEquals(PAYLOAD, msg.getStringPayload());
    assertNotNull(msg.getMetadataValue(METADATA_KEY));
    assertEquals(createHash(), msg.getMetadataValue(METADATA_KEY));
  }

  public void testServiceException() throws Exception {
    PayloadHashingService service = new PayloadHashingService(SHA256, METADATA_KEY);
    AdaptrisMessage msg = new DefectiveMessageFactory().newMessage(PAYLOAD);
    try {
    execute(service, msg);
      fail();
    }
    catch (ServiceException e) {
      ;
    }
  }

  private String createHash() throws Exception {
    MessageDigest digest = MessageDigest.getInstance(SHA256);
    return new Base64ByteTranslator().translate(digest.digest(PAYLOAD.getBytes()));
  }

  @Override
  protected Object retrieveObjectForSampleConfig() {
    return new PayloadHashingService(SHA256, METADATA_KEY);
  }

}