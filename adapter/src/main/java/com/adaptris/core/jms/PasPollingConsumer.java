/*
 * $RCSfile: PasPollingConsumer.java,v $
 * $Revision: 1.16 $
 * $Date: 2009/02/17 12:32:03 $
 * $Author: lchan $
 */
package com.adaptris.core.jms;

import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.NotBlank;

import com.adaptris.core.ConsumeDestination;
import com.adaptris.core.CoreException;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * <p>
 * PAS implementation of <code>JmsPollingConsumer</code>. The consumer created by this class is always durable and thus requires the
 * clientID and subscriptionId to be set. It is up to the user to ensure that these are set such that this consumer is uniquely
 * identified in the context of the broker's other consumers.
 * </p>
 * 
 * @config jms-topic-poller
 * @license BASIC, additional license requirements from the chosen MessageTypeTranslator and VendorImplementation
 */
@XStreamAlias("jms-topic-poller")
public class PasPollingConsumer extends JmsPollingConsumerImpl {

  @NotNull
  @NotBlank
  private String subscriptionId;

  /**
   * <p>
   * Creates a new instance.
   * </p>
   */
  public PasPollingConsumer() {
    super();
    this.setSubscriptionId("");
  }

  public PasPollingConsumer(ConsumeDestination d) {
    this();
    setDestination(d);
  }


  /** @see com.adaptris.core.AdaptrisComponent#init() */
  @Override
  public void init() throws CoreException {
    if (this.getSubscriptionId() == null || "".equals(this.getSubscriptionId())) {

      throw new CoreException("subscriptionId must be set");
    }

    if (this.getClientId() == null || "".equals(this.getClientId())) {
      throw new CoreException("clientId must be set");
    }

    log.trace("client ID [" + this.getClientId() + "] subscription ID ["
        + this.getSubscriptionId() + "]");

    super.init();
  }

  @Override
  protected MessageConsumer createConsumer() throws JMSException {
    return getVendorImplementation().createTopicSubscriber(this.getDestination(), this.getSubscriptionId(), this);
  }

  /**
   * <p>
   * Returns the subscription ID to use.
   * </p>
   *
   * @return subscriptionId the subscription ID to use
   */
  public String getSubscriptionId() {
    return this.subscriptionId;
  }

  /**
   * <p>
   * Sets the subscription ID to use. This, in combination with the clientId
   * should uniquely identify this subscription in the context of the broker.
   * </p>
   *
   * @param s the subscription ID to use
   * @see JmsPollingConsumerImpl
   */
  public void setSubscriptionId(String s) {
    if (s == null) {
      throw new IllegalArgumentException("null param");
    }
    this.subscriptionId = s;
  }
}
