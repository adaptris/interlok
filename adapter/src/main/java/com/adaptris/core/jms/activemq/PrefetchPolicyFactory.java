package com.adaptris.core.jms.activemq;

import org.apache.activemq.ActiveMQPrefetchPolicy;

import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Proxy class for creating ActiveMQPrefetchPolicy objects
 * 
 * <p>
 * This class is simply a class that can be marshalled correctly.
 * </p>
 * *
 * <p>
 * If fields are not explicitly set, then the corresponding {@link ActiveMQPrefetchPolicy} method will not be invoked.
 * </p>
 * 
 * @config activemq-prefetch-policy
 * @author lchan
 * 
 */
@XStreamAlias("activemq-prefetch-policy")
public class PrefetchPolicyFactory {
  private Integer durableTopicPrefetch, maximumPendingMessageLimit, optimizeDurableTopicPrefetch,
      queueBrowserPrefetch, queuePrefetch, topicPrefetch;

  /**
   * Default constructor.
   * <p>
   * All fields are initialised to be null.
   * </p>
   */
  public PrefetchPolicyFactory() {
  }

  /**
   * Create an ActiveMQPrefetchPolicy.
   *
   * @return an ActiveMQPrefetchPolicy
   */
  public ActiveMQPrefetchPolicy create() {
    ActiveMQPrefetchPolicy p = new ActiveMQPrefetchPolicy();
    if (getDurableTopicPrefetch() != null) {
      p.setDurableTopicPrefetch(getDurableTopicPrefetch());
    }
    if (getMaximumPendingMessageLimit() != null) {
      p.setMaximumPendingMessageLimit(getMaximumPendingMessageLimit());
    }
    if (getOptimizeDurableTopicPrefetch() != null) {
      p.setOptimizeDurableTopicPrefetch(getOptimizeDurableTopicPrefetch());
    }
    if (getQueueBrowserPrefetch() != null) {
      p.setQueueBrowserPrefetch(getQueueBrowserPrefetch());
    }
    if (getQueuePrefetch() != null) {
      p.setQueuePrefetch(getQueuePrefetch());
    }
    if (getTopicPrefetch() != null) {
      p.setTopicPrefetch(getTopicPrefetch());
    }
    return p;
  }

  /** @see ActiveMQPrefetchPolicy#getDurableTopicPrefetch() */
  public Integer getDurableTopicPrefetch() {
    return durableTopicPrefetch;
  }

  /** @see ActiveMQPrefetchPolicy#setDurableTopicPrefetch(int) */
  public void setDurableTopicPrefetch(Integer i) {
    durableTopicPrefetch = i;
  }

  /** @see ActiveMQPrefetchPolicy#getMaximumPendingMessageLimit() */
  public Integer getMaximumPendingMessageLimit() {
    return maximumPendingMessageLimit;
  }

  /** @see ActiveMQPrefetchPolicy#setMaximumPendingMessageLimit(int) */
  public void setMaximumPendingMessageLimit(Integer i) {
    maximumPendingMessageLimit = i;
  }

  /** @see ActiveMQPrefetchPolicy#getOptimizeDurableTopicPrefetch() */
  public Integer getOptimizeDurableTopicPrefetch() {
    return optimizeDurableTopicPrefetch;
  }

  /** @see ActiveMQPrefetchPolicy#setOptimizeDurableTopicPrefetch(int) */
  public void setOptimizeDurableTopicPrefetch(Integer i) {
    optimizeDurableTopicPrefetch = i;
  }

  /** @see ActiveMQPrefetchPolicy#getQueueBrowserPrefetch() */
  public Integer getQueueBrowserPrefetch() {
    return queueBrowserPrefetch;
  }

  /** @see ActiveMQPrefetchPolicy#setQueueBrowserPrefetch(int) */
  public void setQueueBrowserPrefetch(Integer i) {
    queueBrowserPrefetch = i;
  }

  /** @see ActiveMQPrefetchPolicy#getQueuePrefetch() */
  public Integer getQueuePrefetch() {
    return queuePrefetch;
  }

  /** @see ActiveMQPrefetchPolicy#setQueuePrefetch(int) */
  public void setQueuePrefetch(Integer i) {
    queuePrefetch = i;
  }

  /** @see ActiveMQPrefetchPolicy#getTopicPrefetch() */
  public Integer getTopicPrefetch() {
    return topicPrefetch;
  }

  /** @see ActiveMQPrefetchPolicy#setTopicPrefetch(int) */
  public void setTopicPrefetch(Integer i) {
    topicPrefetch = i;
  }
}
