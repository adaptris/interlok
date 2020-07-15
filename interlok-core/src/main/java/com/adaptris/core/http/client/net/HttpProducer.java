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

package com.adaptris.core.http.client.net;

import static com.adaptris.core.util.DestinationHelper.logWarningIfNotNull;
import static com.adaptris.core.util.DestinationHelper.mustHaveEither;
import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import org.apache.commons.lang3.BooleanUtils;
import com.adaptris.annotation.AdvancedConfig;
import com.adaptris.annotation.AffectsMetadata;
import com.adaptris.annotation.AutoPopulated;
import com.adaptris.annotation.InputFieldDefault;
import com.adaptris.annotation.InputFieldHint;
import com.adaptris.annotation.Removal;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreException;
import com.adaptris.core.ProduceDestination;
import com.adaptris.core.ProduceException;
import com.adaptris.core.RequestReplyProducerImp;
import com.adaptris.core.http.ConfiguredContentTypeProvider;
import com.adaptris.core.http.ContentTypeProvider;
import com.adaptris.core.http.client.ConfiguredRequestMethodProvider;
import com.adaptris.core.http.client.RequestHeaderProvider;
import com.adaptris.core.http.client.RequestMethodProvider;
import com.adaptris.core.http.client.RequestMethodProvider.RequestMethod;
import com.adaptris.core.http.client.ResponseHeaderHandler;
import com.adaptris.core.util.DestinationHelper;
import com.adaptris.core.util.LoggingHelper;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

/**
 *
 * @author lchan
 *
 */
public abstract class HttpProducer<A, B> extends RequestReplyProducerImp {

  protected static final long DEFAULT_TIMEOUT = -1;
  /**
   * The request method.
   * <p>
   * The default is 'POST'
   * </p>
   */
  @NotNull
  @AutoPopulated
  @Valid
  @Getter
  @Setter
  @NonNull
  private RequestMethodProvider methodProvider;

  /**
   * Content-Type header associated with the HTTP operation
   *
   */
  @NotNull
  @Valid
  @AutoPopulated
  @Getter
  @Setter
  @NonNull
  private ContentTypeProvider contentTypeProvider;

  /**
   * Specify how we handle headers from the HTTP response.
   * <p>
   * The default behaviour will be to discard.
   * </p>
   */
  @AdvancedConfig
  @Valid
  @NotNull
  @AutoPopulated
  @AffectsMetadata
  @Getter
  @Setter
  @NonNull
  private ResponseHeaderHandler<B> responseHeaderHandler;

  /**
   * Specify how we want to generate the initial set of HTTP Headers.
   *
   * <p>
   * The default behaviour is to not have any additional headers
   * </p>
   */
  @AdvancedConfig
  @Valid
  @NotNull
  @AutoPopulated
  @Getter
  @Setter
  @NonNull
  private RequestHeaderProvider<A> requestHeaderProvider;

  /**
   * Set whether to ignore the server response code.
   * <p>
   * In some cases, you may wish to ignore any server response code (such as 500) as this may return
   * meaningful data that you wish to use. If that's the case, make sure this flag is true. It
   * defaults to false.
   * </p>
   * <p>
   * In all cases the metadata key
   * {@link com.adaptris.core.CoreConstants#HTTP_PRODUCER_RESPONSE_CODE} is populated with the last
   * server response.
   * </p>
   */
  @AdvancedConfig
  @InputFieldDefault(value = "false")
  @Getter
  @Setter
  private Boolean ignoreServerResponseCode;
  /**
   * Automatically handle redirection.
   * <p>
   * The default is true if not otherwise specified
   * </p>
   */
  @AdvancedConfig(rare = true)
  @InputFieldDefault(value = "true")
  @Getter
  @Setter
  private Boolean allowRedirect;

  /**
   * The ProduceDestination contains the url we will access.
   *
   */
  @Getter
  @Setter
  @Deprecated
  @Valid
  @Removal(version = "4.0.0", message = "Use 'url' instead")
  private ProduceDestination destination;

  /**
   * The URL endpoint to access.
   */
  @InputFieldHint(expression = true)
  @Getter
  @Setter
  // Needs to be @NotBlank when destination is removed.
  private String url;

  private transient boolean destWarning;

  public HttpProducer() {
    super();
    setContentTypeProvider(new ConfiguredContentTypeProvider());
    setMethodProvider(new ConfiguredRequestMethodProvider(RequestMethod.POST));
  }

  /**
   *
   * @see com.adaptris.core.RequestReplyProducerImp#defaultTimeout()
   */
  @Override
  protected long defaultTimeout() {
    return DEFAULT_TIMEOUT;
  }


  @Override
  protected void doProduce(AdaptrisMessage msg, String dest) throws ProduceException {
    doRequest(msg, dest, defaultTimeout());
  }

  protected boolean handleRedirection() {
    return BooleanUtils.toBooleanDefaultIfNull(getAllowRedirect(), true);
  }


  protected boolean ignoreServerResponseCode() {
    return BooleanUtils.toBooleanDefaultIfNull(getIgnoreServerResponseCode(), false);
  }

  protected RequestMethod getMethod(AdaptrisMessage msg) {
    return getMethodProvider().getMethod(msg);
  }

  protected void logHeaders(String header, String message, Set headers) {
    if (log.isTraceEnabled()) {
      ByteArrayOutputStream out = new ByteArrayOutputStream();
      try (PrintWriter p = new PrintWriter(out)) {
        p.println(header);
        p.println(message);
        for (Iterator i = headers.iterator(); i.hasNext();) {
          Map.Entry e = (Map.Entry) i.next();
          p.println(e.getKey() + ": " + e.getValue());
        }
      }
      log.trace(out.toString());
    }
  }

  @Override
  public void prepare() throws CoreException {
    logWarningIfNotNull(destWarning, () -> destWarning = true, getDestination(),
        "{} uses destination, use 'url' instead", LoggingHelper.friendlyName(this));
    mustHaveEither(getUrl(), getDestination());
    registerEncoderMessageFactory();
  }


  @Override
  public String endpoint(AdaptrisMessage msg) throws ProduceException {
    return DestinationHelper.resolveProduceDestination(getUrl(), getDestination(), msg);
  }

  public <T extends HttpProducer> T withURL(String s) {
    setUrl(s);
    return (T) this;
  }
}
