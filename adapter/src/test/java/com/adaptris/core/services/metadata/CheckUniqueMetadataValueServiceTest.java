package com.adaptris.core.services.metadata;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.BranchingServiceCollection;
import com.adaptris.core.services.BranchingServiceExample;
import com.adaptris.core.services.LogMessageService;
import com.adaptris.core.util.LifecycleHelper;

public class CheckUniqueMetadataValueServiceTest extends BranchingServiceExample {

  private static final String STORE_FILE_URL_KEY = "CheckUniqueMetadataValueServiceTest.storeFileUrl";

  private String storeFileUrl;
  private CheckUniqueMetadataValueService service;
  private AdaptrisMessage msg1;
  private AdaptrisMessage msg2;
  private AdaptrisMessage msg3;
  private AdaptrisMessage msg4;
  private AdaptrisMessage msg5;

  public CheckUniqueMetadataValueServiceTest(String name) {
    super(name);

    storeFileUrl = PROPERTIES.getProperty(STORE_FILE_URL_KEY);
  }

  @Override
  protected void setUp() throws Exception {
    if (checkFileExists(storeFileUrl)) {
      throw new Exception("file [" + storeFileUrl + "] should not exist prior to test");
    }

    service = new CheckUniqueMetadataValueService();
    service.setMetadataKeyToCheck("unique-id");
    service.setNumberOfPreviousValuesToStore(3);
    service.setStoreFileUrl(storeFileUrl);
    service.setNextServiceIdIfUnique(CheckUniqueMetadataValueService.DEFAULT_SERVICE_ID_UNIQUE);
    service.setNextServiceIdIfDuplicate(CheckUniqueMetadataValueService.DEFAULT_SERVICE_ID_DUPLICATE);

    msg1 = AdaptrisMessageFactory.getDefaultInstance().newMessage();
    msg1.addMetadata("unique-id", "001");

    msg2 = AdaptrisMessageFactory.getDefaultInstance().newMessage();
    msg2.addMetadata("unique-id", "002");

    msg3 = AdaptrisMessageFactory.getDefaultInstance().newMessage();
    msg3.addMetadata("unique-id", "003");

    msg4 = AdaptrisMessageFactory.getDefaultInstance().newMessage();
    msg4.addMetadata("unique-id", "001");

    msg5 = AdaptrisMessageFactory.getDefaultInstance().newMessage();
  }

  @Override
  protected void tearDown() {
    removeFile(storeFileUrl);
  }

  public void testSetters() {
    CheckUniqueMetadataValueService s = new CheckUniqueMetadataValueService();
    try {
      s.setMetadataKeyToCheck("");
      fail();
    }
    catch (IllegalArgumentException e) {
      // expected
    }
    try {
      s.setMetadataKeyToCheck(null);
      fail();
    }
    catch (IllegalArgumentException e) {
      // expected
    }
    try {
      s.setStoreFileUrl(null);
      fail();
    }
    catch (IllegalArgumentException e) {
      // expected
    }
    try {
      s.setStoreFileUrl("");
      fail();
    }
    catch (IllegalArgumentException e) {
      // expected
    }
    try {
      s.setNextServiceIdIfDuplicate(null);
      fail();
    }
    catch (IllegalArgumentException e) {
      // expected
    }
    try {
      s.setNextServiceIdIfDuplicate("");
      fail();
    }
    catch (IllegalArgumentException e) {
      // expected
    }
    try {
      s.setNextServiceIdIfUnique(null);
      fail();
    }
    catch (IllegalArgumentException e) {
      // expected
    }
    try {
      s.setNextServiceIdIfUnique("");
      fail();
    }
    catch (IllegalArgumentException e) {
      // expected
    }
    try {
      s.setNumberOfPreviousValuesToStore(0);
      fail();
    }
    catch (IllegalArgumentException e) {
      // expected
    }
  }

  public void testInitWithDefaults() {
    CheckUniqueMetadataValueService s = new CheckUniqueMetadataValueService();

    try {
      LifecycleHelper.init(s);
      fail(); // no metadata key or store file URL
    }
    catch (Exception e) {
      // expected
    }
  }

  public void testInitWithMetadataKey() {
    CheckUniqueMetadataValueService s = new CheckUniqueMetadataValueService();

    try {
      s.setMetadataKeyToCheck("key");
      LifecycleHelper.init(s);
      fail(); // no store file URL
    }
    catch (Exception e) {
      // expected
    }
  }

  public void testInitWithStoreFileUrl() {
    CheckUniqueMetadataValueService s = new CheckUniqueMetadataValueService();

    try {
      s.setStoreFileUrl(storeFileUrl);
      LifecycleHelper.init(s);
      fail(); // no metadata key
    }
    catch (Exception e) {
      // expected
    }
  }

  public void testInitWithMetadataKeyAndStoreFile() throws Exception {
    CheckUniqueMetadataValueService s = new CheckUniqueMetadataValueService();
    try {
      s.setMetadataKeyToCheck("key");
      s.setStoreFileUrl(storeFileUrl);
      LifecycleHelper.init(s);
    }
    finally {
    }
  }

  public void testNullMetadataValue() {
    try {
      execute(service, msg5);
      fail();
    }
    catch (Exception e) {
      // expected
    }
  }

  public void testEmptyMetadataValue() {
    msg5.addMetadata("unique-id", "");

    try {
      execute(service, msg5);
      fail();
    }
    catch (Exception e) {
      // expected
    }
  }

  public void testListSize() throws Exception {
    try {
      start(service);
      assertTrue(service.storeSize() == 0);

      service.doService(msg1);
      assertTrue(service.storeSize() == 1);

      service.doService(msg2);
      assertTrue(service.storeSize() == 2);

      service.doService(msg3);
      assertTrue(service.storeSize() == 3);

      service.doService(msg4);
      assertTrue(service.storeSize() == 3);

      assertTrue(msg4.getNextServiceId().equals(CheckUniqueMetadataValueService.DEFAULT_SERVICE_ID_DUPLICATE));

      msg5.addMetadata("unique-id", "005"); // is unique

      service.doService(msg5);
      assertTrue(service.storeSize() == 3);
    }
    finally {
      stop(service);
    }
  }

  public void testWithDuplicate() throws Exception {
    try {
      start(service);
      service.doService(msg1);
      service.doService(msg4);

      assertTrue(msg1.getNextServiceId().equals(CheckUniqueMetadataValueService.DEFAULT_SERVICE_ID_UNIQUE));

      assertTrue(msg4.getNextServiceId().equals(CheckUniqueMetadataValueService.DEFAULT_SERVICE_ID_DUPLICATE));
    }
    finally {
      stop(service);
    }
  }

  public void testPeristentStore() throws Exception {
    try {
      start(service);

      service.doService(msg1);
      service.doService(msg2);
      service.doService(msg3);

      stop(service);
      start(service);

      service.doService(msg4); // dup if list has been read properly

      assertTrue(msg4.getNextServiceId().equals(CheckUniqueMetadataValueService.DEFAULT_SERVICE_ID_DUPLICATE));
    }
    finally {
      stop(service);
    }
  }

  @Override
  protected Object retrieveObjectForSampleConfig() {

    service.setUniqueId("CheckMetadataUniqueNess");
    service.setStoreFileUrl("file:///path/to/store.file");
    service.setMetadataKeyToCheck("metadata-key");

    BranchingServiceCollection sl = new BranchingServiceCollection();
    sl.addService(service);
    sl.setFirstServiceId(service.getUniqueId());
    sl.addService(new LogMessageService(CheckUniqueMetadataValueService.DEFAULT_SERVICE_ID_UNIQUE));
    sl.addService(new LogMessageService(CheckUniqueMetadataValueService.DEFAULT_SERVICE_ID_DUPLICATE));

    return sl;
  }

  @Override
  protected String createBaseFileName(Object object) {
    return CheckUniqueMetadataValueService.class.getName();
  }
}
