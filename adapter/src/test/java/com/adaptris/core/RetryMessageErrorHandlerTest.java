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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import com.adaptris.core.fs.FsProducer;
import com.adaptris.core.services.exception.ConfiguredException;
import com.adaptris.core.services.exception.ThrowExceptionService;
import com.adaptris.core.stubs.ChannelRestartConnectionErrorHandler;
import com.adaptris.core.stubs.EventHandlerAwareService;
import com.adaptris.core.stubs.FailFirstMockMessageProducer;
import com.adaptris.core.stubs.MockChannel;
import com.adaptris.core.stubs.MockConnection;
import com.adaptris.core.stubs.MockMessageConsumer;
import com.adaptris.core.stubs.MockMessageProducer;
import com.adaptris.core.util.LifecycleHelper;
import com.adaptris.util.SimpleIdGenerator;
import com.adaptris.util.TimeInterval;

public class RetryMessageErrorHandlerTest extends ExampleErrorHandlerCase {

  private static final TimeInterval DEFAULT_RETRY_INTERVAL = new TimeInterval(100L, TimeUnit.MILLISECONDS);

  public RetryMessageErrorHandlerTest(java.lang.String testName) {
    super(testName);
  }

  @Override
  protected void setUp() throws Exception {
  }

  private RetryMessageErrorHandler createMessageErrorHandler(AdaptrisMessageProducer p) throws Exception {
    RetryMessageErrorHandler meh = new RetryMessageErrorHandler();
    meh.setProcessingExceptionService(new ServiceList(new ArrayList<Service>(Arrays.asList(new Service[]
    {
      new StandaloneProducer(p)
    }))));
    return meh;
  }

  private StandardWorkflow createWorkflow(AdaptrisMessageProducer producer, Service[] services) throws Exception {
    StandardWorkflow workflow = new StandardWorkflow();
    workflow.setConsumer(new MockMessageConsumer());
    workflow.setProducer(producer);
    workflow.getServiceCollection().addAll(Arrays.asList(services));
    return workflow;
  }

  private StandardWorkflow createWorkflow(AdaptrisMessageProducer producer) throws Exception {
    return createWorkflow(producer, new Service[]
    {
      new FailingService()
    });
  }

  private Channel createChannel(Workflow workflow, ProcessingExceptionHandler handler) throws Exception {
    Channel result = new MockChannel();
    if (handler != null) {
      result.setMessageErrorHandler(handler);
    }
    result.getWorkflowList().add(workflow);
    return result;
  }

  @Override
  protected void tearDown() throws Exception {
  }

  public void testAdapter_HasEventHandler() throws Exception {
    EventHandlerAwareService srv = new EventHandlerAwareService();
    RetryMessageErrorHandler meh = new RetryMessageErrorHandler(srv);
    DefaultEventHandler eventHandler = new DefaultEventHandler();
    Adapter a = AdapterTest.createAdapter(getName(), eventHandler);
    a.setMessageErrorHandler(meh);
    try {
      LifecycleHelper.init(a);
      assertEquals(eventHandler, srv.retrieveEventHandler());
    }
    finally {
      LifecycleHelper.close(a);
    }
  }

  public void testChannel_HasEventHandler() throws Exception {
    EventHandlerAwareService srv = new EventHandlerAwareService();
    RetryMessageErrorHandler meh = new RetryMessageErrorHandler(srv);
    DefaultEventHandler eventHandler = new DefaultEventHandler();
    Adapter adapter = AdapterTest.createAdapter(getName(), eventHandler);
    Channel c = new Channel();
    c.setMessageErrorHandler(meh);
    adapter.getChannelList().add(c);
    try {
      LifecycleHelper.init(adapter);
      assertEquals(eventHandler, srv.retrieveEventHandler());
    }
    finally {
      LifecycleHelper.close(adapter);
    }
  }

  public void testWorkflow_HasEventHandler() throws Exception {
    EventHandlerAwareService srv = new EventHandlerAwareService();
    RetryMessageErrorHandler meh = new RetryMessageErrorHandler(srv);
    DefaultEventHandler eventHandler = new DefaultEventHandler();
    Adapter a = AdapterTest.createAdapter(getName(), eventHandler);
    Channel c = new Channel();
    StandardWorkflow wf = new StandardWorkflow();
    wf.setMessageErrorHandler(meh);
    c.getWorkflowList().add(wf);
    a.getChannelList().add(c);
    try {
      LifecycleHelper.init(a);
      assertEquals(eventHandler, srv.retrieveEventHandler());
    }
    finally {
      LifecycleHelper.close(a);
    }
  }

  public void testMultipleErrorHandlers_AlwaysHandle_True() throws Exception {
    MockMessageProducer aep = new MockMessageProducer();
    MockMessageProducer wep = new MockMessageProducer();
    MockMessageProducer cep = new MockMessageProducer();
    RetryMessageErrorHandler aeh = new RetryMessageErrorHandler(1, DEFAULT_RETRY_INTERVAL, new StandaloneProducer(aep));
    aeh.setAlwaysHandleException(true);
    RetryMessageErrorHandler ceh = new RetryMessageErrorHandler(1, DEFAULT_RETRY_INTERVAL, new StandaloneProducer(cep));
    ceh.setAlwaysHandleException(true);
    RetryMessageErrorHandler weh = new RetryMessageErrorHandler(1, DEFAULT_RETRY_INTERVAL, new StandaloneProducer(wep));
    Adapter adapter = AdapterTest.createAdapter(getName());
    adapter.setMessageErrorHandler(aeh);
    MockMessageConsumer consumer = new MockMessageConsumer();
    adapter.getChannelList().clear();
    adapter.getChannelList().add(createChannel(consumer, ceh, weh, new ThrowExceptionService(new ConfiguredException(getName()))));
    try {
      start(adapter);
      consumer.submitMessage(AdaptrisMessageFactory.getDefaultInstance().newMessage());
      waitForMessages(wep, 1, 1000);
      waitForMessages(cep, 1, 1000);
      waitForMessages(aep, 1, 1000);
      assertEquals(1, aep.messageCount());
      assertEquals(1, cep.messageCount());
      assertEquals(1, wep.messageCount());
    }
    finally {
      stop(adapter);
    }
  }

  public void testMultipleErrorHandlers_AlwaysHandle_False() throws Exception {
    MockMessageProducer aep = new MockMessageProducer();
    MockMessageProducer wep = new MockMessageProducer();
    MockMessageProducer cep = new MockMessageProducer();
    RetryMessageErrorHandler aeh = new RetryMessageErrorHandler(1, DEFAULT_RETRY_INTERVAL, new StandaloneProducer(aep));
    aeh.setAlwaysHandleException(false);
    RetryMessageErrorHandler weh = new RetryMessageErrorHandler(1, DEFAULT_RETRY_INTERVAL, new StandaloneProducer(wep));
    RetryMessageErrorHandler ceh = new RetryMessageErrorHandler(1, DEFAULT_RETRY_INTERVAL, new StandaloneProducer(cep));
    Adapter adapter = AdapterTest.createAdapter(getName());
    adapter.setMessageErrorHandler(aeh);
    MockMessageConsumer consumer = new MockMessageConsumer();
    adapter.getChannelList().clear();
    adapter.getChannelList().add(createChannel(consumer, ceh, weh, new ThrowExceptionService(new ConfiguredException(getName()))));
    try {
      start(adapter);
      consumer.submitMessage(AdaptrisMessageFactory.getDefaultInstance().newMessage());
      waitForMessages(wep, 1, 1000);
      assertEquals(0, aep.messageCount());
      assertEquals(0, cep.messageCount());
      assertEquals(1, wep.messageCount());
    }
    finally {
      stop(adapter);
    }
  }

  public void testSetLockTimeout() throws Exception {
    RetryMessageErrorHandler meh = new RetryMessageErrorHandler();
    TimeInterval defaultInterval = new TimeInterval(1L, TimeUnit.SECONDS);
    TimeInterval interval = new TimeInterval(20L, TimeUnit.SECONDS);
    assertNull(meh.getLockTimeout());
    assertEquals(defaultInterval.toMilliseconds(), meh.lockTimeoutMs());

    meh.setLockTimeout(interval);
    assertEquals(interval, meh.getLockTimeout());
    assertEquals(interval.toMilliseconds(), meh.lockTimeoutMs());

    meh.setLockTimeout(null);
    assertNull(meh.getLockTimeout());
    assertEquals(defaultInterval.toMilliseconds(), meh.lockTimeoutMs());
  }

  public void testSetRetryInterval() throws Exception {
    RetryMessageErrorHandler meh = new RetryMessageErrorHandler();
    TimeInterval defaultInterval = new TimeInterval(10L, TimeUnit.MINUTES);
    TimeInterval interval = new TimeInterval(20L, TimeUnit.SECONDS);
    assertNull(meh.getRetryInterval());
    assertEquals(defaultInterval.toMilliseconds(), meh.retryIntervalMs());

    meh.setRetryInterval(interval);
    assertEquals(interval, meh.getRetryInterval());
    assertEquals(interval.toMilliseconds(), meh.retryIntervalMs());

    meh.setRetryInterval(null);
    assertNull(meh.getRetryInterval());
    assertEquals(defaultInterval.toMilliseconds(), meh.retryIntervalMs());
  }

  public void testRetryLimit2() throws Exception {
    String name = renameThread("testRetryLimit2");
    try {
      MockMessageProducer failProducer = new MockMessageProducer();
      RetryMessageErrorHandler meh = createMessageErrorHandler(failProducer);
      meh.setRetryInterval(DEFAULT_RETRY_INTERVAL);
      meh.setRetryLimit(2);
      MockMessageProducer workflowProducer = new MockMessageProducer();
      Workflow workflow = createWorkflow(workflowProducer);
      Channel channel = createChannel(workflow, meh);
      channel.prepare();
      channel.requestStart();
      workflow.onAdaptrisMessage(AdaptrisMessageFactory.getDefaultInstance().newMessage("XXXX"));
      waitForMessages(failProducer, 1);
      assertEquals("Ensure produced to fail producer", 1, failProducer.getMessages().size());
      channel.requestClose();
    }
    finally {
      renameThread(name);
    }
  }

  public void testRetryForeverWithStop() throws Exception {
    String name = renameThread("testRetryForeverWithStop");
    try {
      MockMessageProducer failProducer = new MockMessageProducer();
      RetryMessageErrorHandler meh = createMessageErrorHandler(failProducer);
      meh.setRetryInterval(DEFAULT_RETRY_INTERVAL);
      meh.setRetryLimit(2);
      MockMessageProducer workflowProducer = new MockMessageProducer();
      Workflow workflow = createWorkflow(workflowProducer);
      Channel channel = createChannel(workflow, meh);
      channel.prepare();
      channel.requestStart();
      workflow.onAdaptrisMessage(AdaptrisMessageFactory.getDefaultInstance().newMessage("XXXX"));
      channel.requestClose();
      assertEquals("Ensure stop forces fail producer", 1, failProducer.getMessages().size());
    }
    finally {
      renameThread(name);
    }
  }

  public void testRetryWithServiceList() throws Exception {
    String name = renameThread("testRetryWithServiceList");
    try {
      MockMessageProducer failProducer = new MockMessageProducer();
      RetryMessageErrorHandler meh = createMessageErrorHandler(failProducer);
      meh.setRetryInterval(DEFAULT_RETRY_INTERVAL);
      meh.setRetryLimit(2);
      MockMessageProducer workflowProducer = new MockMessageProducer();
      Workflow workflow = createWorkflow(workflowProducer);
      Channel channel = createChannel(workflow, meh);
      meh.setRetryInterval(DEFAULT_RETRY_INTERVAL);
      meh.setProcessingExceptionService(new ServiceList(new Service[]
      {
          new StandaloneProducer(failProducer), new StandaloneProducer(failProducer), new StandaloneProducer(failProducer)
      }));
      channel.prepare();
      channel.requestStart();
      workflow.onAdaptrisMessage(AdaptrisMessageFactory.getDefaultInstance().newMessage("XXXX"));
      waitForMessages(failProducer, 3);
      channel.requestClose();
      assertTrue("Ensure 3 msgs produced to fail producer", failProducer.getMessages().size() == 3);
    }
    finally {
      renameThread(name);
    }
  }

  public void testBug817() throws Exception {
    String name = renameThread("testBug817");
    try {
      MockMessageProducer failProducer = new MockMessageProducer();
      RetryMessageErrorHandler meh = createMessageErrorHandler(failProducer);
      meh.setRetryInterval(DEFAULT_RETRY_INTERVAL);
      meh.setRetryLimit(1);

      MockMessageProducer workflowProducer = new MockMessageProducer();
      FailingService service = new FailingService();
      Workflow workflow = createWorkflow(workflowProducer, new Service[]
      {
        service
      });
      Channel channel = createChannel(workflow, meh);
      channel.prepare();
      channel.requestStart();
      AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage("XXXX");
      workflow.onAdaptrisMessage(msg);
      waitForMessages(failProducer, 1);
      channel.requestClose();
      assertEquals("Ensure produced to fail producer", 1, failProducer.getMessages().size());
      assertEquals("Service should have been called twice", 2, service.callCount());
    }
    finally {
      renameThread(name);
    }
  }

  public void testRetryWithSuccess() throws Exception {
    String name = renameThread("testRetryWithSuccess");
    try {
      MockMessageProducer failProducer = new MockMessageProducer();
      RetryMessageErrorHandler meh = createMessageErrorHandler(failProducer);
      meh.setRetryInterval(DEFAULT_RETRY_INTERVAL);
      meh.setRetryLimit(2);
      FailFirstMockMessageProducer workflowProducer = new FailFirstMockMessageProducer(1);
      StandardWorkflow workflow = createWorkflow(workflowProducer);
      Channel channel = createChannel(workflow, meh);
      workflow.setServiceCollection(new ServiceList());
      channel.prepare();
      channel.requestStart();
      AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage("XXXX");
      workflow.onAdaptrisMessage(msg);
      waitForMessages(workflowProducer, 1);
      channel.requestClose();
      assertEquals("Ensure producer success", 1, workflowProducer.getMessages().size());
    }
    finally {
      renameThread(name);
    }
  }

  public void testBug854() throws Exception {
    String name = renameThread("testBug854");
    try {
      MockMessageProducer failProducer = new MockMessageProducer();
      RetryMessageErrorHandler meh = createMessageErrorHandler(failProducer);
      meh.setRetryInterval(DEFAULT_RETRY_INTERVAL);
      meh.setRetryLimit(0);

      FailFirstMockMessageProducer workflowProducer = new FailFirstMockMessageProducer(1);
      StandardWorkflow workflow = createWorkflow(workflowProducer);
      workflow.setServiceCollection(new ServiceList());
      workflow.setProduceExceptionHandler(new RestartProduceExceptionHandler());

      Channel channel = new Channel();
      channel.getWorkflowList().add(workflow);
      channel.setProduceConnection(new MockConnection());
      channel.getProduceConnection().setConnectionErrorHandler(new ChannelRestartConnectionErrorHandler());

      Adapter adapter = new Adapter();
      adapter.setUniqueId(new SimpleIdGenerator().create(this));
      adapter.setMessageErrorHandler(meh);
      adapter.getChannelList().addChannel(channel);
      // adapter.prepare();
      adapter.requestStart();
      workflow.onAdaptrisMessage(AdaptrisMessageFactory.getDefaultInstance().newMessage("XXXX"));
      waitForMessages(workflowProducer, 1);
      adapter.requestClose();
      assertEquals("Ensure producer success", 1, workflowProducer.getMessages().size());
      assertEquals("Should not have produced to failProducer", 0, failProducer.getMessages().size());
    }
    finally {
      renameThread(name);
    }
  }

  @Override
  protected RetryMessageErrorHandler createForExamples() {
    RetryMessageErrorHandler meh = new RetryMessageErrorHandler();
    FsProducer producer = new FsProducer(new ConfiguredProduceDestination("/path/to/bad-directory"));
    producer.setEncoder(new MimeEncoder(false, null, null));
    meh.setProcessingExceptionService(new ServiceList(new ArrayList(Arrays.asList(new Service[]
    {
      new StandaloneProducer(producer)
    }))));
    return meh;
  }

  @Override
  protected String createBaseFileName(Object object) {
    return RetryMessageErrorHandler.class.getCanonicalName();
  }

  private class FailingService extends ServiceImp {

    private int callCount;

    public FailingService() {
      callCount = 0;
    }

    @Override
    public void doService(AdaptrisMessage msg) throws ServiceException {
      callCount++;
      throw new ServiceException("Failing Service configured");
    }

    @Override
    protected void initService() throws CoreException {
    }

    @Override
    protected void closeService() {
    }

    int callCount() {
      return callCount;
    }

    @Override
    public void prepare() throws CoreException {
    }


  }

}
