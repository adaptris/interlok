/*
 * Copyright 2015 Adaptris Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.adaptris.core.management.webserver;

import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.servlet.Servlet;
import javax.servlet.ServletContext;

import org.eclipse.jetty.security.Authenticator;
import org.eclipse.jetty.security.Authenticator.AuthConfiguration;
import org.eclipse.jetty.security.ConstraintSecurityHandler;
import org.eclipse.jetty.security.IdentityService;
import org.eclipse.jetty.security.LoginService;
import org.eclipse.jetty.security.SecurityHandler;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.AbstractHandlerContainer;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;
import org.eclipse.jetty.server.handler.HandlerCollection;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.servlet.ServletMapping;
import org.eclipse.jetty.webapp.Configuration;
import org.eclipse.jetty.webapp.WebAppContext;
import org.eclipse.jetty.webapp.WebXmlConfiguration;

import com.adaptris.interlok.util.Args;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Synchronized;
import lombok.extern.slf4j.Slf4j;

/**
 * Implementation of the {@link ServerManager} interface for managing Jetty servers.
 * <p>
 * This class only deals with a specific handler structure. The uppermost handler must be a HandlerCollection and we add the
 * deployment to all of it's appropriate childhandlers.
 * </p>
 *
 * @author gcsiki
 *
 */
@Slf4j
public final class JettyServerManager implements ServerManager {

  public static final String DEFAULT_DESCRIPTOR_XML = "com/adaptris/core/management/webserver/jetty-webdefault-failsafe.xml";
  public static final String DEFAULT_JETTY_XML = "com/adaptris/core/management/webserver/jetty-failsafe.xml";

  private static final String OVERRIDE_DESCRIPTOR_XML = "jetty-webdefault.xml";

  /**
   * System property that controls whether or not starting the {@code WebAppContext} should throw an exception or not.
   * <p>
   * The default is false for backwards compatibility; but can be toggled to true; it will be defaulted to true in a future release.
   * </p>
   */
  public static final String SYS_PROP_THROW_UNAVAILABLE_EXCEPTION = "interlok.jetty.throw.unavailable.on.startup";

  private static final boolean THROW_UNAVAILABLE_ON_START = Boolean.getBoolean(SYS_PROP_THROW_UNAVAILABLE_EXCEPTION);

  public static final String CONTEXT_PATH = "contextPath";

  public static final String SECURITY_CONSTRAINTS = "securityConstraints";

  /**
   * Enable additional debug logging by specifying the system property {@code interlok.jetty.debug}
   * to true.
   *
   */
  public static final boolean JETTY_DEBUG =
      Boolean.getBoolean("adp.jetty.debug") || Boolean.getBoolean("interlok.jetty.debug");

  /**
   * This string is for adding a attribute to the ContextHandler for servlets, because if we set the contextPath on the handler and
   * not on the ServletHolder then the tests fail. (Absolutely no idea why...)
   */
  private static final String SERVLET_CONTEXT_PATH_ATTRIBUTE = "servletContextPathAttribute";

  private static final JettyServerManager INSTANCE = new JettyServerManager();

  /**
   * This list contains all the configured Jetty servers for the adapter.
   */
  @Getter(AccessLevel.PRIVATE)
  private final Map<String, Server> servers;

  private final transient Object locker = new Object();

  JettyServerManager() {
    servers = Collections.synchronizedMap(new HashMap<>());
  }

  public static JettyServerManager getInstance() {
    return INSTANCE;
  }

  /** Add a server.
   *  @deprecated since 4.3.0; use addServer(String, Server) instead.
   */
  @Deprecated(since = "4.3.0")
  public void addServer(Server server) {
    String key = UUID.randomUUID().toString();
    log.warn("No Key provided; generated key is {}", key);
    addServer(key, server);
  }

  /** Remove a server
   *  @deprecated since 4.3.0; use removeServer(String) instead.
   */
  @Deprecated(since = "4.3.0")
  public void removeServer(Server server) {
    String key = null;
    for (Map.Entry<String, Server> e : getServers().entrySet()) {
      if (e.getValue() == server) {
        key = e.getKey();
        break;
      }
    }
    removeServer(key);
  }

  /** Add a server.
   *
   * @param key the key
   * @param server the underlying jetty server
   */
  public void addServer(String key, Server server) {
    servers.put(Args.notNull(key, "server-id"), server);
  }

  /** Remove a server
   *
   * @param key the key.
   */
  public void removeServer(String key) {
    servers.remove(key);
  }

  @Override
  public boolean isStarted() {
    if (servers.size() == 0) {
      // no servers, we can't be started
      return false;
    }
    int result = 0;
    for (Server s : getServers().values()) {
      result += s.isStarted() ? 1 : 0;
    }
    return result == servers.size();
  }

  @Deprecated
  @Override
  public void addServlet(Servlet servlet, HashMap<String, Object> additionalProperties) throws Exception {
    addServlet(new ServletHolder(servlet), additionalProperties);
  }

  /**
   * This will add the servlet to all underlying server instances and is discouraged.
   *
   * @deprecated since 4.3.0 use {@link #addServlet(String, ServletHolder, Map)}
   *             instead. This will be removed without warning.
   */
  @Deprecated(since="4.3.0")
  @Synchronized("locker")
  // Note, only used by EmbeddedConnection so this can be removed at any time.
  public void addServlet(ServletHolder servlet, HashMap<String, Object> props)
      throws Exception {
    int timesAdded = 0;
    for (Server server : servers.values()) {
      timesAdded += addServlet(server, servlet, props) ? 1 : 0;
    }
    if (timesAdded <= 0) {
      throw new Exception("Couldn't add servlet to any contexts");
    }
  }

  /** Add a servlet to the corresponding server instance.
   *
   * @param serverId the id of the {@code Server} instance.
   * @param servlet the servlet
   * @param additionalProperties and additional properties to assist deployment
   * @throws Exception on exception
   * @see #addServlet(String, ServletHolder, Map)
   */
  public void addServlet(String serverId, Servlet servlet, Map<String, Object> additionalProperties)
      throws Exception {
    addServlet(serverId, new ServletHolder(servlet), additionalProperties);
  }

  /**
   * @param serverId the id of the {@code Server} instance.
   * @param servlet the servlet
   * @param props and additional properties to assist deployment
   * @throws Exception on exception
   * @return true if the servlet was added
   */
  @Synchronized("locker")
  public boolean addServlet(String serverId, ServletHolder servlet, Map<String, Object> props) throws Exception {
    Server server = Args.notNull(getServers().get(serverId), "server");
    return addServlet(server, servlet, props);
  }

  @Override
  @Synchronized("locker")
  @Deprecated
  public void removeDeployment(String contextPath) throws Exception {
    for (Server server : getServers().values()) {
      destroyContext(contextPath, server.getHandler());
    }
  }

  /** Remove a deployment from the corresponding {@code Server} instance.
   *
   * @param serverId the id of the {@code Server} instance.
   * @param contextPath the path
   * @throws Exception on exception
   */
  @Synchronized("locker")
  public void removeDeployment(String serverId, String contextPath) throws Exception {
    Server server = Args.notNull(getServers().get(serverId), "server");
    destroyContext(contextPath, server.getHandler());
  }

  /**
   * This will remove the servlet from all underlying server instances and is discouraged (but not
   * as much as adding the servlet to all instances).
   *
   * @deprecated since 4.3.0 use {@link #removeDeployment(String, ServletHolder, String)} instead.
   *             This will be removed without warning.
   */
  // Note, only used by EmbeddedConnection so this can be removed at any time.
  @Deprecated(since="4.3.0")
  public void removeDeployment(ServletHolder holder, String path) throws Exception {
    for (String serverId : getServers().keySet()) {
      removeDeployment(serverId, holder, path);
    }
  }

  /** Remove a deployment.
   *
   * @param serverId the id of the {@code Server} instance.
   * @param holder the servlet to remove
   * @param path the path
   * @throws Exception on exception
   */
  @Synchronized("locker")
  public void removeDeployment(String serverId, ServletHolder holder, String path) throws Exception {
    Server server = Args.notNull(getServers().get(serverId), "server");
    WebAppContext rootWar = findRootContext(server, false);
    if (rootWar != null) {
      removeServlet(rootWar, holder, path);
    }
  }


  @Override
  @Deprecated
  public void startDeployment(String contextPath) throws Exception {
    for (String serverId : getServers().keySet()) {
      startDeployment(serverId, contextPath);
    }
  }

  /** Start a deployment
   *
   * @param serverId the id of the {@code Server} instance.
   * @param contextPath the path
   * @throws Exception on exception
   */
  @Synchronized("locker")
  public void startDeployment(String serverId, String contextPath) throws Exception {
    Server server = Args.notNull(getServers().get(serverId), "server");
    if (server.isStarted()) {
      manageHandler(contextPath, true, server.getHandler());
    }
  }



  @Override
  @Deprecated
  public void stopDeployment(String contextPath) throws Exception {
    for (String serverId : getServers().keySet()) {
      stopDeployment(serverId, contextPath);
    }
  }


  /** Stop a deployment
   *
   * @param serverId the id of the {@code Server} instance.
   * @param contextPath the path
   * @throws Exception on exception
   */
  @Synchronized("locker")
  public void stopDeployment(String serverId, String contextPath) throws Exception {
    Server server = Args.notNull(getServers().get(serverId), "server");
    if (server.isStarted()) {
      WebAppContext rootWar = findRootContext(server, false);
      if (rootWar != null) {
        unmapServlet(rootWar, contextPath);
      }
      manageHandler(contextPath, false, server.getHandler());
    }
  }


  private boolean addServlet(Server server, ServletHolder servlet, Map<String, Object> additionalProperties) throws Exception {
    // this will always create a rootWar
    WebAppContext rootWar = findRootContext(server, true);
    reconfigureWar(rootWar, servlet, additionalProperties);
    debugLogging("{}: Current Servlet Handler: {}", rootWar.getWar(), rootWar.getServletHandler().dump());
    debugLogging("{}: Current Security: {}", rootWar.getWar(), rootWar.getSecurityHandler().dump());
    return true;
  }

  private void reconfigureWar(WebAppContext rootWar, ServletHolder servlet, Map<String, Object> props)
      throws Exception {
    // Have to stop the WAR before we can reconfigure the security handler, not true if we just want
    // to add a new servlet; but it's probaby good practice to.
    rootWar.stop();
    rootWar.setThrowUnavailableOnStartupException(THROW_UNAVAILABLE_ON_START);
    String pathSpec = (String) props.get(CONTEXT_PATH);
    log.trace("Adding servlet to existing ROOT WebAppContext against {}", pathSpec);
    rootWar.addServlet(servlet, pathSpec);
    SecurityHandlerWrapper w = (SecurityHandlerWrapper) props.get(SECURITY_CONSTRAINTS);
    if (w != null) {
      rootWar.setSecurityHandler(w.createSecurityHandler());
    }
    rootWar.start();
  }

  private WebAppContext findRootContext(Server server, boolean create)
      throws Exception {
    WebAppContext root = rootContextFromHandler(server.getHandler());
    if (root == null && create) {
      log.trace("No ROOT WebAppContext, creating one");
      root = new WebAppContext();
      root.setContextPath("/");
      root.setSecurityHandler(defaultSecurityStub());
      URL defaultsURL = findDefaultDescriptorXML();
      log.trace("Using default descriptor [{}]", defaultsURL);
      root.setDefaultsDescriptor(defaultsURL.toString());
      root.setConfigurations(new Configuration[]
          {
              new WebXmlConfiguration() {
              }
          });
      ContextHandlerCollection c = firstContextHandler(server.getHandler());
      if (c != null) {
        c.addHandler(root);
      }
      else {
        // Well, we could go down a rabbit warren here, but screw it
        // We make our root the root.
        server.setHandler(root);
      }
    }
    return root;
  }

  // Will be reconfigured as required, in the absence of explicit config
  // 9.4.44.v20210927 causes JASPI to come into play which ultimately causes
  // a NPE because not everything required by jaspi is in play...
  // This is related to javaee / java.auth.security.message
  // c.f. SecurityHandler#doStart() -> and the section about
  // getKnownAuthenticatorFactories()...
  static SecurityHandler defaultSecurityStub() {
    ConstraintSecurityHandler defaultSecurity = new ConstraintSecurityHandler();
    defaultSecurity.setAuthenticatorFactory(new Authenticator.Factory() {

      @Override
      public Authenticator getAuthenticator(Server server, ServletContext context,
          AuthConfiguration configuration, IdentityService identityService,
          LoginService loginService) {
        return null;
      }

    });
    return defaultSecurity;
  }

  private URL findDefaultDescriptorXML() {
    URL defaultsURL = getClass().getClassLoader().getResource(OVERRIDE_DESCRIPTOR_XML);
    // if null, then jetty-webdefault-failsafe.xml is used, which always exists in the jar file.
    if (defaultsURL == null) {
      defaultsURL = getClass().getClassLoader().getResource(DEFAULT_DESCRIPTOR_XML);
    }
    return defaultsURL;
  }

  private WebAppContext rootContextFromHandler(Handler parent) {
    if (parent == null) {
      return null;
    }
    WebAppContext result = null;
    if (parent instanceof WebAppContext) {
      WebAppContext ctx = (WebAppContext) parent;
      if ("/".equals(ctx.getContextPath())) {
        result = ctx;
      }
    }
    else if (parent instanceof HandlerCollection) {
      Handler[] children = ((HandlerCollection) parent).getChildHandlers();
      for (Handler child : children) {
        result = rootContextFromHandler(child);
        if (result != null) {
          break;
        }
      }
    }
    return result;
  }

  private ContextHandlerCollection firstContextHandler(Handler parent) {
    ContextHandlerCollection result = null;
    if (parent == null) {
      return null;
    }
    if (parent instanceof ContextHandlerCollection) {
      result = (ContextHandlerCollection) parent;
    }
    else if (parent instanceof HandlerCollection) {
      Handler[] children = ((HandlerCollection) parent).getChildHandlers();
      for (Handler child : children) {
        result = firstContextHandler(child);
        if (result != null) {
          break;
        }
      }
    }
    return result;
  }


  private void removeServlet(WebAppContext webAppContext, ServletHolder toRemove, String pathSpec) throws Exception {
    log.trace("{}: Removing servlet mapped to {}", webAppContext.getWar(), pathSpec);
    List<ServletHolder> servletsToKeep = new ArrayList<>();
    List<ServletMapping> mappingsToKeep = new ArrayList<>();
    ServletHandler handler = webAppContext.getServletHandler();
    Set<String> names = new HashSet<>();
    debugLogging("{}: Current Mappings: {}", webAppContext.getWar(), Arrays.asList(handler.getServletMappings()));
    debugLogging("{}: Current Servlets: {}", webAppContext.getWar(), Arrays.asList(handler.getServlets()));
    for (ServletHolder holder : handler.getServlets()) {
      if (toRemove.equals(holder)) {
        names.add(holder.getName());
      } else {
        servletsToKeep.add(holder);
      }
    }
    for (ServletMapping mapping : handler.getServletMappings()) {
      if (!names.contains(mapping.getServletName())) {
        mappingsToKeep.add(mapping);
      }
    }
    debugLogging("{}: Mappings Kept: {}", webAppContext.getWar(), mappingsToKeep);
    debugLogging("{}: Servlets Kept: {}", webAppContext.getWar(), servletsToKeep);
    handler.setServletMappings(mappingsToKeep.toArray(new ServletMapping[0]));
    handler.setServlets(servletsToKeep.toArray(new ServletHolder[0]));
  }

  private void unmapServlet(WebAppContext webAppContext, String contextPath) throws Exception {
    log.trace("{}: Unmapping servlet against {}", webAppContext.getWar(), contextPath);
    ServletHandler handler = webAppContext.getServletHandler();
    List<ServletMapping> mappings = new ArrayList<>();
    debugLogging("{}: Current Mappings: {}", webAppContext.getWar(), Arrays.asList(handler.getServletMappings()));
    for (ServletMapping mapping : handler.getServletMappings()) {
      List<String> pathSpecs = Arrays.asList(mapping.getPathSpecs());
      if (!pathSpecs.contains(contextPath)) {
        mappings.add(mapping);
      }
    }
    debugLogging("{}: Mappings Kept: {}", webAppContext.getWar(), mappings);
    handler.setServletMappings(mappings.toArray(new ServletMapping[0]));
  }

  private boolean destroyContext(String contextPath, Handler handler) throws Exception {
    if (handler instanceof HandlerCollection) {
      HandlerCollection hc = (HandlerCollection) handler;
      Handler[] childHandlers = hc.getChildHandlers();
      for (Handler childHandler : childHandlers) {
        boolean contextDestroyed = destroyContext(contextPath, childHandler);
        if (contextDestroyed) {
          removeQuietly(hc, childHandler);
        }
      }
    } else if (handler instanceof SecurityHandler) {
      SecurityHandler sh = (SecurityHandler) handler;
      Handler[] childHandlers = sh.getChildHandlersByClass(HandlerCollection.class);
      for (Handler childHandler : childHandlers) {
        destroyContext(contextPath, childHandler);
      }
    } else if (handler instanceof ServletContextHandler) {
      ServletContextHandler sch = (ServletContextHandler) handler;
      String servletPath = (String) sch.getAttribute(SERVLET_CONTEXT_PATH_ATTRIBUTE);
      if (contextPath.equals(servletPath)) {
        destroyQuietly(sch);
        return true;
      } else if (contextPath.equals(sch.getContextPath())) {
        destroyQuietly(sch);
        return true;
      }
    } else if (handler instanceof ContextHandler) {
      ContextHandler ch = (ContextHandler) handler;
      if (contextPath.equals(ch.getContextPath())) {
        destroyQuietly(ch);
        return true;
      }
    }
    return false;
  }

  private void destroyQuietly(ContextHandler ch) {
    log.trace("Destroying {}({})", ch, ch.getAttribute(SERVLET_CONTEXT_PATH_ATTRIBUTE));
    try {
      ch.stop();
      ch.destroy();
    } catch (Exception e) {

    }
  }

  private void removeQuietly(HandlerCollection parent, Handler child) {
    try {
      parent.removeHandler(child);
    } catch (Exception e) {
      ;
    }
  }


  private void manageHandler(String contextPath, boolean start, Handler handler) throws Exception {
    if (handler instanceof ContextHandler) {
      ContextHandler contextHandler = (ContextHandler) handler;
      if (contextPath.equals(contextHandler.getContextPath())
          || contextPath.equals(contextHandler.getAttribute(SERVLET_CONTEXT_PATH_ATTRIBUTE))) {
        if (start) {
          contextHandler.start();
        } else {
          contextHandler.stop();
        }
      }
    }
    if (handler instanceof AbstractHandlerContainer) {
      AbstractHandlerContainer ahc = (AbstractHandlerContainer) handler;
      Handler[] childHandlers = ahc.getChildHandlers();
      for (Handler childHandler : childHandlers) {
        manageHandler(contextPath, start, childHandler);
      }
    }
  }

  private void debugLogging(String text, Object... args) {
    if (JETTY_DEBUG && log.isTraceEnabled()) {
      log.trace(text, args);
    }
  }

}
