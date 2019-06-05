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
import static org.apache.commons.lang.StringUtils.isBlank;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang3.ObjectUtils;
import org.hibernate.validator.constraints.NotBlank;
import com.adaptris.annotation.AdapterComponent;
import com.adaptris.annotation.AffectsMetadata;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.annotation.DisplayOrder;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreConstants;
import com.adaptris.core.CoreException;
import com.adaptris.core.MetadataElement;
import com.adaptris.core.services.conditional.Switch;
import com.adaptris.core.services.conditional.conditions.ConditionImpl;
import com.adaptris.core.services.metadata.ExtractMetadataService;
import com.adaptris.core.util.Args;
import com.adaptris.core.util.MetadataHelper;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

/**
 * {@link Condition} implementation that evaulates based on the JettyURI and HTTP method.
 * 
 * <p>
 * Designed to be used as part of a {@link Switch} service to branch based on the jettyURI and method and
 * takes the metadata values associated with {@link CoreConstants#HTTP_METHOD} and {@link CoreConstants#JETTY_URI} and matches them
 * against a its configured route. If a route matches the URI and the http method (if specified) then the evaluation is true.
 * </p>
 * <p>
 * Since the URI itself may contain parameters that may need to be extracted as metadata, it is possible to do that as part of the
 * matching process here. Given a URL of {@code /record/zeus/apollo} if you have a url-pattern {@code /record/(.*)/(.*)} with
 * metadata keys {@code recordId, childId} then that metadata will be set as part of the evaluation method. You could achieve the
 * same effect with a {@link ExtractMetadataService} as part of your normal service execution chain.
 * </p>
 * 
 * @config jetty-route-condition
 *
 */
@XStreamAlias("jetty-route-condition")
@AdapterComponent
@ComponentProfile(summary = "Condition that evaluates based on the jettyURI and method", tag = "condition,jetty", since = "3.9.0")
@DisplayOrder(order = {"method", "urlPattern", "metadataKeys"})
public class JettyRouteCondition extends ConditionImpl {

  @NotBlank
  private String urlPattern;
  private String method;
  @XStreamImplicit(itemFieldName = "metadata-key")
  @AffectsMetadata
  private List<String> metadataKeys;

  private transient Pattern _urlPattern;

  public JettyRouteCondition() {
    setMetadataKeys(new ArrayList<>());
  }

  @Override
  public boolean evaluate(AdaptrisMessage msg) throws CoreException {
    String method = msg.getMetadataValue(HTTP_METHOD);
    String uri = msg.getMetadataValue(JETTY_URI);
    JettyRoute m = build(method, uri);
    if (m.matches()) {
      log.trace("[{}][{}], matched by [{}][{}]", method, uri, getMethod(), getUrlPattern());
      if (m.metadata().size() > 0) {
        msg.setMetadata(m.metadata());
      }
    }
    return m.matches();
  }

  @Override
  public void init() throws CoreException {
    super.init();
    Args.notBlank(getUrlPattern(), "url-pattern");
    _urlPattern = Pattern.compile(getUrlPattern());
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

  public JettyRouteCondition withUrlPattern(String pattern) {
    setUrlPattern(pattern);
    return this;
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

  public JettyRouteCondition withMethod(String m) {
    setMethod(m);
    return this;
  }

  public List<String> getMetadataKeys() {
    return metadataKeys;
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

  public JettyRouteCondition withMetadataKeys(List<String> list) {
    setMetadataKeys(list);
    return this;
  }

  public JettyRouteCondition withMetadataKeys(String... list) {
    return withMetadataKeys(new ArrayList<>(Arrays.asList(list)));
  }

  private List<String> metadataKeys() {
    return ObjectUtils.defaultIfNull(getMetadataKeys(), Collections.emptyList());
  }

  public JettyRoute build(String method, String uri) throws CoreException {
    int expected = (isBlank(getMethod()) ? 0 : 1) + 1;
    int rc = 0;
    Set<MetadataElement> matchedMetadata = new HashSet<>();
    if (!isBlank(getMethod())) {
      rc += getMethod().equalsIgnoreCase(method) ? 1 : 0;
    }
    Matcher matcher = _urlPattern.matcher(uri);
    if (matcher.matches()) {
      rc++;
      matchedMetadata = MetadataHelper.metadataFromMatchGroups(matcher, metadataKeys());
    }
    return new JettyRoute(rc == expected, matchedMetadata);
  }

  public class JettyRoute {
    private boolean match;
    private Set<MetadataElement> metadata;

    protected JettyRoute(boolean match, Set<MetadataElement> metadata) {
      this.match = match;
      this.metadata = metadata;
    }

    public boolean matches() {
      return match;
    }

    public Set<MetadataElement> metadata() {
      return metadata;
    }

  }
}
