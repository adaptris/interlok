package com.adaptris.core.jms;

import java.util.concurrent.TimeUnit;

import com.adaptris.core.ConfiguredConsumeDestination;
import com.adaptris.core.FixedIntervalPoller;
import com.adaptris.core.StandaloneConsumer;
import com.adaptris.util.TimeInterval;

public class JmsPollingConsumerTest extends PollingJmsConsumerCase {

  public JmsPollingConsumerTest(String arg0) {
    super(arg0);
  }

  @Override
  protected void setUp() throws Exception {
  }

  @Override
  protected void tearDown() throws Exception {
  }

  @Override
  protected Object retrieveObjectForSampleConfig() {
    JmsPollingConsumer consumer = createConsumer();
    consumer.setPoller(new FixedIntervalPoller(new TimeInterval(1L, TimeUnit.MINUTES)));
    consumer.setUserName("user-name");
    consumer.setPassword("password");
    consumer.setClientId("client-id");
    consumer.setReacquireLockBetweenMessages(true);
    consumer.setDestination(new ConfiguredConsumeDestination("jms:topic:MyTopicName?subscriptionId=mySubscriptionId"));
    StandaloneConsumer result = new StandaloneConsumer();
    result.setConsumer(consumer);

    return result;
  }

  @Override
  protected JmsPollingConsumer createConsumer() {
    return new JmsPollingConsumer();
  }

}
