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

package com.adaptris.core.http.jetty;

import java.util.HashMap;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import javax.validation.Valid;

import org.eclipse.jetty.security.SecurityHandler;
import org.eclipse.jetty.webapp.WebAppContext;

import com.adaptris.annotation.AdapterComponent;
import com.adaptris.annotation.AdvancedConfig;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.annotation.DisplayOrder;
import com.adaptris.annotation.Removal;
import com.adaptris.core.AdaptrisConnectionImp;
import com.adaptris.core.CoreException;
import com.adaptris.core.management.webserver.JettyServerManager;
import com.adaptris.core.management.webserver.SecurityHandlerWrapper;
import com.adaptris.core.management.webserver.ServerManager;
import com.adaptris.core.management.webserver.WebServerManagementUtil;
import com.adaptris.util.TimeInterval;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * The EmbeddedConnection makes use of the existing Jetty Engine that has been enabled as part of the bootstrap process.
 * <p>
 * This is designed to be a replacement for {@link HttpConnection} and {@link HttpsConnection} and allows you to configure a single
 * jetty instance according to your requirements which can be re-used across many channels. Of course if you have not enabled a
 * global jetty instance, then exceptions will be thrown if you attempt to configure an instance of this class.
 * </p>
 * <p>
 * If you use this connection; you may have to delete {@code ROOT.war} from the jetty webapps directory. The default servlet that is
 * registered with the default web application might interfere with the registering of servlets against arbitrary locations; if you
 * get HTTP 404 errors, then enable logging for the {@code org.eclipse.jetty} category and check how the request is routed by the
 * Jetty engine.
 * </p>
 * 
 * @config jetty-embedded-connection
 * 
 */
@XStreamAlias("jetty-embedded-connection")
@AdapterComponent
@ComponentProfile(summary = "Connection that uses the embedded Jetty engine management component for requests",
    tag = "connections,http,https,jetty")
@DisplayOrder(order =
{
    "roles", "securityHandler", "maxStartupWait"
})
public class EmbeddedConnection extends AdaptrisConnectionImp implements JettyServletRegistrar {
  private static final int DEFAULT_WAIT_INTERVAL_MS = 250;

  private static final TimeInterval DEFAULT_MAX_WAIT = new TimeInterval(10l, TimeUnit.MINUTES);

  @AdvancedConfig
  @Deprecated
  @Removal(version = "3.10.0", message = "Has no meaning since you must configure a security-handler")
  private Set<String> roles;
  @AdvancedConfig
  private TimeInterval maxStartupWait;
  @Valid
  @AdvancedConfig
  private SecurityHandlerWrapper securityHandler;


  public EmbeddedConnection() {
    super();
  }


  @Override
  protected void closeConnection() {
  }

  @Override
  protected void initConnection() throws CoreException {
    waitForJettyStart(WebServerManagementUtil.getServerManager(), maxStartupWaitTimeMs());
  }

  /**
   *
   * @see com.adaptris.core.AdaptrisConnectionImp#startConnection()
   */
  @Override
  protected void startConnection() throws CoreException {
  }

  /**
   *
   * @see com.adaptris.core.AdaptrisConnectionImp#stopConnection()
   */
  @Override
  protected void stopConnection() {
  }

  @Override
  protected void prepareConnection() throws CoreException {
  }

  /**
   * 
   * @deprecated since 3.9.1 Has had no meaning since {@link #setSecurityHandler(SecurityHandlerWrapper)} was introduced
   */
  @Deprecated
  @Removal(version = "3.10.0", message = "Has no meaning since you must configure a security-handler")
  public Set<String> getRoles() {
    return roles;
  }

  /**
   * Set any roles that are required to access the consumers.
   *
   * @param roles the roles.
   * @deprecated since 3.9.1 Has had no meaning since {@link #setSecurityHandler(SecurityHandlerWrapper)} was introduced
   */
  @Deprecated
  @Removal(version = "3.10.0", message = "Has no meaning since you must configure a security-handler")
  public void setRoles(Set<String> roles) {
    this.roles = roles;
  }

  @Override
  public void addServlet(ServletWrapper wrapper) throws CoreException {
    try {
      JettyServerManager serverManager = (JettyServerManager) WebServerManagementUtil.getServerManager();
      HashMap<String, Object> additionalProperties = new HashMap<String, Object>();
      additionalProperties.put(JettyServerManager.ROLES, getRoles());
      additionalProperties.put(JettyServerManager.CONTEXT_PATH, wrapper.getUrl());
      additionalProperties.put(JettyServerManager.SECURITY_CONSTRAINTS, getSecurityHandler());
      serverManager.addServlet(wrapper.getServletHolder(), additionalProperties);
      serverManager.startDeployment(wrapper.getUrl());
      log.trace("Added " + wrapper.getServletHolder() + " against " + wrapper.getUrl());
    }
    catch (Exception ex) {
      rethrow(ex);
    }

  }

  @Override
  public void removeServlet(ServletWrapper wrapper) throws CoreException {
    try {
      JettyServerManager serverManager = (JettyServerManager) WebServerManagementUtil.getServerManager();
      serverManager.stopDeployment(wrapper.getUrl());
      serverManager.removeDeployment(wrapper.getUrl());
      serverManager.removeDeployment(wrapper.getServletHolder(), wrapper.getUrl());
      log.trace("Removed " + wrapper.getServletHolder() + " from " + wrapper.getUrl());
    }
    catch (Exception ex) {
      rethrow(ex);
    }
  }

  private void rethrow(Exception e) throws CoreException {
    if (e instanceof CoreException) {
      throw (CoreException) e;
    }
    throw new CoreException(e);
  }

  private static void waitForJettyStart(ServerManager sm, long maxWaitTime) throws CoreException {
    long totalWaitTime = 0;
    try {
      while (!sm.isStarted() && totalWaitTime < maxWaitTime) {
        Thread.sleep(DEFAULT_WAIT_INTERVAL_MS);
        totalWaitTime += DEFAULT_WAIT_INTERVAL_MS;
      }
      if (!sm.isStarted()) {
        throw new CoreException("Max Wait time exceeded : " + maxWaitTime + "ms");
      }
    }
    catch (InterruptedException e) {
      throw new CoreException(e);
    }
  }

  /**
   * @return the maxStartupWait
   */
  public TimeInterval getMaxStartupWait() {
    return maxStartupWait;
  }

  /**
   * Specify the maximum wait time for the underlying Jetty Server instance to startup.
   * <p>
   * The adapter cannot be fully initialised until the underlying {@link org.eclipse.jetty.server.Server} is ready to receive
   * messages. We need to ensure that the {@link com.adaptris.core.AdaptrisConnection#init()} method blocks until the server is ready for registration
   * of servlets and also ready for incoming HTTP requests. This value controls how long we wait for the server to start up before
   * throwing an exception.
   * </p>
   * 
   * @param t the maxStartupWait to set, default if not specified is 10 minutes.
   */
  public void setMaxStartupWait(TimeInterval t) {
    maxStartupWait = t;
  }

  long maxStartupWaitTimeMs() {
    return TimeInterval.toMillisecondsDefaultIfNull(getMaxStartupWait(), DEFAULT_MAX_WAIT);
  }

  /**
   * @return the securityHandler wrapper implementation
   */
  public SecurityHandlerWrapper getSecurityHandler() {
    return securityHandler;
  }

  /**
   * Specify the SecurityHandler implementation.
   * <p>
   * By specifying a {@link SecurityHandlerWrapper} implementation you will overwrite whatever {@link SecurityHandler}
   * implementation that is present in the {@link WebAppContext}. If you have multiple instances of {@link EmbeddedConnection}
   * configured inside your adapter, then results may be undefined; you are advised to configure a single {@code EmbeddedConnection}
   * as a {@code shared-component} to avoid any issues.
   * </p>
   * 
   * @param s the securityHandler wrapper implementation.
   */
  public void setSecurityHandler(SecurityHandlerWrapper s) {
    securityHandler = s;
  }

}
