/*
 * Copyright 2017 Adaptris Ltd.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.adaptris.core;

import static com.adaptris.core.SharedServiceTest.createAdapterForSharedService;

import com.adaptris.core.util.LifecycleHelper;

public class DynamicSharedServiceTest extends ServiceCase {

  public DynamicSharedServiceTest(String name) {
    super(name);
  }

  @Override
  protected Object retrieveObjectForSampleConfig() {
    return new DynamicSharedService("example-shared-service");
  }

  protected String getExampleCommentHeader(Object object) {
    return super.getExampleCommentHeader(object)
        + "<!--\n\n  Note that you need to have configured a shared-component with the name 'example-shared-service'\n\n-->\n";
  }

  protected String createBaseFileName(Object object) {
    return DynamicSharedService.class.getName();
  }

  public void setUp() throws Exception {
  }

  public void tearDown() throws Exception {

  }

  public void testCreateName() {
    DynamicSharedService sharedService = new DynamicSharedService(getName());
    assertEquals(DynamicSharedService.class.getName(), sharedService.createName());
  }

  public void testCreateQualifier() {
    DynamicSharedService sharedService = new DynamicSharedService(getName());
    assertNotNull(sharedService.createQualifier());
  }

  public void testDoService() throws Exception {
    DynamicSharedService sharedService = new DynamicSharedService(getName());
    Adapter adapter = createAdapterForSharedService(sharedService, new NullService(getName()));

    try {
      LifecycleHelper.initAndStart(adapter);
      AdaptrisMessage msg = DefaultMessageFactory.getDefaultInstance().newMessage();
      sharedService.doService(msg);
      sharedService.doService(msg);
      assertEquals(1, sharedService.getCache().size());
    }
    finally {
      LifecycleHelper.stopAndClose(adapter);
    }
    assertEquals(0, sharedService.getCache().size());
  }

  public void testDoService_Selector() throws Exception {
    DynamicSharedService sharedService = new DynamicSharedService("%message{" + getName() + "}");
    Adapter adapter = createAdapterForSharedService(sharedService, new NullService(getName()));

    try {
      LifecycleHelper.initAndStart(adapter);
      AdaptrisMessage msg = DefaultMessageFactory.getDefaultInstance().newMessage();
      msg.addMetadata(getName(), getName());
      sharedService.doService(msg);
      sharedService.doService(msg);
      assertEquals(1, sharedService.getCache().size());
    }
    finally {
      LifecycleHelper.stopAndClose(adapter);
    }
    assertEquals(0, sharedService.getCache().size());
  }

  public void testDoService_Selector_CacheLimit() throws Exception {
    MyDynamicSharedService sharedService = new MyDynamicSharedService("%message{" + getName() + "}");
    Adapter adapter = createAdapterForSharedService(sharedService, new NullService("_1"),
        new NullService("_2"));

    try {
      LifecycleHelper.initAndStart(adapter);
      AdaptrisMessage msg1 = DefaultMessageFactory.getDefaultInstance().newMessage();
      msg1.addMetadata(getName(), "_1");
      sharedService.doService(msg1);

      AdaptrisMessage msg2 = DefaultMessageFactory.getDefaultInstance().newMessage();
      msg2.addMetadata(getName(), "_2");
      sharedService.doService(msg2);

      // Limited to max-size 1
      assertEquals(1, sharedService.getCache().size());
    }
    finally {
      LifecycleHelper.stopAndClose(adapter);
    }
    assertEquals(0, sharedService.getCache().size());
  }

  private class MyDynamicSharedService extends DynamicSharedService {
    MyDynamicSharedService(String lookupName) {
      super(lookupName);
    }

    int maxCacheSize() {
      return 1;
    }
  }
}
