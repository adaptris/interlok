/*
 * $RCSfile: BranchingServiceCollectionTest.java,v $
 * $Revision: 1.9 $
 * $Date: 2008/08/13 13:28:43 $
 * $Author: lchan $
 */
package com.adaptris.core;

import java.util.Arrays;
import java.util.Collection;
import java.util.UUID;

import com.adaptris.core.services.metadata.AddMetadataService;
import com.adaptris.core.stubs.EventHandlerAwareService;
import com.adaptris.core.stubs.ExampleBranchingService;
import com.adaptris.core.stubs.TestBranchingService;
import com.adaptris.core.util.LifecycleHelper;

public class BranchingServiceCollectionTest extends ServiceCollectionCase {

  private static final String BRANCH_LOW = "001";
  private static final String BRANCH_HIGH = "002";
  private static final String FIRST_SERVICE_ID = "000";
  private TestBranchingService branchService;
  private AddMetadataService lowService;
  private AddMetadataService highService;
  private static final String BASE_DIR_KEY = "BranchingServiceExamples.baseDir";

  public BranchingServiceCollectionTest(String name) {
    super(name);
    if (PROPERTIES.getProperty(BASE_DIR_KEY) != null) {
      setBaseDir(PROPERTIES.getProperty(BASE_DIR_KEY));
    }
  }

  @Override
  protected void setUp() throws Exception {
    branchService = new TestBranchingService();
    branchService.setUniqueId(FIRST_SERVICE_ID);

    lowService = new AddMetadataService();
    lowService.setUniqueId(BRANCH_LOW);
    lowService.addMetadataElement("service-id", BRANCH_LOW);

    highService = new AddMetadataService();
    highService.setUniqueId(BRANCH_HIGH);
    highService.addMetadataElement("service-id", BRANCH_HIGH);

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

  public void testSetFirstServiceId() throws Exception {
    BranchingServiceCollection services = createServiceCollection();
    services.setFirstServiceId(FIRST_SERVICE_ID);
    try {
      services.setFirstServiceId(null);
      fail("Null or empty first-service-id");
    }
    catch (IllegalArgumentException expected) {

    }
    try {
      services.setFirstServiceId("");
      fail("Null or empty first-service-id");
    }
    catch (IllegalArgumentException expected) {

    }
    try {
      services.setFirstServiceId(CoreConstants.ENDPOINT_SERVICE_UNIQUE_ID);
      fail("CoreConstants.ENDPOINT_SERVICE_UNIQUE_ID as first-service-id");
    }
    catch (IllegalArgumentException expected) {

    }
    assertEquals(FIRST_SERVICE_ID, services.getFirstServiceId());
  }

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

  public void testAddServiceNoUniqueId() throws Exception {
    BranchingServiceCollection services = createServiceCollection();
    try {
      services.add(new NullService(""));
      fail();
    }
    catch (IllegalArgumentException expected) {

    }
  }

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

    services.setCheckServiceState(false); // this time run without checking states.
    AdaptrisMessage msg2 = AdaptrisMessageFactory.getDefaultInstance().newMessage();
    services.doService(msg2);
    assertTrue(msg2.getMetadataValue("service-id").equals(BRANCH_HIGH));
    assertEquals(2, msg.getMessageLifecycleEvent().getMleMarkers().size());
  }
  
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
