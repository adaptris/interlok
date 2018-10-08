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

import static com.adaptris.core.jms.JmsProducerCase.assertMessages;
import static com.adaptris.core.jms.JmsProducerCase.createMessage;
import static com.adaptris.core.jms.activemq.ActiveMqPasPollingConsumerTest.shutdownQuietly;

import java.util.concurrent.TimeUnit;

import com.adaptris.core.BaseCase;
import com.adaptris.core.ConfiguredConsumeDestination;
import com.adaptris.core.ConfiguredProduceDestination;
import com.adaptris.core.FixedIntervalPoller;
import com.adaptris.core.StandaloneConsumer;
import com.adaptris.core.StandaloneProducer;
import com.adaptris.core.jms.JmsConnection;
import com.adaptris.core.jms.PtpPollingConsumer;
import com.adaptris.core.jms.PtpProducer;
import com.adaptris.core.stubs.MockMessageListener;
import com.adaptris.core.util.ManagedThreadFactory;
import com.adaptris.util.TimeInterval;

public class ActiveMqPtpPollingConsumerTest extends BaseCase {
  private static final ManagedThreadFactory MY_THREAD_FACTORY = new ManagedThreadFactory();

  public ActiveMqPtpPollingConsumerTest(String arg0) {
    super(arg0);
  }

  @Override
  protected void setUp() throws Exception {
  }

  @Override
  protected void tearDown() throws Exception {
  }

  public void testProduceConsume() throws Exception {
    int msgCount = 5;
    final EmbeddedActiveMq broker = new EmbeddedActiveMq();
    final StandaloneProducer sender = new StandaloneProducer(broker.getJmsConnection(), new PtpProducer(
        new ConfiguredProduceDestination(getName())));
    final StandaloneConsumer receiver = createConsumer(broker, "testProduceConsume", getName());
    try {
      broker.start();
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
      shutdownQuietly(sender, receiver, broker);
    }
  }

  private StandaloneConsumer createConsumer(EmbeddedActiveMq broker, String threadName, String destinationName) throws Exception {
    PtpPollingConsumer consumer = new PtpPollingConsumer(new ConfiguredConsumeDestination(destinationName, null, threadName));
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
