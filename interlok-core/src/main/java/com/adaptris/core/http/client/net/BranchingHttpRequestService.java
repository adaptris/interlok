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
package com.adaptris.core.http.client.net;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import com.adaptris.annotation.AdapterComponent;
import com.adaptris.annotation.AutoPopulated;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.annotation.DisplayOrder;
import com.adaptris.annotation.InputFieldHint;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreConstants;
import com.adaptris.core.CoreException;
import com.adaptris.core.ServiceException;
import com.adaptris.core.StandaloneProducer;
import com.adaptris.core.StandaloneRequestor;
import com.adaptris.core.http.client.StatusEvaluator;
import com.adaptris.core.services.BranchingServiceEnabler;
import com.adaptris.core.util.ExceptionHelper;
import com.adaptris.core.util.LifecycleHelper;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamImplicit;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

/**
 * Branch support for HTTP via interrogation of the HTTP status.
 *
 * <p>
 * This service allows you to branch based on the {@code HTTP status code} returned by the web server. Use a specific
 * {@link StatusEvaluator} to determine the appropriate value for {@link AdaptrisMessage#setNextServiceId(String)}. It differs from
 * wrapping {@link HttpRequestService} with a {@link BranchingServiceEnabler} as it allows you more fine-grained control based on
 * HTTP status codes.
 * </p>
 * <p>
 * Note that this service just wraps a {@link StandardHttpProducer} instance but doesn't expose all the possible settings available
 * for the normal {@link StandardHttpProducer}. If you need those features, than continue using the producer wrapped as a
 * {@link StandaloneProducer} or {@link StandaloneRequestor}.
 * </p>
 * <p>
 * String parameters in this service will use the {@link AdaptrisMessage#resolve(String)} which allows you to specify metadata
 * values as part of a constant string e.g. {@code setUrl("%message{http_url}")} will use the metadata value associated with the key
 * {@code http_url}.
 * </p>
 *
 * @config branching-http-request-service
 */
@XStreamAlias("branching-http-request-service")
@AdapterComponent
@ComponentProfile(summary = "Make a HTTP request to a remote server using standard JRE components", tag = "service,http,https,branching", branchSelector = true, metadata =
{
    "adphttpresponse"

})
@DisplayOrder(order = {"url", "method", "contentType", "defaultServiceId", "authentication", "requestHeaderProvider",
    "responseHeaderHandler", "statusMatches"})
public class BranchingHttpRequestService extends HttpRequestServiceImpl {

  /**
   * Set the {@code nextServiceId} based on these evaluators.
   *
   */
  @NotNull
  @AutoPopulated
  @Valid
  @XStreamImplicit
  @Getter
  @Setter
  @NonNull
  private List<StatusEvaluator> statusMatches;

  // Allow this to be null, which just means no branching...
  /**
   * Set the default service-id in the event that no matches are found (optional).
   *
   */
  @Getter
  @Setter
  @InputFieldHint(style = "BLANKABLE")
  private String defaultServiceId = "";

  public BranchingHttpRequestService() {
    super();
    setStatusMatches(new ArrayList<StatusEvaluator>());
  }

  public BranchingHttpRequestService(String url) {
    this();
    setUrl(url);
  }

  @Override
  public boolean isBranching() {
    return true;
  }

  @Override
  public void doService(AdaptrisMessage msg) throws ServiceException {
    StandardHttpProducer p = buildProducer(msg);
    p.setIgnoreServerResponseCode(true);
    try {
      LifecycleHelper.initAndStart(p, false).request(msg);
      Optional.ofNullable(getDefaultServiceId()).ifPresent((s) -> msg.setNextServiceId(s));
      int responseCode = ((Integer) msg.getObjectHeaders().get(CoreConstants.HTTP_PRODUCER_RESPONSE_CODE)).intValue();
      for (StatusEvaluator rp : getStatusMatches()) {
        if (rp.matches(responseCode)) {
          msg.setNextServiceId(rp.serviceId());
          break;
        }
      }
    }
    catch (CoreException e) {
      throw ExceptionHelper.wrapServiceException(e);
    }
    finally {
      LifecycleHelper.stopAndClose(p, false);
    }
  }

  /**
   * @since 3.9.0
   *
   */
  public BranchingHttpRequestService withStatusMatches(List<StatusEvaluator> matchers) {
    setStatusMatches(matchers);
    return this;
  }

  /**
   * @since 3.9.0
   *
   */
  public BranchingHttpRequestService withStatusMatches(StatusEvaluator... matchers) {
    return withStatusMatches(new ArrayList(Arrays.asList(matchers)));
  }

  /**
   * @since 3.9.0
   *
   */
  public BranchingHttpRequestService withDefaultServiceId(String s) {
    setDefaultServiceId(s);
    return this;
  }
}
