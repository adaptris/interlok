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
import com.adaptris.util.PlainIdGenerator;
import com.adaptris.util.TimeInterval;
import com.adaptris.util.license.License;

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
    public void init() throws CoreException {
      initCount++;
      super.init();
    }

    int getInitCount() {
      return initCount;
    }
  }

  private class MarkerService extends ServiceImp {

    private boolean hasTriggered = false;

    public void doService(AdaptrisMessage msg) throws ServiceException {
      hasTriggered = true;
    }

    public void close() {
      hasTriggered = false;
    }

    public void init() throws CoreException {
    }

    @Override
    public boolean isEnabled(License license) throws CoreException {
      return true;
    }

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