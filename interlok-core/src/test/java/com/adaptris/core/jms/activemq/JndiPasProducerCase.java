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
import static com.adaptris.core.jms.activemq.EmbeddedActiveMq.createMessage;

import com.adaptris.core.BaseCase;
import com.adaptris.core.ConfiguredConsumeDestination;
import com.adaptris.core.ConfiguredProduceDestination;
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

public abstract class JndiPasProducerCase extends BaseCase {

  public JndiPasProducerCase(String name) {
    super(name);
  }

  protected abstract StandardJndiImplementation createVendorImplementation();

  public void testProduceAndConsume() throws Exception {
    EmbeddedActiveMq broker = new EmbeddedActiveMq();
    StandardJndiImplementation recvVendorImp = createVendorImplementation();
    StandardJndiImplementation sendVendorImp = createVendorImplementation();
    String queueName = getName() + "_queue";
    String topicName = getName() + "_topic";

    StandaloneConsumer standaloneConsumer = new StandaloneConsumer(broker.getJndiPasConnection(recvVendorImp, false, queueName,
        topicName), new PasConsumer(new ConfiguredConsumeDestination(topicName)));
    MockMessageListener jms = new MockMessageListener();

    standaloneConsumer.registerAdaptrisMessageListener(jms);
    StandaloneProducer standaloneProducer = new StandaloneProducer(broker.getJndiPasConnection(sendVendorImp, false, queueName,
        topicName), new PasProducer(new ConfiguredProduceDestination(topicName)));
    try {
      broker.start();
      execute(standaloneConsumer, standaloneProducer, createMessage(null), jms);
      assertMessages(jms, 1);
    }
    finally {
      broker.destroy();
    }
  }

  public void testProduceAndConsume_ExtraConfig() throws Exception {
    String queueName = getName() + "_queue";
    String topicName = getName() + "_topic";
    SimpleFactoryConfiguration sfc = new SimpleFactoryConfiguration();
    KeyValuePairSet kvps = new KeyValuePairSet();
    kvps.add(new KeyValuePair("ClientID", "testProduceAndConsume_ExtraConfig"));
    kvps.add(new KeyValuePair("UseCompression", "true"));
    sfc.setProperties(kvps);
    EmbeddedActiveMq broker = new EmbeddedActiveMq();
    StandardJndiImplementation recvVendorImp = createVendorImplementation();
    StandardJndiImplementation sendVendorImp = createVendorImplementation();
    sendVendorImp.setExtraFactoryConfiguration(sfc);

    StandaloneConsumer standaloneConsumer = new StandaloneConsumer(broker.getJndiPasConnection(recvVendorImp, false, queueName,
        topicName),
 new PasConsumer(new ConfiguredConsumeDestination(topicName)));
    MockMessageListener jms = new MockMessageListener();

    standaloneConsumer.registerAdaptrisMessageListener(jms);
    StandaloneProducer standaloneProducer = new StandaloneProducer(broker.getJndiPasConnection(sendVendorImp, false, queueName,
        topicName),
 new PasProducer(new ConfiguredProduceDestination(topicName)));
    try {
      broker.start();
      execute(standaloneConsumer, standaloneProducer, createMessage(null), jms);
      assertMessages(jms, 1);
    }
    finally {
      broker.destroy();
    }
  }

  public void testProduceAndConsumeUsingJndiOnly() throws Exception {
    String queueName = getName() + "_queue";
    String topicName = getName() + "_topic";
    EmbeddedActiveMq broker = new EmbeddedActiveMq();
    StandardJndiImplementation recvVendorImp = createVendorImplementation();
    StandardJndiImplementation sendVendorImp = createVendorImplementation();
    StandaloneConsumer standaloneConsumer = new StandaloneConsumer(broker.getJndiPasConnection(recvVendorImp, true, queueName,
        topicName),
 new PasConsumer(new ConfiguredConsumeDestination(topicName)));
    MockMessageListener jms = new MockMessageListener();
    standaloneConsumer.registerAdaptrisMessageListener(jms);
    StandaloneProducer standaloneProducer = new StandaloneProducer(broker.getJndiPasConnection(sendVendorImp, true, queueName,
        topicName),
 new PasProducer(new ConfiguredProduceDestination(topicName)));
    try {
      broker.start();
      execute(standaloneConsumer, standaloneProducer, createMessage(null), jms);
      assertMessages(jms);
    }
    finally {
      broker.destroy();
    }

  }

  public void testProduceJndiOnlyObjectNotFound() throws Exception {
    String queueName = getName() + "_queue";
    String topicName = getName() + "_topic";
    EmbeddedActiveMq broker = new EmbeddedActiveMq();
    StandardJndiImplementation sendVendorImp = createVendorImplementation();
    StandaloneProducer standaloneProducer = new StandaloneProducer(broker.getJndiPasConnection(sendVendorImp, true, queueName,
        topicName),
        new PasProducer(new ConfiguredProduceDestination(this.getClass().getSimpleName())));
    try {
      broker.start();
      start(standaloneProducer);
      standaloneProducer.produce(createMessage(null));
      fail("Expected ProduceException");
    }
    catch (ProduceException e) {
      log.trace("Expected Exception", e);
    }
    finally {
      stop(standaloneProducer);
      broker.destroy();
    }
  }

}
