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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.servlet.ServletMapping;

import com.adaptris.core.AdaptrisConnectionImp;
import com.adaptris.core.CoreException;
import com.adaptris.core.util.ExceptionHelper;

/**
 * This class is the base class that all Jetty based Connections extend.
 * 
 * 
 * @author lchan
 * @author $Author: lchan $
 */
public abstract class JettyConnection extends AdaptrisConnectionImp implements JettyServletRegistrar {

  protected transient Server server;
  protected transient ServletContextHandler context;


  public JettyConnection() {
    super();
  }

  /**
   *
   * @see com.adaptris.core.AdaptrisConnectionImp#closeConnection()
   */
  @Override
  protected void closeConnection() {
    return;
  }

  /**
   *
   * @see com.adaptris.core.AdaptrisConnectionImp#initConnection()
   */
  @Override
  protected void initConnection() throws CoreException {

    // LinkedBlockingQueue<Runnable> queue = new LinkedBlockingQueue<Runnable>(numConsumers);
    // ExecutorThreadPool pool = new ExecutorThreadPool(numConsumers, numConsumers, 60000, TimeUnit.MILLISECONDS, queue);
    // server.setThreadPool(pool);
    // Might want to use FilterHolder and do something like this (7.4.5, not 7.4.2 which is what we're fscking using!).
    // CloseableDoSFilter filter = new CloseableDoSFilter();
    // filter.setMaxRequestPerSec(60);
    // filter.setDelayMs(-1) - which will reject the request.
    //
    // fh = new FilterHolder(DoSFilter)
    // context.addFilter(fh, "/*", .DispatcherType.REQUEST);
    // Would that even work in the context of the logging...
    // Would be better to rate limit based on iptables (!).

    try {
      server = configure(new Server());
      context = new ServletContextHandler(ServletContextHandler.SESSIONS);
      context.setContextPath("/");
      server.setHandler(createHandler(context));
    }
    catch (Exception e) {
      ExceptionHelper.rethrowCoreException(e);
    }
  }

  public void removeServlet(ServletWrapper wrapper) {
    List<ServletHolder> holdersToKeep = new ArrayList<ServletHolder>();
    List<ServletMapping> mappingsToKeep = new ArrayList<ServletMapping>();
    for (ServletHolder holder : context.getServletHandler().getServlets()) {
      if (!holder.equals(wrapper.getServletHolder())) {
        holdersToKeep.add(holder);
      }
    }
    for (ServletMapping mapping : context.getServletHandler().getServletMappings()) {
      boolean keepMe = true;
      for (String pathSpec : mapping.getPathSpecs()) {
        if (pathSpec.equals(wrapper.getUrl())) {
          log.trace("Matched [" + wrapper.getUrl() + "] against [" + pathSpec + "]; remove from URL mappings");
          keepMe = false;
          break;
        }
      }
      if (keepMe) {
        mappingsToKeep.add(mapping);
      }
    }
    context.getServletHandler().setServletMappings(mappingsToKeep.toArray(new ServletMapping[0]));
    context.getServletHandler().setServlets(holdersToKeep.toArray(new ServletHolder[0]));
    log.trace("Removed " + wrapper.getServletHolder() + " from " + wrapper.getUrl());
  }

  public void addServlet(ServletWrapper wrapper) {
    log.trace("Adding " + wrapper.getServletHolder() + " against " + wrapper.getUrl());
    context.addServlet(wrapper.getServletHolder(), wrapper.getUrl());
  }

  /**
   *
   * @see com.adaptris.core.AdaptrisConnectionImp#startConnection()
   */
  @Override
  protected void startConnection() throws CoreException {
    try {
      server.start();
    }
    catch (Exception e) {
      throw new CoreException(e);
    }
  }

  /**
   *
   * @see com.adaptris.core.AdaptrisConnectionImp#stopConnection()
   */
  @Override
  protected void stopConnection() {
    try {
      server.stop();
      server.join();
    }
    catch (Exception e) {
      log.warn("Exception encountered during stop " + e.getMessage());
    }
  }

  abstract Server configure(Server server) throws Exception;

  abstract Handler createHandler(ServletContextHandler context) throws Exception;

}
