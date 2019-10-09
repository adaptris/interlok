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

package com.adaptris.core.jms;

import static org.apache.commons.lang3.StringUtils.isEmpty;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import com.adaptris.interlok.resolver.ExternalResolver;
import com.adaptris.security.password.Password;

/**
 * <p>
 * Abstract factory that insulates vendor-specific code from the rest of the <code>com.adaptris.core.jms</code> package.
 * </p>
 */
public interface VendorImplementation extends VendorImplementationBase {

  /**
   * Returns a {@code ConnectionFactory}.
   * 
   * @return an instance of <code>ConnectionFactory</code>
   * @throws JMSException if any occurs
   */
  ConnectionFactory createConnectionFactory() throws JMSException;

  /**
   * Create a connection based on the factory and configuration.
   * 
   * <p>
   * If the vendor in question doesn't support the JMS 1.1 API specification (i.e. ConnectionFactory
   * doesn't expose a {@code createConnection()} method, this method should be explicitly overriden by
   * the concrete implementations to do the right thing.
   * </p>
   * 
   * @implNote the default implementation just calls {@code createConnection()} or
   *           {@code createConnection(String,String)} depending on whether a username is configured
   *           or not; this should be appropriate for all JMS 1.1 specifications.
   * @param factory the jms connection factory.
   * @param cfg the connection configuration (i.e. username/password)
   * @return
   * @throws Exception
   */
  default Connection createConnection(ConnectionFactory factory, JmsConnectionConfig cfg) throws Exception {
    Connection jmsConnection = null;
    if (isEmpty(cfg.configuredUserName())) {
      jmsConnection = factory.createConnection();
    } else {
      jmsConnection =
          factory.createConnection(cfg.configuredUserName(), Password.decode(ExternalResolver.resolve(cfg.configuredPassword())));
    }
    return jmsConnection;
  }

}
