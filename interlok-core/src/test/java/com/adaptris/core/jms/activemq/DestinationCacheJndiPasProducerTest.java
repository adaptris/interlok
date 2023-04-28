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

import static com.adaptris.core.jms.activemq.EmbeddedActiveMq.createMessage;
import static com.adaptris.interlok.junit.scaffolding.BaseCase.start;
import static com.adaptris.interlok.junit.scaffolding.BaseCase.stop;
import static com.adaptris.interlok.junit.scaffolding.BaseCase.waitForMessages;
import static com.adaptris.interlok.junit.scaffolding.jms.JmsProducerCase.assertMessages;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;

import com.adaptris.core.StandaloneConsumer;
import com.adaptris.core.StandaloneProducer;
import com.adaptris.core.jms.PasConsumer;
import com.adaptris.core.jms.PasProducer;
import com.adaptris.core.jms.jndi.CachedDestinationJndiImplementation;
import com.adaptris.core.jms.jndi.StandardJndiImplementation;
import com.adaptris.core.stubs.MockMessageListener;

public class DestinationCacheJndiPasProducerTest extends JndiPasProducerCase {

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
  
  @Override
  protected CachedDestinationJndiImplementation createVendorImplementation() {
    return new CachedDestinationJndiImplementation();
  }

  @Test
  public void testProduceAndConsumeWithCache(TestInfo tInfo) throws Exception {
    StandardJndiImplementation recvVendorImp = createVendorImplementation();
    StandardJndiImplementation sendVendorImp = createVendorImplementation();
    String queueName = tInfo.getDisplayName() + "_queue";
    String topicName = tInfo.getDisplayName() + "_topic";

    StandaloneConsumer standaloneConsumer = new StandaloneConsumer(activeMqBroker.getJndiPasConnection(recvVendorImp, false, queueName,
            topicName), new PasConsumer().withTopic(topicName));
    MockMessageListener jms = new MockMessageListener();

    standaloneConsumer.registerAdaptrisMessageListener(jms);
    StandaloneProducer standaloneProducer = new StandaloneProducer(activeMqBroker.getJndiPasConnection(sendVendorImp, false, queueName,
            topicName), new PasProducer().withTopic(topicName));
    try {
      start(standaloneConsumer);
      start(standaloneProducer);
      standaloneProducer.doService(createMessage(null));
      standaloneProducer.doService(createMessage(null));
      waitForMessages(jms, 2);
      assertMessages(jms, 2);
    }
    finally {
      stop(standaloneProducer);
      stop(standaloneConsumer);
    }
  }

}
