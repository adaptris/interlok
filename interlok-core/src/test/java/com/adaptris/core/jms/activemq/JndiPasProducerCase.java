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

import static com.adaptris.core.BaseCase.execute;
import static com.adaptris.core.BaseCase.start;
import static com.adaptris.core.BaseCase.stop;
import static com.adaptris.core.jms.JmsProducerCase.assertMessages;
import static com.adaptris.core.jms.activemq.EmbeddedActiveMq.createMessage;
import static org.junit.Assert.fail;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import com.adaptris.core.ProduceException;
import com.adaptris.core.StandaloneConsumer;
import com.adaptris.core.StandaloneProducer;
import com.adaptris.core.jms.PasConsumer;
import com.adaptris.core.jms.PasProducer;
import com.adaptris.core.jms.jndi.SimpleFactoryConfiguration;
import com.adaptris.core.jms.jndi.StandardJndiImplementation;
import com.adaptris.core.stubs.MockMessageListener;
import com.adaptris.util.KeyValuePair;
import com.adaptris.util.KeyValuePairSet;

public abstract class JndiPasProducerCase {

  @Rule
  public TestName testName = new TestName();

  protected abstract StandardJndiImplementation createVendorImplementation();

  protected static EmbeddedActiveMq activeMqBroker;

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
  public void testProduceAndConsume() throws Exception {
    StandardJndiImplementation recvVendorImp = createVendorImplementation();
    StandardJndiImplementation sendVendorImp = createVendorImplementation();
    String queueName = testName.getMethodName() + "_queue";
    String topicName = testName.getMethodName() + "_topic";

    StandaloneConsumer standaloneConsumer = new StandaloneConsumer(activeMqBroker.getJndiPasConnection(recvVendorImp, false, queueName,
            topicName), new PasConsumer().withTopic(topicName));
    MockMessageListener jms = new MockMessageListener();

    standaloneConsumer.registerAdaptrisMessageListener(jms);
    StandaloneProducer standaloneProducer = new StandaloneProducer(activeMqBroker.getJndiPasConnection(sendVendorImp, false, queueName,
            topicName), new PasProducer().withTopic(topicName));

    execute(standaloneConsumer, standaloneProducer, createMessage(null), jms);
    assertMessages(jms, 1);
  }

  @Test
  public void testProduceAndConsume_ExtraConfig() throws Exception {
    String queueName = testName.getMethodName() + "_queue";
    String topicName = testName.getMethodName() + "_topic";
    SimpleFactoryConfiguration sfc = new SimpleFactoryConfiguration();
    KeyValuePairSet kvps = new KeyValuePairSet();
    kvps.add(new KeyValuePair("ClientID", "testProduceAndConsume_ExtraConfig"));
    kvps.add(new KeyValuePair("UseCompression", "true"));
    sfc.setProperties(kvps);

    StandardJndiImplementation recvVendorImp = createVendorImplementation();
    StandardJndiImplementation sendVendorImp = createVendorImplementation();
    sendVendorImp.setExtraFactoryConfiguration(sfc);

    StandaloneConsumer standaloneConsumer = new StandaloneConsumer(activeMqBroker.getJndiPasConnection(recvVendorImp, false, queueName,
        topicName),
        new PasConsumer().withTopic(topicName));
    MockMessageListener jms = new MockMessageListener();

    standaloneConsumer.registerAdaptrisMessageListener(jms);
    StandaloneProducer standaloneProducer = new StandaloneProducer(activeMqBroker.getJndiPasConnection(sendVendorImp, false, queueName,
        topicName),
        new PasProducer().withTopic(topicName));

    execute(standaloneConsumer, standaloneProducer, createMessage(null), jms);
    assertMessages(jms, 1);
  }

  @Test
  public void testProduceAndConsumeUsingJndiOnly() throws Exception {
    String queueName = testName.getMethodName() + "_queue";
    String topicName = testName.getMethodName() + "_topic";

    StandardJndiImplementation recvVendorImp = createVendorImplementation();
    StandardJndiImplementation sendVendorImp = createVendorImplementation();
    StandaloneConsumer standaloneConsumer = new StandaloneConsumer(activeMqBroker.getJndiPasConnection(recvVendorImp, true, queueName,
        topicName),
        new PasConsumer().withTopic(topicName));
    MockMessageListener jms = new MockMessageListener();
    standaloneConsumer.registerAdaptrisMessageListener(jms);
    StandaloneProducer standaloneProducer = new StandaloneProducer(activeMqBroker.getJndiPasConnection(sendVendorImp, true, queueName,
        topicName),
        new PasProducer().withTopic(topicName));

    execute(standaloneConsumer, standaloneProducer, createMessage(null), jms);
    assertMessages(jms);
  }

  @Test
  public void testProduceJndiOnlyObjectNotFound() throws Exception {
    String queueName = testName.getMethodName() + "_queue";
    String topicName = testName.getMethodName() + "_topic";

    StandardJndiImplementation sendVendorImp = createVendorImplementation();
    StandaloneProducer standaloneProducer = new StandaloneProducer(activeMqBroker.getJndiPasConnection(sendVendorImp, true, queueName,
        topicName),
        new PasProducer().withTopic(this.getClass().getSimpleName()));
    try {
      start(standaloneProducer);
      standaloneProducer.produce(createMessage(null));
      fail("Expected ProduceException");
    }
    catch (ProduceException expected) {
    }
    finally {
      stop(standaloneProducer);
    }
  }

}
