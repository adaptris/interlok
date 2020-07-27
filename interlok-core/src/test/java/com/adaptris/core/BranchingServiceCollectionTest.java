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

package com.adaptris.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.util.Arrays;
import java.util.Collection;
import java.util.UUID;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import com.adaptris.core.services.metadata.AddMetadataService;
import com.adaptris.core.stubs.EventHandlerAwareService;
import com.adaptris.core.stubs.ExampleBranchingService;
import com.adaptris.core.stubs.TestBranchingService;
import com.adaptris.core.util.LifecycleHelper;
import com.adaptris.interlok.util.Closer;

public class BranchingServiceCollectionTest extends ServiceCollectionCase {

  private static final String BRANCH_LOW = "001";
  private static final String BRANCH_HIGH = "002";
  private static final String FIRST_SERVICE_ID = "000";
  private TestBranchingService branchService;
  private AddMetadataService lowService;
  private AddMetadataService highService;
  private static final String BASE_DIR_KEY = "BranchingServiceExamples.baseDir";

  @Mock
  private Service mockFailingService;
  @Mock
  private OutOfStateHandler mockOutOfStateHandler;

  private AutoCloseable openMocks = null;

  public BranchingServiceCollectionTest() {
    if (PROPERTIES.getProperty(BASE_DIR_KEY) != null) {
      setBaseDir(PROPERTIES.getProperty(BASE_DIR_KEY));
    }
  }

  @Override
  public boolean isAnnotatedForJunit4() {
    return true;
  }
  @Before
  public void setUp() throws Exception {
    branchService = new TestBranchingService();
    branchService.setUniqueId(FIRST_SERVICE_ID);

    lowService = new AddMetadataService();
    lowService.setUniqueId(BRANCH_LOW);
    lowService.addMetadataElement("service-id", BRANCH_LOW);

    highService = new AddMetadataService();
    highService.setUniqueId(BRANCH_HIGH);
    highService.addMetadataElement("service-id", BRANCH_HIGH);

    openMocks = MockitoAnnotations.openMocks(this);
  }

  @After
  public void tearDown() throws Exception {
    Closer.closeQuietly(openMocks);
  }

  @Override
  public void testInitWithEventHandlerAware() throws Exception {
    EventHandlerAwareService s = new EventHandlerAwareService(UUID.randomUUID().toString());
    BranchingServiceCollection sc = createServiceCollection();
    sc.setFirstServiceId(FIRST_SERVICE_ID);
    DefaultEventHandler eh = new DefaultEventHandler();
    sc.registerEventHandler(eh);
    sc.addService(new NullService(UUID.randomUUID().toString()));
    sc.addService(s);
    LifecycleHelper.init(sc);
    assertNotNull(s.retrieveEventHandler());
    assertEquals(eh, s.retrieveEventHandler());
  }

  @Test
  public void testSetFirstServiceId() throws Exception {
    BranchingServiceCollection services = createServiceCollection();
    services.setFirstServiceId(FIRST_SERVICE_ID);
    try {
      services.setFirstServiceId(null);
      fail("Null or empty first-service-id");
    }
    catch (IllegalArgumentException expected) {

    }
    assertEquals(FIRST_SERVICE_ID, services.getFirstServiceId());
  }

  @Test
  public void testAddServiceWithDuplicates() throws Exception {
    BranchingServiceCollection services = createServiceCollection();
    String uniqueId = UUID.randomUUID().toString();
    services.add(new NullService(uniqueId));
    try {
      services.add(new NullService(uniqueId));
      fail();
    }
    catch (IllegalArgumentException expected) {

    }
  }

  @Test
  public void testAddServiceNoUniqueId() throws Exception {
    BranchingServiceCollection services = createServiceCollection();
    try {
      services.add(new NullService(""));
      fail();
    }
    catch (IllegalArgumentException expected) {

    }
  }

  @Test
  public void testAddAllWithDuplicates() throws Exception {
    BranchingServiceCollection services = createServiceCollection();
    ServiceList sl = new ServiceList();
    String uniqueId = UUID.randomUUID().toString();
    sl.add(new NullService(uniqueId));
    sl.add(new NullService(uniqueId));
    sl.add(new NullService(uniqueId));
    try {
      services.addAll(sl);
      fail();
    }
    catch (IllegalArgumentException expected) {
      ;
    }
    try {
      services.addAll(0, sl);
      fail();
    }
    catch (IllegalArgumentException expected) {
      ;
    }
  }

  @Test
  public void testAddAllWithNoUniqueId() throws Exception {
    BranchingServiceCollection services = createServiceCollection();
    ServiceList sl = new ServiceList();
    sl.add(new NullService(""));
    try {
      services.addAll(sl);
      fail();
    }
    catch (IllegalArgumentException expected) {
      ;
    }
    try {
      services.addAll(0, sl);
      fail();
    }
    catch (IllegalArgumentException expected) {
      ;
    }
  }

  @Test
  public void testInit() {
    BranchingServiceCollection services = createServiceCollection();
    try {
      LifecycleHelper.init(services);
      fail("should not be able to init without first Service ID");
    }
    catch (CoreException e) {
      // expected
    }
    services.setFirstServiceId(BRANCH_LOW);
    try {
      LifecycleHelper.init(services);
    }
    catch (CoreException e) {
      fail("should not get Exception once first Service ID is set");
    }
  }

  @Test
  public void testSetList() throws Exception {
    BranchingServiceCollection services = createServiceCollection();
    services.setServices(Arrays.asList(new Service[]
    {
        branchService, lowService, highService
    }));
    services.setFirstServiceId(FIRST_SERVICE_ID);
    start(services);
    stop(services);
    NullService illegalService1 = new NullService();
    illegalService1.setUniqueId(BRANCH_HIGH);
    try {
      services.setServices(Arrays.asList(new Service[]
      {
          branchService, lowService, highService, illegalService1
      }));
      fail("Initialised with duplicate service-id");
    }
    catch (IllegalArgumentException expected) {

    }
    NullService illegalService2 = new NullService();
    illegalService2.setUniqueId("");
    try {
      services.setServices(Arrays.asList(new Service[]
      {
          branchService, lowService, highService, illegalService2
      }));
      fail("Initialised with '' service-id");
    }
    catch (IllegalArgumentException expected) {

    }
  }

  @Test
  public void testDoService() throws Exception {
    BranchingServiceCollection services = createServiceCollection();
    branchService.changeState(StartedState.getInstance());
    lowService.changeState(StartedState.getInstance());
    highService.changeState(StartedState.getInstance());

    services.addService(branchService);
    services.addService(lowService);
    services.addService(highService);

    services.setFirstServiceId(FIRST_SERVICE_ID);

    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage();
    execute(services, msg);

    assertTrue("msg " + msg, msg.getMetadataValue("service-id").equals(BRANCH_LOW));
    assertEquals(2, msg.getMessageLifecycleEvent().getMleMarkers().size());

    services.setOutOfStateHandler(new NullOutOfStateHandler());
    AdaptrisMessage msg2 = AdaptrisMessageFactory.getDefaultInstance().newMessage();
    services.doService(msg2);
    assertTrue(msg2.getMetadataValue("service-id").equals(BRANCH_HIGH));
    assertEquals(2, msg.getMessageLifecycleEvent().getMleMarkers().size());
  }

  @Test
  public void testDoService_Legacy() throws Exception {
    BranchingServiceCollection services = createServiceCollection();
    branchService.changeState(StartedState.getInstance());
    lowService.changeState(StartedState.getInstance());
    highService.changeState(StartedState.getInstance());

    services.addService(branchService);
    services.addService(lowService);
    services.addService(highService);

    services.setFirstServiceId(FIRST_SERVICE_ID);

    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage();
    execute(services, msg);

    assertTrue("msg " + msg, msg.getMetadataValue("service-id").equals(BRANCH_LOW));
    assertEquals(2, msg.getMessageLifecycleEvent().getMleMarkers().size());

    services.setOutOfStateHandler(new NullOutOfStateHandler()); // this time run without checking states.
    AdaptrisMessage msg2 = AdaptrisMessageFactory.getDefaultInstance().newMessage();
    services.doService(msg2);
    assertTrue(msg2.getMetadataValue("service-id").equals(BRANCH_HIGH));
    assertEquals(2, msg.getMessageLifecycleEvent().getMleMarkers().size());
  }

  @Test
  public void testDoServiceOutOfState() throws Exception {
    BranchingServiceCollection services = createServiceCollection();
    services.addService(branchService);
    services.addService(lowService);
    services.addService(highService);

    services.setFirstServiceId(FIRST_SERVICE_ID);

    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage();
    try {
      branchService.changeState(ClosedState.getInstance());
      lowService.changeState(ClosedState.getInstance());
      highService.changeState(ClosedState.getInstance());

      executeWithoutStarting(services, msg);
      fail("Should fail because services are not 'Started'");
    } catch (ServiceException ex) {
      // expected
    }
  }

  @Test
  public void testDoServiceBadServiceId() throws Exception {
    BranchingServiceCollection services = createServiceCollection();
    services.addService(branchService);
    services.addService(lowService);
    services.addService(highService);

    services.setFirstServiceId("005");
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage();
    try {
      execute(services, msg);
      fail("BadService ID == success");
    }
    catch (ServiceException expected) {

    }

  }

  @Test
  public void testDoServiceFailsNoRestart() throws Exception {
    doThrow(new ServiceException("Expected")).when(mockFailingService).doService(any(AdaptrisMessage.class));
    when(mockFailingService.getUniqueId()).thenReturn(FIRST_SERVICE_ID);
    when(mockFailingService.createName()).thenReturn(Service.class.getName());

    when(mockOutOfStateHandler.isInCorrectState(mockFailingService)).thenReturn(true);

    BranchingServiceCollection services = createServiceCollection();
    services.setOutOfStateHandler(mockOutOfStateHandler);

    services.setFirstServiceId(FIRST_SERVICE_ID);
    services.setRestartAffectedServiceOnException(false);
    services.addService(mockFailingService);

    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage();
    try {
      services.doService(msg);
      fail("Mock service should fail.");
    }
    catch (ServiceException expected) {
      verify(mockFailingService, never()).requestStop();
      verify(mockFailingService, never()).requestClose();
      verify(mockFailingService, never()).requestInit();
      verify(mockFailingService, never()).requestStart();
    }
  }

  @Test
  public void testDoServiceFailsWithServiceRestart() throws Exception {
    doThrow(new ServiceException("Expected")).when(mockFailingService).doService(any(AdaptrisMessage.class));
    when(mockFailingService.getUniqueId()).thenReturn(FIRST_SERVICE_ID);
    when(mockFailingService.createName()).thenReturn(Service.class.getName());

    when(mockOutOfStateHandler.isInCorrectState(mockFailingService)).thenReturn(true);

    BranchingServiceCollection services = createServiceCollection();
    services.setOutOfStateHandler(mockOutOfStateHandler);

    services.setFirstServiceId(FIRST_SERVICE_ID);
    services.setRestartAffectedServiceOnException(true);
    services.addService(mockFailingService);

    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage();
    try {
      services.doService(msg);
      fail("Mock service should fail.");
    }
    catch (ServiceException expected) {
      verify(mockFailingService, times(1)).requestStop();
      verify(mockFailingService, times(1)).requestClose();
      verify(mockFailingService, times(1)).requestInit();
      verify(mockFailingService, times(1)).requestStart();
    }
  }

  @Override
  protected Object retrieveObjectForSampleConfig() {
    BranchingServiceCollection services = createServiceCollection();
    services.setFirstServiceId(FIRST_SERVICE_ID);

    services.addService(lowService);
    services.addService(highService);

    ExampleBranchingService branching = new ExampleBranchingService();
    branching.setUniqueId(FIRST_SERVICE_ID);
    branching.setHigherServiceId(BRANCH_HIGH);
    branching.setLowerServiceId(BRANCH_LOW);

    services.addService(branching);

    return services;
  }

  @Override
  public BranchingServiceCollection createServiceCollection() {
    return new BranchingServiceCollection();
  }

  @Override
  public BranchingServiceCollection createServiceCollection(Collection<Service> c) {
    return new BranchingServiceCollection(c);
  }

}
