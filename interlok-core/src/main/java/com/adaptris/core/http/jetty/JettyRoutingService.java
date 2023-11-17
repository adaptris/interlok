/*
 * Copyright 2017 Adaptris Ltd.
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

import static com.adaptris.core.CoreConstants.HTTP_METHOD;
import static com.adaptris.core.http.jetty.JettyConstants.JETTY_URI;

import java.util.ArrayList;
import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import com.adaptris.annotation.AdapterComponent;
import com.adaptris.annotation.AutoPopulated;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.annotation.DisplayOrder;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.BranchingServiceCollection;
import com.adaptris.core.BranchingServiceImp;
import com.adaptris.core.ComponentLifecycle;
import com.adaptris.core.CoreConstants;
import com.adaptris.core.CoreException;
import com.adaptris.core.ServiceException;
import com.adaptris.core.http.jetty.JettyRouteCondition.JettyRoute;
import com.adaptris.core.util.ExceptionHelper;
import com.adaptris.core.util.LifecycleHelper;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

/**
 * Use as part of a {@link BranchingServiceCollection} to branch based on the jetty URI and method.
 * 
 * <p>
 * Takes the metadata values associated with {@link CoreConstants#HTTP_METHOD} and {@link JettyConstants#JETTY_URI} and matches them
 * against a list of configured routes. If a route matches the URI and the http method (if specified) then the next service id is
 * set appropriately.
 * </p>
 * 
 * @config jetty-routing-service
 *
 */
@XStreamAlias("jetty-routing-service")
@AdapterComponent
@ComponentProfile(summary = "Specify the next branch based on the jettyURI and method", tag = "service,jetty,branching", branchSelector = true)
@DisplayOrder(order =
{
    "defaultServiceId", "routes"
})
public class JettyRoutingService extends BranchingServiceImp {

  @Valid
  @NotNull
  @AutoPopulated
  @XStreamImplicit(itemFieldName = "route")
  private List<JettyRouteSpec> routes;
  private String defaultServiceId;
  
  public JettyRoutingService() {
    setRoutes(new ArrayList<JettyRouteSpec>());
  }

  public JettyRoutingService(String defaultServiceId, List<JettyRouteSpec> specs) {
    this();
    setRoutes(specs);
    setDefaultServiceId(defaultServiceId);
  }

  @Override
  public void doService(AdaptrisMessage msg) throws ServiceException {
    try {
      String method = msg.getMetadataValue(HTTP_METHOD);
      String uri = msg.getMetadataValue(JETTY_URI);
      boolean matched = false;
      for (JettyRouteSpec route : routes) {
        JettyRoute m = route.build(method, uri);
        if (m.matches()) {
          log.trace("[{}][{}], matched by {}", method, uri, route);
          msg.addMetadata(m.metadata());
          msg.setNextServiceId(route.getServiceId());
          matched = true;
          break;
        }
      }
      if (!matched) {
        log.debug("No Matches from configured routes, using {}", getDefaultServiceId());
        msg.setNextServiceId(getDefaultServiceId());
      }
    } catch (Exception e) {
      throw ExceptionHelper.wrapServiceException(e);
    }
  }

  @Override
  public void prepare() throws CoreException {
    LifecycleHelper.prepare(getRoutes().toArray(new ComponentLifecycle[0]));
  }

  @Override
  protected void initService() throws CoreException {
    LifecycleHelper.init(getRoutes().toArray(new ComponentLifecycle[0]));
  }

  @Override
  public void start() throws CoreException {
    LifecycleHelper.start(getRoutes().toArray(new ComponentLifecycle[0]));
    super.start();
  }

  @Override
  public void stop() {
    super.stop();
    LifecycleHelper.stop(getRoutes().toArray(new ComponentLifecycle[0]));
  }

  @Override
  protected void closeService() {
    LifecycleHelper.close(getRoutes().toArray(new ComponentLifecycle[0]));
  }

  public List<JettyRouteSpec> getRoutes() {
    return routes;
  }

  public void setRoutes(List<JettyRouteSpec> r) {
    this.routes = r;
  }

  public String getDefaultServiceId() {
    return defaultServiceId;
  }

  /**
   * Set the default service id if there are no matches.
   * 
   * @param id
   */
  public void setDefaultServiceId(String id) {
    this.defaultServiceId = id;
  }

}
