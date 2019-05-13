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

package com.adaptris.core.services;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.AdaptrisMessageProducer;
import com.adaptris.core.ClosedState;
import com.adaptris.core.CoreException;
import com.adaptris.core.GeneralServiceExample;
import com.adaptris.core.NullService;
import com.adaptris.core.Service;
import com.adaptris.core.ServiceException;
import com.adaptris.core.ServiceList;
import com.adaptris.core.StandaloneProducer;
import com.adaptris.core.stubs.ExampleBranchingService;
import com.adaptris.core.stubs.MockMessageProducer;
import com.adaptris.core.util.LifecycleHelper;

public class StatelessServiceWrapperTest extends GeneralServiceExample {

  public StatelessServiceWrapperTest(java.lang.String testName) {
    super(testName);
  }

  @Override
  protected void setUp() throws Exception {
  }

  public void testNoWrappedService() throws Exception {
    StatelessServiceWrapper ws = new StatelessServiceWrapper();
    ws.setContinueOnFail(true);
    ws.setIsTrackingEndpoint(true);
    ws.setUniqueId(ws.getClass().getSimpleName());
    assertNotNull(ws.getContinueOnFail());
    assertNotNull(ws.getIsTrackingEndpoint());
    assertEquals(true, ws.isTrackingEndpoint());
    assertEquals(true, ws.continueOnFailure());
    assertNull(ws.getService());
    assertEquals(ws.getClass().getSimpleName(), ws.createQualifier());
    assertEquals(ws.getClass().getName(), ws.createName());
    assertNotNull(ws.wrappedServices());
    assertEquals(0, ws.wrappedServices().length);

  }

  public void testWrappingOfServiceList() throws Exception {
    ServiceList ws = new ServiceList();
    ws.setUniqueId(ServiceList.class.getSimpleName());
    ws.setContinueOnFail(true);
    ws.setIsTrackingEndpoint(true);
    StatelessServiceWrapper s = new StatelessServiceWrapper(ws);
    s.setUniqueId(StatelessServiceWrapper.class.getSimpleName());
    assertNotNull(ws.getIsTrackingEndpoint());
    assertEquals(ws.isTrackingEndpoint(), s.isTrackingEndpoint());
    assertEquals(ws.isBranching(), s.isBranching());
    assertEquals(ws.createName(), s.createName());
    assertEquals(ws.createQualifier(), s.createQualifier());
    assertNotSame(ws.getUniqueId(), s.getUniqueId());
  }

  public void testWrappingOfServiceListWrappedSettersAreCalled() throws Exception {
    ServiceList ws = new ServiceList();
    ws.setUniqueId(ServiceList.class.getSimpleName());
    StatelessServiceWrapper s = new StatelessServiceWrapper(ws);
    s.setUniqueId(StatelessServiceWrapper.class.getSimpleName());
    s.setContinueOnFail(true);
    s.setIsTrackingEndpoint(true);
    assertNotNull(ws.getIsTrackingEndpoint());
    assertEquals(s.getIsTrackingEndpoint(), ws.getIsTrackingEndpoint());
    assertEquals(ws.isTrackingEndpoint(), s.isTrackingEndpoint());
  }

  public void testWrapping() throws Exception {
    ExampleBranchingService ws = new ExampleBranchingService();
    ws.setUniqueId(ExampleBranchingService.class.getSimpleName());
    ws.setContinueOnFail(true);
    ws.setIsTrackingEndpoint(true);
    StatelessServiceWrapper s = new StatelessServiceWrapper(ws);
    s.setUniqueId(StatelessServiceWrapper.class.getSimpleName());
    assertNotNull(ws.getIsTrackingEndpoint());
    assertEquals(ws.isTrackingEndpoint(), s.isTrackingEndpoint());
    assertEquals(ws.isBranching(), s.isBranching());
    assertEquals(ws.createName(), s.createName());
    assertEquals(ws.createQualifier(), s.createQualifier());
    assertNotSame(ws.getUniqueId(), s.getUniqueId());
    ws.setUniqueId("");
    assertEquals(StatelessServiceWrapper.class.getSimpleName(), s.createQualifier());
    assertNotNull(s.wrappedServices());
    assertEquals(1, s.wrappedServices().length);

  }

  public void testWrappingWrappedSettersAreCalled() throws Exception {
    ExampleBranchingService ws = new ExampleBranchingService();
    ws.setUniqueId(ExampleBranchingService.class.getSimpleName());
    StatelessServiceWrapper s = new StatelessServiceWrapper(ws);
    s.setUniqueId(StatelessServiceWrapper.class.getSimpleName());
    s.setContinueOnFail(true);
    s.setIsTrackingEndpoint(true);
    assertNotNull(ws.getIsTrackingEndpoint());
    assertEquals(s.getIsTrackingEndpoint(), ws.getIsTrackingEndpoint());
    assertEquals(ws.isTrackingEndpoint(), s.isTrackingEndpoint());

  }

  public void testService() throws Exception {
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage("ABC");
    MockMessageProducer prod = new MockMessageProducer();
    Service s = create(prod);
    execute(s, msg);
    assertEquals(ClosedState.getInstance(), prod.retrieveComponentState());
    assertEquals(1, prod.getMessages().size());
    assertEquals("ABC", prod.getMessages().get(0).getContent());
  }

  public void testServiceThatFailsToStart() throws Exception {
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage("ABC");
    StatelessServiceWrapper service = new StatelessServiceWrapper(new NullService() {
      @Override
      public void start() throws CoreException {
        throw new CoreException("testServiceThatFailsToStart");
      }
    });
    try {
      execute(service, msg);
      fail();
    }
    catch (ServiceException e) {
      assertNotNull(e.getCause());
      assertEquals(CoreException.class, e.getCause().getClass());
      assertEquals("testServiceThatFailsToStart", e.getCause().getMessage());
    }
  }

  public void testWrappedServiceCreateName() throws Exception {
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage("");
    MockMessageProducer prod = new MockMessageProducer();
    Service s = create(prod);
    assertNotSame(StatelessServiceWrapper.class.getName(), s.createName());
    assertEquals(MockMessageProducer.class.getName(), s.createName());
    StatelessServiceWrapper sw = new StatelessServiceWrapper();
    assertEquals(StatelessServiceWrapper.class.getName(), sw.createName());
  }

  public void testWrappedServiceDefaults() throws Exception {
    StatelessServiceWrapper sw = new StatelessServiceWrapper();
    assertEquals(StatelessServiceWrapper.class.getName(), sw.createName());
    assertEquals(false, sw.isTrackingEndpoint());
    assertEquals(false, sw.continueOnFailure());
    assertEquals(false, sw.isBranching());
  }

  public void testTryInitNoWrappedService() throws Exception {
    StatelessServiceWrapper sw = new StatelessServiceWrapper();
    try {
      LifecycleHelper.init(sw);
      fail("Initialised w/o a wrapped service");
    }
    catch (CoreException e) {
      // expected
    }
  }

  @Override
  protected Object retrieveObjectForSampleConfig() {
    return new StatelessServiceWrapper(new StandaloneProducer());
  }

  private StatelessServiceWrapper create(AdaptrisMessageProducer producer) {
    return new StatelessServiceWrapper(new StandaloneProducer(producer));
  }
}
