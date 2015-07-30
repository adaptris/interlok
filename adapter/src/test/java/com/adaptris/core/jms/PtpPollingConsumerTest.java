package com.adaptris.core.jms;

import java.util.concurrent.TimeUnit;

import com.adaptris.core.ConfiguredConsumeDestination;
import com.adaptris.core.FixedIntervalPoller;
import com.adaptris.core.StandaloneConsumer;
import com.adaptris.util.TimeInterval;

public class PtpPollingConsumerTest extends PollingJmsConsumerCase {
  private static final long DEFAULT_WAIT = 100;

  public PtpPollingConsumerTest(String arg0) {
    super(arg0);
  }

  @Override
  protected Object retrieveObjectForSampleConfig() {
    PtpPollingConsumer consumer = new PtpPollingConsumer();
    consumer.setDestination(new ConfiguredConsumeDestination("MyQueueName"));
    consumer.setPoller(new FixedIntervalPoller(new TimeInterval(1L, TimeUnit.MINUTES)));
    consumer.setUserName("user-name");
    consumer.setPassword("password");
    consumer.setClientId("client-id");
    consumer.setReacquireLockBetweenMessages(true);

    StandaloneConsumer result = new StandaloneConsumer(consumer);

    return result;
  }

  @Override
  protected PtpPollingConsumer createConsumer() {
    return new PtpPollingConsumer();
  }
}
