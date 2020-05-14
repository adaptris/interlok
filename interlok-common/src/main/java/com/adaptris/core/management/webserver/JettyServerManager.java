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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.servlet.Servlet;
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
    if (servers.size() == 0) {
      // no servers, we can't be started
      return false;
    }
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
      WebAppContext rootWar = findRootContext(server, true);
      if (rootWar != null) {
        reconfigureWar(rootWar, servlet, additionalProperties);
        debugLogging("{}: Current Servlet Handler: {}", rootWar.getWar(), rootWar.getServletHandler().dump());
        debugLogging("{}: Current Security: {}", rootWar.getWar(), rootWar.getSecurityHandler().dump());
        // log.trace("ROOT.war config : {}", AggregateLifeCycle.dump(rootWar));
        addedAtLeastOnce = true;
      }
    }
    if (!addedAtLeastOnce) {
      throw new Exception("Couldn't add servlet to any contexts");
    }
  }


  private void reconfigureWar(WebAppContext rootWar, ServletHolder servlet, HashMap<String, Object> additionalProperties)
      throws Exception {
    // Have to stop the WAR before we can reconfigure the security handler, not true if we just want
    // to add a new servlet; but it's probaby good practice to.
    rootWar.stop();
    rootWar.setThrowUnavailableOnStartupException(THROW_UNAVAILABLE_ON_START);
    String pathSpec = (String) additionalProperties.get(CONTEXT_PATH);
    log.trace("Adding servlet to existing ROOT WebAppContext against {}", pathSpec);
    rootWar.addServlet(servlet, pathSpec);
    SecurityHandlerWrapper w = (SecurityHandlerWrapper) additionalProperties.get(SECURITY_CONSTRAINTS);
    if (w != null) {
      rootWar.setSecurityHandler(w.createSecurityHandler());
    }
    rootWar.start();
  }

  private WebAppContext findRootContext(Server server, boolean create) throws Exception {
    WebAppContext root = rootContextFromHandler(server.getHandler());
    if (root == null && create) {
      log.trace("No ROOT WebAppContext, creating one");
      root = new WebAppContext();
      root.setContextPath("/");
      URL defaultsURL = findDefaultDescriptorXML();
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

  @Override
  public void removeDeployment(String contextPath) throws Exception {
    for (Server server : servers) {
      destroyContext(contextPath, server.getHandler());
    }
  }

  public void removeDeployment(ServletHolder holder, String path) throws Exception {
    for (Server server : servers) {
      WebAppContext rootWar = findRootContext(server, false);
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
    List<ServletMapping> mappings = new ArrayList<ServletMapping>();
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
        WebAppContext rootWar = findRootContext(server, false);
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
