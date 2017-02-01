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

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import com.adaptris.core.event.AdapterCloseEvent;
import com.adaptris.core.event.AdapterInitEvent;
import com.adaptris.core.event.AdapterStartEvent;
import com.adaptris.core.event.AdapterStopEvent;
import com.adaptris.core.jms.JmsConnection;
import com.adaptris.core.jms.PtpProducer;
import com.adaptris.core.jms.activemq.BasicActiveMqImplementation;
import com.adaptris.core.runtime.AdapterManager;
import com.adaptris.core.stubs.MockLogHandler;
import com.adaptris.core.stubs.MockMessageConsumer;
import com.adaptris.core.stubs.MockMessageProducer;
import com.adaptris.core.stubs.StubAdapterStartUpEvent;
import com.adaptris.core.stubs.StubEventHandler;
import com.adaptris.core.util.LifecycleHelper;
import com.adaptris.util.TimeInterval;

public class AdapterTest extends BaseCase {

  public AdapterTest(java.lang.String testName) {
    super(testName);
  }

  @Override
  protected void setUp() throws Exception {
  }

  public void testSetUniqueId() throws Exception {
    Adapter a = new Adapter();
    a.setUniqueId("testSetUniqueId");
    assertEquals("testSetUniqueId", a.getUniqueId());
    try {
      a.setUniqueId(null);
      fail();
    }
    catch (IllegalArgumentException e) {

    }
    assertEquals("testSetUniqueId", a.getUniqueId());
    try {
      a.setUniqueId("");
      fail();
    }
    catch (IllegalArgumentException e) {

    }
    assertEquals("testSetUniqueId", a.getUniqueId());
  }


  public void testAdapterInit() throws Exception {
    Adapter a = new Adapter();
    try {
      a.requestInit();
      fail();
    }
    catch (CoreException expected) {
    }
    finally {
      a.requestClose();
    }
    a = new Adapter();
    a.setUniqueId("testAdapterInit");
    a.requestInit();
    a.requestInit();
    a.requestClose();
    a.init();
    // Technically possible, but unlikely.
    a.stop();
  }

  public void testAdapterStart() throws Exception {
    Adapter a = new Adapter();
    a.setUniqueId("testAdapterStart");
    a.requestStart();
    a.requestStart();
    a.requestClose();
  }

  public void testAdapterStop() throws Exception {
    Adapter a = new Adapter();
    a.setUniqueId("testAdapterStart");
    a.requestStart();
    a.requestStop();
    a.requestStop();
    a.requestClose();
  }

  public void testAdapterClose() throws Exception {
    Adapter a = new Adapter();
    a.setUniqueId("testAdapterStart");
    a.requestStart();
    a.requestClose();
    a.requestClose();
  }

  public void testSetAdapterHeartbeatEventInterval() throws Exception {
    Adapter a = new Adapter();
    assertEquals(new TimeInterval(15L, TimeUnit.MINUTES.name()).toMilliseconds(), a.heartbeatInterval());
    assertNull(a.getHeartbeatEventInterval());
    TimeInterval interval = new TimeInterval(20L, TimeUnit.MINUTES.name());

    a.setHeartbeatEventInterval(interval);
    assertEquals(interval, a.getHeartbeatEventInterval());
    assertEquals(interval.toMilliseconds(), a.heartbeatInterval());

    a.setHeartbeatEventInterval(null);
    assertNull(a.getHeartbeatEventInterval());
    assertEquals(new TimeInterval(15L, TimeUnit.MINUTES.name()).toMilliseconds(), a.heartbeatInterval());
  }


  public void testAdapter_StateManagedComponentContainerInit() throws Exception {
    Adapter adapter = new Adapter();
    adapter.setUniqueId("testAdapter_StateManagedComponentContainerInit");
    Channel channel = new Channel();
    channel.setUniqueId("channel");
    adapter.getChannelList().add(channel);
    adapter.requestInit();
    // Now Close the channel.
    channel.requestClose();
    assertEquals(ClosedState.getInstance(), channel.retrieveComponentState());
    assertEquals(InitialisedState.getInstance(), adapter.retrieveComponentState());

    // This should reinitialise the channel.
    adapter.requestInit();

    assertEquals(InitialisedState.getInstance(), channel.retrieveComponentState());
  }

  public void testAdapter_StateManagedComponentContainerStart() throws Exception {
    Adapter adapter = new Adapter();
    adapter.setUniqueId("testAdapter_StateManagedComponentContainerStart");
    Channel channel = new Channel();
    channel.setUniqueId("channel");
    adapter.getChannelList().add(channel);
    adapter.requestInit();
    adapter.requestStart();
    // Now Close the channel.
    channel.requestClose();
    assertEquals(ClosedState.getInstance(), channel.retrieveComponentState());
    assertEquals(StartedState.getInstance(), adapter.retrieveComponentState());
    // This should restart the channel.
    adapter.requestStart();
    assertEquals(StartedState.getInstance(), channel.retrieveComponentState());
  }

  public void testAdapter_StateManagedComponentContainerStop() throws Exception {
    Adapter adapter = new Adapter();
    adapter.setUniqueId("testAdapter_StateManagedComponentContainerStop");
    Channel channel = new Channel();
    channel.setUniqueId("channel");
    adapter.getChannelList().add(channel);
    adapter.requestInit();
    adapter.requestStart();
    adapter.requestStop();
    // Now Stop the channel.
    channel.requestStart();
    assertEquals(StartedState.getInstance(), channel.retrieveComponentState());
    assertEquals(StoppedState.getInstance(), adapter.retrieveComponentState());
    // This should re-stop the channel.
    adapter.requestStop();
    assertEquals(StoppedState.getInstance(), channel.retrieveComponentState());
  }

  public void testAdapter_StateManagedComponentContainerClose() throws Exception {
    Adapter adapter = new Adapter();
    adapter.setUniqueId("testAdapter_StateManagedComponentContainerClose");
    Channel channel = new Channel();
    channel.setUniqueId("channel");
    adapter.getChannelList().add(channel);
    adapter.requestInit();
    adapter.requestStart();
    adapter.requestClose();
    // Now Stop the channel.
    channel.requestStart();
    assertEquals(StartedState.getInstance(), channel.retrieveComponentState());
    assertEquals(ClosedState.getInstance(), adapter.retrieveComponentState());
    // This should re-stop the channel.
    adapter.requestClose();
    assertEquals(ClosedState.getInstance(), channel.retrieveComponentState());
  }

  public void testAdapterInitThrowsException() throws Exception {
    MockMessageProducer mockEventProducer = new MockMessageProducer();
    Adapter a = createAdapter("testAdapterInitThrowsException", new DefaultEventHandler(mockEventProducer));
    a.getChannelList().add(new Channel() {
      @Override
      public void init() throws CoreException {
        super.init();
        throw new CoreException();
      }
    });
    try {
      a.requestInit();
      fail();
    }
    catch (CoreException e) {
    }
    waitForMessages(mockEventProducer, 1); // if channel.init() fails, we never
                                           // send a StartUp Event.
    assertEquals(1, mockEventProducer.messageCount());
    Event event = ((EventHandlerBase) a.getEventHandler()).createEvent(mockEventProducer.getMessages().get(0));
    assertEquals(AdapterInitEvent.class, event.getClass());
    assertEquals(false, ((AdapterInitEvent) event).getWasSuccessful());
    a.requestClose();

  }

  public void testAdapterStartThrowsException() throws Exception {
    MockMessageProducer mockEventProducer = new MockMessageProducer();
    Adapter a = createAdapter("testAdapterStartThrowsException", new DefaultEventHandler(mockEventProducer));

    a.getChannelList().add(new Channel() {
      @Override
      public void start() throws CoreException {
        super.start();
        throw new CoreException();
      }
    });
    a.requestInit();
    try {
      a.requestStart();
      fail();
    }
    catch (CoreException e) {
    }
    waitForMessages(mockEventProducer, 3);
    assertEquals(3, mockEventProducer.messageCount());
    Event event = ((EventHandlerBase) a.getEventHandler()).createEvent(mockEventProducer.getMessages().get(2));
    assertEquals(AdapterStartEvent.class, event.getClass());
    assertEquals(false, ((AdapterStartEvent) event).getWasSuccessful());
    a.requestClose();
  }

  // Probably a redundant test, but you get a nice warm feeling from have 100%
  // code coverage don't you
  public void testAdapterLogsEventSendException() throws Exception {
    Adapter a = createAdapter("testAdapterLogsEventSendException", new StubEventHandler() {
      @Override
      public void send(Event evt) throws CoreException {
        if (evt instanceof AdapterCloseEvent) {
          throw new CoreException();
        }
        else if (evt instanceof AdapterStopEvent) {
          throw new CoreException();
        }
      }
    });
    a.requestStart();
    a.requestClose();
  }

  // Probably a redundant test, but you get a nice warm feeling from have 100%
  // code coverage don't you
  public void testAdapterLogHandlerFails() throws Exception {
    Adapter a = createAdapter("testAdapterLogFileHandlerFails", new StubEventHandler());
    a.setLogHandler(new MockLogHandler() {
      @Override
      public void clean() throws IOException {
        throw new IOException();
      }
    });
    a.prepare();
  }

  public void testDuplicateWorkflowsForRetrier() throws Exception {
    Adapter adapter = new Adapter();
    adapter.setUniqueId("testDuplicateWorkflowsForRetrier");
    DefaultFailedMessageRetrier retrier = new DefaultFailedMessageRetrier();
    adapter.setFailedMessageRetrier(retrier);
    Channel c1 = new Channel();
    c1.getWorkflowList().add(createWorkflow("workflow"));
    Channel c2 = new Channel();
    c2.getWorkflowList().add(createWorkflow("workflow"));
    // Ok so now we have 2x workflows that should resolve to
    // NullConnection-NullConnection-workflow-null-null...
    adapter.getChannelList().add(c1);
    adapter.getChannelList().add(c2);
    adapter.prepare();
    assertNotSame(retrier, adapter.getFailedMessageRetrier());
  }

  public void testErrorHandlerPropagation() throws Exception {
    Adapter adapter = createAdapter("testErrorHandlerPropagation");
    Channel c1 = adapter.getChannelList().get(0);
    StandardWorkflow wf = (StandardWorkflow) c1.getWorkflowList().get(0);
    ProcessingExceptionHandler meh = adapter.getMessageErrorHandler();
    adapter.prepare();
    assertEquals(meh, adapter.getMessageErrorHandler());
    assertEquals(meh, c1.retrieveActiveMsgErrorHandler());
    assertEquals(meh, wf.retrieveActiveMsgErrorHandler());
  }

  public void testErrorHandlerSetInChannel() throws Exception {
    Adapter adapter = createAdapter("testErrorHandlerSetInChannel");
    NullProcessingExceptionHandler meh1 = new NullProcessingExceptionHandler();
    Channel c1 = adapter.getChannelList().get(0);
    c1.setMessageErrorHandler(meh1);
    StandardWorkflow wf = (StandardWorkflow) c1.getWorkflowList().get(0);
    adapter.prepare();
    assertNotSame(adapter.getMessageErrorHandler(), c1.retrieveActiveMsgErrorHandler());
    assertEquals(meh1, c1.retrieveActiveMsgErrorHandler());
    assertNotSame(adapter.getMessageErrorHandler(), wf.retrieveActiveMsgErrorHandler());
    assertEquals(meh1, wf.retrieveActiveMsgErrorHandler());
  }

  public void testErrorHandlerSetInChannelAndWorkflow() throws Exception {
    Adapter adapter = createAdapter("testErrorHandlerSetInChannelAndWorkflow");

    NullProcessingExceptionHandler meh1 = new NullProcessingExceptionHandler();
    NullProcessingExceptionHandler meh2 = new NullProcessingExceptionHandler();
    Channel c1 = adapter.getChannelList().get(0);
    c1.setMessageErrorHandler(meh1);
    StandardWorkflow wf = (StandardWorkflow) c1.getWorkflowList().get(0);
    wf.setMessageErrorHandler(meh2);
    adapter.prepare();
    assertNotSame(adapter.getMessageErrorHandler(), c1.retrieveActiveMsgErrorHandler());
    assertEquals(meh1, c1.retrieveActiveMsgErrorHandler());
    assertNotSame(adapter.getMessageErrorHandler(), wf.retrieveActiveMsgErrorHandler());
    assertEquals(meh2, wf.retrieveActiveMsgErrorHandler());
  }

  private static StandardWorkflow createWorkflow(String destination) {
    StandardWorkflow wf = new StandardWorkflow();
    wf.getConsumer().setDestination(new ConfiguredConsumeDestination(destination));
    return wf;
  }

  public void testLifecycleWithBlockingLifecycleStrategy() throws Exception {
    Adapter adapter = AdapterTest.createAdapter("testLifecycleWithBlockingLifecycleStrategy");
    adapter.getChannelList().setLifecycleStrategy(new com.adaptris.core.lifecycle.BlockingChannelLifecycleStrategy());

    adapter.requestInit();
    waitFor(adapter, InitialisedState.getInstance());
    assertEquals(InitialisedState.getInstance(), adapter.retrieveComponentState());
    waitFor(adapter.getChannelList(), InitialisedState.getInstance());
    assertState(adapter.getChannelList(), InitialisedState.getInstance());

    adapter.requestStart();
    waitFor(adapter, StartedState.getInstance());
    assertEquals(StartedState.getInstance(), adapter.retrieveComponentState());
    waitFor(adapter.getChannelList(), StartedState.getInstance());
    assertState(adapter.getChannelList(), StartedState.getInstance());

    adapter.requestStop();
    waitFor(adapter, StoppedState.getInstance());
    assertEquals(StoppedState.getInstance(), adapter.retrieveComponentState());
    waitFor(adapter.getChannelList(), StoppedState.getInstance());
    assertState(adapter.getChannelList(), StoppedState.getInstance());

    adapter.requestClose();
    waitFor(adapter, ClosedState.getInstance());
    assertEquals(ClosedState.getInstance(), adapter.retrieveComponentState());
    waitFor(adapter.getChannelList(), ClosedState.getInstance());
    assertState(adapter.getChannelList(), ClosedState.getInstance());
  }

  public void testLifecycleWithNonBlockingLifecycleStrategy() throws Exception {
    Adapter adapter = AdapterTest.createAdapter("testLifecycleWithBlockingLifecycleStrategy");
    adapter.getChannelList().setLifecycleStrategy(new com.adaptris.core.lifecycle.NonBlockingChannelStartStrategy());

    adapter.requestInit();
    waitFor(adapter, InitialisedState.getInstance());
    assertEquals(InitialisedState.getInstance(), adapter.retrieveComponentState());
    waitFor(adapter.getChannelList(), InitialisedState.getInstance());
    assertState(adapter.getChannelList(), InitialisedState.getInstance());

    adapter.requestStart();
    waitFor(adapter, StartedState.getInstance());
    assertEquals(StartedState.getInstance(), adapter.retrieveComponentState());
    waitFor(adapter.getChannelList(), StartedState.getInstance());
    assertState(adapter.getChannelList(), StartedState.getInstance());

    adapter.requestStop();
    waitFor(adapter, StoppedState.getInstance());
    assertEquals(StoppedState.getInstance(), adapter.retrieveComponentState());
    waitFor(adapter.getChannelList(), StoppedState.getInstance());
    assertState(adapter.getChannelList(), StoppedState.getInstance());

    adapter.requestClose();
    waitFor(adapter, ClosedState.getInstance());
    assertEquals(ClosedState.getInstance(), adapter.retrieveComponentState());
    waitFor(adapter.getChannelList(), ClosedState.getInstance());
    assertState(adapter.getChannelList(), ClosedState.getInstance());

  }

  public void testBug1654() throws Exception {
    Adapter adapter = new Adapter();
    adapter.setUniqueId("1234");
    adapter.setMessageErrorHandler(new StandardProcessingExceptionHandler());
    Channel c1 = new Channel();
    c1.setUniqueId("c1");
    c1.setMessageErrorHandler(new RetryMessageErrorHandler());

    Channel c2 = new Channel();
    c2.setUniqueId("c2");

    ChannelList cl = new ChannelList();
    cl.addChannel(c1);
    cl.addChannel(c2);
    adapter.setChannelList(cl);
    LifecycleHelper.init(adapter);
    Channel initedChannel = adapter.getChannelList().getChannel("c2");
    assertEquals(StandardProcessingExceptionHandler.class, initedChannel.retrieveActiveMsgErrorHandler().getClass());
  }

  public void testBug469AndBug1268() throws Exception {
    final Adapter adapter = AdapterTest.createRetryingAdapter("testBug469AndBug1268");
    // NonBlockingChannelStartStrategy tests Bug1268...
    adapter.getChannelList().setLifecycleStrategy(new com.adaptris.core.lifecycle.NonBlockingChannelStartStrategy());
    AdapterManager am = new AdapterManager(adapter);
    try {
      adapter.requestStart();
      waitFor(adapter, StartedState.getInstance());
      assertEquals(StartedState.getInstance(), adapter.retrieveComponentState());
      // So the Adapter is started but channel1 should currently be "retrying",
      // so let's try and stop the adapter.
      // Channel should have a componentState of closed.
      Channel c = adapter.getChannelList().getChannel("testBug469AndBug1268_c1");
      assertEquals(ClosedState.getInstance(), c.retrieveComponentState());

      new Thread() {
        @Override
        public void run() {
          Thread.currentThread().setName("testBug469AndBug1268 stop");
          adapter.requestClose();
        }
      }.start();
      waitFor(adapter, ClosedState.getInstance());
      // Bug469 is tested by the fact that the Adapter is now closed.
      assertEquals(ClosedState.getInstance(), adapter.retrieveComponentState());
    }
    finally {
      am.forceClose();
    }
  }

  public void testBug1362() throws Exception {
    int maxChannels = 10;
    int maxWorkflowsInChannels = 10;

    final Adapter adapter = AdapterTest.createQuartzPollingAdapter("testBug1362", maxChannels, maxWorkflowsInChannels);
    adapter.getChannelList().setLifecycleStrategy(new com.adaptris.core.lifecycle.NonBlockingChannelStartStrategy());
    adapter.requestInit();
    waitFor(adapter, InitialisedState.getInstance());

    assertEquals(InitialisedState.getInstance(), adapter.retrieveComponentState());
    ChannelList cl = adapter.getChannelList();

    waitFor(cl, InitialisedState.getInstance());
    assertState(cl, InitialisedState.getInstance());
    adapter.requestClose();
    assertEquals(ClosedState.getInstance(), adapter.retrieveComponentState());
  }

  public void testXmlRoundTrip() throws Exception {
    Adapter adapter = AdapterTest.createAdapter("test-adapter");
    AdaptrisMarshaller m = DefaultMarshaller.getDefaultMarshaller();
    String xml = m.marshal(adapter);
    System.out.println("Xml is:\n" + xml);
    Adapter a2 = (Adapter) m.unmarshal(xml);
    assertRoundtripEquality(adapter, a2);
  }

  public void testBug1267() throws Exception {
    MockMessageProducer producer = new MockMessageProducer();
    DefaultEventHandler eh = new DefaultEventHandler(producer);
    Adapter adapter = createAdapter("testBug1267");
    adapter.setEventHandler(eh);
    LifecycleHelper.init(adapter);
    assertEquals("EH should already be started on adapter init", eh.retrieveComponentState(), StartedState.getInstance());
    LifecycleHelper.start(adapter);
    assertEquals("EH should still be started", eh.retrieveComponentState(), StartedState.getInstance());
    LifecycleHelper.stop(adapter);
    assertEquals("EH should still be started", eh.retrieveComponentState(), StartedState.getInstance());
    LifecycleHelper.close(adapter);
    assertEquals("EH should now be closed", eh.retrieveComponentState(), ClosedState.getInstance());

  }

  public void testBug2049_Init() throws Exception {
    Adapter a = createAdapter("testBug2049_Init");
    try {
      a.getChannelList().addChannel(new Channel() {
        @Override
        public void init() throws CoreException {
          super.init();
          throw new RuntimeException("testBug2049_Init");
        }
      });
      a.requestInit();
      fail("test init with RuntimeException throw from init()");
    }
    catch (CoreException e) {
      assertNotNull("CoreException Cause != null)", e.getCause());
      assertEquals("Wrapped Exception is a " + RuntimeException.class.getSimpleName(), RuntimeException.class, e.getCause()
          .getClass());
    }
  }

  public void testBug2049_Start() throws Exception {
    Adapter a = createAdapter("testBug2049_Start");
    try {
      a.getChannelList().addChannel(new Channel() {
        @Override
        public void start() throws CoreException {
          super.start();
          throw new RuntimeException("testBug2049_Start");
        }
      });
      a.requestStart();
      fail("test init with RuntimeException throw from start()");
    }
    catch (CoreException e) {
      assertNotNull("CoreException Cause != null)", e.getCause());
      assertEquals("Wrapped Exception is a " + RuntimeException.class.getSimpleName(), RuntimeException.class, e.getCause()
          .getClass());
    }
  }

  public void testSetters() throws Exception {
    Adapter a = createAdapter("testHeartbeatTimerTask");
    try {
      a.setChannelList(null);
      fail();
    }
    catch (IllegalArgumentException expected) {

    }
    try {
      a.setEventHandler(null);
      fail();
    }
    catch (IllegalArgumentException expected) {

    }
    try {
      a.setFailedMessageRetrier(null);
      fail();
    }
    catch (IllegalArgumentException expected) {

    }
    try {
      a.setHeartbeatEventImp(null);
      fail();
    }
    catch (IllegalArgumentException expected) {

    }
    try {
      a.setHeartbeatEventImp("");
      fail();
    }
    catch (IllegalArgumentException expected) {

    }
    try {
      a.setLogHandler(null);
      fail();
    }
    catch (IllegalArgumentException expected) {

    }
    a.setLogHandler(new FileLogHandler());

    try {
      a.setMessageErrorHandler(null);
      fail();
    }
    catch (IllegalArgumentException expected) {

    }
    try {
      a.setStartUpEventImp(null);
      fail();
    }
    catch (IllegalArgumentException expected) {

    }
    try {
      a.setStartUpEventImp("");
      fail();
    }
    catch (IllegalArgumentException expected) {

    }
  }

  public void testHeartbeatTimerTask() throws Exception {
    Adapter a = createAdapter("testHeartbeatTimerTask");
    MockMessageProducer mock = new MockMessageProducer();
    DefaultEventHandler sce = new DefaultEventHandler(mock);
    a.setEventHandler(sce);
    a.setHeartbeatEventInterval(new TimeInterval(500L, TimeUnit.MILLISECONDS.name()));
    try {
      a.requestStart();
      waitForMessages(mock, 4);
      assertTrue(mock.messageCount() >= 4);
      AdaptrisMarshaller m = DefaultMarshaller.getDefaultMarshaller();
      assertEquals(AdapterInitEvent.class, createEvent(mock.getMessages().get(0), m).getClass());
      assertEquals(StubAdapterStartUpEvent.class, createEvent(mock.getMessages().get(1), m).getClass());
      assertEquals(AdapterStartEvent.class, createEvent(mock.getMessages().get(2), m).getClass());
    }
    finally {
      a.requestClose();
    }
  }

  public void testHeartbeatTimerTask_Deprecated() throws Exception {
    Adapter a = createAdapter("testHeartbeatTimerTask_Deprecated");
    MockMessageProducer mock = new MockMessageProducer();
    DefaultEventHandler sce = new DefaultEventHandler(mock);
    a.setEventHandler(sce);
    a.setHeartbeatEventInterval(new TimeInterval(500L, TimeUnit.MILLISECONDS));
    try {
      a.requestStart();
      waitForMessages(mock, 4);
      assertTrue(mock.messageCount() >= 4);
      AdaptrisMarshaller m = DefaultMarshaller.getDefaultMarshaller();
      assertEquals(AdapterInitEvent.class, createEvent(mock.getMessages().get(0), m).getClass());
      assertEquals(StubAdapterStartUpEvent.class, createEvent(mock.getMessages().get(1), m).getClass());
      assertEquals(AdapterStartEvent.class, createEvent(mock.getMessages().get(2), m).getClass());
    }
    finally {
      a.requestClose();
    }
  }

  /**
   * <p>
   * Creates a valid Adapter to use in tests. Uese Mock aka Memory MessageConsumer / Producer.
   * </p>
   * 
   * @param uniqueId a uniqure identifier for this <code>Adapter</code>
   */
  public static Adapter createAdapter(String uniqueId) throws Exception {
    // Yes that's right we use the no-arg constructor due to
    // what happens if you use the unique-id constructor...
    // Behaviour is changed when it calls setEventHandler();
    Adapter result = new Adapter();
    // XStream sometimes throws a java.util.ConcurrentModificationException when
    // marshalling, in DefaultAdapterStartupEvent#setAdapter()
    // This *works* round the symptom; but it's not clear as to exactly why it occurs;
    // It's clearly a timing issue, but I don't really understand why, as it is single threaded; so the lists shouldn't be
    // being modified by anything else.
    // So, let's use a StubAdapterStartupEvent that does nothing, doesn't even save the adapter. as we don't care about it.
    // redmineID #2557
    result.setStartUpEventImp(StubAdapterStartUpEvent.class.getCanonicalName());

    StandardWorkflow workflow1 = createWorkflow(uniqueId + "_workflow");
    workflow1.setConsumer(new MockMessageConsumer());
    workflow1.setProducer(new MockMessageProducer());
    Channel channel = new Channel();
    channel.setUniqueId(uniqueId + "_channel1");
    channel.getWorkflowList().add(workflow1);
    result.getChannelList().add(channel);
    result.setUniqueId(uniqueId);
    return result;
  }

  /**
   * <p>
   * Creates a valid Adapter to use in tests. Uese Mock aka Memory MessageConsumer / Producer.
   * </p>
   * 
   * @param uniqueId a uniqure identifier for this <code>Adapter</code>
   */
  public static Adapter createRetryingAdapter(String uniqueId) throws Exception {
    Adapter result = null;

    AdaptrisConnection consume = new NullConnection();
    JmsConnection produce = new JmsConnection(new BasicActiveMqImplementation("tcp://localhost:5000"));
    produce.setConnectionRetryInterval(new TimeInterval(1L, TimeUnit.SECONDS.name()));
    produce.setConnectionAttempts(20);
    AdaptrisMessageProducer producer1 = new PtpProducer();

    StandardWorkflow workflow1 = new StandardWorkflow();
    workflow1.setProducer(producer1);
    WorkflowList workflows = new WorkflowList();
    workflows.add(workflow1);
    Channel channel = new Channel();
    channel.setUniqueId(uniqueId + "_c1");
    channel.setConsumeConnection(consume);
    channel.setProduceConnection(produce);
    channel.setWorkflowList(workflows);
    ChannelList channels = new ChannelList();
    channels.addChannel(channel);

    result = new Adapter();
    result.setChannelList(channels);
    result.setUniqueId(uniqueId);
    return result;
  }

  private static Adapter createQuartzPollingAdapter(String uniqueId, int channels, int workflowsInChannels) throws Exception {
    Adapter result = new Adapter();
    ChannelList cl = new ChannelList();
    for (int i = 0; i < channels; i++) {
      Channel c = new Channel();
      String name = uniqueId + "_c" + i;
      c.setUniqueId(name);
      c.setWorkflowList(createQuartzWorkflowList(name, workflowsInChannels));
      cl.addChannel(c);
    }
    result.setChannelList(cl);
    result.setUniqueId(uniqueId);
    return result;

  }

  private static WorkflowList createQuartzWorkflowList(String prefix, int count) throws Exception {
    WorkflowList wf = new WorkflowList();
    for (int i = 0; i < count; i++) {
      StandardWorkflow swf = new StandardWorkflow();
      PollingTrigger pt = new PollingTrigger();
      pt.setDestination(new ConfiguredConsumeDestination(prefix + "_wf" + i));
      pt.setTemplate("<dummy>");
      pt.setPoller(new QuartzCronPoller("*/1 * * * * ?"));
      swf.setConsumer(pt);
      wf.add(swf);
    }
    return wf;
  }

  private void waitFor(ChannelList cl, ComponentState state) throws Exception {
    long waitTime = 0;
    boolean allCorrect = false;
    while (waitTime < MAX_WAIT && !allCorrect) {
      int matchingState = 0;
      waitTime += DEFAULT_WAIT_INTERVAL;
      Thread.sleep(DEFAULT_WAIT_INTERVAL);
      for (Channel c : cl.getChannels()) {
        if (c.retrieveComponentState().equals(state)) {
          matchingState++;
        }
      }

      if (matchingState == cl.size()) {
        allCorrect = true;
      }

    }
    return;
  }

  private void assertState(ChannelList cl, ComponentState state) {
    for (Channel c : cl.getChannels()) {
      assertEquals(c.getUniqueId(), state, c.retrieveComponentState());
      WorkflowList wl = c.getWorkflowList();
      for (int i = 0; i < wl.size(); i++) {
        Workflow w = wl.get(i);
        if (w instanceof StateManagedComponent) {
          assertEquals(c.getUniqueId() + " wf " + i, state, ((StateManagedComponent) w).retrieveComponentState());
        }
      }
    }
  }

  private static Event createEvent(AdaptrisMessage msg, AdaptrisMarshaller marshaller) throws Exception {
    String payload = msg.getContent();
    Event result = null;

    if (payload == null) {
      throw new CoreException("Cannot create event from empty payload");
    }
    try {
      result = (Event) marshaller.unmarshal(payload);
    }
    catch (CoreException e) {
      // if (msg.containsKey(CoreConstants.EVENT_CLASS)) {
      // result = (Event) marshaller.unmarshal(payload);
      // }
    }
    return result;
  }

  public static Adapter createAdapter(String name, EventHandler eventHandler) throws Exception {
    Adapter a = createAdapter(name);
    a.setEventHandler(eventHandler);
    return a;
  }
}
