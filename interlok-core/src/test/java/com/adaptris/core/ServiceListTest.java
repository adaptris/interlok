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

import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.TimeUnit;

import com.adaptris.core.services.WaitService;
import com.adaptris.core.services.exception.ConfiguredException;
import com.adaptris.core.services.exception.ThrowExceptionService;
import com.adaptris.core.services.metadata.AddMetadataService;
import com.adaptris.core.stubs.CheckComponentStateService;
import com.adaptris.core.stubs.MockStopProcessingService;
import com.adaptris.core.util.LifecycleHelper;
import com.adaptris.util.PlainIdGenerator;
import com.adaptris.util.TimeInterval;

public class ServiceListTest extends ServiceCollectionCase {

  private static final String VAL4 = "val4";
  private static final String KEY4 = "key4";
  private static final String VAL3 = "val3";
  private static final String KEY3 = "key3";
  private static final String VAL2 = "val2";
  private static final String KEY2 = "key2";
  private static final String VAL1 = "val1";
  private static final String KEY1 = "key1";

  public ServiceListTest(String name) {
    super(name);
  }

  @Override
  public ServiceList createServiceCollection() {
    return new ServiceList();
  }

  @Override
  public ServiceList createServiceCollection(Collection<Service> c) {
    return new ServiceList(c);
  }

  private ServiceList createServiceList(boolean assignId) {
    ServiceList services = createServiceCollection();
    services.addService(new AddMetadataService(Arrays.asList(new MetadataElement(KEY1, VAL1))));
    services.addService(new AddMetadataService(Arrays.asList(new MetadataElement(KEY2, VAL2))));
    if (assignId) {
    for (Service s : services) {
        s.setUniqueId(new PlainIdGenerator().create(s));
    }
    }
    return services;

  }

  @Override
  public void testAddService() {
    ServiceList services = createServiceList(false);
    AddMetadataService newService = new AddMetadataService(Arrays.asList(new MetadataElement(KEY3, VAL3)));
    services.addService(newService);
    Collection tmp = services.getServices();
    assertTrue(tmp.contains(newService));
  }

  public void testDoService() throws CoreException {
    ServiceList services = createServiceList(true);

    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage();
    execute(services, msg);

    assertTrue(msg.getMetadataValue(KEY1).equals(VAL1));
    assertTrue(msg.getMetadataValue(KEY2).equals(VAL2));
    assertTrue(msg.getMetadataValue(KEY3) == null);
  }
  
  public void testDoServiceOutOfState() throws CoreException {
    ServiceList services = createServiceList(true);

    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage();
    try {
      executeWithoutStarting(services, msg);
      fail("Should fail, services are not started.");
    } catch (ServiceException ex) {
      //expected
    }
  }

  public void testBreakOutOfServiceList() throws CoreException {
    ServiceList services = createServiceList(false);

    services.addService(new MockStopProcessingService());
    services.addService(new AddMetadataService(Arrays.asList(new MetadataElement(KEY3, VAL3))));

    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage();
    execute(services, msg);

    assertTrue(msg.getMetadataValue(KEY1).equals(VAL1));
    assertTrue(msg.getMetadataValue(KEY2).equals(VAL2));

    // test break out metadata has been set...
    assertTrue(msg.getMetadataValue(CoreConstants.STOP_PROCESSING_KEY).equals(CoreConstants.STOP_PROCESSING_VALUE));

    // ...and that fourth service has not been applied
    assertTrue(msg.getMetadataValue(KEY3) == null);
  }
  
  public void testDontProcessFirstServiceIfStopProcessingSet() throws CoreException {
    ServiceList services = createServiceList(false);

    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage();
    msg.addMetadata(CoreConstants.STOP_PROCESSING_KEY, CoreConstants.STOP_PROCESSING_VALUE);
    
    execute(services, msg);

    // Test that services have not been applied
    assertNull(msg.getMetadataValue(KEY1));
    assertNull(msg.getMetadataValue(KEY2));
  }

  public void testFailWithNoContinueOnFail() throws Exception {
    ServiceList services = createServiceList(false);
    services.addService(new ThrowExceptionService(new ConfiguredException("Fail")));
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage();

    try {
      execute(services, msg);
      fail("Expected Service Exception");
    }
    catch (ServiceException e) {
      assertEquals("Fail", e.getMessage());
    }
  }

  public void testFail_CheckObjectMetadata() throws Exception {
    ServiceList services = createServiceList(false);
    ThrowExceptionService errorService = new ThrowExceptionService(new ConfiguredException("Fail"));
    errorService.setUniqueId(getName());
    services.addService(errorService);
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage();
    String expectedName = String.format("%s(%s)",  ThrowExceptionService.class.getSimpleName(), getName());

    try {
      execute(services, msg);
      fail("Expected Service Exception");
    }
    catch (ServiceException e) {
      assertEquals("Fail", e.getMessage());
      Exception captured = (Exception) msg.getObjectHeaders().get(CoreConstants.OBJ_METADATA_EXCEPTION);
      String problemComponent = (String) msg.getObjectHeaders().get(CoreConstants.OBJ_METADATA_EXCEPTION_CAUSE);
      assertNotNull(captured);
      assertEquals(captured, e);
      assertNotNull(problemComponent);
      assertEquals(expectedName, problemComponent);
    }
  }

  public void testFail_NestedServiceList_ObjectMetadata() throws Exception {
    ServiceList services = createServiceList(false);
    ThrowExceptionService errorService = new ThrowExceptionService(new ConfiguredException("Fail"));
    errorService.setUniqueId(getName());
    services.addService(new ServiceList(new Service[]
    {
        errorService
    }));
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage();
    String expectedName = String.format("%s(%s)", ThrowExceptionService.class.getSimpleName(), getName());

    try {
      execute(services, msg);
      fail("Expected Service Exception");
    }
    catch (ServiceException e) {
      assertEquals("Fail", e.getMessage());
      Exception captured = (Exception) msg.getObjectHeaders().get(CoreConstants.OBJ_METADATA_EXCEPTION);
      String problemComponent = (String) msg.getObjectHeaders().get(CoreConstants.OBJ_METADATA_EXCEPTION_CAUSE);
      assertNotNull(captured);
      assertEquals(captured, e);
      assertNotNull(problemComponent);
      assertEquals(expectedName, problemComponent);
    }
  }

  public void testFailWithNoContinueOnFailAndRuntimeException() throws Exception {
    ServiceList services = createServiceList(false);
    services.addService(new NullService() {
      @Override
      public void doService(AdaptrisMessage msg) throws ServiceException {
        throw new RuntimeException("testFailWithNoContinueOnFailAndRuntimeException");
      }
    });
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage();

    try {
      execute(services, msg);
      fail("Expected Service Exception");
    }
    catch (ServiceException e) {
      assertNotNull(e.getCause());
      assertEquals("testFailWithNoContinueOnFailAndRuntimeException", e.getCause().getMessage());
    }
  }

  public void testFailWithContinueOnFail() throws Exception {
    ServiceList services = createServiceList(true);

    ThrowExceptionService service3 = new ThrowExceptionService(new ConfiguredException("Fail"));
    service3.setContinueOnFail(true);
    services.addService(service3);
    services.addService(new AddMetadataService(Arrays.asList(new MetadataElement(KEY4, VAL4))));
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage();
    execute(services, msg);
    assertTrue(msg.getMetadataValue(KEY4).equals(VAL4));
  }

  public void testRestartAffectedServiceOnFail() throws Exception {
    ServiceList services = createServiceList(true);
    services.setRestartAffectedServiceOnException(Boolean.TRUE);
    FailWithInitCount service = new FailWithInitCount();
    services.addService(service);

    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage();

    try {
      execute(services, msg);
      fail("Expected Service Exception");
    }
    catch (Exception e) {
      assertEquals(2, service.getInitCount());
      // expected...
    }
  }

  public void testBug2055() throws Exception {
    String name = Thread.currentThread().getName();
    Thread.currentThread().setName("testBug2055");
    final ServiceList services = new ServiceList();
    MarkerService marker = new MarkerService();
    services.addService(marker);
    services.addService(new WaitService(new TimeInterval(3L, TimeUnit.SECONDS)));
    services.addService(new CheckComponentStateService());
    start(services);
    final ExceptionContainer c = new ExceptionContainer();
    Thread t = new Thread(new Runnable() {

      public void run() {
        try {
          AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage();
          services.doService(msg);
        }
        catch (ServiceException e) {
          c.setException(e);
        }
      }
    });
    t.start();
    while (!marker.hasTriggered) {
      Thread.sleep(10);
    }
    stop(services);
    t.join();
    if (c.getException() != null) {
      fail("Services failed, " + c.getException().getMessage());
    }
    Thread.currentThread().setName(name);
  }

  public void testAllowSkip() {
    ServiceList result = new ServiceList();
    assertTrue(result.forwardSearch());
    assertNull(result.getAllowForwardSearch());
    result.setAllowForwardSearch(false);
    assertEquals(Boolean.FALSE, result.getAllowForwardSearch());
    assertFalse(result.forwardSearch());
  }

  public void testForwardSearch_Default() throws Exception {
    NextServiceIdSetter first = new NextServiceIdSetter(getName(), "third");
    MarkerService second = new MarkerService("second");
    MarkerService third = new MarkerService("third");
    MarkerService fourth = new MarkerService("fourth");
    ServiceList service = new ServiceList(first, second, third, fourth);
    try {
      LifecycleHelper.initAndStart(service);
      AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage();
      service.doService(msg);
      assertFalse(second.hasTriggered);
      assertTrue(third.hasTriggered);
      assertTrue(fourth.hasTriggered);
    } finally {
      LifecycleHelper.stopAndClose(service);
    }
  }

  public void testForwardSearch_NotEnabled() throws Exception {
    NextServiceIdSetter first = new NextServiceIdSetter(getName(), "third");
    MarkerService second = new MarkerService("second");
    MarkerService third = new MarkerService("third");
    MarkerService fourth = new MarkerService("fourth");
    ServiceList service = new ServiceList(first, second, third, fourth);
    service.setAllowForwardSearch(false);
    try {
      LifecycleHelper.initAndStart(service);
      AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage();
      service.doService(msg);
      assertTrue(second.hasTriggered);
      assertTrue(third.hasTriggered);
      assertTrue(fourth.hasTriggered);
    } finally {
      LifecycleHelper.stopAndClose(service);
    }
  }

  public void testForwardSearch_MissingUniqueId() throws Exception {
    NextServiceIdSetter first = new NextServiceIdSetter(getName(), "third");
    MarkerService second = new MarkerService("");
    MarkerService third = new MarkerService("");
    MarkerService fourth = new MarkerService("");
    ServiceList service = new ServiceList(first, second, third, fourth);
    try {
      LifecycleHelper.initAndStart(service);
      AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage();
      service.doService(msg);
      assertTrue(second.hasTriggered);
      assertTrue(third.hasTriggered);
      assertTrue(fourth.hasTriggered);
    } finally {
      LifecycleHelper.stopAndClose(service);
    }
  }


  public void testForwardSearch_NoMatch() throws Exception {
    NextServiceIdSetter first = new NextServiceIdSetter(getName(), "99 Luftballon");
    MarkerService second = new MarkerService("second");
    MarkerService third = new MarkerService("third");
    MarkerService fourth = new MarkerService("fourth");
    ServiceList service = new ServiceList(first, second, third, fourth);
    try {
      LifecycleHelper.initAndStart(service);
      AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage();
      service.doService(msg);
      assertTrue(second.hasTriggered);
      assertTrue(third.hasTriggered);
      assertTrue(fourth.hasTriggered);
    } finally {
      LifecycleHelper.stopAndClose(service);
    }
  }

  public void testForwardSearch_BackwardsNotAllowed() throws Exception {
    MarkerService first = new MarkerService("first");
    NextServiceIdSetter second = new NextServiceIdSetter(getName(), "first");
    MarkerService third = new MarkerService("third");
    MarkerService fourth = new MarkerService("fourth");
    ServiceList service = new ServiceList(first, second, third, fourth);
    try {
      LifecycleHelper.initAndStart(service);
      AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage();
      service.doService(msg);
      assertTrue(first.hasTriggered);
      assertTrue(third.hasTriggered);
      assertTrue(fourth.hasTriggered);
    } finally {
      LifecycleHelper.stopAndClose(service);
    }
  }

  @Override
  protected Object retrieveObjectForSampleConfig() {
    ServiceList result = new ServiceList();
    result.setRestartAffectedServiceOnException(false);
    result.addService(new NullService());
    result.addService(new NullService());
    result.addService(new NullService());

    return result;
  }

  private class FailWithInitCount extends ThrowExceptionService {
    private int initCount = 0;

    FailWithInitCount() {
      super(new ConfiguredException("Fail"));
    }

    @Override
    protected void initService() throws CoreException {
      initCount++;
      super.initService();
    }


    int getInitCount() {
      return initCount;
    }
  }

  private class MarkerService extends ServiceImp {

    private transient boolean hasTriggered = false;

    public MarkerService() {}

    public MarkerService(String s) {
      this();
      setUniqueId(s);
    }

    public void doService(AdaptrisMessage msg) throws ServiceException {
      hasTriggered = true;
    }

    @Override
    protected void initService() throws CoreException {}

    @Override
    protected void closeService() {
      hasTriggered = false;
    }

    @Override
    public void prepare() throws CoreException {}

  }

  private class NextServiceIdSetter extends ServiceImp {

    private transient String nextServiceId;

    private NextServiceIdSetter(String myUid, String next) {
      super();
      setUniqueId(myUid);
      nextServiceId = next;
    }

    public void doService(AdaptrisMessage msg) throws ServiceException {
      msg.setNextServiceId(nextServiceId);
    }

    @Override
    protected void initService() throws CoreException {}

    @Override
    protected void closeService() {}

    @Override
    public void prepare() throws CoreException {}

  }
  private class ExceptionContainer {
    private Exception exception;

    ExceptionContainer() {

    }

    public Exception getException() {
      return exception;
    }

    public void setException(Exception e) {
      this.exception = e;
    }
  }

}
