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

import static com.adaptris.core.WorkflowWithObjectPool.DEFAULT_MAX_IDLE;
import static com.adaptris.core.WorkflowWithObjectPool.DEFAULT_MAX_POOLSIZE;
import static com.adaptris.core.WorkflowWithObjectPool.DEFAULT_MIN_IDLE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.jupiter.api.Test;

import com.adaptris.core.services.WaitService;
import com.adaptris.core.services.exception.ConfiguredException;
import com.adaptris.core.services.exception.ThrowExceptionService;
import com.adaptris.core.stubs.MockChannel;
import com.adaptris.core.stubs.MockMessageConsumer;
import com.adaptris.core.stubs.MockMessageProducer;
import com.adaptris.core.stubs.MockSkipProducerService;
import com.adaptris.core.stubs.MockWorkflowInterceptor;
import com.adaptris.core.stubs.StaticMockMessageProducer;
import com.adaptris.core.stubs.XmlRoundTripService;
import com.adaptris.core.util.Args;
import com.adaptris.core.util.LifecycleHelper;
import com.adaptris.util.TimeInterval;

public class PoolingWorkflowTest
    extends com.adaptris.interlok.junit.scaffolding.ExampleWorkflowCase {
  /**
   *
   */
  private static final String COUNT = "Count";



  @Test
  public void testSingleMessage() throws Exception {
    MockChannel channel = createChannel();
    PoolingWorkflow wf = (PoolingWorkflow) channel.getWorkflowList().get(0);
    MockMessageProducer prod = (MockMessageProducer) wf.getProducer();
    try {
      start(channel);
      submitMessages(wf, 1);
      waitForMessages(prod, 1);
      assertMessages(prod, 1);
    }
    finally {
      stop(channel);
    }
  }

  @Test
  public void testOnMessage_SkipProducer() throws Exception {
    StaticMockMessageProducer serviceProducer = new StaticMockMessageProducer();
    serviceProducer.getMessages().clear();
    MockChannel channel = createAndPrepareChannel(Arrays.asList(new Service[]
    {
        createService(), new StandaloneProducer(serviceProducer), new MockSkipProducerService()
    }));
    try {
      PoolingWorkflow workflow = (PoolingWorkflow) channel.getWorkflowList().get(0);
      MockMessageProducer workflowProducer = (MockMessageProducer) workflow.getProducer();
      start(channel);
      submitMessages(workflow, 10);
      waitForMessages(serviceProducer, 10);
      // assertEquals(10, serviceProducer.messageCount());
      assertEquals(0, workflowProducer.messageCount());
    }
    finally {
      stop(channel);
    }
  }

  @Test
  public void testWorkflowWithInterceptor() throws Exception {
    MockChannel channel = createChannel();
    PoolingWorkflow wf = (PoolingWorkflow) channel.getWorkflowList().get(0);
    MockMessageProducer prod = (MockMessageProducer) wf.getProducer();
    MockWorkflowInterceptor interceptor = new MockWorkflowInterceptor();
    wf.addInterceptor(interceptor);
    try {
      start(channel);
      submitMessages(wf, 1);
      waitForMessages(prod, 1);
      assertMessages(prod, 1);
      assertEquals(1, interceptor.messageCount());
    }
    finally {
      stop(channel);
    }
  }

  @Test
  public void testSetPoolSize() throws Exception {
    PoolingWorkflow workflow = new PoolingWorkflow();
    workflow.setPoolSize(20);
    assertEquals(20, workflow.poolSize());
    assertEquals(20, workflow.getPoolSize().intValue());
    workflow.setPoolSize(null);
    assertNull(workflow.getPoolSize());
    assertEquals(DEFAULT_MAX_POOLSIZE, workflow.poolSize());

  }

  @Test
  public void testSetShutdownWaitTime() throws Exception {
    PoolingWorkflow workflow = new PoolingWorkflow();
    TimeInterval defaultInterval = new TimeInterval(60L, TimeUnit.SECONDS.name());
    assertNull(workflow.getShutdownWaitTime());
    assertEquals(defaultInterval.toMilliseconds(), workflow.shutdownWaitTimeMs());

    TimeInterval interval = new TimeInterval(200L, TimeUnit.MILLISECONDS.name());
    workflow.setShutdownWaitTime(interval);
    assertEquals(interval, workflow.getShutdownWaitTime());
    assertNotSame(defaultInterval.toMilliseconds(), workflow.shutdownWaitTimeMs());
    assertEquals(interval.toMilliseconds(), workflow.shutdownWaitTimeMs());

    workflow.setShutdownWaitTime(null);
    assertNull(workflow.getShutdownWaitTime());
    assertEquals(defaultInterval.toMilliseconds(), workflow.shutdownWaitTimeMs());

  }

  @Test
  public void testSetThreadLifetime() throws Exception {
    PoolingWorkflow workflow = new PoolingWorkflow();
    TimeInterval defaultInterval = new TimeInterval(60L, TimeUnit.SECONDS.name());
    assertNull(workflow.getThreadKeepAlive());
    assertEquals(defaultInterval.toMilliseconds(), workflow.threadLifetimeMs());

    TimeInterval interval = new TimeInterval(200L, TimeUnit.MILLISECONDS.name());
    workflow.setThreadKeepAlive(interval);
    assertEquals(interval, workflow.getThreadKeepAlive());
    assertNotSame(defaultInterval.toMilliseconds(), workflow.threadLifetimeMs());
    assertEquals(interval.toMilliseconds(), workflow.threadLifetimeMs());

    workflow.setThreadKeepAlive(null);

    assertNull(workflow.getThreadKeepAlive());
    assertEquals(defaultInterval.toMilliseconds(), workflow.threadLifetimeMs());

  }

  @Test
  public void testInitWaitTime() throws Exception {
    PoolingWorkflow workflow = new PoolingWorkflow();
    TimeInterval defaultInterval = new TimeInterval(60L, TimeUnit.SECONDS.name());
    assertNull(workflow.getInitWaitTime());
    assertEquals(defaultInterval.toMilliseconds(), workflow.initWaitTimeMs());

    TimeInterval interval = new TimeInterval(200L, TimeUnit.MILLISECONDS.name());
    workflow.setInitWaitTime(interval);
    assertEquals(interval, workflow.getInitWaitTime());
    assertNotSame(defaultInterval.toMilliseconds(), workflow.initWaitTimeMs());
    assertEquals(interval.toMilliseconds(), workflow.initWaitTimeMs());

    workflow.setThreadKeepAlive(null);

    assertNull(workflow.getThreadKeepAlive());
    assertEquals(defaultInterval.toMilliseconds(), workflow.threadLifetimeMs());

  }

  @Test
  public void testSetMaxIdle() throws Exception {
    PoolingWorkflow workflow = new PoolingWorkflow();
    workflow.setPoolSize(20);
    workflow.setMaxIdle(20);
    assertEquals(20, workflow.getMaxIdle().intValue());
    assertEquals(20, workflow.maxIdle());
    workflow.setMaxIdle(null);
    assertNull(workflow.getMaxIdle());
    assertEquals(DEFAULT_MAX_IDLE, workflow.maxIdle());
  }

  @Test
  public void testSetMinIdle() throws Exception {
    PoolingWorkflow workflow = new PoolingWorkflow();
    workflow.setPoolSize(20);
    workflow.setMinIdle(20);
    assertEquals(20, workflow.getMinIdle().intValue());
    assertEquals(20, workflow.minIdle());
    workflow.setMinIdle(null);
    assertNull(workflow.getMinIdle());
    assertEquals(DEFAULT_MIN_IDLE, workflow.minIdle());
  }

  @Test
  public void testSetThreadPriority() throws Exception {
    PoolingWorkflow workflow = new PoolingWorkflow();
    assertNull(workflow.getThreadPriority());
    assertEquals(Thread.NORM_PRIORITY, workflow.threadPriority());
    workflow.setThreadPriority(Thread.NORM_PRIORITY);
    assertNotNull(workflow.getThreadPriority());
    assertEquals(Thread.NORM_PRIORITY, workflow.threadPriority());
    assertEquals(Thread.NORM_PRIORITY, workflow.getThreadPriority().intValue());

    workflow.setThreadPriority(null);
    assertEquals(Thread.NORM_PRIORITY, workflow.threadPriority());
    assertNull(workflow.getThreadPriority());

    workflow.setThreadPriority(99);
    assertNotNull(workflow.getThreadPriority());
    // It might be set, but it won't be 99 when we get it.
    assertEquals(Thread.NORM_PRIORITY, workflow.threadPriority());
    workflow.setThreadPriority(-1);
    assertNotNull(workflow.getThreadPriority());
    assertEquals(Thread.NORM_PRIORITY, workflow.threadPriority());
  }

  @Test
  public void testLessThanPoolSize() throws Exception {
    MockChannel channel = createChannel();
    PoolingWorkflow wf = (PoolingWorkflow) channel.getWorkflowList().get(0);
    int count = wf.poolSize() - 1;
    MockMessageProducer prod = (MockMessageProducer) wf.getProducer();
    try {
      start(channel);
      submitMessages(wf, count);
      waitForMessages(prod, count);
      assertTrue(wf.currentObjectPoolCount() > 1);
      assertTrue(wf.currentThreadPoolCount() > 1);
      assertTrue(wf.currentlyIdleObjects() >= 0);
      assertTrue(wf.currentlyActiveObjects() >= 0);
      assertMessages(prod, count);
    }
    finally {
      stop(channel);
    }
  }

  @Test
  public void testMaxIdle_CannotExceed_Poolsize() throws Exception {
    MockChannel channel = createChannel();
    PoolingWorkflow wf = (PoolingWorkflow) channel.getWorkflowList().get(0);
    wf.setMaxIdle(100);
    wf.setThreadKeepAlive(new TimeInterval(100L, TimeUnit.MILLISECONDS));
    LifecycleHelper.init(channel);
    assertEquals(10, wf.poolSize());
    assertEquals(10, wf.maxIdle());
    assertEquals(DEFAULT_MIN_IDLE, wf.minIdle());
    LifecycleHelper.close(channel);
  }

  @Test
  public void testMinIdle_Changes_MaxIdle() throws Exception {
    MockChannel channel = createChannel();
    PoolingWorkflow wf = (PoolingWorkflow) channel.getWorkflowList().get(0);
    wf.setPoolSize(1000);
    wf.setMaxIdle(10);
    wf.setMinIdle(100);
    wf.setThreadKeepAlive(new TimeInterval(100L, TimeUnit.MILLISECONDS));
    LifecycleHelper.init(channel);
    assertEquals(100, wf.maxIdle());
    LifecycleHelper.close(channel);
  }

  @Test
  public void testMinIdle_Cannot_Poolsize() throws Exception {
    MockChannel channel = createChannel();
    PoolingWorkflow wf = (PoolingWorkflow) channel.getWorkflowList().get(0);
    wf.setMinIdle(100);
    wf.setThreadKeepAlive(new TimeInterval(100L, TimeUnit.MILLISECONDS));
    LifecycleHelper.init(channel);
    assertEquals(10, wf.poolSize());
    assertEquals(10, wf.maxIdle());
    assertEquals(10, wf.minIdle());
    LifecycleHelper.close(channel);
  }

  @Test
  public void testFixedPoolsizeAfterProcessing() throws Exception {
    MockChannel channel = createChannel();
    PoolingWorkflow wf = (PoolingWorkflow) channel.getWorkflowList().get(0);
    wf.setMaxIdle(DEFAULT_MAX_POOLSIZE);
    wf.setMinIdle(DEFAULT_MAX_POOLSIZE);
    wf.setThreadKeepAlive(new TimeInterval(100L, TimeUnit.MILLISECONDS));
    int count = wf.poolSize() * 2;
    MockMessageProducer prod = (MockMessageProducer) wf.getProducer();
    try {
      start(channel);
      submitMessages(wf, count);
      waitForMessages(prod, count);
      Thread.sleep(200);
      assertTrue(wf.currentObjectPoolCount() >= 1);
      assertTrue(wf.currentlyIdleObjects() >= 1);
      assertMessages(prod, count);
    }
    finally {
      stop(channel);
    }
  }

  @Test
  public void testFixedPoolsizeOnStart() throws Exception {
    MockChannel channel = createChannel();
    PoolingWorkflow wf = (PoolingWorkflow) channel.getWorkflowList().get(0);
    wf.setMaxIdle(DEFAULT_MAX_POOLSIZE);
    wf.setMinIdle(DEFAULT_MAX_POOLSIZE);
    wf.setThreadKeepAlive(new TimeInterval(100L, TimeUnit.MILLISECONDS));
    try {
      start(channel);
      Thread.sleep(200);
      assertTrue(wf.currentObjectPoolCount() >= 1);
      assertTrue(wf.currentlyIdleObjects() >= 1);
    }
    finally {
      stop(channel);
    }
  }

  @Test
  public void testGreaterThanPoolSize() throws Exception {
    MockChannel channel = createChannel();
    PoolingWorkflow wf = (PoolingWorkflow) channel.getWorkflowList().get(0);
    int count = wf.poolSize() * 2;
    MockMessageProducer prod = (MockMessageProducer) wf.getProducer();
    try {
      start(channel);
      submitMessages(wf, count);
      waitForMessages(prod, count);
      assertTrue(wf.currentObjectPoolCount() > 1);
      assertTrue(wf.currentThreadPoolCount() > 1);
      assertTrue(wf.currentlyIdleObjects() >= 0);
      assertTrue(wf.currentlyActiveObjects() >= 0);
      assertMessages(prod, count);
    }
    finally {
      stop(channel);
    }
  }

  @Test
  public void testHandleServiceException() throws Exception {
    MockChannel channel = createChannel();
    PoolingWorkflow wf = (PoolingWorkflow) channel.getWorkflowList().get(0);
    MockMessageProducer meh = new MockMessageProducer();
    channel.setMessageErrorHandler(new StandardProcessingExceptionHandler(
    		new ServiceList(new ArrayList<Service>(Arrays.asList(new Service[]
    	    	      {
    	    	        new StandaloneProducer(meh)
    	    	      })))));
    wf.getServiceCollection().add(new ThrowExceptionService(new ConfiguredException("Fail")));
    try {
      start(channel);
      submitMessages(wf, 1);
      waitForMessages(meh, 1);
      assertEquals(1, meh.messageCount());
      for (Iterator i = meh.getMessages().iterator(); i.hasNext();) {
        AdaptrisMessage m = (AdaptrisMessage) i.next();
        assertNotNull(m.getObjectHeaders().get(CoreConstants.OBJ_METADATA_EXCEPTION));
        assertNotNull(m.getObjectHeaders().get(CoreConstants.OBJ_METADATA_EXCEPTION_CAUSE));
        assertEquals(ThrowExceptionService.class.getSimpleName(),
            m.getObjectHeaders().get(CoreConstants.OBJ_METADATA_EXCEPTION_CAUSE));
      }
    }
    finally {
      stop(channel);
    }
  }

  @Test
  public void testHandleProduceException() throws Exception {
    MockChannel channel = createChannel();
    PoolingWorkflow wf = (PoolingWorkflow) channel.getWorkflowList().get(0);
    MockMessageProducer meh = new MockMessageProducer();
    MockMessageProducer prod = new MockMessageProducer() {
      @Override
      protected void doProduce(AdaptrisMessage msg, String endpoint) throws ProduceException {
        throw new ProduceException();
      }
    };
    wf.setProducer(prod);
    channel.setMessageErrorHandler(new StandardProcessingExceptionHandler(
    		new ServiceList(new ArrayList<Service>(Arrays.asList(new Service[]
    	    	      {
    	    	        new StandaloneProducer(meh)
    	    	      })))));
    try {
      start(channel);
      submitMessages(wf, 1);
      waitForMessages(meh, 1);
      assertEquals(1, meh.messageCount());
    }
    finally {
      stop(channel);
    }
  }

  @Test
  public void testRedmine1681() throws Exception {
    MockChannel channel = createChannel();
    PoolingWorkflow wf = (PoolingWorkflow) channel.getWorkflowList().get(0);
    MockMessageProducer meh = new MockMessageProducer();
    MockMessageProducer prod = (MockMessageProducer) wf.getProducer();
    wf.getServiceCollection().add(new XmlRoundTripService());
    channel.setMessageErrorHandler(new StandardProcessingExceptionHandler(
    		new ServiceList(new ArrayList<Service>(Arrays.asList(new Service[]
    	    	      {
    	    	        new StandaloneProducer(meh)
    	    	      })))));
    try {
      start(channel);
      submitMessages(wf, 1);
      waitForMessages(prod, 1);
      assertEquals(0, meh.messageCount());
      assertEquals(1, prod.messageCount());
    }
    finally {
      stop(channel);
    }
  }

  @Test
  public void testHandleRuntimeException() throws Exception {
    MockChannel channel = createChannel();
    PoolingWorkflow wf = (PoolingWorkflow) channel.getWorkflowList().get(0);
    MockMessageProducer meh = new MockMessageProducer();
    MockMessageProducer prod = new MockMessageProducer() {
      @Override
      protected void doProduce(AdaptrisMessage msg, String endpoint) throws ProduceException {
        throw new RuntimeException();
      }

    };
    wf.setProducer(prod);
    channel.setMessageErrorHandler(new StandardProcessingExceptionHandler(
    		new ServiceList(new ArrayList<Service>(Arrays.asList(new Service[]
  	    	      {
  	    	        new StandaloneProducer(meh)
  	    	      })))));
    try {
      start(channel);
      submitMessages(wf, 1);
      waitForMessages(meh, 1);
      assertEquals(1, meh.messageCount());
    }
    finally {
      stop(channel);
    }
  }

  @Test
  public void testHandleChannelUnavailable() throws Exception {
    final MockChannel channel = createChannel();
    PoolingWorkflow wf = (PoolingWorkflow) channel.getWorkflowList().get(0);
    MockMessageProducer prod = (MockMessageProducer) wf.getProducer();
    try {
      wf.setChannelUnavailableWaitInterval(new TimeInterval(1200L, TimeUnit.MILLISECONDS));

      start(channel);
      AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage("");
      Timer t = new Timer(true);
      channel.toggleAvailability(false);
      t.schedule(new TimerTask() {
        @Override
        public void run() {
          channel.toggleAvailability(true);
        }

      }, 500);
      wf.onAdaptrisMessage(msg);
      waitForMessages(prod, 1);
      assertEquals(1, prod.messageCount());
    }
    finally {
      stop(channel);
    }
  }

  @Test
  public void testHandleChannelUnavailableForever() throws Exception {
    MockMessageProducer producer = new MockMessageProducer();
    final MockChannel channel = createChannel();
    PoolingWorkflow workflow = (PoolingWorkflow) channel.getWorkflowList().get(0);
    MockMessageProducer meh = new MockMessageProducer();

    try {
      workflow.setChannelUnavailableWaitInterval(new TimeInterval(100L, TimeUnit.MILLISECONDS));
      workflow.setMessageErrorHandler(new StandardProcessingExceptionHandler(
    		  new ServiceList(new ArrayList<Service>(Arrays.asList(new Service[]
    	    	      {
    	    	        new StandaloneProducer(meh)
    	    	      })))));
      AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(PAYLOAD_1);
      start(channel);
      channel.toggleAvailability(false);
      workflow.onAdaptrisMessage(msg);
      assertEquals(0, producer.getMessages().size());
      assertEquals(1, meh.getMessages().size());
    }
    finally {
      stop(channel);
    }
  }

  @Test
  public void testOnMessage_WithConsumeLocation() throws Exception {
    MockChannel channel = createChannel();
    PoolingWorkflow wf = (PoolingWorkflow) channel.getWorkflowList().get(0);
    MockMessageProducer prod = (MockMessageProducer) wf.getProducer();
    wf.setConsumer(new ConsumerWithLocation(getName()));
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage();
    msg.addMessageHeader(getName(), "hello world");
    try {
      start(channel);
      MockMessageConsumer m = (MockMessageConsumer) wf.getConsumer();
      m.submitMessage(msg);
      waitForMessages(prod, 1);
      AdaptrisMessage consumed = prod.getMessages().get(0);
      assertTrue(consumed.headersContainsKey(CoreConstants.MESSAGE_CONSUME_LOCATION));
      assertEquals("hello world",
          consumed.getMetadataValue(CoreConstants.MESSAGE_CONSUME_LOCATION));
    } finally {
      stop(channel);
    }
  }

  @Test
  public void testOnMessage_WithConsumeLocation_NoMatch() throws Exception {
    MockChannel channel = createChannel();
    PoolingWorkflow wf = (PoolingWorkflow) channel.getWorkflowList().get(0);
    MockMessageProducer prod = (MockMessageProducer) wf.getProducer();
    wf.setConsumer(new ConsumerWithLocation(getName()));
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage();
    try {
      start(channel);
      MockMessageConsumer m = (MockMessageConsumer) wf.getConsumer();
      m.submitMessage(msg);
      waitForMessages(prod, 1);
      AdaptrisMessage consumed = prod.getMessages().get(0);
      assertFalse(consumed.headersContainsKey(CoreConstants.MESSAGE_CONSUME_LOCATION));
    } finally {
      stop(channel);
    }
  }

  @Test
  public void testOnMessage_SuccessCallback() throws Exception {
    AtomicBoolean onSuccess = new AtomicBoolean(false);
    MockChannel channel = createChannel();
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(PAYLOAD_1);
    PoolingWorkflow wf = (PoolingWorkflow) channel.getWorkflowList().get(0);
    MockMessageProducer prod = (MockMessageProducer) wf.getProducer();
    try {
      start(channel);
      wf.onAdaptrisMessage(msg, (m) -> {
        onSuccess.set(true);
      });
      waitForMessages(prod, 1);
      assertTrue(onSuccess.get());
    } finally {
      stop(channel);
    }

  }

  private void submitMessages(PoolingWorkflow wf, int number) throws Exception {
    MockMessageConsumer m = (MockMessageConsumer) wf.getConsumer();
    for (int i = 0; i < number; i++) {
      AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(String.valueOf(i));
      msg.addMetadata(COUNT, String.valueOf(i));
      m.submitMessage(msg);
    }
  }

  private void assertMessages(MockMessageProducer producer, int count) {
    assertEquals(count, producer.getMessages().size());
    List<AdaptrisMessage> list = producer.getMessages();
    Collections.sort(list, new MyMetadataComparator());
    for (int i = 0; i < count; i++) {
      AdaptrisMessage msg = list.get(i);
      assertEquals(i, Integer.valueOf(msg.getMetadataValue(COUNT)).intValue());
    }
  }

  private MockChannel createChannel() throws Exception {
    return createAndPrepareChannel(Arrays.asList(new Service[]
    {
        createService(), createService()
    }));
  }

  private MockChannel createChannel(ProcessingExceptionHandler handler, List<Service> services) throws Exception {
    MockChannel channel = buildChannel(services);
    channel.setMessageErrorHandler(handler);
    return channel;
  }

  private MockChannel buildChannel(List<Service> services) throws Exception {
    MockChannel channel = new MockChannel();
    PoolingWorkflow wf = new PoolingWorkflow();
    MockMessageConsumer consumer = new MockMessageConsumer();
    MockMessageProducer producer = new MockMessageProducer();
    wf.getServiceCollection().addAll(services);
    wf.setConsumer(consumer);
    wf.setProducer(producer);
    channel.getWorkflowList().add(wf);
    return channel;
  }

  private MockChannel createAndPrepareChannel(List<Service> services) throws Exception {
    MockChannel channel = buildChannel(services);
    LifecycleHelper.prepare(channel);
    return channel;
  }

  private Service createService() {
    WaitService waitService = new WaitService(new TimeInterval(20L, TimeUnit.MILLISECONDS));
    return waitService;
  }

  @Override
  protected Object retrieveObjectForSampleConfig() {
    Channel c = new Channel();
    try {
      c.setUniqueId(UUID.randomUUID().toString());
      PoolingWorkflow wf = new PoolingWorkflow();
      wf.setUniqueId(UUID.randomUUID().toString());
      wf.getServiceCollection().add(createService());
      wf.getServiceCollection().add(createService());
      wf.setProducer(new NullMessageProducer());
      wf.setConsumer(new NullMessageConsumer());
      c.getWorkflowList().add(wf);
    }
    catch (Exception e) {
      throw new RuntimeException(e);
    }
    return c;
  }

  @Override
  protected String createBaseFileName(Object object) {
    return PoolingWorkflow.class.getName();
  }

  @Override
  protected PoolingWorkflow createWorkflowForGenericTests() throws CoreException {
    return new PoolingWorkflow();
  }

  public class MyMetadataComparator implements Comparator {

    @Override
    public int compare(Object arg1, Object arg2) {
      Integer m1count = Integer.valueOf(((AdaptrisMessage) arg1).getMetadataValue(COUNT));
      Integer m2count = Integer.valueOf(((AdaptrisMessage) arg2).getMetadataValue(COUNT));
      return m1count.compareTo(m2count);
    }
  }

  private class ConsumerWithLocation extends MockMessageConsumer {
    private String metadataKey;

    public ConsumerWithLocation(String key) {
      metadataKey = Args.notBlank(key, "metadataKey");
    }

    @Override
    public String consumeLocationKey() {
      return metadataKey;
    }
  }
}
