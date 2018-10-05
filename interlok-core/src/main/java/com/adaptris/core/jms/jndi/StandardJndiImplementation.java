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

package com.adaptris.core.jms.jndi;

import javax.jms.ConnectionFactory;
import javax.jms.JMSException;

import com.adaptris.annotation.DisplayOrder;
import com.adaptris.core.jms.VendorImplementation;
import com.adaptris.util.KeyValuePairSet;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * {@link VendorImplementation} that gets a <code>ConnectionFactory</code> from the configured JNDI Store.
 * <p>
 * This implementation <b>ignores</b> any broker configuration in {@link com.adaptris.core.jms.JmsConnection}.
 * </p>
 * <p>
 * Depending on your configuration you will need have additional jars available to the Adapter that handles the specific
 * ConnectionFactories that are referenced in JNDI.
 * </p>
 * 
 * @config standard-jndi-implementation
 * 
 */
@XStreamAlias("standard-jndi-implementation")
@DisplayOrder(order = {"jndiName", "jndiParams", "enableEncodedPassword", "encodedPasswordKeys", "useJndiForQueues",
    "useJndiForTopics", "newContextOnException", "extraFactoryConfiguration"})
public class StandardJndiImplementation extends BaseJndiImplementation implements VendorImplementation {

  public StandardJndiImplementation() {
    setJndiParams(new KeyValuePairSet());
    setExtraFactoryConfiguration(new NoOpFactoryConfiguration());
  }

  public StandardJndiImplementation(String jndiName) {
    this();
    setJndiName(jndiName);
  }

  @Override
  public ConnectionFactory createConnectionFactory() throws JMSException {
    ConnectionFactory result = (ConnectionFactory) lookup(jndiName);
    getExtraFactoryConfiguration().applyConfiguration(result);
    return result;
  }

  // properties

}
