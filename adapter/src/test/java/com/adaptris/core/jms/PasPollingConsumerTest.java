/*
 * $RCSfile: PasPollingConsumerTest.java,v $
 * $Revision: 1.14 $
 * $Date: 2009/02/17 14:51:16 $
 * $Author: lchan $
 */
package com.adaptris.core.jms;

import java.util.concurrent.TimeUnit;

import com.adaptris.core.ConfiguredConsumeDestination;
import com.adaptris.core.FixedIntervalPoller;
import com.adaptris.core.StandaloneConsumer;
import com.adaptris.util.TimeInterval;

public class PasPollingConsumerTest extends PollingJmsConsumerCase {

  public PasPollingConsumerTest(String arg0) {
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
    PasPollingConsumer consumer = createConsumer();
    consumer.setPoller(new FixedIntervalPoller(new TimeInterval(1L, TimeUnit.MINUTES)));
    consumer.setUserName("user-name");
    consumer.setPassword("password");
    consumer.setClientId("client-id");
    consumer.setSubscriptionId("subscription-id");
    consumer.setPoller(new FixedIntervalPoller(new TimeInterval(1L, TimeUnit.MINUTES)));
    consumer.setReacquireLockBetweenMessages(true);
    consumer.setDestination(new ConfiguredConsumeDestination("MyTopic"));
    StandaloneConsumer result = new StandaloneConsumer();
    result.setConsumer(consumer);

    return result;
  }

  @Override
  protected PasPollingConsumer createConsumer() {
    return new PasPollingConsumer();
  }

}
