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

package com.adaptris.core.jms.activemq;

import static com.adaptris.core.stubs.ObjectUtils.asSetters;
import static com.adaptris.core.stubs.ObjectUtils.invokeSetter;
import static com.adaptris.interlok.junit.scaffolding.BaseCase.MAX_WAIT;
import static com.adaptris.interlok.junit.scaffolding.BaseCase.waitForMessages;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adaptris.core.Adapter;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.ClosedState;
import com.adaptris.core.ComponentState;
import com.adaptris.core.CoreException;
import com.adaptris.core.InitialisedState;
import com.adaptris.core.SharedConnection;
import com.adaptris.core.StandaloneProducer;
import com.adaptris.core.StandardWorkflow;
import com.adaptris.core.StartedState;
import com.adaptris.core.Workflow;
import com.adaptris.core.jms.JmsConnection;
import com.adaptris.core.jms.JmsConnectionErrorHandler;
import com.adaptris.core.jms.PasConsumer;
import com.adaptris.core.jms.PasProducer;
import com.adaptris.core.jms.jndi.StandardJndiImplementation;
import com.adaptris.core.stubs.MockChannel;
import com.adaptris.core.stubs.MockMessageProducer;
import com.adaptris.core.stubs.ObjectUtils;
import com.adaptris.core.util.LifecycleHelper;
import com.adaptris.interlok.junit.scaffolding.services.ExampleServiceCase;
import com.adaptris.util.TimeInterval;

public class JmsConnectionErrorHandlerTest {

  private static final long SLEEP_TIME_MS = 100L;

  private Logger log = LoggerFactory.getLogger(this.getClass());
  
  
  
  private static EmbeddedActiveMq activeMqBroker;

  @BeforeAll
  public static void setUpAll() throws Exception {
    activeMqBroker = new EmbeddedActiveMq();
    activeMqBroker.start();
  }
  
  @AfterAll
  public static void tearDownAll() throws Exception {
    if(activeMqBroker != null)
      activeMqBroker.destroy();
  }

  @Test
  public void testConnectionErrorHandler(TestInfo info) throws Exception {
    MockChannel channel = createChannel(activeMqBroker, activeMqBroker.getJmsConnection(new BasicActiveMqImplementation(), true),
        info.getDisplayName(), new JmsConnectionErrorHandler());
    try {
      channel.requestStart();
      assertEquals(StartedState.getInstance(), channel.retrieveComponentState());
      activeMqBroker.stop();
      Thread.sleep(1000);
      activeMqBroker.start();
      waitForChannelToMatchState(StartedState.getInstance(), channel);

      assertEquals(StartedState.getInstance(), channel.retrieveComponentState());
      assertEquals(2, channel.getStartCount());

    }
    finally {
      channel.requestClose();
    }
  }

  // Tests INTERLOK-2063
  @Test
  public void testConnectionErrorHandler_WithRuntimeException(TestInfo info) throws Exception {
    MockChannel channel = createChannel(activeMqBroker,
        new JmsConnectionCloseWithRuntimeException(activeMqBroker.getJmsConnection(new BasicActiveMqImplementation(), true)),
        info.getDisplayName(), new JmsConnectionErrorHandler() {
          @Override
          protected long retryWaitTimeMs() {
            return 500L;
          }
        });
    try {
      channel.requestStart();
      assertEquals(StartedState.getInstance(), channel.retrieveComponentState());
      activeMqBroker.stop();
      Thread.sleep(1000);
      activeMqBroker.start();
      waitForChannelToMatchState(StartedState.getInstance(), channel);

      assertEquals(StartedState.getInstance(), channel.retrieveComponentState());
      assertEquals(2, channel.getStartCount());

    } finally {
      channel.requestClose();
    }
  }

  @Test
  public void testConnectionErrorHandler_WithInitialiseException(TestInfo info) throws Exception {
    MockChannel channel = new MockChannelFail(createChannel(activeMqBroker,
        activeMqBroker.getJmsConnection(new BasicActiveMqImplementation(), true),
        info.getDisplayName(), new JmsConnectionErrorHandler()),
        MockChannelFail.WhenToFail.INIT_AFTER_CLOSE);
    try {
      channel.requestStart();
      assertEquals(StartedState.getInstance(), channel.retrieveComponentState());
      activeMqBroker.stop();
      Thread.sleep(1000);
      activeMqBroker.start();
      // This is a bit artificial, but we shouldn't ever transition from ClosedState.
      Thread.sleep(1000);
      waitForChannelToMatchState(ClosedState.getInstance(), channel);

      assertEquals(ClosedState.getInstance(), channel.retrieveComponentState());
      assertEquals(1, channel.getStartCount());
      assertEquals(1, channel.getInitCount());

    } finally {
      channel.requestClose();
    }
  }

  @Test
  public void testConnectionErrorHandler_WithStartException(TestInfo info) throws Exception {
    MockChannel channel = new MockChannelFail(createChannel(activeMqBroker,
        activeMqBroker.getJmsConnection(new BasicActiveMqImplementation(), true),
        info.getDisplayName(), new JmsConnectionErrorHandler()),
        MockChannelFail.WhenToFail.START_AFTER_CLOSE);
    try {
      channel.requestStart();
      assertEquals(StartedState.getInstance(), channel.retrieveComponentState());
      activeMqBroker.stop();
      Thread.sleep(1000);
      activeMqBroker.start();
      waitForChannelToMatchState(InitialisedState.getInstance(), channel);

      assertEquals(InitialisedState.getInstance(), channel.retrieveComponentState());
      assertEquals(1, channel.getStartCount());
      assertEquals(2, channel.getInitCount());

    } finally {
      channel.requestClose();
    }
  }

  @Test
  public void testConnectionErrorHandler_NotSingleExecution(TestInfo info) throws Exception {
    MockChannel channel = createChannel(activeMqBroker, activeMqBroker.getJmsConnection(new BasicActiveMqImplementation(), true),
        info.getDisplayName(), new JmsConnectionErrorHandler(false));
    try {
      channel.requestStart();
      assertEquals(StartedState.getInstance(), channel.retrieveComponentState());
      activeMqBroker.stop();
      Thread.sleep(1000);
      activeMqBroker.start();
      waitForChannelToMatchState(StartedState.getInstance(), channel);

      assertEquals(StartedState.getInstance(), channel.retrieveComponentState());
      assertEquals(2, channel.getStartCount());

    }
    finally {
      channel.requestClose();
    }
  }

  @Test
  public void testConnectionErrorHandlerWithUnamedChannel(TestInfo info) throws Exception {
    MockChannel channel = createChannel(null, activeMqBroker,
        activeMqBroker.getJmsConnection(new BasicActiveMqImplementation(), true),
        info.getDisplayName());
    try {
      channel.requestStart();
      assertEquals(StartedState.getInstance(), channel.retrieveComponentState());
      activeMqBroker.stop();
      Thread.sleep(1000);
      activeMqBroker.start();
      waitForChannelToMatchState(StartedState.getInstance(), channel);

      assertEquals(StartedState.getInstance(), channel.retrieveComponentState());
      assertEquals(2, channel.getStartCount());

    }
    finally {
      channel.requestClose();
    }
  }

  @Test
  public void testConnectionErrorHandlerWithJndi(TestInfo info) throws Exception {
    String queueName = info.getDisplayName() + "_queue";
    String topicName = info.getDisplayName() + "_topic";
    MockChannel channel = createChannel(activeMqBroker,
        activeMqBroker.getJndiPasConnection(new StandardJndiImplementation(), false, queueName, topicName), topicName,
        new JmsConnectionErrorHandler());
    try {
      channel.requestStart();
      assertEquals(StartedState.getInstance(), channel.retrieveComponentState());
      activeMqBroker.stop();
      Thread.sleep(1000);
      activeMqBroker.start();
      waitForChannelToMatchState(StartedState.getInstance(), channel);

      assertEquals(StartedState.getInstance(), channel.retrieveComponentState());
      assertEquals(2, channel.getStartCount());

    }
    finally {
      channel.requestClose();
    }
  }

  @Test
  public void testBug1926(TestInfo info) throws Exception {
    MockChannel channel = createChannel(activeMqBroker, activeMqBroker.getJmsConnection(new BasicActiveMqImplementation(), true),
        info.getDisplayName(), new JmsConnectionErrorHandler());
    try {
      channel.requestStart();
      assertEquals(StartedState.getInstance(), channel.retrieveComponentState());
      activeMqBroker.stop();
      Thread.sleep(1000);
      activeMqBroker.start();
      waitForChannelToMatchState(StartedState.getInstance(), channel);

      assertEquals(StartedState.getInstance(), channel.retrieveComponentState());
      assertEquals(2, channel.getStartCount());

    }
    finally {
      channel.requestClose();
    }
  }

  @Test
  public void testRestartSharedConnection(TestInfo info) throws Exception {
    Adapter adapter = new Adapter();
    adapter.setUniqueId(info.getDisplayName());
    JmsConnection connection = activeMqBroker.getJmsConnection(new BasicActiveMqImplementation(), true);
    connection.setConnectionErrorHandler(new JmsConnectionErrorHandler());
    connection.setUniqueId(info.getDisplayName());
    connection.setConnectionRetryInterval(new TimeInterval(5L, "SECONDS"));
    connection.setConnectionAttempts(null);
    adapter.getSharedComponents().addConnection(connection);
    MockChannel channel = createChannel(activeMqBroker,
        new SharedConnection(info.getDisplayName()), info.getDisplayName());
    MockMessageProducer producer = (MockMessageProducer) channel.getWorkflowList().get(0).getProducer();
    adapter.getChannelList().add(channel);
    try {
      adapter.requestStart();
      assertEquals(StartedState.getInstance(), channel.retrieveComponentState());
      // Now try and send a message
      ExampleServiceCase.execute(createProducer(activeMqBroker, info.getDisplayName()),
          AdaptrisMessageFactory.getDefaultInstance().newMessage("ABC"));
      waitForMessages(producer, 1);

      activeMqBroker.stop();
      log.trace(info.getDisplayName() + ": Waiting for channel death (i.e. !StartedState)");
      long totalWaitTime = waitForChannelToChangeState(StartedState.getInstance(), channel);
      log.trace(info.getDisplayName()
          + ": Channel appears to be not started now, and I waited for " + totalWaitTime);

      activeMqBroker.start();

      totalWaitTime = waitForChannelToMatchState(StartedState.getInstance(), channel);
      log.trace(info.getDisplayName() + ": Channel now started now, and I waited for "
          + totalWaitTime);

      waitForChannelToMatchState(StartedState.getInstance(), channel);
      assertEquals(StartedState.getInstance(), channel.retrieveComponentState());

      // Now try and send a message
      ExampleServiceCase.execute(createProducer(activeMqBroker, info.getDisplayName()),
          AdaptrisMessageFactory.getDefaultInstance().newMessage("ABC"));
      waitForMessages(producer, 2);

    }
    finally {
      adapter.requestClose();
    }
  }

  @Test
  public void testRestartSharedConnection_ChannelNotStarted(TestInfo info) throws Exception {
    Adapter adapter = new Adapter();
    adapter.setUniqueId(info.getDisplayName());
    JmsConnection connection = activeMqBroker.getJmsConnection(new BasicActiveMqImplementation(), true);
    connection.setConnectionErrorHandler(new JmsConnectionErrorHandler());
    connection.setUniqueId(info.getDisplayName());
    connection.setConnectionRetryInterval(new TimeInterval(5L, "SECONDS"));
    connection.setConnectionAttempts(null);
    adapter.getSharedComponents().addConnection(connection);
    MockChannel started = createChannel(activeMqBroker,
        new SharedConnection(info.getDisplayName()), info.getDisplayName());
    MockChannel neverStarted = createChannel(activeMqBroker,
        new SharedConnection(info.getDisplayName()), info.getDisplayName() + "_2");
    neverStarted.setAutoStart(false);
    MockMessageProducer producer = (MockMessageProducer) started.getWorkflowList().get(0).getProducer();
    adapter.getChannelList().add(started);
    adapter.getChannelList().add(neverStarted);
    try {
      adapter.requestStart();
      assertEquals(StartedState.getInstance(), started.retrieveComponentState());
      assertEquals(ClosedState.getInstance(), neverStarted.retrieveComponentState());
      // Now try and send a message
      ExampleServiceCase.execute(createProducer(activeMqBroker, info.getDisplayName()),
          AdaptrisMessageFactory.getDefaultInstance().newMessage("ABC"));
      waitForMessages(producer, 1);

      activeMqBroker.stop();
      log.trace(info.getDisplayName() + ": Waiting for channel death (i.e. !StartedState)");
      long totalWaitTime = waitForChannelToChangeState(StartedState.getInstance(), started);
      log.trace(info.getDisplayName()
          + ": Channel appears to be not started now, and I waited for " + totalWaitTime);

      activeMqBroker.start();

      totalWaitTime = waitForChannelToMatchState(StartedState.getInstance(), started);
      log.trace(info.getDisplayName() + ": Channel now started now, and I waited for "
          + totalWaitTime);
      assertEquals(StartedState.getInstance(), started.retrieveComponentState());


      // Now try and send a message
      ExampleServiceCase.execute(createProducer(activeMqBroker, info.getDisplayName()),
          AdaptrisMessageFactory.getDefaultInstance().newMessage("ABC"));
      waitForMessages(producer, 2);
      assertEquals(ClosedState.getInstance(), neverStarted.retrieveComponentState());
      assertEquals(0, neverStarted.getInitCount());
    }
    finally {
      adapter.requestClose();
    }
  }

  private MockChannel createChannel(EmbeddedActiveMq mq, JmsConnection con, String destinationName, JmsConnectionErrorHandler h)
      throws Exception {
    return createChannel(mq.getName() + "_channel", mq, con, destinationName, h);
  }

  private MockChannel createChannel(EmbeddedActiveMq mq, SharedConnection con, String destinationName) throws Exception {
    MockChannel result = new MockChannel();
    result.setUniqueId(mq.getName() + "_channel" + "_" + destinationName);
    result.setConsumeConnection(con);
    Workflow workflow = createWorkflow(mq, destinationName);
    result.getWorkflowList().add(workflow);
    return result;
  }

  private MockChannel createChannel(String channelName, EmbeddedActiveMq mq, JmsConnection con, String destinationName)
      throws Exception {
    return createChannel(channelName, mq, con, destinationName, new JmsConnectionErrorHandler());
  }

  private MockChannel createChannel(String channelName, EmbeddedActiveMq mq, JmsConnection con, String destinationName,
                                    JmsConnectionErrorHandler handler)
      throws Exception {
    MockChannel result = new MockChannel();
    result.setUniqueId(channelName);
    con.setConnectionRetryInterval(new TimeInterval(1L, TimeUnit.SECONDS.name()));
    con.setConnectionAttempts(50);
    con.setConnectionErrorHandler(handler);
    result.setConsumeConnection(con);
    Workflow workflow = createWorkflow(mq, destinationName);
    result.getWorkflowList().add(workflow);
    return result;
  }

  private Workflow createWorkflow(EmbeddedActiveMq mq, String destination) throws Exception {
    StandardWorkflow wf = new StandardWorkflow();
    PasConsumer consumer = new PasConsumer().withTopic(destination);
    wf.setProducer(new MockMessageProducer());
    wf.setConsumer(consumer);
    return wf;
  }

  private long waitForChannelToChangeState(ComponentState state, MockChannel channel) throws Exception {
    long totalWaitTime = 0;
    while (channel.retrieveComponentState().equals(state) && totalWaitTime < MAX_WAIT) {
      LifecycleHelper.waitQuietly(SLEEP_TIME_MS);
      totalWaitTime += SLEEP_TIME_MS;
    }
    return totalWaitTime;
  }

  protected long waitForChannelToMatchState(ComponentState state, MockChannel channel) throws Exception {
    long totalWaitTime = 0;
    while (!channel.retrieveComponentState().equals(state) && totalWaitTime < MAX_WAIT) {
      LifecycleHelper.waitQuietly(SLEEP_TIME_MS);
      totalWaitTime += SLEEP_TIME_MS;
    }
    return totalWaitTime;
  }

  private StandaloneProducer createProducer(EmbeddedActiveMq mq, String dest) {
    JmsConnection conn = mq.getJmsConnection(new BasicActiveMqImplementation(), true);
    conn.setConnectionErrorHandler(new JmsConnectionErrorHandler());
    PasProducer producer = new PasProducer().withTopic(dest);
    return new StandaloneProducer(conn, producer);
  }

  private static class MockChannelFail extends MockChannel {

    private enum WhenToFail {
      NEVER, INIT_AFTER_CLOSE, START_AFTER_CLOSE
    };

    private transient boolean closeCalled = false;
    private transient WhenToFail whenToFail = WhenToFail.NEVER;

    public MockChannelFail(MockChannel other, WhenToFail b) throws Exception {
      super();
      configureSelf(other);
      whenToFail = b;
    }

    private void configureSelf(MockChannel other) throws Exception {
      String[] getterMethods = ObjectUtils.filterGetterWithNoSetter(MockChannel.class, ObjectUtils.getGetters(MockChannel.class));
      String[] setterMethods = asSetters(getterMethods);
      for (int i = 0; i < getterMethods.length; i++) {
        invokeSetter(this, MockChannel.class, setterMethods[i], getterMethods[i],
            ObjectUtils.invokeGetter(other, getterMethods[i]));
      }
    }

    @Override
    public void init() throws CoreException {
      if (closeCalled && whenToFail == WhenToFail.INIT_AFTER_CLOSE) {
        throw new CoreException("Well, now's a good time to fail.");
      }
      super.init();
    }

    @Override
    public void start() throws CoreException {
      if (closeCalled && whenToFail == WhenToFail.START_AFTER_CLOSE) {
        throw new CoreException("Well, now's a good time to fail.");
      }
      super.start();
    }

    @Override
    public void close() {
      if (!closeCalled) {
        closeCalled = true;
      }
      super.close();
    }
  }


  private class JmsConnectionCloseWithRuntimeException extends JmsConnection {

    private transient boolean failOnClose = true;

    public JmsConnectionCloseWithRuntimeException(JmsConnection other) throws Exception {
      super();
      configureSelf(other);
    }

    @Override
    protected void closeConnection() {
      if (failOnClose) {
        failOnClose = false;
        throw new RuntimeException();
      }
      super.closeConnection();
    }

    private void configureSelf(JmsConnection other) throws Exception {
      String[] getterMethods = ObjectUtils.filterGetterWithNoSetter(JmsConnection.class,
          ObjectUtils.getGetters(JmsConnection.class));
      String[] setterMethods = asSetters(getterMethods);
      for (int i = 0; i < getterMethods.length; i++) {
        invokeSetter(this, JmsConnection.class, setterMethods[i], getterMethods[i],
            ObjectUtils.invokeGetter(other, getterMethods[i]));
      }
    }
  }

}
