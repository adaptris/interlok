package com.adaptris.core.jms.activemq;

import java.util.concurrent.TimeUnit;

import com.adaptris.core.Adapter;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.BaseCase;
import com.adaptris.core.ClosedState;
import com.adaptris.core.ComponentState;
import com.adaptris.core.ConfiguredConsumeDestination;
import com.adaptris.core.ConfiguredProduceDestination;
import com.adaptris.core.ServiceCase;
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
import com.adaptris.core.stubs.LicenseStub;
import com.adaptris.core.stubs.MockChannel;
import com.adaptris.core.stubs.MockMessageProducer;
import com.adaptris.util.TimeInterval;

public class JmsConnectionErrorHandlerTest extends BaseCase {

  public JmsConnectionErrorHandlerTest(String name) {
    super(name);
  }

  @Override
  protected void setUp() throws Exception {
  }

  @Override
  protected void tearDown() throws Exception {
  }

  public void testConnectionErrorHandler() throws Exception {
    EmbeddedActiveMq activeMqBroker = new EmbeddedActiveMq();
    MockChannel channel = createChannel(activeMqBroker, activeMqBroker.getJmsConnection(new BasicActiveMqImplementation(), true),
        getName());
    try {
      activeMqBroker.start();
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
      activeMqBroker.destroy();
    }
  }

  public void testConnectionErrorHandlerWithUnamedChannel() throws Exception {
    EmbeddedActiveMq activeMqBroker = new EmbeddedActiveMq();
    MockChannel channel = createChannel(null, activeMqBroker,
        activeMqBroker.getJmsConnection(new BasicActiveMqImplementation(), true), getName());
    try {
      activeMqBroker.start();
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
      activeMqBroker.destroy();
    }
  }

  public void testConnectionErrorHandlerWithJndi() throws Exception {
    EmbeddedActiveMq activeMqBroker = new EmbeddedActiveMq();
    String queueName = getName() + "_queue";
    String topicName = getName() + "_topic";
    MockChannel channel = createChannel(activeMqBroker,
        activeMqBroker.getJndiPasConnection(new StandardJndiImplementation(), false, queueName, topicName), topicName);
    try {
      activeMqBroker.start();
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
      activeMqBroker.destroy();
    }
  }

  public void testBug1926() throws Exception {
    EmbeddedActiveMq activeMqBroker = new EmbeddedActiveMq();
    MockChannel channel = createChannel(activeMqBroker, activeMqBroker.getJmsConnection(new BasicActiveMqImplementation(), true),
        getName());
    try {
      activeMqBroker.start();
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
      activeMqBroker.destroy();
    }
  }

  public void testRestartSharedConnection() throws Exception {
    EmbeddedActiveMq activeMqBroker = new EmbeddedActiveMq();
    Adapter adapter = new Adapter();
    adapter.setUniqueId(getName());
    JmsConnection connection = activeMqBroker.getJmsConnection(new BasicActiveMqImplementation(), true);
    connection.setConnectionErrorHandler(new JmsConnectionErrorHandler());
    connection.setUniqueId(getName());
    adapter.getSharedComponents().addConnection(connection);
    MockChannel channel = createChannel(activeMqBroker, new SharedConnection(getName()), getName());
    MockMessageProducer producer = (MockMessageProducer) channel.getWorkflowList().get(0).getProducer();
    adapter.getChannelList().add(channel);
    adapter.registerLicense(new LicenseStub());
    try {
      activeMqBroker.start();
      adapter.requestStart();
      assertEquals(StartedState.getInstance(), channel.retrieveComponentState());
      // Now try and send a message
      ServiceCase.execute(createProducer(activeMqBroker, getName()), AdaptrisMessageFactory.getDefaultInstance().newMessage("ABC"));
      waitForMessages(producer, 1);

      activeMqBroker.stop();
      log.trace(getName() + ": Waiting for channel death (i.e. !StartedState)");
      long totalWaitTime = waitForChannelToChangeState(StartedState.getInstance(), channel);
      log.trace(getName() + ": Channel appears to be not started now, and I waited for " + totalWaitTime);

      activeMqBroker.start();

      totalWaitTime = waitForChannelToMatchState(StartedState.getInstance(), channel);
      log.trace(getName() + ": Channel now started now, and I waited for " + totalWaitTime);

      waitForChannelToMatchState(StartedState.getInstance(), channel);
      assertEquals(StartedState.getInstance(), channel.retrieveComponentState());

      // Now try and send a message
      ServiceCase.execute(createProducer(activeMqBroker, getName()), AdaptrisMessageFactory.getDefaultInstance().newMessage("ABC"));
      waitForMessages(producer, 2);

    }
    finally {
      adapter.requestClose();
      activeMqBroker.destroy();
    }
  }

  public void testRestartSharedConnection_ChannelNotStarted() throws Exception {
    EmbeddedActiveMq activeMqBroker = new EmbeddedActiveMq();
    Adapter adapter = new Adapter();
    adapter.setUniqueId(getName());
    JmsConnection connection = activeMqBroker.getJmsConnection(new BasicActiveMqImplementation(), true);
    connection.setConnectionErrorHandler(new JmsConnectionErrorHandler());
    connection.setUniqueId(getName());
    adapter.getSharedComponents().addConnection(connection);
    MockChannel started = createChannel(activeMqBroker, new SharedConnection(getName()), getName());
    MockChannel neverStarted = createChannel(activeMqBroker, new SharedConnection(getName()), getName() + "_2");
    neverStarted.setAutoStart(false);
    MockMessageProducer producer = (MockMessageProducer) started.getWorkflowList().get(0).getProducer();
    adapter.getChannelList().add(started);
    adapter.getChannelList().add(neverStarted);
    adapter.registerLicense(new LicenseStub());
    try {
      activeMqBroker.start();
      adapter.requestStart();
      assertEquals(StartedState.getInstance(), started.retrieveComponentState());
      assertEquals(ClosedState.getInstance(), neverStarted.retrieveComponentState());
      // Now try and send a message
      ServiceCase.execute(createProducer(activeMqBroker, getName()), AdaptrisMessageFactory.getDefaultInstance().newMessage("ABC"));
      waitForMessages(producer, 1);

      activeMqBroker.stop();
      log.trace(getName() + ": Waiting for channel death (i.e. !StartedState)");
      long totalWaitTime = waitForChannelToChangeState(StartedState.getInstance(), started);
      log.trace(getName() + ": Channel appears to be not started now, and I waited for " + totalWaitTime);

      activeMqBroker.start();

      totalWaitTime = waitForChannelToMatchState(StartedState.getInstance(), started);
      log.trace(getName() + ": Channel now started now, and I waited for " + totalWaitTime);
      assertEquals(StartedState.getInstance(), started.retrieveComponentState());


      // Now try and send a message
      ServiceCase.execute(createProducer(activeMqBroker, getName()), AdaptrisMessageFactory.getDefaultInstance().newMessage("ABC"));
      waitForMessages(producer, 2);
      assertEquals(ClosedState.getInstance(), neverStarted.retrieveComponentState());
      assertEquals(0, neverStarted.getInitCount());
    }
    finally {
      adapter.requestClose();
      activeMqBroker.destroy();
    }
  }

  private MockChannel createChannel(EmbeddedActiveMq mq, JmsConnection con, String destinationName) throws Exception {
    return createChannel(mq.getName() + "_channel", mq, con, destinationName);
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
    MockChannel result = new MockChannel();
    result.setUniqueId(channelName);
    con.setConnectionRetryInterval(new TimeInterval(1L, TimeUnit.SECONDS.name()));
    con.setConnectionAttempts(50);
    con.setConnectionErrorHandler(new JmsConnectionErrorHandler());
    result.setConsumeConnection(con);
    Workflow workflow = createWorkflow(mq, destinationName);
    result.getWorkflowList().add(workflow);
    return result;
  }

  private Workflow createWorkflow(EmbeddedActiveMq mq, String destination) throws Exception {
    StandardWorkflow wf = new StandardWorkflow();
    PasConsumer consumer = new PasConsumer(new ConfiguredConsumeDestination(destination));
    wf.setProducer(new MockMessageProducer());
    wf.setConsumer(consumer);
    return wf;
  }

  private long waitForChannelToChangeState(ComponentState state, MockChannel channel) throws Exception {
    long totalWaitTime = 0;
    while (channel.retrieveComponentState().equals(state) && totalWaitTime < MAX_WAIT) {
      totalWaitTime += 1000;
      Thread.sleep(1000);
    }
    return totalWaitTime;
  }

  protected long waitForChannelToMatchState(ComponentState state, MockChannel channel) throws Exception {
    long totalWaitTime = 0;
    while (!channel.retrieveComponentState().equals(state) && totalWaitTime < MAX_WAIT) {
      totalWaitTime += 1000;
      Thread.sleep(1000);
    }
    return totalWaitTime;
  }

  private StandaloneProducer createProducer(EmbeddedActiveMq mq, String dest) {
    JmsConnection conn = mq.getJmsConnection(new BasicActiveMqImplementation(), true);
    conn.setConnectionErrorHandler(new JmsConnectionErrorHandler());
    PasProducer producer = new PasProducer(new ConfiguredProduceDestination(dest));
    return new StandaloneProducer(conn, producer);
  }
}
