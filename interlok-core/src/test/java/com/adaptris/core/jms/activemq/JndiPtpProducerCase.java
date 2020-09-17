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
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import com.adaptris.core.ProduceException;
import com.adaptris.core.StandaloneConsumer;
import com.adaptris.core.StandaloneProducer;
import com.adaptris.core.jms.PtpConsumer;
import com.adaptris.core.jms.PtpProducer;
import com.adaptris.core.jms.jndi.SimpleFactoryConfiguration;
import com.adaptris.core.jms.jndi.StandardJndiImplementation;
import com.adaptris.core.stubs.MockMessageListener;
import com.adaptris.util.KeyValuePair;
import com.adaptris.util.KeyValuePairSet;

public abstract class JndiPtpProducerCase {

  @Rule
  public TestName testName = new TestName();

  protected abstract StandardJndiImplementation createVendorImplementation();

  @Test
  public void testProduceAndConsume() throws Exception {

    EmbeddedActiveMq activeMqBroker = new EmbeddedActiveMq();
    StandardJndiImplementation recvVendorImp = createVendorImplementation();
    StandardJndiImplementation sendVendorImp = createVendorImplementation();
    String queueName = testName.getMethodName() + "_queue";
    String topicName = testName.getMethodName() + "_topic";
    StandaloneConsumer standaloneConsumer = new StandaloneConsumer(activeMqBroker.getJndiPtpConnection(recvVendorImp, false,
            queueName, topicName), new PtpConsumer().withQueue((queueName)));
    MockMessageListener jms = new MockMessageListener();
    standaloneConsumer.registerAdaptrisMessageListener(jms);
    StandaloneProducer standaloneProducer = new StandaloneProducer(activeMqBroker.getJndiPtpConnection(sendVendorImp, false,
            queueName, topicName), new PtpProducer().withQueue((queueName)));
    try {
      activeMqBroker.start();
      execute(standaloneConsumer, standaloneProducer, createMessage(null), jms);
      assertMessages(jms);
    }
    finally {
      activeMqBroker.destroy();
    }
  }

  @Test
  public void testProduceAndConsume_ExtraConfig() throws Exception {

    SimpleFactoryConfiguration sfc = new SimpleFactoryConfiguration();
    KeyValuePairSet kvps = new KeyValuePairSet();
    kvps.add(new KeyValuePair("ClientID", "testProduceAndConsume_ExtraConfig"));
    kvps.add(new KeyValuePair("UseCompression", "true"));
    sfc.setProperties(kvps);
    String queueName = testName.getMethodName() + "_queue";
    String topicName = testName.getMethodName() + "_topic";
    EmbeddedActiveMq activeMqBroker = new EmbeddedActiveMq();
    StandardJndiImplementation recvVendorImp = createVendorImplementation();
    StandardJndiImplementation sendVendorImp = createVendorImplementation();
    sendVendorImp.setExtraFactoryConfiguration(sfc);
    StandaloneConsumer standaloneConsumer = new StandaloneConsumer(activeMqBroker.getJndiPtpConnection(recvVendorImp, false,
            queueName, topicName), new PtpConsumer().withQueue(queueName));
    MockMessageListener jms = new MockMessageListener();
    standaloneConsumer.registerAdaptrisMessageListener(jms);
    StandaloneProducer standaloneProducer = new StandaloneProducer(activeMqBroker.getJndiPtpConnection(sendVendorImp, false,
            queueName, topicName), new PtpProducer().withQueue((queueName)));
    try {
      activeMqBroker.start();
      execute(standaloneConsumer, standaloneProducer, createMessage(null), jms);
      assertMessages(jms);
    }
    finally {
      activeMqBroker.destroy();
    }
  }

  @Test
  public void testProduceAndConsumeUsingJndiOnly() throws Exception {

    EmbeddedActiveMq activeMqBroker = new EmbeddedActiveMq();
    StandardJndiImplementation recvVendorImp = createVendorImplementation();
    StandardJndiImplementation sendVendorImp = createVendorImplementation();
    String queueName = testName.getMethodName() + "_queue";
    String topicName = testName.getMethodName() + "_topic";

    PtpConsumer consumer = new PtpConsumer().withQueue(queueName);
    StandaloneConsumer standaloneConsumer = new StandaloneConsumer(activeMqBroker.getJndiPtpConnection(recvVendorImp, true,
            queueName, topicName), new PtpConsumer().withQueue(queueName));
    MockMessageListener jms = new MockMessageListener();
    standaloneConsumer.registerAdaptrisMessageListener(jms);
    StandaloneProducer standaloneProducer = new StandaloneProducer(activeMqBroker.getJndiPtpConnection(sendVendorImp, true,
            queueName, topicName), new PtpProducer().withQueue((queueName)));
    try {
      activeMqBroker.start();
      execute(standaloneConsumer, standaloneProducer, createMessage(null), jms);
      assertMessages(jms);
    }
    finally {
      activeMqBroker.destroy();

    }
  }

  @Test
  public void testProduceJndiOnlyObjectNotFound() throws Exception {

    EmbeddedActiveMq activeMqBroker = new EmbeddedActiveMq();
    String queueName = testName.getMethodName() + "_queue";
    String topicName = testName.getMethodName() + "_topic";
    StandardJndiImplementation sendVendorImp = createVendorImplementation();
    StandaloneProducer standaloneProducer = new StandaloneProducer(activeMqBroker.getJndiPtpConnection(sendVendorImp, true,
            queueName, topicName), new PtpProducer().withQueue((this.getClass().getSimpleName())));
    try {
      activeMqBroker.start();
      start(standaloneProducer);
      standaloneProducer.produce(createMessage(null));
      fail("Expected ProduceException");
    }
    catch (ProduceException expected) {
    }
    finally {
      stop(standaloneProducer);
      activeMqBroker.destroy();
    }
  }
}
