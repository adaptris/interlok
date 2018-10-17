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

import java.util.ArrayList;
import java.util.Arrays;

import com.adaptris.core.services.LogMessageService;
import com.adaptris.core.services.exception.ConfiguredException;
import com.adaptris.core.services.exception.ThrowExceptionService;
import com.adaptris.core.stubs.MockService;
import com.adaptris.core.util.LifecycleHelper;

public class SharedServiceTest extends ServiceCase {

  public SharedServiceTest(String name) {
    super(name);
  }

  @Override
  protected Object retrieveObjectForSampleConfig() {
    return new SharedService("example-shared-service");
  }

  protected Service retrieveObjectForCastorRoundTrip() {
    // bit of a frig really, but otherwise we can't "lookup" in JNDI
    SharedService sharedService = new SharedService(getName());
    sharedService.setClonedService(new LogMessageService(getName()));
    return sharedService;
  }

  protected String getExampleCommentHeader(Object object) {
    return super.getExampleCommentHeader(object)
        + "<!--\n\n  Note that you need to have configured a shared-component with the name 'example-shared-service'\n\n-->\n";
  }

  protected String createBaseFileName(Object object) {
    return SharedService.class.getName();
  }

  public void setUp() throws Exception {
  }

  public void tearDown() throws Exception {

  }

  public void testIsConfirmation() {
    assertFalse(new SharedService(getName()).isConfirmation());
  }

  public void testContinueOnFail() {
    SharedService sharedService = new SharedService(getName());
    assertNull(sharedService.getContinueOnFail());
    assertFalse(sharedService.continueOnFailure());
    sharedService.setContinueOnFail(Boolean.TRUE);
    assertTrue(sharedService.continueOnFailure());
  }

  public void testIsTrackingEndpoint() {
    SharedService sharedService = new SharedService(getName());
    assertNull(sharedService.getIsTrackingEndpoint());
    assertFalse(sharedService.isTrackingEndpoint());
    sharedService.setIsTrackingEndpoint(Boolean.TRUE);
    assertTrue(sharedService.isTrackingEndpoint());
  }

  public void testCloneService() throws Exception {
    NullService mockService = new NullService(getName());
    SharedService sharedService = new SharedService(getName());
    Adapter adapter = createAdapterForSharedService(sharedService, mockService);

    try {
      LifecycleHelper.prepare(adapter);
      LifecycleHelper.init(adapter);
      assertTrue(sharedService.cloneService());
      assertFalse(mockService == sharedService.getClonedService());
      assertEquals(mockService.getUniqueId(), sharedService.getClonedService().getUniqueId());
    }
    finally {
      LifecycleHelper.stopAndClose(adapter);
    }
  }

  public void testNoCloneService() throws Exception {
    NullService mockService = new NullService(getName());
    SharedService sharedService = new SharedService(getName());
    Adapter adapter = createAdapterForSharedService(sharedService, mockService);

    try {
      sharedService.setCloneService(false);
      assertNotNull(sharedService.getCloneService());
      assertFalse(sharedService.cloneService());
      LifecycleHelper.prepare(adapter);
      LifecycleHelper.init(adapter);
      assertTrue(mockService == sharedService.getClonedService());
      assertEquals(mockService.getUniqueId(), sharedService.getClonedService().getUniqueId());
    }
    finally {
      LifecycleHelper.stopAndClose(adapter);
    }
  }

  public void testDoService() throws Exception {
    MockService mockService = new MockService(getName());
    SharedService sharedService = new SharedService(getName());
    Adapter adapter = createAdapterForSharedService(sharedService, mockService);

    try {
      LifecycleHelper.initAndStart(adapter);
      AdaptrisMessage msg = DefaultMessageFactory.getDefaultInstance().newMessage();
      sharedService.doService(msg);
      assertEquals(1, ((MockService) sharedService.getClonedService()).callCount);
      MleMarker marker = msg.getMessageLifecycleEvent().getMleMarkers().get(0);
      assertEquals(MockService.class.getName(), marker.getName());
      assertEquals(getName(), marker.getQualifier());
      assertEquals(true, marker.getWasSuccessful());

    }
    finally {
      LifecycleHelper.stopAndClose(adapter);
    }
  }

  public void testDoService_WithFailure() throws Exception {
    ThrowExceptionService mockService = new ThrowExceptionService(getName(), new ConfiguredException("Fail"));
    SharedService sharedService = new SharedService(getName());
    Adapter adapter = createAdapterForSharedService(sharedService, mockService);
    AdaptrisMessage msg = DefaultMessageFactory.getDefaultInstance().newMessage();

    try {
      LifecycleHelper.initAndStart(adapter);
      sharedService.doService(msg);
      fail();
    }
    catch (ServiceException expected) {
      MleMarker marker = msg.getMessageLifecycleEvent().getMleMarkers().get(0);
      assertEquals(ThrowExceptionService.class.getName(), marker.getName());
      assertEquals(getName(), marker.getQualifier());
      assertEquals(false, marker.getWasSuccessful());
    }
    finally {
      LifecycleHelper.stopAndClose(adapter);
    }
  }

  static Adapter createAdapterForSharedService(SharedServiceImpl sharedService, Service... mockServices) throws Exception {
    Adapter adapter = new Adapter();
    adapter.setUniqueId("adapterId");
    adapter.getSharedComponents().setServices(new ArrayList(Arrays.asList(mockServices)));

    adapter.getChannelList().add(new Channel());
    adapter.getChannelList().get(0).getWorkflowList().add(new StandardWorkflow());
    ((StandardWorkflow) adapter.getChannelList().get(0).getWorkflowList().get(0)).getServiceCollection().add(sharedService);

    return adapter;
  }

}
