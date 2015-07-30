/*
 * $RCSfile: BasicSonicMqImplementation.java,v $
 * $Revision: 1.5 $
 * $Date: 2009/06/03 18:57:00 $
 * $Author: lchan $
 */
package com.adaptris.core.jms.activemq;

import javax.jms.ConnectionFactory;
import javax.jms.JMSException;

import org.apache.activemq.ActiveMQConnectionFactory;

import com.adaptris.core.jms.UrlVendorImplementation;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * <p>
 * ActiveMQ implementation of <code>VendorImplementation</code>.
 * </p>
 * <p>
 * <b>This was built against ActiveMQ 5.2.0</b>
 * </p>
 * 
 * @config basic-active-mq-implementation
 * @license BASIC
 */
@XStreamAlias("basic-active-mq-implementation")
public class BasicActiveMqImplementation extends UrlVendorImplementation {


  public BasicActiveMqImplementation() {
    super();
  }

  public BasicActiveMqImplementation(String url) {
    this();
    setBrokerUrl(url);
  }

  /**
   * <p>
   * Returns a new instance of
   * <code>org.apache.activemq.ActiveMQConnectionFactory</code>.
   * </p>
   *
   * @return a <code>QueueConnectionFactory</code>
   * @throws JMSException if any occur
   */
  @Override
  public ConnectionFactory createConnectionFactory() throws JMSException {
    return create(getBrokerUrl());
  }

  protected ActiveMQConnectionFactory create(String url) {
    return new ActiveMQConnectionFactory(url);
  }

}
