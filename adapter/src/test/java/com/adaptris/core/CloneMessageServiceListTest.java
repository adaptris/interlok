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

import com.adaptris.core.metadata.NoOpMetadataFilter;
import com.adaptris.core.metadata.RegexMetadataFilter;
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

  public void testNormalOperationPreserveKey() throws Exception {
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage();
    CloneMessageServiceList service = createServiceList();
    RegexMetadataFilter rmf = new RegexMetadataFilter();
    rmf.addIncludePattern(KEY1);
    service.setOverrideMetadata(true);
    service.setOverrideMetadataFilter(rmf);
    for(Service srvc : service.getServices())
      srvc.changeState(StartedState.getInstance());

    service.doService(msg);

    // md not present because Service applied to a clone
    assertNotNull(msg.getMetadataValue(KEY1));
    assertEquals(VAL1, msg.getMetadataValue(KEY1));
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
    result.setOverrideMetadata(false);
    result.setOverrideMetadataFilter(new NoOpMetadataFilter());

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
    protected void initService() throws CoreException {
      initCount++;
      super.initService();
    }

    int getInitCount() {
      return initCount;
    }
  }
}
