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

package com.adaptris.core.management.webserver;

import java.util.HashMap;

import javax.servlet.Servlet;

/**
 * Interface for webserver deployment management.
 *
 * @author gcsiki
 *
 */
public interface ServerManager {

  /**
   * Query whether or not the underlying webserver is started or not.
   * 
   * @return true if the underlying webserver is started.
   *
   */
  boolean isStarted();
  /**
   * Method for adding a servlet to the server(s).
   *
   * @param servlet - The servlet to be added to the server.
   * @param additionalProperties - Additional properties needed for the deployment.
   * @throws Exception on exception.
   */
  public void addServlet(Servlet servlet, HashMap<String, Object> additionalProperties) throws Exception;

  /**
   * Method for adding a webapp (war file) to the server(s).
   *
   * @param path - The path of the webapp.
   * @param additionalProperties - Additional properties needed for the deployment.
   * @throws Exception on exception.
   */
  public void addWebapp(String path, HashMap<String, Object> additionalProperties) throws Exception;

  /**
   * Method for adding a webapp (directory) to the server(s).
   *
   * @param path - The path of the webapp.
   * @param additionalProperties - Additional properties needed for the deployment.
   * @throws Exception on exception.
   */
  public void addWebappDir(String path, HashMap<String, Object> additionalProperties) throws Exception;

  /**
   * Method for removing a deployment from the server(s). Basically this is for removing a deployment during runtime.
   * <p>
   * This is for Jetty, additional methods may be needed for other servers.
   *
   * @param contextPath - The context path of the deployment.
   * @throws Exception on exception.
   */
  public void removeDeployment(String contextPath) throws Exception;

  /**
   * If for any reason a deployment should be started.
   * <p>
   * This is for Jetty, additional methods may be needed for other servers.
   * </p>
   *
   * @throws Exception on exception.
   */
  public void startDeployment(String contextPath) throws Exception;

  /**
   * If for any reason a deployment should be stopped.
   * <p>
   * This is for Jetty, additional methods may be needed for other servers.
   * </p>
   *
   * @throws Exception on exception.
   */
  public void stopDeployment(String contextPath) throws Exception;

}
