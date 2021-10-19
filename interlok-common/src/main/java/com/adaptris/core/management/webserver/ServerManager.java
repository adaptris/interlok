/*
 * Copyright 2015 Adaptris Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package com.adaptris.core.management.webserver;

import java.util.HashMap;
import java.util.Map;
import javax.servlet.Servlet;

/**
 * Interface for webserver deployment management.
 *
 * @deprecated since 4.3.0, use {@link }JettyServerManager} directly, this is a redundant interface; there are no other
 * instances other than {@link JettyServerManager} and assumption of the {@code javax.servlet} API isn't always appropriate (e.g.
 * Undertow / Quarkus).
 */
@Deprecated(since = "4.3.0", forRemoval=true)
public interface ServerManager {

  /**
   * Query whether or not the underlying webserver is started or not.
   *
   * @return true if the underlying webserver is started.
   */
  boolean isStarted();

  /**
   * Method for adding a servlet to the server(s).
   * <p>
   * Note the this method will add the servlet to all registered instances.
   * </p>
   *
   * @param servlet              - The servlet to be added to the server.
   * @param additionalProperties - Additional properties needed for the deployment.
   * @throws Exception on exception.
   */
  void addServlet(Servlet servlet, HashMap<String, Object> additionalProperties)
      throws Exception;

  /**
   * Removing a deployment from the server(s). Basically this is for removing a deployment during runtime.
   * <p>
   * This operation works on all servers currently being managed.
   * </p>
   *
   * @param contextPath - The context path of the deployment.
   * @throws Exception on exception.
   * @deprecated since 4.3.0, Use removeDeployment(String, String) instead
   */
  @Deprecated
  void removeDeployment(String contextPath) throws Exception;

  /**
   * If for any reason a deployment should be started.
   * <p>
   * This operation works on all servers currently being managed.
   * </p>
   *
   * @param contextPath - The context path of the deployment.
   * @throws Exception on exception.
   */
  void startDeployment(String contextPath) throws Exception;

  /**
   * If for any reason a deployment should be stopped.
   *
   * @param contextPath - The context path of the deployment.
   * @throws Exception on exception.
   */
  void stopDeployment(String contextPath) throws Exception;


}
