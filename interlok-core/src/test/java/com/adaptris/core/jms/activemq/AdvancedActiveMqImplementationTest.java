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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.ActiveMQPrefetchPolicy;
import org.apache.activemq.RedeliveryPolicy;
import org.apache.activemq.blob.BlobTransferPolicy;
import com.adaptris.util.KeyValuePair;
import com.adaptris.util.KeyValuePairSet;

public class AdvancedActiveMqImplementationTest extends BasicActiveMqImplementationTest {

  private static final String TRUE = Boolean.TRUE.toString();
  private static final String FALSE = Boolean.FALSE.toString();

  @Override
  protected void doAssertions(ActiveMQConnectionFactory f) throws Exception {
    super.doAssertions(f);
    doClient53Assertions(f);
    doClient59Assertions(f);
    doAssertions(f.getBlobTransferPolicy());
    doAssertions(f.getRedeliveryPolicy());
    doAssertions(f.getPrefetchPolicy());
  }

  private void doAssertions(BlobTransferPolicy p) throws Exception {
    assertNotNull("BlobTransferPolicy", p);
    assertEquals("BlobUploadStrategy", ExampleNoOpBlobUploadStrategy.class,
        p.getUploadStrategy().getClass());
    assertEquals("setBrokerUploadUrl", "http://localhost:80/activemq",
        p.getBrokerUploadUrl());
    assertEquals("setDefaultUploadUrl", "ftp://myname:mypassword@localhost:21/activemq",
        p.getDefaultUploadUrl());
    assertEquals("setUploadUrl", "https://localhost:443/activemq", p.getUploadUrl());
    assertEquals("setBufferSize", 256 * 1024, p.getBufferSize());
  }

  private void doAssertions(ActiveMQPrefetchPolicy p) throws Exception {
    assertNotNull("ActiveMQPrefetchPolicy", p);
    assertEquals("setDurableTopicPrefetch", 10, p.getDurableTopicPrefetch());
    assertEquals("setMaximumPendingMessageLimit", 900, p.getMaximumPendingMessageLimit());
    assertEquals("setOptimizeAcknowledgePrefetch", 5, p.getOptimizeDurableTopicPrefetch());
    assertEquals("setQueueBrowserPrefetch", 10, p.getQueueBrowserPrefetch());
    assertEquals("setQueuePrefetch", 3, p.getQueuePrefetch());
    assertEquals("setTopicPrefetch", 3, p.getTopicPrefetch());
  }

  private void doAssertions(RedeliveryPolicy p) throws Exception {
    assertNotNull("RedeliveryPolicy", p);
    assertEquals("setBackOffMultiplier", Double.valueOf("10"),
        Double.valueOf(p.getBackOffMultiplier()));
    assertEquals("setCollisionAvoidancePercent", 50, p.getCollisionAvoidancePercent());
    assertEquals("setInitialRedeliveryDelay", 10000, p.getInitialRedeliveryDelay());
    assertEquals("setUseCollisionAvoidance", true, p.isUseCollisionAvoidance());
    assertEquals("setUseExponentialBackOff", false, p.isUseExponentialBackOff());
  }

  @Override
  protected AdvancedActiveMqImplementation create() {
    return createImpl();
  }

  protected static AdvancedActiveMqImplementation createImpl() {
    AdvancedActiveMqImplementation vendor = new AdvancedActiveMqImplementation();
    vendor.setBrokerUrl("tcp://localhost:61616");
    vendor.setConnectionFactoryProperties(createFactoryProperties());
    vendor.setBlobTransferPolicy(createBlobTransferPolicy());
    vendor.setPrefetchPolicy(createPrefetchPolicy());
    vendor.setRedeliveryPolicy(createRedeliveryPolicy());
    return vendor;
  }

  private static KeyValuePairSet createFactoryProperties() {
    KeyValuePairSet props = new KeyValuePairSet();
    props.addKeyValuePair(new KeyValuePair("Unsupported_Property", "99"));

    props.addAll(createClient53Properties());
    props.addAll(createClient59Properties());
    return props;
  }

  private static KeyValuePairSet createClient53Properties() {
    KeyValuePairSet props = new KeyValuePairSet();
    props.addKeyValuePair(new KeyValuePair("AlwaysSyncSend", FALSE));
    props.addKeyValuePair(new KeyValuePair("CloseTimeout", "10"));
    props.addKeyValuePair(new KeyValuePair("DisableTimeStampsByDefault", TRUE));
    props.addKeyValuePair(new KeyValuePair("DispatchAsync", TRUE));
    props.addKeyValuePair(new KeyValuePair("NestedMapAndListEnabled", TRUE));
    props.addKeyValuePair(new KeyValuePair("OptimizeAcknowledge", TRUE));
    props.addKeyValuePair(new KeyValuePair("ProducerWindowSize", "100"));
    props.addKeyValuePair(new KeyValuePair("SendTimeout", "99"));
    props.addKeyValuePair(new KeyValuePair("AlwaysSessionAsync", TRUE));
    props.addKeyValuePair(new KeyValuePair("ClientID", "flintstone"));
    props.addKeyValuePair(new KeyValuePair("ClientIDPrefix", "fred"));
    props.addKeyValuePair(new KeyValuePair("CopyMessageOnSend", TRUE));
    props.addKeyValuePair(new KeyValuePair("ExclusiveConsumer", TRUE));
    props.addKeyValuePair(new KeyValuePair("ObjectMessageSerializationDeferred", TRUE));
    props.addKeyValuePair(new KeyValuePair("ObjectMessageSerializationDefered", TRUE));
    props.addKeyValuePair(new KeyValuePair("OptimizedMessageDispatch", TRUE));
    props.addKeyValuePair(new KeyValuePair("SendAcksAsync", TRUE));
    props.addKeyValuePair(new KeyValuePair("UseCompression", TRUE));
    props.addKeyValuePair(new KeyValuePair("UseRetroactiveConsumer", TRUE));
    return props;
  }

  private static void doClient53Assertions(ActiveMQConnectionFactory f) {
    assertEquals(false, f.isAlwaysSyncSend());
    assertEquals(10, f.getCloseTimeout());
    assertEquals(true, f.isDisableTimeStampsByDefault());
    assertEquals(true, f.isDispatchAsync());
    assertEquals(true, f.isNestedMapAndListEnabled());
    assertEquals(true, f.isOptimizeAcknowledge());
    assertEquals(100, f.getProducerWindowSize());
    assertEquals(99, f.getSendTimeout());
    assertEquals(true, f.isAlwaysSessionAsync());
    assertEquals("flintstone", f.getClientID());
    assertEquals("fred", f.getClientIDPrefix());
    assertEquals(true, f.isCopyMessageOnSend());
    assertEquals(true, f.isExclusiveConsumer());
    assertEquals(true, f.isObjectMessageSerializationDefered());
    assertEquals(true, f.isOptimizedMessageDispatch());
    assertEquals(true, f.isSendAcksAsync());
    assertEquals(true, f.isUseCompression());
    assertEquals(true, f.isUseRetroactiveConsumer());
  }

  private static KeyValuePairSet createClient59Properties() {
    KeyValuePairSet props = new KeyValuePairSet();
    // New 5.9 connection factory properties.
    props.addKeyValuePair(new KeyValuePair("AuditDepth", "99"));
    props.addKeyValuePair(new KeyValuePair("AuditMaximumProducerNumber", "99"));
    props.addKeyValuePair(new KeyValuePair("ConnectionIDPrefix", "wilma"));
    props.addKeyValuePair(new KeyValuePair("ConsumerFailoverRedeliveryWaitPeriod", "99"));
    props.addKeyValuePair(new KeyValuePair("CheckForDuplicates", TRUE));
    props.addKeyValuePair(new KeyValuePair("MaxThreadPoolSize", "99"));
    props.addKeyValuePair(new KeyValuePair("MessagePrioritySupported", TRUE));
    props.addKeyValuePair(new KeyValuePair("NonBlockingRedelivery", TRUE));
    props.addKeyValuePair(new KeyValuePair("OptimizeAcknowledgeTimeOut", "99"));
    props.addKeyValuePair(new KeyValuePair("OptimizedAckScheduledAckInterval", "99"));
    props.addKeyValuePair(new KeyValuePair("StatsEnabled", TRUE));
    props.addKeyValuePair(new KeyValuePair("TransactedIndividualAck", TRUE));
    props.addKeyValuePair(new KeyValuePair("UseAsyncSend", TRUE));
    props.addKeyValuePair(new KeyValuePair("UseDedicatedTaskRunner", TRUE));
    props.addKeyValuePair(new KeyValuePair("WarnAboutUnstartedConnectionTimeout", "99"));
    return props;
  }

  private static void doClient59Assertions(ActiveMQConnectionFactory f) {
    assertEquals(99, f.getAuditDepth());
    assertEquals(99, f.getAuditMaximumProducerNumber());
    assertEquals(99, f.getConsumerFailoverRedeliveryWaitPeriod());
    // Can't check connectionIDPrefix.
    assertEquals(true, f.isCheckForDuplicates());
    assertEquals(99, f.getMaxThreadPoolSize());
    assertEquals(true, f.isMessagePrioritySupported());
    assertEquals(true, f.isNonBlockingRedelivery());
    assertEquals(99, f.getOptimizeAcknowledgeTimeOut());
    assertEquals(99, f.getOptimizedAckScheduledAckInterval());
    assertEquals(true, f.isStatsEnabled());
    assertEquals(true, f.isTransactedIndividualAck());
    assertEquals(true, f.isUseAsyncSend());
    assertEquals(true, f.isUseDedicatedTaskRunner());
    assertEquals(99, f.getWarnAboutUnstartedConnectionTimeout());
  }

  private static BlobTransferPolicyFactory createBlobTransferPolicy() {
    BlobTransferPolicyFactory result = new BlobTransferPolicyFactory();
    result.setBrokerUploadUrl("http://localhost:80/activemq");
    result.setDefaultUploadUrl("ftp://myname:mypassword@localhost:21/activemq");
    result.setUploadUrl("https://localhost:443/activemq");
    result.setBufferSize(256 * 1024);
    result.setUploadStrategy(new ExampleNoOpBlobUploadStrategy());
    return result;
  }

  private static RedeliveryPolicyFactory createRedeliveryPolicy() {
    RedeliveryPolicyFactory result = new RedeliveryPolicyFactory();
    result.setBackOffMultiplier(Double.valueOf("10"));
    result.setCollisionAvoidancePercent(Short.valueOf("50"));
    result.setInitialRedeliveryDelay(10000L);
    result.setUseCollisionAvoidance(true);
    result.setUseExponentialBackOff(false);
    return result;
  }

  private static PrefetchPolicyFactory createPrefetchPolicy() {
    PrefetchPolicyFactory result = new PrefetchPolicyFactory();
    result.setDurableTopicPrefetch(10);
    result.setMaximumPendingMessageLimit(900);
    result.setOptimizeDurableTopicPrefetch(5);
    result.setQueueBrowserPrefetch(10);
    result.setQueuePrefetch(3);
    result.setTopicPrefetch(3);
    return result;
  }

}
