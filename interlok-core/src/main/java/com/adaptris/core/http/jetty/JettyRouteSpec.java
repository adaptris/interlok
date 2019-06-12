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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.validation.Valid;

import org.apache.commons.lang3.ObjectUtils;
import org.hibernate.validator.constraints.NotBlank;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adaptris.annotation.AffectsMetadata;
import com.adaptris.annotation.DisplayOrder;
import com.adaptris.annotation.Removal;
import com.adaptris.core.ComponentLifecycle;
import com.adaptris.core.CoreException;
import com.adaptris.core.http.jetty.JettyRouteCondition.JettyRoute;
import com.adaptris.core.services.metadata.ExtractMetadataService;
import com.adaptris.core.util.Args;
import com.adaptris.core.util.LifecycleHelper;
import com.adaptris.core.util.LoggingHelper;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

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
    "condition", "method", "urlPattern", "serviceId", "metadataKeys"
})
@XStreamAlias("jetty-route-spec")
public class JettyRouteSpec implements ComponentLifecycle {

  private transient Logger log = LoggerFactory.getLogger(this.getClass());

  @Deprecated
  @Removal(version = "3.12.0", message = "Use a condition instead")
  private String urlPattern;
  @Deprecated
  @Removal(version = "3.12.0", message = "Use a condition instead")
  private String method;
  @XStreamImplicit(itemFieldName = "metadata-key")
  @AffectsMetadata
  @Deprecated
  @Removal(version = "3.12.0", message = "Use a condition instead")
  private List<String> metadataKeys;
  @NotBlank
  private String serviceId;
  @Valid
  private JettyRouteCondition condition;

  private transient boolean warningLogged;
  private transient JettyRouteCondition conditionToUse = null;

  public JettyRouteSpec() {
    setMetadataKeys(new ArrayList<String>());
  }

  @Deprecated
  public JettyRouteSpec(String urlPattern, String method, List<String> keys, String serviceId) {
    this();
    setUrlPattern(urlPattern);
    setMethod(method);
    setMetadataKeys(keys);
    setServiceId(serviceId);
  }

  @Override
  public void init() throws CoreException {
    conditionToUse = build();
    LifecycleHelper.init(conditionToUse);
  }

  @Override
  public void start() throws CoreException {
    LifecycleHelper.start(conditionToUse);
  }

  @Override
  public void stop() {
    LifecycleHelper.stop(conditionToUse);
  }

  @Override
  public void close() {
    LifecycleHelper.close(conditionToUse);
  }

  /**
   * 
   * @deprecated since 3.9.0 use a condition instead
   */
  @Deprecated
  @Removal(version = "3.12.0", message = "Use a condition instead")
  public String getUrlPattern() {
    return urlPattern;
  }

  /**
   * Set the URL pattern that you want to match against.
   * 
   * @param urlPattern the pattern.
   * @deprecated since 3.9.0 use a condition instead
   */
  @Deprecated
  @Removal(version = "3.12.0", message = "Use a condition instead")
  public void setUrlPattern(String urlPattern) {
    this.urlPattern = Args.notBlank(urlPattern, "urlPattern");
  }

  private String urlPattern() {
    return Args.notBlank(urlPattern, "urlPattern");
  }

  /**
   * 
   * @deprecated since 3.9.0 use a condition instead
   */
  @Deprecated
  @Removal(version = "3.12.0", message = "Use a condition instead")
  public String getMethod() {
    return method;
  }

  /**
   * Specify a method to match against (optional).
   * 
   * @deprecated since 3.9.0 use a condition instead
   */
  @Deprecated
  @Removal(version = "3.12.0", message = "Use a condition instead")
  public void setMethod(String method) {
    this.method = method;
  }

  /**
   * 
   * @deprecated since 3.9.0 use a condition instead
   */
  @Deprecated
  @Removal(version = "3.12.0", message = "Use a condition instead")
  public List<String> getMetadataKeys() {
    return metadataKeys;
  }

  private List<String> metadataKeys() {
    return ObjectUtils.defaultIfNull(getMetadataKeys(), Collections.emptyList());
  }

  /**
   * Specify the metadata that should be populated based on any captured groups in your url pattern.
   * <p>
   * The list of keys is processed in order, against each capturing match group in order
   * </p>
   * 
   * @param s list of keys.
   */
  public void setMetadataKeys(List<String> s) {
    this.metadataKeys = s;
  }

  public String getServiceId() {
    return serviceId;
  }

  /**
   * Set the service-id that will be used if the route matches.
   */
  public void setServiceId(String serviceId) {
    this.serviceId = serviceId;
  }

  public JettyRouteSpec withServiceId(String s) {
    setServiceId(s);
    return this;
  }

  public JettyRouteCondition getCondition() {
    return condition;
  }

  /**
   * Specify the conditions for the route.
   * 
   * @param condition the condition.
   */
  public void setCondition(JettyRouteCondition condition) {
    this.condition = condition;
  }

  public JettyRouteSpec withCondition(JettyRouteCondition condition) {
    setCondition(condition);
    return this;
  }

  private JettyRouteCondition build() {
    if (getCondition() == null) {
      LoggingHelper.logWarning(warningLogged, () -> {
        warningLogged = true;
      }, "Direct use of urlPattern/metadataKeys/method is deprecated; use a condition instead");
      return new JettyRouteCondition().withMetadataKeys(metadataKeys()).withMethod(getMethod()).withUrlPattern(urlPattern());
    }
    return getCondition();
  }

  public JettyRoute build(String method, String uri) throws CoreException {
    return conditionToUse.build(method, uri);
  }


  @Override
  public String toString() {
    JettyRouteCondition condition = build();
    return String.format("[%s][%s]", condition.getMethod(), condition.getUrlPattern());
  }
}
