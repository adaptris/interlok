/*
 * Copyright 2015 Adaptris Ltd.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/

package com.adaptris.core.services.duplicate;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import java.io.File;
import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.BranchingServiceCollection;
import com.adaptris.core.ServiceException;
import com.adaptris.core.fs.FsHelper;
import com.adaptris.core.services.BranchingServiceExample;
import com.adaptris.core.services.LogMessageService;
import com.adaptris.core.util.LifecycleHelper;

public class CheckMetadataValueServiceTest extends BranchingServiceExample {

  private static final String STORE_FILE_URL = "CheckMetadataValueServiceTest.storeFileUrl";
  private static final String DEFAULT_METADATA_KEY = "key";

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


  @Override
  public boolean isAnnotatedForJunit4() {
    return true;
  }

  @Before
  public void setUp() throws Exception {
    File f = FsHelper.toFile(PROPERTIES.getProperty(STORE_FILE_URL));
    FileUtils.deleteQuietly(f);
  }

  @Test
  public void testInit() throws Exception {
    CheckMetadataValueService newService = new CheckMetadataValueService();
    assertTrue(newService.isBranching());
    newService.setNextServiceIdIfDuplicate(DEFAULT_SERVICE_ID_DUPLICATE);
    newService.setNextServiceIdIfUnique(DEFAULT_SERVICE_ID_UNIQUE);
    try {
      LifecycleHelper.initAndStart(newService);
      fail("no metadata key set");
    }
    catch (Exception e) {
      // expected
    } finally {
      LifecycleHelper.stopAndClose(newService);
    }

    newService.setMetadataKey(DEFAULT_METADATA_KEY);
    newService.setStoreFileUrl(PROPERTIES.getProperty(STORE_FILE_URL));
    try {
      LifecycleHelper.initAndStart(newService);
    } finally {
      LifecycleHelper.stopAndClose(newService);
    }
  }

  private StoreMetadataValueService createStorer() {
    StoreMetadataValueService service = new StoreMetadataValueService();
    service.setMetadataKey(DEFAULT_METADATA_KEY);
    service.setStoreFileUrl(PROPERTIES.getProperty(STORE_FILE_URL));
    return service;
  }


  private CheckMetadataValueService createChecker() {
    CheckMetadataValueService service = new CheckMetadataValueService();
    service.setNextServiceIdIfDuplicate(DEFAULT_SERVICE_ID_DUPLICATE);
    service.setNextServiceIdIfUnique(DEFAULT_SERVICE_ID_UNIQUE);
    service.setMetadataKey(DEFAULT_METADATA_KEY);
    service.setStoreFileUrl(PROPERTIES.getProperty(STORE_FILE_URL));
    return service;
  }

  private void seedStore(String... values) throws Exception {
    StoreMetadataValueService service = createStorer();
    try {
      LifecycleHelper.initAndStart(service);
      for (int i = 0; i < values.length; i++) {
        AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage();
        msg.addMetadata(DEFAULT_METADATA_KEY, values[i]);
        service.doService(msg);
      }
    } finally {
      LifecycleHelper.stopAndClose(service);
    }
  }

  @Test
  public void testService() throws Exception {
    seedStore("123", "123", "456");
    CheckMetadataValueService service = createChecker();
    try {
      LifecycleHelper.initAndStart(service);
      assertEquals(3, service.storeSize());

      AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage();

      msg.addMetadata(DEFAULT_METADATA_KEY, "123"); // exists in store
      service.doService(msg);
      assertEquals(DEFAULT_SERVICE_ID_DUPLICATE, msg.getNextServiceId());

      msg.addMetadata(DEFAULT_METADATA_KEY, "789"); // doesn't exist
      service.doService(msg);
      assertEquals(DEFAULT_SERVICE_ID_UNIQUE, msg.getNextServiceId());

      msg.clearMetadata();

      try {
        service.doService(msg);
        fail();
      } catch (ServiceException e) {
        // expected
      }

    } finally {
      LifecycleHelper.stopAndClose(service);
    }
  }

  @Override
  protected Object retrieveObjectForSampleConfig() {
    CheckMetadataValueService s = createChecker();
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
