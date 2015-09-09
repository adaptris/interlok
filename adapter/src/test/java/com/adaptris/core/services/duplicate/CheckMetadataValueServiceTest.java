package com.adaptris.core.services.duplicate;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.BranchingServiceCollection;
import com.adaptris.core.ServiceException;
import com.adaptris.core.services.BranchingServiceExample;
import com.adaptris.core.services.LogMessageService;
import com.adaptris.core.util.LifecycleHelper;

public class CheckMetadataValueServiceTest extends BranchingServiceExample {

  /**
   * <p>
   * Default next Service ID to set if the message metadata value does not
   * appear in the store of previously received values.
   * </p>
   */
  private static final String DEFAULT_SERVICE_ID_UNIQUE = "001";

  /**
   * <p>
   * Default next Service ID to set if the message metadata value <em>does</em>
   * appear in the store of previously received values.
   * </p>
   */
  private static final String DEFAULT_SERVICE_ID_DUPLICATE = "002";

  private CheckMetadataValueService checkMetadataValueService;
  private StoreMetadataValueService storeMetadataValueService;
  private AdaptrisMessage msg;
  private String metadataKey;
  private String storeFileUrl;

  public CheckMetadataValueServiceTest(String name) {
    super(name);

    metadataKey = "key";

    storeFileUrl = PROPERTIES
        .getProperty("CheckMetadataValueServiceTest.storeFileUrl");

    msg = AdaptrisMessageFactory.getDefaultInstance().newMessage();
  }

  @Override
  protected void setUp() throws Exception {
    storeMetadataValueService = new StoreMetadataValueService();
    storeMetadataValueService.setMetadataKey(metadataKey);
    storeMetadataValueService.setStoreFileUrl(storeFileUrl);

    LifecycleHelper.init(storeMetadataValueService);
    storeMetadataValueService.deleteStore();
    LifecycleHelper.start(storeMetadataValueService);

    checkMetadataValueService = new CheckMetadataValueService();
    checkMetadataValueService
        .setNextServiceIdIfDuplicate(DEFAULT_SERVICE_ID_DUPLICATE);
    checkMetadataValueService
        .setNextServiceIdIfUnique(DEFAULT_SERVICE_ID_UNIQUE);
    checkMetadataValueService.setMetadataKey(metadataKey);
    checkMetadataValueService.setStoreFileUrl(storeFileUrl);

    LifecycleHelper.init(checkMetadataValueService);
    LifecycleHelper.start(checkMetadataValueService);
  }

  @Override
  protected void tearDown() {
    storeMetadataValueService.deleteStore();
    LifecycleHelper.stop(storeMetadataValueService);
    LifecycleHelper.close(storeMetadataValueService);
    LifecycleHelper.stop(checkMetadataValueService);
    LifecycleHelper.close(checkMetadataValueService);
  }

  public void testInit() throws Exception {
    CheckMetadataValueService newService = new CheckMetadataValueService();
    newService.setNextServiceIdIfDuplicate(DEFAULT_SERVICE_ID_DUPLICATE);
    newService.setNextServiceIdIfUnique(DEFAULT_SERVICE_ID_UNIQUE);

    try {
      newService.init();
      fail("no metadata key set");
    }
    catch (Exception e) {
      // expected
    }

    newService.setMetadataKey(metadataKey);
    try {
      newService.init();
      fail("no store file URL set");
    }
    catch (Exception e) {
      // expected
    }

    newService.setStoreFileUrl(storeFileUrl);

    try {
      newService.init();
    }
    catch (Exception e) {
      fail(e.getMessage());
    }
  }

  public void testService() throws Exception {
    // set up store...
    msg.addMetadata(metadataKey, "123");
    storeMetadataValueService.doService(msg);

    storeMetadataValueService.doService(msg);

    msg.addMetadata(metadataKey, "456");
    storeMetadataValueService.doService(msg);

    assertEquals(3, storeMetadataValueService.storeSize());

    msg.addMetadata(metadataKey, "123"); // exists in store
    checkMetadataValueService.doService(msg);

    assertEquals(DEFAULT_SERVICE_ID_DUPLICATE, msg.getNextServiceId());

    msg.addMetadata(metadataKey, "456"); // exists in store
    checkMetadataValueService.doService(msg);

    assertEquals(DEFAULT_SERVICE_ID_DUPLICATE, msg.getNextServiceId());

    msg.addMetadata(metadataKey, "789"); // new
    checkMetadataValueService.doService(msg);

    assertEquals(DEFAULT_SERVICE_ID_UNIQUE, msg.getNextServiceId());

    msg.clearMetadata();

    try {
      execute(checkMetadataValueService, msg);
      fail();
    }
    catch (ServiceException e) {
      // expected
    }
  }

  @Override
  protected Object retrieveObjectForSampleConfig() {
    CheckMetadataValueService s = new CheckMetadataValueService();
    s.setMetadataKey(metadataKey);
    s.setStoreFileUrl(storeFileUrl);
    s.setNextServiceIdIfDuplicate("duplicate");
    s.setNextServiceIdIfUnique("unique");
    s.setUniqueId("CheckMetadataAgainstPreviousValues");

    BranchingServiceCollection sl = new BranchingServiceCollection();
    sl.addService(s);
    sl.setFirstServiceId(s.getUniqueId());
    sl.addService(new LogMessageService("duplicate"));
    sl.addService(new LogMessageService("unique"));
    return sl;

  }

  @Override
  protected String createBaseFileName(Object object) {
    return CheckMetadataValueService.class.getName();
  }

  @Override
  protected String getExampleCommentHeader(Object o) {
    return super.getExampleCommentHeader(o) + "<!--" + "\nThis service is unsed in conjunction with StoreMetadataValueService. "
        + "\nThe file locations for both services should be the same."
        + "\nStoreMetadataValueService stores keys and values that should be unique, "
        + "\nand CheckMetadataValueService checks the file for any duplicates." + "\n-->\n";
  }
}
