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

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.apache.activemq.ActiveMQConnectionFactory;

import com.adaptris.annotation.AdvancedConfig;
import com.adaptris.annotation.AutoPopulated;
import com.adaptris.annotation.DisplayOrder;
import com.adaptris.util.KeyValuePair;
import com.adaptris.util.KeyValuePairSet;
import com.adaptris.util.SimpleBeanUtil;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * <p>
 * ActiveMQ implementation of <code>VendorImplementation</code>.
 * </p>
 * <p>
 * This vendor implementation class directly exposes almost all the getter and setters that are available in the ConnectionFactory
 * for maximum flexibility in configuration.
 * </p>
 * <p>
 * The key from the <code>connection-factory-properties</code> element should match the name of the underlying ActiveMQ
 * ConnectionFactory property.
 * 
 * <pre>
 * {@code 
 *   <connection-factory-properties>
 *     <key-value-pair>
 *        <key>AlwaysSessionAsync</key>
 *        <value>true</value>
 *     </key-value-pair>
 *   </connection-factory-properties>
 * }
 * </pre>
 * will invoke {@link ActiveMQConnectionFactory#setAlwaysSessionAsync(boolean)}, setting the AlwaysSessionAsync property to true.
 * </p>
 * <p>
 * <b>This was built against ActiveMQ 5.9.0</b>
 * </p>
 * <p>
 * 
 * @config advanced-active-mq-implementation
 * 
 */
@XStreamAlias("advanced-active-mq-implementation")
@DisplayOrder(order = {"brokerUrl", "connectionFactoryProperties"})
public class AdvancedActiveMqImplementation extends BasicActiveMqImplementation {

  /**
   * Non-Exhaustive list that matches various ActiveMQConnectionFactory methods.
   */
  public enum ConnectionFactoryProperty {

    /**
     * @see ActiveMQConnectionFactory#setAlwaysSessionAsync(boolean)
     */
    AlwaysSessionAsync {
      @Override
      void applyProperty(ActiveMQConnectionFactory cf, String o) {
        cf.setAlwaysSessionAsync(Boolean.valueOf(o));
      }
    },
    /**
     * @see ActiveMQConnectionFactory#setAlwaysSyncSend(boolean)
     */
    AlwaysSyncSend {
      @Override
      void applyProperty(ActiveMQConnectionFactory cf, String o) {
        cf.setAlwaysSyncSend(Boolean.valueOf(o));
      }
    },
    /**
     * @see ActiveMQConnectionFactory#setAuditDepth(int)
     * 
     */
    AuditDepth {
      @Override
      void applyProperty(ActiveMQConnectionFactory cf, String o) {
        cf.setAuditDepth(Integer.parseInt(o));
      }
    },
    /**
     * @see ActiveMQConnectionFactory#setAuditMaximumProducerNumber(int)
     */
    AuditMaximumProducerNumber{
      @Override
      void applyProperty(ActiveMQConnectionFactory cf, String o) {
        cf.setAuditMaximumProducerNumber(Integer.parseInt(o));
      }
    },
    /**
     * @see ActiveMQConnectionFactory#setCheckForDuplicates(boolean)
     */
    CheckForDuplicates {
      @Override
      void applyProperty(ActiveMQConnectionFactory cf, String o) {
        cf.setCheckForDuplicates(Boolean.parseBoolean(o));
      }
    },
    /**
     * @see ActiveMQConnectionFactory#setClientID(String)
     */
    ClientID {
      @Override
      void applyProperty(ActiveMQConnectionFactory f, String o) {
        f.setClientID(o);
      }
    },
    /**
     * @see ActiveMQConnectionFactory#setClientIDPrefix(String)
     */
    ClientIDPrefix {
      @Override
      void applyProperty(ActiveMQConnectionFactory cf, String o) {
        cf.setClientIDPrefix(o);
      }
    },
    /**
     * @see ActiveMQConnectionFactory#setCloseTimeout(int)
     */
    CloseTimeout {
      @Override
      void applyProperty(ActiveMQConnectionFactory cf, String o) {
        cf.setCloseTimeout(Integer.valueOf(o));
      }
    },
    /**
     * @see ActiveMQConnectionFactory#setConnectionIDPrefix(String)
     * 
     */
    ConnectionIDPrefix {
      @Override
      void applyProperty(ActiveMQConnectionFactory cf, String o) {
        cf.setConnectionIDPrefix(o);
      }
      
    },
    /**
     * @see ActiveMQConnectionFactory#setConsumerFailoverRedeliveryWaitPeriod(long)
     */
    ConsumerFailoverRedeliveryWaitPeriod {
      @Override
      void applyProperty(ActiveMQConnectionFactory cf, String o) {
        cf.setConsumerFailoverRedeliveryWaitPeriod(Long.parseLong(o));
      }
    },
    /**
     * @see ActiveMQConnectionFactory#setCopyMessageOnSend(boolean)
     */
    CopyMessageOnSend {
      @Override
      void applyProperty(ActiveMQConnectionFactory cf, String o) {
        cf.setCopyMessageOnSend(Boolean.valueOf(o));
      }
    },
    /**
     * @see ActiveMQConnectionFactory#setDisableTimeStampsByDefault(boolean)
     */
    DisableTimeStampsByDefault {
      @Override
      void applyProperty(ActiveMQConnectionFactory cf, String o) {
        cf.setDisableTimeStampsByDefault(Boolean.valueOf(o));
      }
    },
    /**
     * @see ActiveMQConnectionFactory#setDispatchAsync(boolean)
     */
    DispatchAsync {
      @Override
      void applyProperty(ActiveMQConnectionFactory cf, String o) {
        cf.setDispatchAsync(Boolean.valueOf(o));
      }
    },
    /**
     * @see ActiveMQConnectionFactory#setExclusiveConsumer(boolean)
     */
    ExclusiveConsumer {
      @Override
      void applyProperty(ActiveMQConnectionFactory cf, String o) {
        cf.setExclusiveConsumer(Boolean.valueOf(o));
      }
    },
    /**
     * @see ActiveMQConnectionFactory#setMaxThreadPoolSize(int)
     * 
     */
    MaxThreadPoolSize {
      @Override
      void applyProperty(ActiveMQConnectionFactory cf, String o) {
        cf.setMaxThreadPoolSize(Integer.valueOf(o));
      }
    },
    /**
     * @see ActiveMQConnectionFactory#setMessagePrioritySupported(boolean)
     * 
     */
    MessagePrioritySupported {
      @Override
      void applyProperty(ActiveMQConnectionFactory cf, String o) {
        cf.setMessagePrioritySupported(Boolean.parseBoolean(o));
      }
    },
    /**
     * @see ActiveMQConnectionFactory#setNestedMapAndListEnabled(boolean)
     */
    NestedMapAndListEnabled {
      @Override
      void applyProperty(ActiveMQConnectionFactory cf, String o) {
        cf.setNestedMapAndListEnabled(Boolean.valueOf(o));
      }
    },
    /**
     * @see ActiveMQConnectionFactory#setNonBlockingRedelivery(boolean)
     * 
     */
    NonBlockingRedelivery {
      @Override
      void applyProperty(ActiveMQConnectionFactory cf, String o) {
        cf.setNonBlockingRedelivery(Boolean.valueOf(o));
      }
    },
    /**
     * This is just the correct spelling for
     * {@link ActiveMQConnectionFactory#setObjectMessageSerializationDefered(boolean)}
     * 
     * @see ActiveMQConnectionFactory#setObjectMessageSerializationDefered(boolean)
     */
    ObjectMessageSerializationDeferred {
      @Override
      void applyProperty(ActiveMQConnectionFactory cf, String o) {
        cf.setObjectMessageSerializationDefered(Boolean.valueOf(o));
      }
    },
    /**
     * @see ActiveMQConnectionFactory#setOptimizeAcknowledge(boolean)
     */
    OptimizeAcknowledge {
      @Override
      void applyProperty(ActiveMQConnectionFactory cf, String o) {
        cf.setOptimizeAcknowledge(Boolean.valueOf(o));
      }
    },
    /**
     * @see ActiveMQConnectionFactory#setOptimizeAcknowledgeTimeOut(long)
     */
    OptimizeAcknowledgeTimeout {
      @Override
      void applyProperty(ActiveMQConnectionFactory cf, String o) {
        cf.setOptimizeAcknowledgeTimeOut(Long.valueOf(o));
      }
    },
    /**
     * @see ActiveMQConnectionFactory#setOptimizedAckScheduledAckInterval(long)
     */
    OptimizedAckScheduledAckInterval {
      @Override
      void applyProperty(ActiveMQConnectionFactory cf, String o) {
        cf.setOptimizedAckScheduledAckInterval(Long.valueOf(o));
      }
    },
    /**
     * @see ActiveMQConnectionFactory#setOptimizedMessageDispatch(boolean)
     */
    OptimizedMessageDispatch {
      @Override
      void applyProperty(ActiveMQConnectionFactory cf, String o) {
        cf.setOptimizedMessageDispatch(Boolean.valueOf(o));
      }

    },
    /**
     * @see ActiveMQConnectionFactory#setProducerWindowSize(int)
     */
    ProducerWindowSize {
      @Override
      void applyProperty(ActiveMQConnectionFactory cf, String o) {
        cf.setProducerWindowSize(Integer.valueOf(o));
      }
    },
    /**
     * @see ActiveMQConnectionFactory#setSendAcksAsync(boolean)
     */
    SendAcksAsync {
      @Override
      void applyProperty(ActiveMQConnectionFactory cf, String o) {
        cf.setSendAcksAsync(Boolean.valueOf(o));
      }
    },
    /**
     * @see ActiveMQConnectionFactory#setSendTimeout(int)
     */
    SendTimeout {
      @Override
      void applyProperty(ActiveMQConnectionFactory cf, String o) {
        cf.setSendTimeout(Integer.valueOf(o));
      }
    },
    /**
     * @see ActiveMQConnectionFactory#setStatsEnabled(boolean)
     */
    StatsEnabled {
      @Override
      void applyProperty(ActiveMQConnectionFactory cf, String o) {
        cf.setStatsEnabled(Boolean.parseBoolean(o));
      }
    },
    /**
     * @see ActiveMQConnectionFactory#setTransactedIndividualAck(boolean)
     */
    TransactedIndividualAck {
      @Override
      void applyProperty(ActiveMQConnectionFactory cf, String o) {
        cf.setTransactedIndividualAck(Boolean.parseBoolean(o));
      }
    },
    /**
     * @see ActiveMQConnectionFactory#setUseAsyncSend(boolean)
     */
    UseAsyncSend {
      @Override
      void applyProperty(ActiveMQConnectionFactory cf, String o) {
        cf.setUseAsyncSend(Boolean.parseBoolean(o));
      }
    },
    /**
     * @see ActiveMQConnectionFactory#setUseCompression(boolean)
     */
    UseCompression {
      @Override
      void applyProperty(ActiveMQConnectionFactory cf, String o) {
        cf.setUseCompression(Boolean.valueOf(o));
      }
    },
    /**
     * @see ActiveMQConnectionFactory#setUseDedicatedTaskRunner(boolean)
     */
    UseDedicatedTaskRunner {
      @Override
      void applyProperty(ActiveMQConnectionFactory cf, String o) {
        cf.setUseDedicatedTaskRunner(Boolean.valueOf(o));
      }
    },
    /**
     * @see ActiveMQConnectionFactory#setUseRetroactiveConsumer(boolean)
     */
    UseRetroactiveConsumer {
      @Override
      void applyProperty(ActiveMQConnectionFactory cf, String o) {
        cf.setUseRetroactiveConsumer(Boolean.valueOf(o));
      }
    },
    /**
     * @see ActiveMQConnectionFactory#setWarnAboutUnstartedConnectionTimeout(long)
     */
    WarnAboutUnstartedConnectionTimeout {
      @Override
      void applyProperty(ActiveMQConnectionFactory cf, String o) {
        cf.setWarnAboutUnstartedConnectionTimeout(Long.valueOf(o));
      }
    };
    abstract void applyProperty(ActiveMQConnectionFactory cf, String s);
  };

  @AdvancedConfig
  private BlobTransferPolicyFactory blobTransferPolicy;
  @AdvancedConfig
  private PrefetchPolicyFactory prefetchPolicy;
  @AdvancedConfig
  private RedeliveryPolicyFactory redeliveryPolicy;
  @NotNull
  @AutoPopulated
  @Valid
  private KeyValuePairSet connectionFactoryProperties;

  public AdvancedActiveMqImplementation() {
    super();
    setConnectionFactoryProperties(new KeyValuePairSet());
  }

  @Override
  protected ActiveMQConnectionFactory create(String url) {
    ActiveMQConnectionFactory cf = super.create(url);
    if (blobTransferPolicy != null) {
      cf.setBlobTransferPolicy(blobTransferPolicy.create());
    }
    if (prefetchPolicy != null) {
      cf.setPrefetchPolicy(prefetchPolicy.create());
    }
    if (redeliveryPolicy != null) {
      cf.setRedeliveryPolicy(redeliveryPolicy.create());
    }
    for (KeyValuePair kvp : getConnectionFactoryProperties().getKeyValuePairs()) {
      // Yeah we could use valueOf here, but really, are we going to be
      // consistent valueOf is case sensitive.
      boolean matched = false;
      for (ConnectionFactoryProperty sp : ConnectionFactoryProperty.values()) {
        if (kvp.getKey().equalsIgnoreCase(sp.toString())) {
          sp.applyProperty(cf, kvp.getValue());
          matched = true;
          break;
        }
      }
      if (!matched) {
        if (!SimpleBeanUtil.callSetter(cf, "set" + kvp.getKey(), kvp.getValue())) {
          log.trace("Ignoring unsupported Property {}", kvp.getKey());
        }
      }
    }
    return cf;
  }

  /**
   * @return The additional connection factory properties.
   */
  public KeyValuePairSet getConnectionFactoryProperties() {
    return connectionFactoryProperties;
  }

  /**
   * Set any additional ActiveMQConnectionFactory properties that are required.
   * <p>
   * The key from the <code>connection-factory-properties</code> element should match the name of the underlying ActiveMQ
   * ConnectionFactory property.
   * 
   * <pre>
   * {@code
   *   <connection-factory-properties>
   *     <key-value-pair>
   *        <key>AlwaysSessionAsync</key>
   *        <value>true</value>
   *     </key-value-pair>
   *   </connection-factory-properties>
   * }
   * </pre>
   * will invoke {@link ActiveMQConnectionFactory#setAlwaysSessionAsync(boolean)}, setting the AlwaysSessionAsync property to true.
   * Only explicitly configured properties will invoke the associated setter method.
   * </p>
   * 
   * @param kvps the additional connectionFactoryProperties to set
   */
  public void setConnectionFactoryProperties(KeyValuePairSet kvps) {
    this.connectionFactoryProperties = kvps;
  }

  /**
   * Get the Blob Transfer Policy.
   *
   * @return the Blob Transfer Policy.
   * @see org.apache.activemq.blob.BlobTransferPolicy
   * @see BlobTransferPolicyFactory
   */
  public BlobTransferPolicyFactory getBlobTransferPolicy() {
    return blobTransferPolicy;
  }

  /**
   * Set the Blob Transfer Policy.
   * <p>
   * If not explicitly configured then the associated setter
   * {@link ActiveMQConnectionFactory#setBlobTransferPolicy(org.apache.activemq.blob.BlobTransferPolicy)}
   * is never invoked.
   * <p>
   *
   * @param f the Blob Transfer Policy.
   * @see org.apache.activemq.blob.BlobTransferPolicy
   * @see BlobTransferPolicyFactory
   */
  public void setBlobTransferPolicy(BlobTransferPolicyFactory f) {
    this.blobTransferPolicy = f;
  }

  /**
   * Get the Prefetch Policy.
   *
   * @return the PrefetchPolicy.
   * @see org.apache.activemq.ActiveMQPrefetchPolicy
   * @see PrefetchPolicyFactory
   */
  public PrefetchPolicyFactory getPrefetchPolicy() {
    return prefetchPolicy;
  }

  /**
   * Set the Prefetch Policy.
   * <p>
   * If not explicitly configured then the associated setter
   * {@link ActiveMQConnectionFactory#setPrefetchPolicy(org.apache.activemq.ActiveMQPrefetchPolicy)}
   * is never invoked.
   * <p>
   *
   * @param f the PrefetchPolicy.
   * @see org.apache.activemq.ActiveMQPrefetchPolicy
   * @see PrefetchPolicyFactory
   */
  public void setPrefetchPolicy(PrefetchPolicyFactory f) {
    this.prefetchPolicy = f;
  }

  /**
   * Get the Redelivery Policy.
   *
   * @return the redelivery Policy.
   * @see org.apache.activemq.RedeliveryPolicy
   * @see RedeliveryPolicyFactory
   */
  public RedeliveryPolicyFactory getRedeliveryPolicy() {
    return redeliveryPolicy;
  }

  /**
   * Set the Redelivery Policy.
   * <p>
   * If not explicitly configured then the associated setter
   * {@link ActiveMQConnectionFactory#setRedeliveryPolicy(org.apache.activemq.RedeliveryPolicy)} is
   * never invoked.
   * <p>
   *
   * @param f the redelivery Policy.
   * @see org.apache.activemq.RedeliveryPolicy
   * @see RedeliveryPolicyFactory
   */
  public void setRedeliveryPolicy(RedeliveryPolicyFactory f) {
    this.redeliveryPolicy = f;
  }

}
