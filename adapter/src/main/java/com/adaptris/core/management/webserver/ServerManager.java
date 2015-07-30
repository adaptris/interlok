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
