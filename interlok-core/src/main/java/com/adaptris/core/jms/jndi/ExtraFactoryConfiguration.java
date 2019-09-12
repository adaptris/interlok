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

import javax.jms.JMSException;

/**
 * Interface that allows you to configure the ConnectionFactory that is returned from a {@link StandardJndiImplementation}.
 * <p>
 * Generally speaking, this is not encouraged, as you are now keeping configuration in 2 separate locations (both JNDI and adapter
 * config). The ConnectionFactory should ideally be configured in JNDI with all the settings that are required for each connection.
 * </p>
 *
 * @author lchan
 *
 */
public interface ExtraFactoryConfiguration {

  /**
   * Apply any additional configuration to the XA/ConnectionFactory.
   * 
   * @param cf the connection factory
   */
  void applyConfiguration(Object cf) throws JMSException;

}
