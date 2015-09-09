package com.adaptris.core;

import java.util.Arrays;
import java.util.Collection;

import com.adaptris.core.services.exception.ConfiguredException;
import com.adaptris.core.services.exception.ThrowExceptionService;
import com.adaptris.core.services.metadata.AddMetadataService;

public class CloneMessageServiceListTest extends ServiceCollectionCase {

  private static final String VAL1 = "val1";
  private static final String KEY1 = "key1";

  public CloneMessageServiceListTest(String name) {
    super(name);
  }

  @Override
  public CloneMessageServiceList createServiceCollection() {
    return new CloneMessageServiceList();
  }

  @Override
  public CloneMessageServiceList createServiceCollection(Collection<Service> c) {
    return new CloneMessageServiceList(c);
  }

  private CloneMessageServiceList createServiceList() {
    CloneMessageServiceList services = createServiceCollection();
    services.addService(new AddMetadataService(Arrays.asList(new MetadataElement(KEY1, VAL1))));
    return services;
  }

  public void testServiceOutOfStateOperation() throws Exception {
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage();
    CloneMessageServiceList service = createServiceList();
    try {
      service.doService(msg);
      fail("Should fail because service is not 'started'");
    } catch (ServiceException ex) {
      //expected
    }
  }
  
  public void testNormalOperation() throws Exception {
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage();
    CloneMessageServiceList service = createServiceList();
    for(Service srvc : service.getServices())
      srvc.changeState(StartedState.getInstance());
    
    service.doService(msg);

    // md not present because Service applied to a clone
    assertTrue(msg.getMetadataValue(KEY1) == null);
  }

  public void testFailWithNoContinueOnFail() throws Exception {
    CloneMessageServiceList services = createServiceList();
    services.addService(new ThrowExceptionService(new ConfiguredException("Fail")));
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage();

    try {
      execute(services, msg);
      fail("Expected Service Exception");
    }
    catch (Exception e) {
      // expected...
    }
  }

  public void testFailWithContinueOnFail() throws Exception {
    CloneMessageServiceList services = createServiceList();
    ServiceImp service3 = new ThrowExceptionService(new ConfiguredException("Fail"));
    service3.setContinueOnFail(true);
    services.addService(service3);
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage();
    execute(services, msg);
  }

  public void testRestartAffectedServiceOnFail() throws Exception {
    CloneMessageServiceList services = createServiceList();
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

  @Override
  protected Object retrieveObjectForSampleConfig() {
    CloneMessageServiceList result = new CloneMessageServiceList();
    result.addService(new NullService());
    result.addService(new NullService());
    result.addService(new NullService());

    return result;
  }

  protected Class marshalledClassName() {
    return CloneMessageServiceList.class;
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
}
