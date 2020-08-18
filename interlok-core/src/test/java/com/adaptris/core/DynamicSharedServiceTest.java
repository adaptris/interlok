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
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;
import java.util.concurrent.TimeUnit;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import com.adaptris.core.services.exception.ConfiguredException;
import com.adaptris.core.services.exception.ThrowExceptionService;
import com.adaptris.core.util.LifecycleHelper;
import com.adaptris.util.TimeInterval;

public class DynamicSharedServiceTest
    extends com.adaptris.interlok.junit.scaffolding.services.ExampleServiceCase {


  @Override
  protected Object retrieveObjectForSampleConfig() {
    return new DynamicSharedService("example-shared-service");
  }

  @Override
  protected String getExampleCommentHeader(Object object) {
    return super.getExampleCommentHeader(object)
        + "<!--\n\n  Note that you need to have configured a shared-component with the name 'example-shared-service'\n\n-->\n";
  }

  @Override
  protected String createBaseFileName(Object object) {
    return DynamicSharedService.class.getName();
  }

  @Before
  public void setUp() throws Exception {
  }

  @After
  public void tearDown() throws Exception {

  }

  @Test
  public void testIsBranching() {
    DynamicSharedService sharedService = new DynamicSharedService(getName());
    assertFalse(sharedService.isBranching());
  }

  @Test
  public void testCreateName() {
    DynamicSharedService sharedService = new DynamicSharedService(getName());
    assertEquals(DynamicSharedService.class.getName(), sharedService.createName());
  }

  @Test
  public void testCreateQualifier() {
    DynamicSharedService sharedService = new DynamicSharedService(getName());
    assertNotNull(sharedService.createQualifier());
  }

  @Test
  public void testMaxEntries() {
    DynamicSharedService sharedService = new DynamicSharedService(getName());
    assertEquals(16, sharedService.maxEntries());
    assertNull(sharedService.getMaxEntries());
    sharedService.withMaxEntries(10);
    assertEquals(10, sharedService.maxEntries());
    assertEquals(10, sharedService.getMaxEntries().intValue());
  }

  @Test
  public void testExpiration() {
    TimeInterval oneHour = new TimeInterval(1L, TimeUnit.HOURS);
    DynamicSharedService sharedService = new DynamicSharedService(getName());
    assertEquals(oneHour.toMilliseconds(), sharedService.expirationMillis());
    assertNull(sharedService.getExpiration());
    sharedService.withExpiration(new TimeInterval(1L, TimeUnit.MILLISECONDS));
    assertEquals(1, sharedService.expirationMillis());
    assertNotNull(sharedService.getExpiration());
  }

  @Test
  public void testDoService() throws Exception {
    DynamicSharedService sharedService = new DynamicSharedService(getName());
    Adapter adapter = createAdapterForSharedService(sharedService, new NullService(getName()));

    try {
      LifecycleHelper.initAndStart(adapter);
      AdaptrisMessage msg = DefaultMessageFactory.getDefaultInstance().newMessage();
      sharedService.doService(msg);
      sharedService.doService(msg);
      assertEquals(1, sharedService.getCache().size());
      MleMarker marker = msg.getMessageLifecycleEvent().getMleMarkers().get(0);
      assertEquals(NullService.class.getName(), marker.getName());
      assertEquals(getName(), marker.getQualifier());
      assertEquals(true, marker.getWasSuccessful());

    }
    finally {
      LifecycleHelper.stopAndClose(adapter);
    }
    assertEquals(0, sharedService.getCache().size());
  }

  @Test
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

  @Test
  public void testDoService_Selector_CacheLimit() throws Exception {
    DynamicSharedService sharedService = new DynamicSharedService("%message{" + getName() + "}").withMaxEntries(1);
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

  @Test
  public void testDoService_Selector_CacheExpiry() throws Exception {
    DynamicSharedService sharedService = new DynamicSharedService("%message{" + getName() + "}");
    sharedService.setExpiration(new TimeInterval(500L, TimeUnit.MILLISECONDS));
    Adapter adapter = createAdapterForSharedService(sharedService, new NullService("_1"), new NullService("_2"));

    try {
      LifecycleHelper.initAndStart(adapter);
      AdaptrisMessage msg1 = DefaultMessageFactory.getDefaultInstance().newMessage();
      msg1.addMetadata(getName(), "_1");
      sharedService.doService(msg1);
      LifecycleHelper.waitQuietly(1500);
      // should have expired.
      assertEquals(0, sharedService.getCache().size());
    } finally {
      LifecycleHelper.stopAndClose(adapter);
    }
  }

  @Test
  public void testDoService_WithFailure() throws Exception {
    ThrowExceptionService mockService = new ThrowExceptionService(getName(), new ConfiguredException("Fail"));
    DynamicSharedService sharedService = new DynamicSharedService(getName());
    Adapter adapter = createAdapterForSharedService(sharedService, mockService);
    AdaptrisMessage msg = DefaultMessageFactory.getDefaultInstance().newMessage();

    try {
      LifecycleHelper.initAndStart(adapter);
      sharedService.doService(msg);
      fail();
    }
    catch (ServiceException expected) {
      assertEquals(1, sharedService.getCache().size());
      MleMarker marker =  msg.getMessageLifecycleEvent().getMleMarkers().get(0);
      assertEquals(ThrowExceptionService.class.getName(), marker.getName());
      assertEquals(getName(), marker.getQualifier());
      assertEquals(false, marker.getWasSuccessful());
    }
    finally {
      LifecycleHelper.stopAndClose(adapter);
    }
  }

  @Test
  public void testDoService_NoService() throws Exception {
    DynamicSharedService sharedService = new DynamicSharedService(getName());
    Adapter adapter = createAdapterForSharedService(sharedService);

    try {
      LifecycleHelper.initAndStart(adapter);
      AdaptrisMessage msg = DefaultMessageFactory.getDefaultInstance().newMessage();
      sharedService.doService(msg);
      fail();
    } catch (ServiceException expected) {

    } finally {
      LifecycleHelper.stopAndClose(adapter);
    }
  }
}
