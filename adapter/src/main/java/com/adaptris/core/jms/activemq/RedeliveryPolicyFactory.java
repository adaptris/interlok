package com.adaptris.core.jms.activemq;

import org.apache.activemq.RedeliveryPolicy;

import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Proxy class for creating RedeliveryPolicy objects
 * 
 * <p>
 * This class is simply a class that can be marshalled correctly.
 * </p>
 * *
 * <p>
 * If fields are not explicitly set, then the corresponding {@link RedeliveryPolicy} method will not be invoked.
 * </p>
 * 
 * @config activemq-redelivery-policy
 * @author lchan
 * 
 */
@XStreamAlias("activemq-redelivery-policy")
public class RedeliveryPolicyFactory {
  private Short collisionAvoidancePercent;
  private Double backOffMultiplier;
  private Long initialRedeliveryDelay;
  private Integer maximumRedeliveries;
  private Boolean useCollisionAvoidance, useExponentialBackOff;

  /**
   * Default constructor.
   * <p>
   * All fields are initialised to be null.
   * </p>
   */
  public RedeliveryPolicyFactory() {
  }

  /**
   * Create a RedeliveryPolicy.
   *
   * @return a RedeliveryPolicy
   */
  public RedeliveryPolicy create() {
    RedeliveryPolicy p = new RedeliveryPolicy();
    if (getBackOffMultiplier() != null) {
      p.setBackOffMultiplier(getBackOffMultiplier());
    }
    if (getCollisionAvoidancePercent() != null) {
      p.setCollisionAvoidancePercent(getCollisionAvoidancePercent());
    }
    if (getInitialRedeliveryDelay() != null) {
      p.setInitialRedeliveryDelay(getInitialRedeliveryDelay());
    }
    if (getMaximumRedeliveries() != null) {
      p.setMaximumRedeliveries(getMaximumRedeliveries());
    }
    if (getUseCollisionAvoidance() != null) {
      p.setUseCollisionAvoidance(getUseCollisionAvoidance());
    }
    if (getUseExponentialBackOff() != null) {
      p.setUseExponentialBackOff(getUseExponentialBackOff());
    }
    return p;
  }

  /** @see RedeliveryPolicy#getBackOffMultiplier() */
  public Double getBackOffMultiplier() {
    return backOffMultiplier;
  }

  /** @see RedeliveryPolicy#setBackOffMultiplier(double) */
  public void setBackOffMultiplier(Double s) {
    backOffMultiplier = s;
  }

  /** @see RedeliveryPolicy#getCollisionAvoidancePercent() */
  public Short getCollisionAvoidancePercent() {
    return collisionAvoidancePercent;
  }

  /** @see RedeliveryPolicy#setCollisionAvoidancePercent(short) */
  public void setCollisionAvoidancePercent(Short s) {
    collisionAvoidancePercent = s;
  }

  /** @see RedeliveryPolicy#getInitialRedeliveryDelay() */
  public Long getInitialRedeliveryDelay() {
    return initialRedeliveryDelay;
  }

  /** @see RedeliveryPolicy#setInitialRedeliveryDelay(long) */
  public void setInitialRedeliveryDelay(Long l) {
    initialRedeliveryDelay = l;
  }

  /** @see RedeliveryPolicy#getMaximumRedeliveries() */
  public Integer getMaximumRedeliveries() {
    return maximumRedeliveries;
  }

  /** @see RedeliveryPolicy#setMaximumRedeliveries(int) */
  public void setMaximumRedeliveries(Integer i) {
    maximumRedeliveries = i;
  }

  /** @see RedeliveryPolicy#isUseCollisionAvoidance() */
  public Boolean getUseCollisionAvoidance() {
    return useCollisionAvoidance;
  }

  /** @see RedeliveryPolicy#setUseCollisionAvoidance(boolean) */
  public void setUseCollisionAvoidance(Boolean b) {
    useCollisionAvoidance = b;
  }

  /** @see RedeliveryPolicy#isUseExponentialBackOff() */
  public Boolean getUseExponentialBackOff() {
    return useExponentialBackOff;
  }

  /** @see RedeliveryPolicy#setUseExponentialBackOff(boolean) */
  public void setUseExponentialBackOff(Boolean b) {
    useExponentialBackOff = b;
  }
}
