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

import static com.adaptris.core.BaseCase.start;
import static com.adaptris.core.BaseCase.waitForMessages;
import static com.adaptris.core.jms.JmsProducerCase.assertMessages;
import static com.adaptris.core.jms.JmsProducerCase.createMessage;
import static com.adaptris.core.jms.activemq.ActiveMqPasPollingConsumerTest.shutdownQuietly;
import static com.adaptris.core.jms.activemq.ActiveMqPasPollingConsumerTest.startAndStop;
import java.util.concurrent.TimeUnit;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import com.adaptris.core.FixedIntervalPoller;
import com.adaptris.core.Poller;
import com.adaptris.core.StandaloneConsumer;
import com.adaptris.core.StandaloneProducer;
import com.adaptris.core.jms.JmsConnection;
import com.adaptris.core.jms.JmsPollingConsumer;
import com.adaptris.core.jms.JmsProducer;
import com.adaptris.core.jms.activemq.ActiveMqPasPollingConsumerTest.Sometime;
import com.adaptris.core.stubs.MockMessageListener;
import com.adaptris.core.util.LifecycleHelper;
import com.adaptris.core.util.ManagedThreadFactory;
import com.adaptris.util.TimeInterval;

public class ActiveMqJmsPollingConsumerTest {

  @Rule
  public TestName testName = new TestName();

  private static final ManagedThreadFactory MY_THREAD_FACTORY = new ManagedThreadFactory();

  @Test
  public void testTopic_NoSubscriptionId() throws Exception {
    String rfc6167 = "jms:topic:" + testName.getMethodName();
    final EmbeddedActiveMq broker = new EmbeddedActiveMq();
    final StandaloneConsumer consumer =
        createStandaloneConsumer(broker, testName.getMethodName(), rfc6167);
    try {
      broker.start();
      consumer.registerAdaptrisMessageListener(new MockMessageListener());
      // This won't fail, but... there will be errors in the log file...
      start(consumer);
    } finally {
      shutdownQuietly(null, consumer, broker);
    }
  }

  @Test
  public void testQueue_ProduceConsume() throws Exception {
    int msgCount = 5;
    String rfc6167 = "jms:queue:" + testName.getMethodName();
    final EmbeddedActiveMq broker = new EmbeddedActiveMq();
    final StandaloneProducer sender =
        new StandaloneProducer(broker.getJmsConnection(), new JmsProducer().withEndpoint(rfc6167));
    final StandaloneConsumer receiver =
        createStandaloneConsumer(broker, testName.getMethodName(), rfc6167);
    try {
      broker.start();
      MockMessageListener jms = new MockMessageListener();
      receiver.registerAdaptrisMessageListener(jms);
      // INTERLOK-3329 For coverage so the prepare() warning is executed 2x
      LifecycleHelper.prepare(sender);
      LifecycleHelper.prepare(receiver);
      start(receiver);
      start(sender);
      for (int i = 0; i < msgCount; i++) {
        sender.doService(createMessage());
      }
      long totalWaitTime = 0;
      waitForMessages(jms, msgCount);
      assertMessages(jms, msgCount);
    } finally {
      shutdownQuietly(sender, receiver, broker);
    }
  }

  @Test
  public void testTopic_ProduceConsume() throws Exception {
    int msgCount = 5;
    String rfc6167 =
        "jms:topic:" + testName.getMethodName() + "?subscriptionId=" + testName.getMethodName();
    final EmbeddedActiveMq broker = new EmbeddedActiveMq();
    final StandaloneProducer sender =
        new StandaloneProducer(broker.getJmsConnection(), new JmsProducer().withEndpoint(rfc6167));
    Sometime poller = new Sometime();
    JmsPollingConsumer consumer = createConsumer(broker, testName.getMethodName(), rfc6167, poller);
    final StandaloneConsumer receiver = new StandaloneConsumer(consumer);
    try {
      broker.start();
      MockMessageListener jms = new MockMessageListener();
      receiver.registerAdaptrisMessageListener(jms);

      startAndStop(receiver);
      start(receiver);
      start(sender);
      for (int i = 0; i < msgCount; i++) {
        sender.doService(createMessage());
      }
      long totalWaitTime = 0;
      waitForMessages(jms, msgCount);
      assertMessages(jms, msgCount);
    } finally {
      shutdownQuietly(sender, receiver, broker);
    }
  }



  private StandaloneConsumer createStandaloneConsumer(EmbeddedActiveMq broker, String threadName, String destinationName)
      throws Exception {
    return new StandaloneConsumer(createConsumer(broker, threadName, destinationName));
  }

  private JmsPollingConsumer createConsumer(EmbeddedActiveMq broker, String threadName, String destinationName, Poller poller) {
    JmsPollingConsumer consumer = new JmsPollingConsumer().withEndpoint(destinationName);
    consumer.setPoller(poller);
    JmsConnection c = broker.getJmsConnection();
    consumer.setClientId(c.configuredClientId());
    consumer.setUserName(c.configuredUserName());
    consumer.setPassword(c.configuredPassword());
    consumer.setReacquireLockBetweenMessages(true);
    consumer.setVendorImplementation(c.getVendorImplementation());
    return consumer;
  }


  private JmsPollingConsumer createConsumer(EmbeddedActiveMq broker, String threadName, String destinationName) {
    return createConsumer(broker, threadName, destinationName, new FixedIntervalPoller(
        new TimeInterval(500L, TimeUnit.MILLISECONDS)));
  }

}
