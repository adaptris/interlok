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

package com.adaptris.core.runtime;

import com.adaptris.core.AdaptrisConnection;
import com.adaptris.core.CoreException;
import com.adaptris.interlok.types.SerializableMessage;

/**
 * MBean for the UI to ask the adapter to test configuration.
 * 
 * @author lchan
 * 
 */
public interface AdapterComponentCheckerMBean extends BaseComponentMBean {

  String COMPONENT_CHECKER_TYPE = AdapterComponentMBean.JMX_DOMAIN_NAME + ":type=ComponentChecker";


  /**
   * Check that this XML will initialise.
   * 
   * @param xml the XML configuration, which could be a service, or a connection.
   * @throws CoreException wrapping any other exception.
   */
  void checkInitialise(String xml) throws CoreException;

  /**
   * Apply the configured services to the msg.
   * 
   * @param xml String XML representation of the service (or service-list)
   * @param msg the message.
   * @return the result of applying these services.
   * @throws CoreException wrapping any other exception
   * @deprecated since 3.7.0 use {@link #applyService(String, SerializableMessage, boolean)} instead.
   */
  @Deprecated
  SerializableMessage applyService(String xml, SerializableMessage msg) throws CoreException;

  /**
   * Apply the configured services to the msg.
   * 
   * @param xml String XML representation of the service (or service-list)
   * @param msg the message.
   * @param rewriteConnections use {@link AdaptrisConnection#cloneForTesting()} to generate a new connection.
   * @return the result of applying these services.
   * @throws CoreException wrapping any other exception
   */
  SerializableMessage applyService(String xml, SerializableMessage msg, boolean rewriteConnections) throws CoreException;

}
