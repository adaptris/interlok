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

import static com.adaptris.core.jms.activemq.ActiveMqPasPollingConsumerTest.shutdownQuietly;
import static com.adaptris.interlok.junit.scaffolding.BaseCase.start;
import static com.adaptris.interlok.junit.scaffolding.BaseCase.waitForMessages;
import static com.adaptris.interlok.junit.scaffolding.jms.JmsProducerCase.assertMessages;
import static com.adaptris.interlok.junit.scaffolding.jms.JmsProducerCase.createMessage;

import java.util.concurrent.TimeUnit;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

import com.adaptris.core.FixedIntervalPoller;
import com.adaptris.core.StandaloneConsumer;
import com.adaptris.core.StandaloneProducer;
import com.adaptris.core.jms.JmsConnection;
import com.adaptris.core.jms.PtpPollingConsumer;
import com.adaptris.core.jms.PtpProducer;
import com.adaptris.core.stubs.MockMessageListener;
import com.adaptris.util.TimeInterval;

public class ActiveMqPtpPollingConsumerTest {
  
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
  public void testProduceConsume() throws Exception {

    int msgCount = 5;
    final StandaloneProducer sender = new StandaloneProducer(activeMqBroker.getJmsConnection(),
        new PtpProducer().withQueue((testName.getMethodName())));
    final StandaloneConsumer receiver =
        createConsumer(activeMqBroker, "testProduceConsume", testName.getMethodName());
    try {
      MockMessageListener jms = new MockMessageListener();
      receiver.registerAdaptrisMessageListener(jms);
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

  private StandaloneConsumer createConsumer(EmbeddedActiveMq broker, String threadName, String destinationName) throws Exception {
    PtpPollingConsumer consumer = new PtpPollingConsumer().withQueue(destinationName);
    consumer.setPoller(new FixedIntervalPoller(new TimeInterval(500L, TimeUnit.MILLISECONDS)));
    JmsConnection c = broker.getJmsConnection();
    consumer.setClientId(c.configuredClientId());
    consumer.setUserName(c.configuredUserName());
    consumer.setPassword(c.configuredPassword());
    consumer.setReacquireLockBetweenMessages(true);
    consumer.setVendorImplementation(c.getVendorImplementation());
    StandaloneConsumer sc = new StandaloneConsumer(consumer);
    return sc;
  }
}
