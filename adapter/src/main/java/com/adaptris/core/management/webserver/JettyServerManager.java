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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.servlet.Servlet;

import org.eclipse.jetty.security.ConstraintMapping;
import org.eclipse.jetty.security.ConstraintSecurityHandler;
import org.eclipse.jetty.security.SecurityHandler;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.AbstractHandlerContainer;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.server.handler.HandlerCollection;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.servlet.ServletMapping;
import org.eclipse.jetty.util.security.Constraint;
import org.eclipse.jetty.webapp.WebAppContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
public class JettyServerManager implements ServerManager {

  public static final String HOST = "host";
  public static final String CONNECTOR_NAMES = "connectorNames";
  public static final String CONTEXT_PATH = "contextPath";
  public static final String ROLES = "roles";

  /**
   * Enable additional debug logging by specifying the system property {@code adp.jetty.debug} to true.
   * 
   */
  public static final boolean JETTY_DEBUG = Boolean.getBoolean("adp.jetty.debug");
  private Logger log = LoggerFactory.getLogger(this.getClass());

  /**
   * This string is for adding a attribute to the ContextHandler for servlets, because if we set the contextPath on the handler and
   * not on the ServletHolder then the tests fail. (Absolutely no idea why...)
   */
  private static final String SERVLET_CONTEXT_PATH_ATTRIBUTE = "servletContextPathAttribute";

  /**
   * This list contains all the configured Jetty servers for the adapter.
   */
  private List<Server> servers;

  public JettyServerManager() {
    servers = new ArrayList<Server>();
  }

  public void addServer(Server server) {
    servers.add(server);
  }

  public void removeServer(Server server) {
    servers.remove(server);
  }

  @Override
  public boolean isStarted() {
    int result = 0;
    for (Server server : servers) {
      result += server.isStarted() ? 1 : 0;
    }
    return result == servers.size();
  }

  @Override
  public void addServlet(Servlet servlet, HashMap<String, Object> additionalProperties) throws Exception {
    addServlet(new ServletHolder(servlet), additionalProperties);
  }

  public void addServlet(ServletHolder servlet, HashMap<String, Object> additionalProperties) throws Exception {
    boolean addedAtLeastOnce = false;
    for (Server server : servers) {
      WebAppContext rootWar = findRootContext(server);
      if (rootWar != null) {
        log.trace("Adding servlet to existing ROOT.war");
        rootWar.addServlet(servlet, (String) additionalProperties.get(CONTEXT_PATH));
        debugLogging("{}: Current Servlet Mappings: {}", rootWar.getWar(),
            Arrays.asList(rootWar.getServletHandler().getServletMappings()));
        debugLogging("{}: Current Servlets: {}", rootWar.getWar(), Arrays.asList(rootWar.getServletHandler().getServlets()));
        // log.trace("ROOT.war config : {}", AggregateLifeCycle.dump(rootWar));
        addedAtLeastOnce = true;
      }
    }
    if (!addedAtLeastOnce) {
      addDeployment(
          additionalProperties,
          createServletHandler(servlet, (String) additionalProperties.get(CONTEXT_PATH),
              (String) additionalProperties.get(CONNECTOR_NAMES)));
    }

  }

  @Override
  public void addWebapp(String path, HashMap<String, Object> additionalProperties) throws Exception {
    addDeployment(
        additionalProperties,
        createWebappHandler(path, (String) additionalProperties.get(CONTEXT_PATH),
            (String) additionalProperties.get(CONNECTOR_NAMES)));
  }

  @Override
  public void addWebappDir(String path, HashMap<String, Object> additionalProperties) throws Exception {
    addDeployment(
        additionalProperties,
        createWebappDirHandler(path, (String) additionalProperties.get(CONTEXT_PATH),
            (String) additionalProperties.get(CONNECTOR_NAMES)));
  }

  /**
   * Adds the deployment handler to the server and creates the additional configuration if needed.
   * 
   * @param additionalProperties - The properties that specify the host, port and other parameters for the deployment.
   * @param handler - The deployment handler.
   * @throws Exception
   */
  @SuppressWarnings("unchecked")
  private void addDeployment(HashMap<String, Object> additionalProperties, ServletContextHandler handler) throws Exception {
    for (Server server : servers) {
      String host = (String) additionalProperties.get(HOST);
      Set<String> roles = null;
      try {
        roles = (Set<String>) additionalProperties.get(ROLES);
      }
      catch (Exception ex) {
      }
      if (host == null) {
        addHandlerToServer(server, handler, roles);
      }
      else {
        Connector[] connectors = server.getConnectors();
        for (Connector connector : connectors) {
          if (host.equalsIgnoreCase(connector.getHost())) {
            addHandlerToServer(server, handler, roles);
            break;
          }
        }
      }
    }
  }

  /**
   * Adds the handler to all appropriate configured handlers in the Jetty instance. The outmost handler should be a
   * HandlerCollection otherwise the method throws an exception.
   * 
   * @param server - The server to which the deployment will be added.
   * @param deploymentHandler - The handler for the deployment.
   * @param roles - The roles for the deployment.
   * @throws Exception
   */
  private void addHandlerToServer(Server server, ServletContextHandler deploymentHandler, Set<String> roles) throws Exception {
    Handler handler = server.getHandler();
    log.trace("Adding Handler roles : {}", roles);
    if (handler == null) {
      throw new NullPointerException("No configured handler!");
    }
    else if (handler instanceof HandlerCollection) {
      HandlerCollection outer = (HandlerCollection) handler;

      if (roles == null) {
        Handler[] handlers = outer.getHandlers();
        for (Handler childHandler : handlers) {
          if (childHandler instanceof HandlerCollection) {
            HandlerCollection inner = (HandlerCollection) childHandler;
            debugLogging("Adding {} to handler {}", deploymentHandler.getDisplayName(), inner);
            inner.addHandler(deploymentHandler);
          }
        }        
      }
      else {
        Handler[] handlers = outer.getHandlers();
        for (Handler childHandler : handlers) {
          if (childHandler instanceof ConstraintSecurityHandler) {
            ConstraintSecurityHandler securityHandler = (ConstraintSecurityHandler) childHandler;
            securityHandler.addConstraintMapping(createConstraintMapping(deploymentHandler.getContextPath(),
                roles.toArray(new String[roles.size()])));
            HandlerCollection inner = (HandlerCollection) securityHandler.getHandler();
            if (inner != null) {
              debugLogging("Adding {} to handler {}", deploymentHandler.getDisplayName(), inner);
              inner.addHandler(deploymentHandler);
            }
          }
        }
      }
    }
    else {
      throw new Exception("Unexpected handler class: " + handler.getClass());
    }

  }

  private WebAppContext findRootContext(Server server) throws Exception {
    WebAppContext result = null;
    HandlerCollection rootHandler = (HandlerCollection) server.getHandler();
    Handler[] children = rootHandler.getHandlers();
    for (Handler child : children) {
      if (child instanceof HandlerCollection) {
        Handler[] grandchildren = ((HandlerCollection) child).getChildHandlers();
        // Now See if we want to add to the root Context or not...
        for (Handler grandchild : grandchildren) {
          if (grandchild instanceof WebAppContext) {
            WebAppContext ctx = (WebAppContext) grandchild;
            if ("/".equals(ctx.getContextPath())) {
              result = ctx;
              break;
            }
          }
        }
      }
    }
    return result;
  }

  private ServletContextHandler createServletHandler(ServletHolder servlet, String contextPath, String port) throws Exception {
    ServletContextHandler contextHandler = new ServletContextHandler();
    contextHandler.setDisplayName("[ServletContextHandler for " + contextPath + "]");
    contextHandler.setAttribute(SERVLET_CONTEXT_PATH_ATTRIBUTE, contextPath);
    contextHandler.addServlet(servlet, contextPath);
    if (port != null) {
      contextHandler.setConnectorNames(new String[]
      {
        port
      });
    }

    return contextHandler;
  }

  /**
   * Creates a WebappContext for the webapp (war file).
   * 
   * @param path - The path to the webapp.
   * @param contextPath - The context path of the webapp.
   * @return - The Handler for the webapp.
   * @throws Exception
   */
  private WebAppContext createWebappHandler(String path, String contextPath, String port) throws Exception {
    WebAppContext context = new WebAppContext();
    context.setWar(path);
    context.setContextPath(contextPath);
    if (port != null) {
      context.setConnectorNames(new String[]
      {
        port
      });
    }

    return context;
  }

  /**
   * Creates a WebappContext for the webapp (directory).
   * 
   * @param path - The path to the webapp.
   * @param contextPath - The context path of the webapp.
   * @return - The Handler for the webapp.
   * @throws Exception
   */
  private WebAppContext createWebappDirHandler(String path, String contextPath, String port) throws Exception {
    WebAppContext context = new WebAppContext();
    context.setResourceBase(path);
    context.setContextPath(contextPath);
    if (port != null) {
      context.setConnectorNames(new String[]
      {
        port
      });
    }

    return context;
  }

  /**
   * 
   * @param pathSpec - The path of the deployment. We need this parameter to restrict the contraint only to this path.
   * @param roles - The roles for the deployment.
   * @return - The constraint.
   */
  private ConstraintMapping createConstraintMapping(String pathSpec, String[] roles) {
    ConstraintMapping constraintMapping = new ConstraintMapping();

    Constraint constraint = new Constraint();
    constraint.setRoles(roles);
    constraint.setAuthenticate(true);

    constraintMapping.setConstraint(constraint);
    constraintMapping.setPathSpec(pathSpec);

    return constraintMapping;
  }

  @Override
  public void removeDeployment(String contextPath) throws Exception {
    for (Server server : servers) {
      destroyContext(contextPath, server.getHandler());
    }
  }

  public void removeDeployment(ServletHolder holder, String path) throws Exception {
    for (Server server : servers) {
      WebAppContext rootWar = findRootContext(server);
      if (rootWar != null) {
        removeServlet(rootWar, holder, path);
        // log.trace("ROOT.war config : {}", AggregateLifeCycle.dump(rootWar));
      }
    }
  }

  private void removeServlet(WebAppContext webAppContext, ServletHolder toRemove, String pathSpec) throws Exception {
    log.trace("{}: Removing servlet mapped to {}", webAppContext.getWar(), pathSpec);
    List<ServletHolder> servletsToKeep = new ArrayList<ServletHolder>();
    List<ServletMapping> mappingsToKeep = new ArrayList<ServletMapping>();
    ServletHandler handler = webAppContext.getServletHandler();
    Set<String> names = new HashSet<String>();
    debugLogging("{}: Current Mappings: {}", webAppContext.getWar(), Arrays.asList(handler.getServletMappings()));
    debugLogging("{}: Current Servlets: {}", webAppContext.getWar(), Arrays.asList(handler.getServlets()));
    for (ServletHolder holder : handler.getServlets()) {
      if (toRemove.equals(holder)) {
        names.add(holder.getName());
      }
      else {
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
    List<ServletMapping> mappings = new ArrayList<ServletMapping>();
    debugLogging("{}: Current Mappings: {}", webAppContext.getWar(), handler.getServletMappings());
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
    }
    else if (handler instanceof SecurityHandler) {
      SecurityHandler sh = (SecurityHandler) handler;
      Handler[] childHandlers = sh.getChildHandlersByClass(HandlerCollection.class);
      for (Handler childHandler : childHandlers) {
        destroyContext(contextPath, childHandler);
      }
    }
    else if (handler instanceof ServletContextHandler) {
      ServletContextHandler sch = (ServletContextHandler) handler;
      String servletPath = (String) sch.getAttribute(SERVLET_CONTEXT_PATH_ATTRIBUTE);
      if (contextPath.equals(servletPath)) {
        destroyQuietly(sch);
        return true;
      }
      else if (contextPath.equals(sch.getContextPath())) {
        destroyQuietly(sch);
        return true;
      }
    }
    else if (handler instanceof ContextHandler) {
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
    }
    catch (Exception e) {

    }
  }

  private void removeQuietly(HandlerCollection parent, Handler child) {
    try {
      parent.removeHandler(child);
    }
    catch (Exception e) {
      ;
    }
  }
  @Override
  public void startDeployment(String contextPath) throws Exception {
    for (Server server : servers) {
      if (server.isStarted()) {
        manageHandler(contextPath, true, server.getHandler());
      }
    }
  }

  @Override
  public void stopDeployment(String contextPath) throws Exception {
    for (Server server : servers) {
      if (server.isStarted()) {
        WebAppContext rootWar = findRootContext(server);
        if (rootWar != null) {
          unmapServlet(rootWar, contextPath);
        }
        manageHandler(contextPath, false, server.getHandler());
      }
    }
  }

  private void manageHandler(String contextPath, boolean start, Handler handler) throws Exception {
    if (handler instanceof ContextHandler) {
      ContextHandler contextHandler = (ContextHandler) handler;
      if (contextPath.equals(contextHandler.getContextPath())
          || contextPath.equals(contextHandler.getAttribute(SERVLET_CONTEXT_PATH_ATTRIBUTE))) {
        if (start) {
          contextHandler.start();
        }
        else {
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
