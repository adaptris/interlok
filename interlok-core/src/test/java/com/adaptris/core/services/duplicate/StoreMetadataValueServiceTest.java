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
import static org.junit.Assert.fail;
import java.io.File;
import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.ServiceException;
import com.adaptris.core.fs.FsHelper;
import com.adaptris.core.services.SyntaxRoutingServiceExample;
import com.adaptris.core.util.LifecycleHelper;

public class StoreMetadataValueServiceTest extends SyntaxRoutingServiceExample {

  private static final String STORE_URL = "StoreMetadataValueServiceTest.storeFileUrl";
  private static final String DEFAULT_METADATA_KEY = "key";

  @Override
  public boolean isAnnotatedForJunit4() {
    return true;
  }

  @Before
  public void setUp() throws Exception {
    File f = FsHelper.toFile(PROPERTIES.getProperty(STORE_URL));
    FileUtils.deleteQuietly(f);
  }

  private StoreMetadataValueService createService() {
    StoreMetadataValueService service = new StoreMetadataValueService();
    service.setStoreFileUrl(PROPERTIES.getProperty(STORE_URL));
    service.setMetadataKey(DEFAULT_METADATA_KEY);
    return service;
  }

  @Test
  public void testPreviousValues() throws Exception {
    StoreMetadataValueService newService = new StoreMetadataValueService();
    assertEquals(1000, newService.getNumberOfPreviousValuesToStore());
    newService.setNumberOfPreviousValuesToStore(1);
    assertEquals(1, newService.getNumberOfPreviousValuesToStore());
    try {
      newService.setNumberOfPreviousValuesToStore(0);
      fail();
    } catch (IllegalArgumentException e) {

    }
    assertEquals(1, newService.getNumberOfPreviousValuesToStore());
  }

  @Test
  public void testInit() throws Exception {
    StoreMetadataValueService newService = new StoreMetadataValueService();
    try {
      LifecycleHelper.initAndStart(newService);
      fail("no metadata key set");
    }
    catch (Exception e) {
      // expected
    }
    newService.setMetadataKey(DEFAULT_METADATA_KEY);
    newService.setStoreFileUrl(PROPERTIES.getProperty(STORE_URL));
    LifecycleHelper.initAndStart(newService);
  }

  @Test
  public void testService() throws Exception {
    StoreMetadataValueService service = createService();
    StoreMetadataValueService service2 = createService();
    try {
      LifecycleHelper.initAndStart(service);
      AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage();
      assertEquals(0, service.storeSize());
      msg.addMetadata(DEFAULT_METADATA_KEY, "123");
      service.doService(msg);
      assertEquals(1, service.storeSize());
      // Since it's a list it doesn't care.
      service.doService(msg);
      assertEquals(2, service.storeSize());
      msg.addMetadata(DEFAULT_METADATA_KEY, "456");
      service.doService(msg);
      assertEquals(3, service.storeSize());

      LifecycleHelper.initAndStart(service2);
      assertEquals(3, service2.storeSize()); // still 3 values in store
    } finally {
      LifecycleHelper.stopAndClose(service);
      LifecycleHelper.stopAndClose(service2);
    }
  }

  @Test
  public void testService_ExceedsHistory() throws Exception {
    final int maxPrevious = 10;
    final int maxCount = maxPrevious + 10;
    StoreMetadataValueService service = createService();
    service.setNumberOfPreviousValuesToStore(maxPrevious);
    try {
      LifecycleHelper.initAndStart(service);
      assertEquals(0, service.storeSize());
      for (int i = 0; i < maxCount; i++) {
        AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage();
        msg.addMetadata(DEFAULT_METADATA_KEY, "123");
        service.doService(msg);
      }
      assertEquals(maxPrevious, service.storeSize());
    } finally {
      LifecycleHelper.stopAndClose(service);
    }
  }

  @Test
  public void testService_Exception() throws Exception {
    StoreMetadataValueService service = createService();
    try {
      execute(service, null);
      fail();
    } catch (ServiceException expected) {

    }
  }

  @Override
  protected StoreMetadataValueService retrieveObjectForSampleConfig() {
    return createService();
  }

  @Override
  protected String getExampleCommentHeader(Object o) {
    return super.getExampleCommentHeader(o) + "<!--"
        + "\nThis service is unsed in conjunction with DuplicateMessageRoutingService. "
        + "\nThe file locations for both services should be the same."
        + "\nStoreMetadataValueService stores keys and values that should be unique, "
        + "\nand DuplicateMessageRoutingService checks the file for any duplicates." + "\n-->\n";
  }
}
