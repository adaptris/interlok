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

import static com.adaptris.core.jms.activemq.AdvancedActiveMqImplementationTest.createImpl;
import org.apache.activemq.ActiveMQPrefetchPolicy;
import org.apache.activemq.RedeliveryPolicy;
import com.adaptris.core.ConfiguredConsumeDestination;
import com.adaptris.core.ConfiguredProduceDestination;
import com.adaptris.core.StandaloneConsumer;
import com.adaptris.core.StandaloneProducer;
import com.adaptris.core.jms.JmsConfig;
import com.adaptris.core.jms.JmsConnection;
import com.adaptris.core.jms.PtpConsumer;
import com.adaptris.core.jms.PtpProducer;
import com.adaptris.core.jms.UrlVendorImplementation;
import com.adaptris.core.stubs.MockMessageListener;
import com.adaptris.util.KeyValuePair;

public class AdvancedActiveMqProducerTest extends BasicActiveMqProducerTest {

  public AdvancedActiveMqProducerTest(String name) {
    super(name);
  }

  @Override
  protected void setUp() throws Exception {
    super.setUp();
  }

  @Override
  protected String createBaseFileName(Object object) {
    return ((StandaloneProducer) object).getProducer().getClass().getName() + "-AdvancedActiveMQ";
  }

  public void testQueueProduceAndConsumeWithRedeliveryPolicy() throws Exception {
    // This would be best, but we can't mix Junit3 with Junit4 assumptions.
    // Assume.assumeTrue(JmsConfig.jmsTestsEnabled());
    if (!JmsConfig.jmsTestsEnabled()) {
      return;
    }
    EmbeddedActiveMq activeMqBroker = new EmbeddedActiveMq();
    try {
      activeMqBroker.start();
      PtpConsumer consumer = new PtpConsumer(new ConfiguredConsumeDestination(getName()));
      consumer.setAcknowledgeMode("AUTO_ACKNOWLEDGE");

      StandaloneConsumer standaloneConsumer = new StandaloneConsumer(activeMqBroker.getJmsConnection(createVendorImpl(
          createRedelivery(), null)), consumer);
      MockMessageListener jms = new MockMessageListener();
      standaloneConsumer.registerAdaptrisMessageListener(jms);

      StandaloneProducer standaloneProducer = new StandaloneProducer(activeMqBroker.getJmsConnection(createVendorImpl(
          createRedelivery(), null)), new PtpProducer(new ConfiguredProduceDestination(getName())));

      execute(standaloneConsumer, standaloneProducer, createMessage(), jms);
      assertMessages(jms, 1);
    }
    finally {
      activeMqBroker.destroy();
    }
  }

  public void testQueueProduceAndConsumeWithPrefetch() throws Exception {
    // This would be best, but we can't mix Junit3 with Junit4 assumptions.
    // Assume.assumeTrue(JmsConfig.jmsTestsEnabled());
    if (!JmsConfig.jmsTestsEnabled()) {
      return;
    }
    EmbeddedActiveMq activeMqBroker = new EmbeddedActiveMq();
    try {
      activeMqBroker.start();
      PtpConsumer consumer = new PtpConsumer(new ConfiguredConsumeDestination(getName()));
      consumer.setAcknowledgeMode("AUTO_ACKNOWLEDGE");

      StandaloneConsumer standaloneConsumer = new StandaloneConsumer(activeMqBroker.getJmsConnection(createVendorImpl(null,
          createPrefetch())), consumer);
      MockMessageListener jms = new MockMessageListener();
      standaloneConsumer.registerAdaptrisMessageListener(jms);

      StandaloneProducer standaloneProducer = new StandaloneProducer(activeMqBroker.getJmsConnection(createVendorImpl(null,
          createPrefetch())), new PtpProducer(new ConfiguredProduceDestination(getName())));

      execute(standaloneConsumer, standaloneProducer, createMessage(), jms);
      assertMessages(jms, 1);
    }
    finally {
      activeMqBroker.destroy();
    }
  }

  @Override
  protected Object retrieveObjectForSampleConfig() {

    JmsConnection connection = new JmsConnection();
    PtpProducer producer = new PtpProducer();
    ConfiguredProduceDestination dest = new ConfiguredProduceDestination();
    dest.setDestination("destination");

    producer.setDestination(dest);
    UrlVendorImplementation vendorImpl = createImpl();
    vendorImpl.setBrokerUrl(BasicActiveMqImplementationTest.PRIMARY);
    connection.setUserName("BrokerUsername");
    connection.setPassword("BrokerPassword");
    connection.setVendorImplementation(vendorImpl);
    StandaloneProducer result = new StandaloneProducer();
    result.setConnection(connection);
    result.setProducer(producer);

    return result;
  }

  @Override
  protected String getExampleCommentHeader(Object obj) {
    return super.getExampleCommentHeader(obj) + "<!-- Not all elements within the VendorImplementation are required. \n"
        + "\nThis example explicitly configures elements to show you possible configuration \n"
        + "Use of these values may cause failure within your ActiveMQ environment\n"
        + "Check the ActiveMQ documentation for the exact meanings of each field.\n-->\n";
  }

  private BasicActiveMqImplementation createVendorImpl(RedeliveryPolicyFactory redel, PrefetchPolicyFactory pref) {

    AdvancedActiveMqImplementation result = (AdvancedActiveMqImplementation) createVendorImpl();
    if (redel != null) {
      result.setRedeliveryPolicy(redel);
    }
    if (pref != null) {
      result.setPrefetchPolicy(pref);
    }
    return result;
  }

  @Override
  protected BasicActiveMqImplementation createVendorImpl() {
    AdvancedActiveMqImplementation result = new AdvancedActiveMqImplementation();
    result.getConnectionFactoryProperties().addKeyValuePair(new KeyValuePair("DisableTimeStampsByDefault", "true"));
    result.getConnectionFactoryProperties().addKeyValuePair(new KeyValuePair("NestedMapAndListEnabled", "true"));
    result.setPrefetchPolicy(new PrefetchPolicyFactory());
    result.setRedeliveryPolicy(new RedeliveryPolicyFactory());
    result.setBlobTransferPolicy(new BlobTransferPolicyFactory());
    return result;
  }

  private RedeliveryPolicyFactory createRedelivery() {
    RedeliveryPolicy rp = new RedeliveryPolicy();
    RedeliveryPolicyFactory result = new RedeliveryPolicyFactory();
    result.setBackOffMultiplier(rp.getBackOffMultiplier());
    result.setCollisionAvoidancePercent(rp.getCollisionAvoidancePercent());
    result.setInitialRedeliveryDelay(rp.getInitialRedeliveryDelay());
    result.setMaximumRedeliveries(rp.getMaximumRedeliveries());
    result.setUseCollisionAvoidance(rp.isUseCollisionAvoidance());
    result.setUseExponentialBackOff(rp.isUseExponentialBackOff());
    return result;
  }

  private PrefetchPolicyFactory createPrefetch() {
    ActiveMQPrefetchPolicy rp = new ActiveMQPrefetchPolicy();
    PrefetchPolicyFactory result = new PrefetchPolicyFactory();
    result.setDurableTopicPrefetch(rp.getDurableTopicPrefetch());
    result.setMaximumPendingMessageLimit(rp.getMaximumPendingMessageLimit());
    result.setOptimizeDurableTopicPrefetch(rp.getOptimizeDurableTopicPrefetch());
    result.setQueueBrowserPrefetch(rp.getQueueBrowserPrefetch());
    result.setQueuePrefetch(rp.getQueuePrefetch());
    result.setTopicPrefetch(rp.getTopicPrefetch());
    return result;
  }
}
