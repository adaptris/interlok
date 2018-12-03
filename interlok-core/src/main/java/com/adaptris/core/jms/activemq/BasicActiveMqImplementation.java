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

import javax.jms.ConnectionFactory;
import javax.jms.JMSException;

import org.apache.activemq.ActiveMQConnectionFactory;

import com.adaptris.annotation.DisplayOrder;
import com.adaptris.core.jms.UrlVendorImplementation;
import com.adaptris.core.jms.VendorImplementation;
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
 * 
 */
@XStreamAlias("basic-active-mq-implementation")
@DisplayOrder(order = {"brokerUrl"})
public class BasicActiveMqImplementation extends UrlVendorImplementation implements VendorImplementation {


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
