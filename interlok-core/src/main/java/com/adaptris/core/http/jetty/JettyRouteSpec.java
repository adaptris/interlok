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

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import com.adaptris.annotation.DisplayOrder;
import com.adaptris.core.ComponentLifecycle;
import com.adaptris.core.CoreException;
import com.adaptris.core.http.jetty.JettyRouteCondition.JettyRoute;
import com.adaptris.core.services.metadata.ExtractMetadataService;
import com.adaptris.core.util.LifecycleHelper;
import com.adaptris.interlok.util.Args;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import lombok.Getter;
import lombok.Setter;

/**
 * Used with {@link JettyRoutingService} to help decide which branch to execute.
 * <p>
 * Since the URI itself may contain parameters that may need to be extracted as metadata, it is possible to do that as part of the
 * matching process here. Given a URL of {@code /record/zeus/apollo} the following configuration will match provided the HTTP method
 * is {@code POST}; the URL pattern will be parsed for capturing groups and the metadata {@code parentId=zeus},
 * {@code childId=apollo} will be set; the next service-id will be set to {@code handleInsert}.
 * </p>
 * <pre>
   {@code
      <jetty-route-spec>
        <condition>
          <url-pattern>^/record/(.*)/(.*)$</url-pattern>
          <method>POST</method>
          <metadata-key>parentId</metadata-key>
          <metadata-key>childId</metadata-key>
        </condition>
        <service-id>handleInsert</service-id>
      </jetty-route-spec>
   }
 * </pre>
 * You could achieve the same effect with a {@link ExtractMetadataService} as part of your normal service execution chain.
 * </p>
 */
@DisplayOrder(order =
{
    "serviceId", "condition"
})
@XStreamAlias("jetty-route-spec")
public class JettyRouteSpec implements ComponentLifecycle {

  /**
   * The service-id that will be used if the route matches.
   *
   */
  @NotBlank
  @Getter
  @Setter
  private String serviceId;
  /**
   * The condition that causes a match.
   *
   */
  @Valid
  @NotNull
  @Getter
  @Setter
  private JettyRouteCondition condition;

  public JettyRouteSpec() {
  }

  @Override
  public void init() throws CoreException {
    Args.notNull(getCondition(), "condition");
    LifecycleHelper.init(getCondition());
  }

  @Override
  public void start() throws CoreException {
    LifecycleHelper.start(getCondition());
  }

  @Override
  public void stop() {
    LifecycleHelper.stop(getCondition());
  }

  @Override
  public void close() {
    LifecycleHelper.close(getCondition());
  }

  public JettyRouteSpec withServiceId(String s) {
    setServiceId(s);
    return this;
  }

  public JettyRouteSpec withCondition(JettyRouteCondition condition) {
    setCondition(condition);
    return this;
  }

  public JettyRoute build(String method, String uri) throws CoreException {
    return getCondition().build(method, uri);
  }

  @Override
  public String toString() {
    return String.format("[%s][%s]", getCondition().getMethod(), getCondition().getUrlPattern());
  }
}
