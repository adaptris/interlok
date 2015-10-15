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

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.services.SyntaxRoutingServiceExample;
import com.adaptris.core.util.LifecycleHelper;

public class StoreMetadataValueServiceTest extends SyntaxRoutingServiceExample {

  private StoreMetadataValueService service;
  private AdaptrisMessage msg;
  private String metadataKey;
  private String storeFileUrl;

  public StoreMetadataValueServiceTest(String name) {
    super(name);

    metadataKey = "key";

    storeFileUrl = PROPERTIES
        .getProperty("StoreMetadataValueServiceTest.storeFileUrl");

    msg = AdaptrisMessageFactory.getDefaultInstance().newMessage();
  }

  @Override
  protected void setUp() throws Exception {
    service = new StoreMetadataValueService();

    service.setMetadataKey(metadataKey);
    service.setStoreFileUrl(storeFileUrl);

    LifecycleHelper.init(service);
    service.deleteStore();
    LifecycleHelper.start(service);
  }

  @Override
  protected void tearDown() {
    service.deleteStore();
    LifecycleHelper.stop(service);
    LifecycleHelper.close(service);
  }

  public void testInit() throws Exception {
    StoreMetadataValueService newService = new StoreMetadataValueService();

    try {
      LifecycleHelper.init(newService);
      fail("no metadata key set");
    }
    catch (Exception e) {
      // expected
    }

    newService.setMetadataKey(metadataKey);

    try {
      LifecycleHelper.init(newService);
      fail("no store file URL set");
    }
    catch (Exception e) {
      // expected
    }

    newService.setStoreFileUrl(storeFileUrl);

    try {
      LifecycleHelper.init(newService);
    }
    catch (Exception e) {
      fail(e.getMessage());
    }
  }

  public void testService() throws Exception {
    assertEquals(0, service.storeSize());

    msg.addMetadata(metadataKey, "123");
    service.doService(msg);
    assertEquals(1, service.storeSize());

    service.doService(msg);
    assertEquals(2, service.storeSize());

    msg.addMetadata(metadataKey, "456");
    service.doService(msg);
    assertEquals(3, service.storeSize());

    // simulate restart...
    service = null;

    service = new StoreMetadataValueService();

    service.setMetadataKey(metadataKey);
    service.setStoreFileUrl(storeFileUrl);

    LifecycleHelper.init(service);

    assertEquals(3, service.storeSize()); // still 3 values in store
  }

  @Override
  protected Object retrieveObjectForSampleConfig() {
    service = new StoreMetadataValueService();

    service.setMetadataKey(metadataKey);
    service.setStoreFileUrl(storeFileUrl);

    return service;
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
