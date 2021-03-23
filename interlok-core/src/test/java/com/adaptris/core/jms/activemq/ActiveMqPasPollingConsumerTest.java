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

import static com.adaptris.interlok.junit.scaffolding.BaseCase.start;
import static com.adaptris.interlok.junit.scaffolding.BaseCase.stop;
import static com.adaptris.interlok.junit.scaffolding.BaseCase.waitForMessages;
import static com.adaptris.interlok.junit.scaffolding.jms.JmsProducerCase.assertMessages;
import static com.adaptris.interlok.junit.scaffolding.jms.JmsProducerCase.createMessage;
import static org.junit.Assert.fail;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import com.adaptris.core.AdaptrisPollingConsumer;
import com.adaptris.core.CoreException;
import com.adaptris.core.RandomIntervalPoller;
import com.adaptris.core.StandaloneConsumer;
import com.adaptris.core.StandaloneProducer;
import com.adaptris.core.StandardWorkflow;
import com.adaptris.core.jms.JmsConnection;
import com.adaptris.core.jms.PasPollingConsumer;
import com.adaptris.core.jms.PasProducer;
import com.adaptris.core.stubs.MockMessageListener;
import com.adaptris.core.util.LifecycleHelper;
import com.adaptris.core.util.ManagedThreadFactory;
import com.adaptris.util.SafeGuidGenerator;
import com.adaptris.util.TimeInterval;

public class ActiveMqPasPollingConsumerTest {

  private static final String MY_SUBSCRIPTION_ID = new SafeGuidGenerator().safeUUID();
  private static final ManagedThreadFactory MY_THREAD_FACTORY = new ManagedThreadFactory();

  @Rule
  public TestName testName = new TestName();
  
  private static EmbeddedActiveMq activeMqBroker;

  @BeforeClass
  public static void setUpAll() throws Exception {
    activeMqBroker = new EmbeddedActiveMq();
    activeMqBroker.start();
  }
  
  @AfterClass
  public static void tearDownAll() throws Exception {
    if(activeMqBroker != null)
      activeMqBroker.destroy();
  }

  @Test
  public void testInitNoClientIdOrSubscriptionId() throws Exception {
    PasPollingConsumer consumer = create();
    try {
      consumer.init();
      fail("Should fail due to missing subscription Id, client Id");
    }
    catch (CoreException e) {
      // expected
    }
  }

  @Test
  public void testInitClientOnly() throws Exception {
    PasPollingConsumer consumer = create();
    consumer.setClientId("xxxx");

    try {
      consumer.init();
      fail("Should fail due to missing subscription Id");
    }
    catch (CoreException e) {
      // expected
    }
  }

  @Test
  public void testInitSubscriptionOnly() throws Exception {
    PasPollingConsumer consumer = create();
    consumer.setSubscriptionId("xxxx");

    try {
      consumer.init();
      fail("Should fail due to missing client Id");
    }
    catch (CoreException e) {
      // expected
    }
  }

  @Test
  public void testInitClientAndSubscriptionSet() throws Exception {
    PasPollingConsumer consumer = create();
    consumer.setClientId("xxxx");
    consumer.setSubscriptionId("xxxx");

    try {
      consumer.init();
    }
    catch (CoreException e) {
      fail(e.getMessage());
    }
  }

  @Test
  public void testProduceConsume() throws Exception {

    int msgCount = 5;
    final StandaloneProducer sender = new StandaloneProducer(activeMqBroker.getJmsConnection(),
        new PasProducer().withTopic(testName.getMethodName()));
    final StandaloneConsumer receiver =
        createStandalone(activeMqBroker, "testProduceConsume", testName.getMethodName());
    try {
      MockMessageListener jms = new MockMessageListener();
      receiver.registerAdaptrisMessageListener(jms);
      startAndStop(receiver);
      start(receiver);
      start(sender);
      for (int i = 0; i < msgCount; i++) {
        sender.doService(createMessage());
      }
      waitForMessages(jms, msgCount);
      assertMessages(jms, msgCount);
    }
    finally {
      shutdownQuietly(sender, receiver);
    }
  }

  static void startAndStop(StandaloneConsumer c) throws Exception {
    LifecycleHelper.init(c);
    LifecycleHelper.start(c);
    while (!((Sometime) ((AdaptrisPollingConsumer) c.getConsumer()).getPoller()).hasTriggered()) {
      Thread.sleep(100);
    }
    LifecycleHelper.stop(c);
    LifecycleHelper.close(c);
  }

  private StandaloneConsumer createStandalone(EmbeddedActiveMq mq, String threadName, String destinationName) throws Exception {
    PasPollingConsumer consumer = new PasPollingConsumer().withTopic(destinationName);
    consumer.setPoller(new Sometime());
    JmsConnection c = mq.getJmsConnection();
    consumer.setVendorImplementation(c.getVendorImplementation());
    consumer.setClientId(c.getClientId());
    consumer.setSubscriptionId(MY_SUBSCRIPTION_ID);
    consumer.setReacquireLockBetweenMessages(true);
    consumer.setAdditionalDebug(true);
    consumer.setReceiveTimeout(new TimeInterval(Integer.valueOf(new Random().nextInt(100) + 100).longValue(), TimeUnit.MILLISECONDS));
    StandaloneConsumer sc = new StandaloneConsumer(consumer);
    return sc;
  }

  private static PasPollingConsumer create() {
    PasPollingConsumer consumer = new PasPollingConsumer().withTopic("destination");
    consumer.setVendorImplementation(new BasicActiveMqImplementation());
    consumer.registerAdaptrisMessageListener(new StandardWorkflow());
    return consumer;
  }

  static class Sometime extends RandomIntervalPoller {
    private boolean hasTriggered = false;

    public Sometime() {
      super(new TimeInterval(1L, TimeUnit.SECONDS));
    }

    @Override
    protected void scheduleTask() {
      if (executor != null && !executor.isShutdown()) {
        long delay = ThreadLocalRandom.current().nextLong(1000);
        pollerTask = executor.schedule(new MyPollerTask(), delay, TimeUnit.MILLISECONDS);
      }
    }

    boolean hasTriggered() {
      return hasTriggered;
    }

    private class MyPollerTask implements Runnable {
      @Override
      public void run() {
        processMessages();
        scheduleTask();
        hasTriggered = true;
      }
    }
  }

  static void shutdownQuietly(final StandaloneProducer sender, final StandaloneConsumer receiver) {
    MY_THREAD_FACTORY.newThread(new Runnable() {
      @Override
      public void run() {
        LifecycleHelper.waitQuietly(1000);
        stop(sender);
        LifecycleHelper.waitQuietly(1000);
        stop(receiver);
        LifecycleHelper.waitQuietly(1000);
      }
    }).start();
  }
}
