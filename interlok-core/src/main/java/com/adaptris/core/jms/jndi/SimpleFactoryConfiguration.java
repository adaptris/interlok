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
import javax.jms.XAConnectionFactory;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import org.apache.commons.lang3.BooleanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.adaptris.annotation.AutoPopulated;
import com.adaptris.core.util.Args;
import com.adaptris.util.KeyValuePair;
import com.adaptris.util.KeyValuePairSet;
import com.adaptris.util.SimpleBeanUtil;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * {@link ExtraFactoryConfiguration} implementation using reflection to configure fields on the
 * ConnectionFactory.
 * 
 * <p>
 * This implementation uses reflection to configure fields on the ConnectionFactory after it has
 * been returned from the JNDI store. Generally speaking, this is not encouraged, as you are now
 * keeping configuration in 2 separate locations (both JNDI and adapter config). The
 * ConnectionFactory should be configured in JNDI with all the settings that are required for each
 * connection.
 * </p>
 * 
 * <p>
 * As the name suggests, this is a very simple implementation, primitive values are supported along
 * with strings, but not objects. Every fieldname referenced is expected to have an associated
 * method set[fieldname] which has a single parameter; the match for which is case-insensitive. If
 * you have more more complex requirements then you will have to write your own implementation of
 * {@link ExtraFactoryConfiguration}.
 * </p>
 * 
 * 
 * @config simple-jndi-factory-configuration
 */
@XStreamAlias("simple-jndi-factory-configuration")
public class SimpleFactoryConfiguration implements ExtraFactoryConfiguration {

  private transient Logger log = LoggerFactory.getLogger(this.getClass());

  @NotNull
  @AutoPopulated
  @Valid
  private KeyValuePairSet properties;

  public SimpleFactoryConfiguration() {
    setProperties(new KeyValuePairSet());
  }

  @Override
  public void applyConfiguration(Object cf) throws JMSException {
    if (BooleanUtils
        .or(new boolean[] {cf instanceof ConnectionFactory, cf instanceof XAConnectionFactory})) {
      getProperties().stream().forEach((kvp) -> SimpleBeanUtil.callSetter(cf, "set" + kvp.getKey(), kvp.getValue()));
    } else {
      throw new JMSException("Object to apply configuration is not a XA/ConnectionFactory.");
    }
  }

  public KeyValuePairSet getProperties() {
    return properties;
  }

  /**
   * Set any extra properties that need to be configured on the connection factory.
   * <p>
   * The key portion of the underlying {@link KeyValuePair} should match the name of the underlying ConnectionFactory field, the
   * value is the parameter to the associated setter. Note that only primitive types are supported (long, string, boolean, int,
   * float, double).
   * </p>
   * <p>
   * If, for instance, you are looking up a Aurea SonicMQ connection factory from JNDI, and you wished to change the ConnectID
   * field, then the following configuration would be appropriate.
   * 
   * <pre>
   * {@code 
   *   <properties>
   *     <key-value-pair>
   *        <key>ConnectID</key>
   *        <value>MyConnectId</value>
   *     </key-value-pair>
   *   </properties>
   * }
   * </pre>
   * which will invoke the associated setConnectID(String), setting the ConnectID property to 'MyConnectId'.
   * </p>
   * *
   * 
   * @param extras
   */
  public void setProperties(KeyValuePairSet extras) {
    properties = Args.notNull(extras, "properties");
  }
}
