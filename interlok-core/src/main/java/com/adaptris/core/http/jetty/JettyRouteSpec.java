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

import static org.apache.commons.lang.StringUtils.isBlank;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang3.ObjectUtils;
import org.hibernate.validator.constraints.NotBlank;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.adaptris.annotation.AffectsMetadata;
import com.adaptris.annotation.DisplayOrder;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.MetadataElement;
import com.adaptris.core.ServiceException;
import com.adaptris.core.util.Args;
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
      <url-pattern>^/record/(.*)/(.*)$</url-pattern>
      <method>POST</method>
      <metadata-key>parentId</metadata-key>
      <metadata-key>childId</metadata-key>
      <service-id>handleInsert</service-id>   
   }
 * </pre>
 */
@DisplayOrder(order =
{
    "method", "urlPattern", "serviceId", "metadataKeys"
})
@XStreamAlias("jetty-route-spec")
public class JettyRouteSpec {

  private transient Logger log = LoggerFactory.getLogger(this.getClass());

  @NotBlank
  private String urlPattern;
  private String method;
  @XStreamImplicit(itemFieldName = "metadata-key")
  @AffectsMetadata
  private List<String> metadataKeys;
  @NotBlank
  private String serviceId;

  private transient Pattern _urlPattern;

  public JettyRouteSpec() {
    setMetadataKeys(new ArrayList<String>());
  }

  public JettyRouteSpec(String urlPattern, String method, List<String> keys, String serviceId) {
    this();
    setUrlPattern(urlPattern);
    setMethod(method);
    setMetadataKeys(keys);
    setServiceId(serviceId);
  }

  public String getUrlPattern() {
    return urlPattern;
  }

  /**
   * Set the URL pattern that you want to match against.
   * 
   * @param urlPattern the pattern.
   */
  public void setUrlPattern(String urlPattern) {
    this.urlPattern = Args.notBlank(urlPattern, "urlPattern");
  }

  public String getMethod() {
    return method;
  }

  /**
   * Specify a method to match against (optional).
   * 
   * @param method
   */
  public void setMethod(String method) {
    this.method = method;
  }

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

  public void setServiceId(String serviceId) {
    this.serviceId = serviceId;
  }

  public RouteMatch build(String method, String uri) throws ServiceException {
    int expected = (isBlank(getMethod()) ? 0 : 1) + 1;
    int rc = 0;
    Set<MetadataElement> matchedMetadata = new HashSet<>();
    if (!isBlank(getMethod())) {
      rc += getMethod().equalsIgnoreCase(method) ? 1 : 0;
    }
    Matcher matcher = createMatcher(uri);
    if (matcher.matches()) {
      rc++;
      matchedMetadata = createMetadata(matcher);
    }
    return new RouteMatch(rc == expected, matchedMetadata);
  }

  private Matcher createMatcher(String uri) {
    if (_urlPattern == null || !_urlPattern.pattern().equals(getUrlPattern())) {
      _urlPattern = Pattern.compile(getUrlPattern());
    }
    Matcher matcher = _urlPattern.matcher(uri);
    return matcher;
  }

  private Set<MetadataElement> createMetadata(Matcher matcher) throws ServiceException {
    Set<MetadataElement> result = new HashSet<>();
    List<String> keys = Collections.unmodifiableList(metadataKeys());
    if (matcher.groupCount() > keys.size()) {
      String msg = String.format("'%s' has %d match-groups, but only %d metadata keys defined",
          matcher.pattern().pattern(), matcher.groupCount(), keys.size());
      log.error(msg);
      throw new ServiceException(msg);
    }
    for (int i = 1; i <= matcher.groupCount(); i++) {
      result.add(new MetadataElement(keys.get(i - 1), matcher.group(i)));
    }
    return result;
  }

  protected class RouteMatch {
    private boolean match;
    private Set<MetadataElement> metadata;

    RouteMatch(boolean match, Set<MetadataElement> metadata) {
      this.match = match;
      this.metadata = metadata;
    }

    public boolean matches() {
      return match;
    }

    public void apply(AdaptrisMessage msg) {
      log.trace("Adding [{}] as metadata", metadata);
      log.trace("nextServiceID={}", getServiceId());
      msg.setMetadata(metadata);
      msg.setNextServiceId(getServiceId());
    }
  }
}
