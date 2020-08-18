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

import static com.adaptris.interlok.junit.scaffolding.BaseCase.MAX_WAIT;
import static com.adaptris.interlok.junit.scaffolding.BaseCase.waitForMessages;
import static org.junit.Assert.assertEquals;
import java.security.SecureRandom;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import org.junit.Test;
import com.adaptris.core.Adapter;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.ClosedState;
import com.adaptris.core.ComponentState;
import com.adaptris.core.SharedConnection;
import com.adaptris.core.StandaloneProducer;
import com.adaptris.core.StandardWorkflow;
import com.adaptris.core.StartedState;
import com.adaptris.core.Workflow;
import com.adaptris.core.jms.ActiveJmsConnectionErrorHandler;
import com.adaptris.core.jms.JmsConnection;
import com.adaptris.core.jms.PasConsumer;
import com.adaptris.core.jms.PasProducer;
import com.adaptris.core.jms.jndi.StandardJndiImplementation;
import com.adaptris.core.stubs.MockChannel;
import com.adaptris.core.stubs.MockMessageProducer;
import com.adaptris.interlok.junit.scaffolding.services.ExampleServiceCase;
import com.adaptris.util.TimeInterval;

public class ActiveJmsConnectionErrorHandlerTest
    extends com.adaptris.interlok.junit.scaffolding.jms.ActiveJmsConnectionErrorHandlerCase {


  private Random random = new SecureRandom();

  @Test
  public void testConnectionErrorHandler() throws Exception {
    EmbeddedActiveMq activeMqBroker = new EmbeddedActiveMq();
    String queueName = testName.getMethodName() + "_queue";
    String topicName = testName.getMethodName() + "_topic";
    MockChannel channel = createChannel(activeMqBroker, activeMqBroker.getJmsConnection(new BasicActiveMqImplementation(), true),
        topicName);
    try {
      activeMqBroker.start();
      channel.requestStart();
      assertEquals(StartedState.getInstance(), channel.retrieveComponentState());
      activeMqBroker.stop();
      log.trace("Waiting for channel death (i.e. !StartedState)");
      long totalWaitTime = waitForChannelToChangeState(StartedState.getInstance(), channel);
      log.trace("Channel appears to be not started now, and I waited for " + totalWaitTime);
      activeMqBroker.start();
      waitForChannelToMatchState(StartedState.getInstance(), channel);
      assertEquals(StartedState.getInstance(), channel.retrieveComponentState());
      assertEquals(2, channel.getStartCount());
    }
    finally {
      channel.requestClose();
      activeMqBroker.destroy();
    }
  }

  @Test
  public void testConnectionErrorHandlerWithJndi() throws Exception {
    String oldName = Thread.currentThread().getName();
    Thread.currentThread().setName(testName.getMethodName());
    EmbeddedActiveMq activeMqBroker = new EmbeddedActiveMq();
    String queueName = testName.getMethodName() + "_queue";
    String topicName = testName.getMethodName() + "_topic";
    JmsConnection conn = activeMqBroker.getJndiPasConnection(
        new MyStandardJndiImpl(testName.getMethodName()), false, queueName, topicName);
    conn.setUniqueId(testName.getMethodName());
    MockChannel channel = createChannel(activeMqBroker, conn, topicName);
    try {
      activeMqBroker.start();
      channel.requestStart();
      assertEquals(StartedState.getInstance(), channel.retrieveComponentState());
      activeMqBroker.stop();
      // Give the ErrorHandler time to check that it's stopped
      log.trace("Waiting for channel death (i.e. !StartedState)");
      long totalWaitTime = waitForChannelToChangeState(StartedState.getInstance(), channel);
      // assertNotSame(StartedState.getInstance(), channel.retrieveComponentState());
      log.trace("Channel appears to be not started now, and I waited for " + totalWaitTime);
      activeMqBroker.start();
      waitForChannelToMatchState(StartedState.getInstance(), channel);
      assertEquals(StartedState.getInstance(), channel.retrieveComponentState());
      assertEquals(2, channel.getStartCount());
    }
    finally {
      channel.requestClose();
      activeMqBroker.destroy();
      Thread.currentThread().setName(oldName);
    }
  }

  @Test
  public void testConnectionErrorHandlerWhileConnectionIsClosed() throws Exception {
    EmbeddedActiveMq activeMqBroker = new EmbeddedActiveMq();
    String queueName = testName.getMethodName() + "_queue";
    String topicName = testName.getMethodName() + "_topic";
    MockChannel channel = createChannel(activeMqBroker,
        activeMqBroker.getJndiPasConnection(new StandardJndiImplementation(), false, queueName, topicName), topicName);
    try {
      ActiveJmsConnectionErrorHandler handler = new ActiveJmsConnectionErrorHandler();
      handler.setCheckInterval(new TimeInterval(100L, TimeUnit.MILLISECONDS));
      handler.setAdditionalLogging(Boolean.TRUE);
      channel.getConsumeConnection().setConnectionErrorHandler(handler);
      activeMqBroker.start();
      channel.requestStart();
      assertEquals(StartedState.getInstance(), channel.retrieveComponentState());
      channel.requestClose();
      activeMqBroker.stop();
      activeMqBroker.start();
      assertEquals(ClosedState.getInstance(), channel.retrieveComponentState());
    }
    finally {
      channel.requestClose();
      activeMqBroker.destroy();
    }
  }

  @Test
  public void testBug1926() throws Exception {
    EmbeddedActiveMq activeMqBroker = new EmbeddedActiveMq();
    String queueName = testName.getMethodName() + "_queue";
    String topicName = testName.getMethodName() + "_topic";
    MockChannel channel = createChannel(activeMqBroker,
        activeMqBroker.getJndiPasConnection(new StandardJndiImplementation(), false, queueName, topicName), topicName);
    try {
      activeMqBroker.start();
      channel.requestStart();
      assertEquals(StartedState.getInstance(), channel.retrieveComponentState());
      activeMqBroker.stop();
      // Give the ErrorHandler time to check that it's stopped
      Thread.sleep(1000);
      activeMqBroker.start();
      waitForChannelToMatchState(StartedState.getInstance(), channel);

      assertEquals(StartedState.getInstance(), channel.retrieveComponentState());
    }
    finally {
      channel.requestClose();
      activeMqBroker.destroy();
    }
  }

  @Test
  public void testActiveRestartSharedConnection() throws Exception {
    EmbeddedActiveMq activeMqBroker = new EmbeddedActiveMq();
    Adapter adapter = new Adapter();
    adapter.setUniqueId(testName.getMethodName());
    JmsConnection connection = activeMqBroker.getJmsConnection(new BasicActiveMqImplementation(), true);
    connection.setConnectionErrorHandler(createErrorHandler());
    connection.setUniqueId(testName.getMethodName());
    connection.setConnectionRetryInterval(new TimeInterval(5L, "SECONDS"));
    adapter.getSharedComponents().addConnection(connection);
    MockChannel channel = createChannel(activeMqBroker,
        new SharedConnection(testName.getMethodName()), testName.getMethodName());
    MockMessageProducer producer = (MockMessageProducer) channel.getWorkflowList().get(0).getProducer();
    adapter.getChannelList().add(channel);
    try {
      activeMqBroker.start();
      adapter.requestStart();
      assertEquals(StartedState.getInstance(), channel.retrieveComponentState());
      // Now try and send a message
      ExampleServiceCase.execute(createProducer(activeMqBroker, testName.getMethodName()),
          AdaptrisMessageFactory.getDefaultInstance().newMessage("ABC"));
      waitForMessages(producer, 1);

      activeMqBroker.stop();
      log.trace(testName.getMethodName() + ": Waiting for channel death (i.e. !StartedState)");
      long totalWaitTime = waitForChannelToChangeState(StartedState.getInstance(), channel);
      log.trace(testName.getMethodName()
          + ": Channel appears to be not started now, and I waited for " + totalWaitTime);

      activeMqBroker.start();

      totalWaitTime = waitForChannelToMatchState(StartedState.getInstance(), channel);
      log.trace(testName.getMethodName() + ": Channel now started now, and I waited for "
          + totalWaitTime);
      assertEquals(StartedState.getInstance(), channel.retrieveComponentState());

      // Now try and send a message
      ExampleServiceCase.execute(createProducer(activeMqBroker, testName.getMethodName()),
          AdaptrisMessageFactory.getDefaultInstance().newMessage("ABC"));
      waitForMessages(producer, 2);

    }
    finally {
      adapter.requestClose();
      activeMqBroker.destroy();
    }
  }

  @Test
  public void testActiveRestartSharedConnection_ChannelNotStarted() throws Exception {
    EmbeddedActiveMq activeMqBroker = new EmbeddedActiveMq();
    Adapter adapter = new Adapter();
    adapter.setUniqueId(testName.getMethodName());
    JmsConnection connection = activeMqBroker.getJmsConnection(new BasicActiveMqImplementation(), true);
    connection.setConnectionErrorHandler(createErrorHandler());
    connection.setUniqueId(testName.getMethodName());
    connection.setConnectionRetryInterval(new TimeInterval(5L, "SECONDS"));
    adapter.getSharedComponents().addConnection(connection);
    MockChannel started = createChannel(activeMqBroker,
        new SharedConnection(testName.getMethodName()), testName.getMethodName());
    MockChannel neverStarted = createChannel(activeMqBroker,
        new SharedConnection(testName.getMethodName()), testName.getMethodName() + "_2");
    neverStarted.setAutoStart(false);
    MockMessageProducer producer = (MockMessageProducer) started.getWorkflowList().get(0).getProducer();
    adapter.getChannelList().add(started);
    adapter.getChannelList().add(neverStarted);
    try {
      activeMqBroker.start();
      adapter.requestStart();
      assertEquals(StartedState.getInstance(), started.retrieveComponentState());
      assertEquals(ClosedState.getInstance(), neverStarted.retrieveComponentState());
      // Now try and send a message
      ExampleServiceCase.execute(createProducer(activeMqBroker, testName.getMethodName()),
          AdaptrisMessageFactory.getDefaultInstance().newMessage("ABC"));
      waitForMessages(producer, 1);

      activeMqBroker.stop();
      log.trace(testName.getMethodName() + ": Waiting for channel death (i.e. !StartedState)");
      long totalWaitTime = waitForChannelToChangeState(StartedState.getInstance(), started);
      log.trace(testName.getMethodName()
          + ": Channel appears to be not started now, and I waited for " + totalWaitTime);

      activeMqBroker.start();

      totalWaitTime = waitForChannelToMatchState(StartedState.getInstance(), started);
      log.trace(testName.getMethodName() + ": Channel now started now, and I waited for "
          + totalWaitTime);
      assertEquals(StartedState.getInstance(), started.retrieveComponentState());

      // Now try and send a message
      ExampleServiceCase.execute(createProducer(activeMqBroker, testName.getMethodName()),
          AdaptrisMessageFactory.getDefaultInstance().newMessage("ABC"));
      waitForMessages(producer, 2);
      assertEquals(ClosedState.getInstance(), neverStarted.retrieveComponentState());
      assertEquals(0, neverStarted.getInitCount());
    }
    finally {
      adapter.requestClose();
      activeMqBroker.destroy();
    }
  }

  private long waitForChannelToChangeState(ComponentState state, MockChannel channel) throws Exception {
    long totalWaitTime = 0;
    while (channel.retrieveComponentState().equals(state) && totalWaitTime < MAX_WAIT) {
      long sleep = Integer.valueOf(random.nextInt(100)).longValue();
      totalWaitTime += sleep;
      Thread.sleep(sleep);
    }
    return totalWaitTime;
  }

  private long waitForChannelToMatchState(ComponentState state, MockChannel channel) throws Exception {
    long totalWaitTime = 0;
    while (!channel.retrieveComponentState().equals(state) && totalWaitTime < MAX_WAIT) {
      long sleep = Integer.valueOf(random.nextInt(100)).longValue();
      totalWaitTime += sleep;
      Thread.sleep(sleep);
    }
    return totalWaitTime;
  }

  private MockChannel createChannel(EmbeddedActiveMq mq, SharedConnection con, String destinationName) throws Exception {
    MockChannel result = new MockChannel();
    result.setUniqueId(mq.getName() + "_channel" + "_" + destinationName);
    result.setConsumeConnection(con);
    Workflow workflow = createWorkflow(mq, destinationName);
    result.getWorkflowList().add(workflow);
    return result;
  }

  private MockChannel createChannel(EmbeddedActiveMq mq, JmsConnection con, String destName) throws Exception {
    MockChannel result = new MockChannel();
    result.setUniqueId(testName.getMethodName() + "_channel");
    con.setConnectionRetryInterval(new TimeInterval(100L, TimeUnit.MILLISECONDS.name()));
    con.setConnectionAttempts(50);
    con.setConnectionErrorHandler(createErrorHandler());
    result.setConsumeConnection(con);
    Workflow workflow = createWorkflow(mq, destName);
    result.getWorkflowList().add(workflow);
    return result;
  }

  private Workflow createWorkflow(EmbeddedActiveMq mq, String destName) throws Exception {
    StandardWorkflow wf = new StandardWorkflow();
    PasConsumer consumer = new PasConsumer().withTopic(destName);
    wf.setProducer(new MockMessageProducer());
    wf.setConsumer(consumer);
    return wf;
  }

  private StandaloneProducer createProducer(EmbeddedActiveMq mq, String dest) {
    JmsConnection conn = mq.getJmsConnection(new BasicActiveMqImplementation(), true);
    conn.setConnectionErrorHandler(createErrorHandler());
    PasProducer producer = new PasProducer().withTopic(dest);
    return new StandaloneProducer(conn, producer);
  }

  private ActiveJmsConnectionErrorHandler createErrorHandler() {
    ActiveJmsConnectionErrorHandler handler = new ActiveJmsConnectionErrorHandler();
    handler.setAdditionalLogging(true);
    handler.setCheckInterval(new TimeInterval(500L, TimeUnit.MILLISECONDS));
    return handler;
  }

  private class MyStandardJndiImpl extends StandardJndiImplementation {
    private String myId;

    public MyStandardJndiImpl(String id) {
      myId = id;
    }

    /**
     * @see com.adaptris.core.jms.VendorImplementationImp #retrieveBrokerDetailsForLogging()
     */
    @Override
    public String retrieveBrokerDetailsForLogging() {
      return myId;
    }
  }
}
