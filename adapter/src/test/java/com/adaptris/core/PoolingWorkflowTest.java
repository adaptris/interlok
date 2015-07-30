package com.adaptris.core;

import static com.adaptris.core.PoolingWorkflow.DEFAULT_MAX_IDLE;
import static com.adaptris.core.PoolingWorkflow.DEFAULT_MAX_POOLSIZE;
import static com.adaptris.core.PoolingWorkflow.DEFAULT_MIN_IDLE;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

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
import com.adaptris.core.util.LifecycleHelper;
import com.adaptris.util.TimeInterval;

public class PoolingWorkflowTest extends ExampleWorkflowCase {
  /**
   *
   */
  private static final String COUNT = "Count";

  public PoolingWorkflowTest(java.lang.String testName) {
    super(testName);
  }

  @Override
  protected void setUp() throws Exception {

  }

  @Override
  protected void tearDown() throws Exception {
  }

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

  public void testOnMessage_SkipProducer() throws Exception {
    StaticMockMessageProducer serviceProducer = new StaticMockMessageProducer();
    serviceProducer.getMessages().clear();
    MockChannel channel = createChannel(Arrays.asList(new Service[]
    {
        createService(), new StandaloneProducer(serviceProducer), new MockSkipProducerService()
    }));
    try {
      PoolingWorkflow workflow = (PoolingWorkflow) channel.getWorkflowList().get(0);
      MockMessageProducer workflowProducer = (MockMessageProducer) workflow.getProducer();
      start(channel);
      submitMessages(workflow, 10);
      waitForMessages(serviceProducer, 10);
      assertEquals(10, serviceProducer.messageCount());
      assertEquals(0, workflowProducer.messageCount());
    }
    finally {
      stop(channel);
    }
  }

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

  public void testSetPoolSize() throws Exception {
    PoolingWorkflow workflow = new PoolingWorkflow();
    workflow.setPoolSize(20);
    assertEquals(20, workflow.poolSize());
    assertEquals(20, workflow.getPoolSize().intValue());
    workflow.setPoolSize(null);
    assertNull(workflow.getPoolSize());
    assertEquals(DEFAULT_MAX_POOLSIZE, workflow.poolSize());

  }

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

  public void testSetThreadPriority() throws Exception {
    PoolingWorkflow workflow = new PoolingWorkflow();
    assertNull(workflow.getThreadPriority());
    assertEquals(Thread.NORM_PRIORITY, workflow.threadPriority());
    workflow.setThreadPriority(Thread.NORM_PRIORITY);
    assertNotNull(workflow.getThreadPriority());
    assertEquals(Thread.NORM_PRIORITY, workflow.threadPriority());
    assertEquals(Thread.NORM_PRIORITY, workflow.getThreadPriority().intValue());

    try {
      workflow.setThreadPriority(null);
      fail();
    }
    catch (IllegalArgumentException expected) {

    }
    assertNotNull(workflow.getThreadPriority());
    assertEquals(Thread.NORM_PRIORITY, workflow.getThreadPriority().intValue());

    try {
      workflow.setThreadPriority(99);
      fail();
    }
    catch (IllegalArgumentException expected) {

    }
    assertNotNull(workflow.getThreadPriority());
    assertEquals(Thread.NORM_PRIORITY, workflow.getThreadPriority().intValue());
    assertEquals(Thread.NORM_PRIORITY, workflow.threadPriority());
    try {
      workflow.setThreadPriority(-1);
      fail();
    }
    catch (IllegalArgumentException expected) {

    }
    assertNotNull(workflow.getThreadPriority());
    assertEquals(Thread.NORM_PRIORITY, workflow.getThreadPriority().intValue());
    assertEquals(Thread.NORM_PRIORITY, workflow.threadPriority());
  }

  public void testLessThanPoolSize() throws Exception {
    MockChannel channel = createChannel();
    PoolingWorkflow wf = (PoolingWorkflow) channel.getWorkflowList().get(0);
    int count = wf.poolSize() - 1;
    MockMessageProducer prod = (MockMessageProducer) wf.getProducer();
    try {
      start(channel);
      submitMessages(wf, count);
      waitForMessages(prod, count);
      assertTrue("ObjectPool > 1", wf.currentObjectPoolCount() > 1);
      assertTrue("ThreadPool > 1", wf.currentThreadPoolCount() > 1);
      assertTrue("ObjectPool >=0", wf.currentlyIdleObjects() >= 0);
      assertTrue("ThreadPool >=0", wf.currentlyActiveObjects() >= 0);
      assertMessages(prod, count);
    }
    finally {
      stop(channel);
    }
  }

  public void testMaxIdle_Changes_Poolsize() throws Exception {
    MockChannel channel = createChannel();
    PoolingWorkflow wf = (PoolingWorkflow) channel.getWorkflowList().get(0);
    wf.setMaxIdle(100);
    wf.setThreadKeepAlive(new TimeInterval(100L, TimeUnit.MILLISECONDS));
    LifecycleHelper.init(channel);
    assertEquals(100, wf.poolSize());
    assertEquals(DEFAULT_MIN_IDLE, wf.minIdle());
    LifecycleHelper.close(channel);
  }

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

  public void testMinIdle_Changes_Poolsize() throws Exception {
    MockChannel channel = createChannel();
    PoolingWorkflow wf = (PoolingWorkflow) channel.getWorkflowList().get(0);
    wf.setMinIdle(100);
    wf.setThreadKeepAlive(new TimeInterval(100L, TimeUnit.MILLISECONDS));
    LifecycleHelper.init(channel);
    assertEquals(100, wf.poolSize());
    assertEquals(100, wf.maxIdle());
    LifecycleHelper.close(channel);
  }

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
      assertTrue("ObjectPool == 10", wf.currentObjectPoolCount() == DEFAULT_MAX_POOLSIZE);
      assertTrue("ObjectPool idle == 10", wf.currentlyIdleObjects() == DEFAULT_MAX_POOLSIZE);
      assertMessages(prod, count);
    }
    finally {
      stop(channel);
    }
  }

  public void testFixedPoolsizeOnStart() throws Exception {
    MockChannel channel = createChannel();
    PoolingWorkflow wf = (PoolingWorkflow) channel.getWorkflowList().get(0);
    wf.setMaxIdle(DEFAULT_MAX_POOLSIZE);
    wf.setMinIdle(DEFAULT_MAX_POOLSIZE);
    wf.setThreadKeepAlive(new TimeInterval(100L, TimeUnit.MILLISECONDS));
    try {
      start(channel);
      Thread.sleep(200);
      assertEquals(DEFAULT_MAX_POOLSIZE, wf.currentObjectPoolCount());
      assertEquals(DEFAULT_MAX_POOLSIZE, wf.currentlyIdleObjects());
    }
    finally {
      stop(channel);
    }
  }

  public void testGreaterThanPoolSize() throws Exception {
    MockChannel channel = createChannel();
    PoolingWorkflow wf = (PoolingWorkflow) channel.getWorkflowList().get(0);
    int count = wf.poolSize() * 2;
    MockMessageProducer prod = (MockMessageProducer) wf.getProducer();
    try {
      start(channel);
      submitMessages(wf, count);
      waitForMessages(prod, count);
      assertTrue("ObjectPool > 1", wf.currentObjectPoolCount() > 1);
      assertTrue("ThreadPool > 1", wf.currentThreadPoolCount() > 1);
      assertTrue("ObjectPool >=0", wf.currentlyIdleObjects() >= 0);
      assertTrue("ThreadPool >=0", wf.currentlyActiveObjects() >= 0);
      assertMessages(prod, count);
    }
    finally {
      stop(channel);
    }
  }

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
    }
    finally {
      stop(channel);
    }
  }

  public void testHandleProduceException() throws Exception {
    MockChannel channel = createChannel();
    PoolingWorkflow wf = (PoolingWorkflow) channel.getWorkflowList().get(0);
    MockMessageProducer meh = new MockMessageProducer();
    MockMessageProducer prod = new MockMessageProducer() {
      @Override
      public void produce(AdaptrisMessage msg) throws ProduceException {
        throw new ProduceException();
      }

      @Override
      public void produce(AdaptrisMessage msg, ProduceDestination destination) throws ProduceException {
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

  public void testHandleRuntimeException() throws Exception {
    MockChannel channel = createChannel();
    PoolingWorkflow wf = (PoolingWorkflow) channel.getWorkflowList().get(0);
    MockMessageProducer meh = new MockMessageProducer();
    MockMessageProducer prod = new MockMessageProducer() {
      @Override
      public void produce(AdaptrisMessage msg) throws ProduceException {
        throw new RuntimeException();
      }

      @Override
      public void produce(AdaptrisMessage msg, ProduceDestination destination) throws ProduceException {
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
      assertEquals("Make none produced", 0, producer.getMessages().size());
      assertEquals(1, meh.getMessages().size());
    }
    finally {
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
    assertEquals("Make sure all produced", count, producer.getMessages().size());
    List<AdaptrisMessage> list = producer.getMessages();
    Collections.sort(list, new MyMetadataComparator());
    for (int i = 0; i < count; i++) {
      AdaptrisMessage msg = list.get(i);
      assertEquals("Metadata value", i, Integer.valueOf(msg.getMetadataValue(COUNT)).intValue());
    }
  }

  private MockChannel createChannel() throws Exception {
    return createChannel(Arrays.asList(new Service[]
    {
        createService(), createService()
    }));
  }

  private MockChannel createChannel(List<Service> services) throws Exception {
    MockChannel channel = new MockChannel();
    PoolingWorkflow wf = new PoolingWorkflow();
    MockMessageConsumer consumer = new MockMessageConsumer();
    MockMessageProducer producer = new MockMessageProducer();
    wf.getServiceCollection().addAll(services);
    wf.setConsumer(consumer);
    wf.setProducer(producer);
    channel.getWorkflowList().add(wf);
    channel.prepare();
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
      PoolingWorkflow wf = new PoolingWorkflow();
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
}