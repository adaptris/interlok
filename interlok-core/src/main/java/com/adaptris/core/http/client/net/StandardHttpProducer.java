/*
 * Copyright 2015 Adaptris Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package com.adaptris.core.http.client.net;

import static com.adaptris.core.AdaptrisMessageFactory.defaultIfNull;
import static com.adaptris.core.http.HttpConstants.CONTENT_TYPE;
import static com.adaptris.util.stream.StreamUtil.copyAndClose;
import static org.apache.commons.lang3.StringUtils.isEmpty;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.Authenticator;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import javax.mail.internet.ContentType;
import javax.mail.internet.ParseException;
import javax.net.ssl.HttpsURLConnection;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.ObjectUtils;
import com.adaptris.annotation.AdapterComponent;
import com.adaptris.annotation.AdvancedConfig;
import com.adaptris.annotation.AutoPopulated;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.annotation.DisplayOrder;
import com.adaptris.annotation.InputFieldDefault;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageImp;
import com.adaptris.core.CoreConstants;
import com.adaptris.core.MetadataElement;
import com.adaptris.core.NullConnection;
import com.adaptris.core.ProduceException;
import com.adaptris.core.common.InputStreamWithEncoding;
import com.adaptris.core.common.PayloadStreamInputParameter;
import com.adaptris.core.common.PayloadStreamOutputParameter;
import com.adaptris.core.http.auth.AdapterResourceAuthenticator;
import com.adaptris.core.http.auth.HttpAuthenticator;
import com.adaptris.core.http.auth.NoAuthentication;
import com.adaptris.core.http.client.RequestMethodProvider;
import com.adaptris.core.http.client.RequestMethodProvider.RequestMethod;
import com.adaptris.core.util.ExceptionHelper;
import com.adaptris.interlok.InterlokException;
import com.adaptris.interlok.config.DataInputParameter;
import com.adaptris.interlok.config.DataOutputParameter;
import com.adaptris.util.TimeInterval;
import com.adaptris.util.stream.Slf4jLoggingOutputStream;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

/**
 * Default {@link HttpProducer} implementation that uses {@link HttpURLConnection} available in a
 * standard java runtime.
 *
 * <p>
 * This uses {@code com.adaptris.core.http.client} interfaces to manage request and response headers
 * and also the {@link DataInputParameter} and {@link DataOutputParameter} interfaces to source the
 * HTTP body and to handle the HTTP response body respectively. Without specific overrides for these
 * new fields then they default to the payload body.
 * </p>
 * <p>
 * Note that configuring a {@link com.adaptris.core.AdaptrisMessageEncoder} instance will cause the
 * {@link DataInputParameter} and {@link DataOutputParameter} fields to be ignored.
 * </p>
 * <p>
 * When interacting with HTTPS sites, then you may need to configure a truststore / keystore system
 * properties if non-default certificate handling is required. As this uses
 * {@link HttpsURLConnection} under the covers; then please consult <a href=
 * "https://docs.oracle.com/javase/8/docs/technotes/guides/security/jsse/JSSERefGuide.html#InstallationAndCustomization">Oracles
 * JSSE documentation</a> for a full discussion of the required system properties.
 * </p>
 *
 * @config standard-http-producer
 *
 * @author lchan
 */
@XStreamAlias("standard-http-producer")
@AdapterComponent
@ComponentProfile(summary = "Make a HTTP request to a remote server using standard JRE components",
    tag = "producer,http,https", metadata = {"adphttpresponse"

    }, recommended = {NullConnection.class})
@DisplayOrder(order = {"url", "authenticator", "allowRedirect", "ignoreServerResponseCode",
    "alwaysSendPayload", "methodProvider", "contentTypeProvider", "requestHeaderProvider",
    "requestBody", "responseHeaderHandler", "responseBody"})
public class StandardHttpProducer extends HttpProducer<HttpURLConnection, HttpURLConnection> {

  private static final String PARAM_CHARSET = "charset";

  private static final Collection<RequestMethodProvider.RequestMethod> METHOD_ALLOWS_OUTPUT =
      Collections.unmodifiableCollection(Arrays.asList(
          new RequestMethod[] {RequestMethod.POST, RequestMethod.PUT, RequestMethod.PATCH}));

  private static final Collection<RequestMethodProvider.RequestMethod> NEVER_OUTPUT =
      Collections.unmodifiableCollection(Arrays.asList(new RequestMethod[] {RequestMethod.TRACE}));

  private transient DataInputParameter<InputStream> defaultRequest =
      new PayloadStreamInputParameter();
  private transient DataOutputParameter<InputStreamWithEncoding> defaultResponse =
      new PayloadStreamOutputParameter();

  /**
   * Set where the HTTP Request body is going to come from.
   *
   * <p>
   * The default is {@link PayloadStreamInputParameter}.
   * </p>
   */
  @Valid
  @AdvancedConfig
  @Getter
  @Setter
  private DataInputParameter<InputStream> requestBody;
  /**
   * Set where the HTTP Response Body will be written to.
   * <p>
   * Note that if you have configured an {@link com.adaptris.core.AdaptrisMessageEncoder} via
   * {@link #setEncoder(com.adaptris.core.AdaptrisMessageEncoder)} (such as for AS2) then this may
   * have no effect.
   * </p>
   *
   * <p>
   * The default is {@link PayloadStreamOutputParameter}.
   * </p>
   */
  @Valid
  @AdvancedConfig
  @Getter
  @Setter
  private DataOutputParameter<InputStreamWithEncoding> responseBody;

  @Valid
  @AdvancedConfig
  @NotNull
  @AutoPopulated
  @Getter
  @Setter
  @NonNull
  private HttpAuthenticator authenticator = new NoAuthentication();

  /**
   * Whether or not to always attempt to send the payload as the entity body.
   * <p>
   * Only the TRACE method explicitly forbids an entity body; other methods are technically
   * unrestricted. However, best practice suggests that entity bodies are only included for the
   * POST/PUT/(PATCH) methods.
   * </p>
   * <p>
   * The default is fails if not otherwise specified
   * </p>
   *
   */
  @AdvancedConfig(rare = true)
  @InputFieldDefault(value = "false")
  @Getter
  @Setter
  private Boolean alwaysSendPayload;

  /**
   * The connect timeout.
   *
   */
  @Valid
  @AdvancedConfig(rare = true)
  @Getter
  @Setter
  private TimeInterval connectTimeout;
  /**
   * Set the read timeout.
   * <p>
   * Note that any read timeout will be overridden by the timeout value passed in via the
   * {{@link #request(AdaptrisMessage, long)} method; if it is not the same as
   * {@value com.adaptris.core.http.HttpConstants#DEFAULT_SOCKET_TIMEOUT}
   * </p>
   */
  @Valid
  @AdvancedConfig(rare = true)
  @Getter
  @Setter
  private TimeInterval readTimeout;

  public StandardHttpProducer() {
    super();
    setResponseHeaderHandler(new DiscardResponseHeaders());
    setRequestHeaderProvider(new NoRequestHeaders());
    Authenticator.setDefault(AdapterResourceAuthenticator.getInstance());
  }

  @Override
  protected void doProduce(AdaptrisMessage msg, String dest) throws ProduceException {
    doRequest(msg, dest, defaultTimeout(), defaultIfNull(getMessageFactory()).newMessage());
  }

  @Override
  protected AdaptrisMessage doRequest(AdaptrisMessage msg, String endpointUrl, long timeout)
      throws ProduceException {
    return doRequest(msg, endpointUrl, timeout, msg);
  }

  private AdaptrisMessage doRequest(AdaptrisMessage msg, String endpointUrl, long timeout,
      AdaptrisMessage reply) throws ProduceException {
    try {
      URL url = new URL(endpointUrl);
      authenticator.setup(url.toString(), msg, null);
      HttpURLConnection http =
          configure(configureTimeouts((HttpURLConnection) url.openConnection(), timeout), msg);
      if (authenticator instanceof HttpURLConnectionAuthenticator) {
        ((HttpURLConnectionAuthenticator) authenticator).configureConnection(http);
      }
      writeData(getMethod(msg), msg, http);
      handleResponse(http, reply);
    } catch (Exception e) {
      throw ExceptionHelper.wrapProduceException(e);
    } finally {
      authenticator.close();
    }
    return reply;
  }

  private HttpURLConnection configure(HttpURLConnection http, AdaptrisMessage msg)
      throws Exception {
    RequestMethod rm = getMethod(msg);
    log.trace("HTTP Request Method is : [{}]", rm);
    http.setRequestMethod(rm.name());
    http.setInstanceFollowRedirects(handleRedirection());
    http.setDoInput(true);
    getRequestHeaderProvider().addHeaders(msg, http);
    String contentType = getContentTypeProvider().getContentType(msg);
    if (!isEmpty(contentType)) {
      http.setRequestProperty(CONTENT_TYPE, contentType);
    }
    return http;
  }

  private HttpURLConnection configureTimeouts(HttpURLConnection http, long timeout) {
    if (getConnectTimeout() != null) {
      http.setConnectTimeout(Long.valueOf(getConnectTimeout().toMilliseconds()).intValue());
    }
    if (getReadTimeout() != null) {
      http.setReadTimeout(Long.valueOf(getReadTimeout().toMilliseconds()).intValue());
    }
    if (timeout != DEFAULT_TIMEOUT) {
      http.setReadTimeout(Long.valueOf(timeout).intValue());
    }
    return http;
  }

  private void writeData(RequestMethod methodToUse, AdaptrisMessage src, HttpURLConnection dest)
      throws IOException, InterlokException {
    // INTERLOK-1569
    if (NEVER_OUTPUT.contains(methodToUse)) {
      return;
    }
    if (doOutput(methodToUse)) {
      dest.setDoOutput(true);
      if (getEncoder() != null) {
        getEncoder().writeMessage(src, dest);
      } else {
        copyAndClose(requestBody().extract(src), dest.getOutputStream());
      }
    }
  }

  private boolean doOutput(RequestMethod m) {
    if (alwaysSendPayload()) {
      return true;
    }
    if (!METHOD_ALLOWS_OUTPUT.contains(m)) {
      log.trace("Ignoring payload with use of {} method", m.name());
      return false;
    }
    return true;
  }

  private void handleResponse(HttpURLConnection http, AdaptrisMessage reply)
      throws IOException, InterlokException {
    int responseCode = http.getResponseCode();
    logHeaders("Response Information", http.getResponseMessage(),
        http.getHeaderFields().entrySet());
    log.trace("Content-Length is " + http.getContentLength());

    if (responseCode < 200 || responseCode > 299) {
      if (ignoreServerResponseCode()) {
        log.trace("Ignoring HTTP Reponse code {}", responseCode);
        responseBody().insert(
            new InputStreamWithEncoding(http.getErrorStream(), getContentEncoding(http)), reply);
      } else {
        fail(responseCode,
            new InputStreamWithEncoding(http.getErrorStream(), getContentEncoding(http)));
      }
    } else {
      if (getEncoder() != null) {
        AdaptrisMessage decodedReply = getEncoder().readMessage(http);
        AdaptrisMessageImp.copyPayload(decodedReply, reply);
        reply.getObjectHeaders().putAll(decodedReply.getObjectHeaders());
        reply.setMetadata(decodedReply.getMetadata());
      } else {
        responseBody().insert(
            new InputStreamWithEncoding(http.getInputStream(), getContentEncoding(http)), reply);
      }
    }
    getResponseHeaderHandler().handle(http, reply);
    reply.addMetadata(new MetadataElement(CoreConstants.HTTP_PRODUCER_RESPONSE_CODE,
        String.valueOf(http.getResponseCode())));
    reply.addObjectHeader(CoreConstants.HTTP_PRODUCER_RESPONSE_CODE,
        Integer.valueOf(http.getResponseCode()));
  }

  private String getContentEncoding(HttpURLConnection http) {
    if (http.getContentEncoding() != null) {
      return http.getContentEncoding();
    }
    // Parse Content-Type header for encoding
    try {
      ContentType contentType = new ContentType(http.getContentType());
      if (!isEmpty(contentType.getParameter(PARAM_CHARSET))) {
        return contentType.getParameter(PARAM_CHARSET);
      }
    } catch (ParseException e) {
      log.trace("Unable to parse Content-Type header \"{}\": {}", http.getContentType(),
          e.toString());
    }
    return null;
  }

  private void fail(int responseCode, InputStreamWithEncoding data) throws ProduceException {
    if (log.isTraceEnabled()) {
      try {
        try (
            OutputStream slf4j =
                new Slf4jLoggingOutputStream(log, Slf4jLoggingOutputStream.LogLevel.TRACE);
            InputStream in = new BufferedInputStream(data.inputStream);
            PrintStream out = data.encoding == null ? new PrintStream(slf4j)
                : new PrintStream(slf4j, false, data.encoding)) {
          out.println("Error Data from remote server :");
          IOUtils.copy(in, out);
        }
      } catch (IOException e) {
        log.trace("No Error Data available");
      }
    }
    throw new ProduceException("Failed to send payload, got " + responseCode);
  }

  private DataInputParameter<InputStream> requestBody() {
    return ObjectUtils.defaultIfNull(getRequestBody(), defaultRequest);
  }


  private DataOutputParameter<InputStreamWithEncoding> responseBody() {
    return ObjectUtils.defaultIfNull(getResponseBody(), defaultResponse);
  }

  private boolean alwaysSendPayload() {
    return BooleanUtils.toBooleanDefaultIfNull(getAlwaysSendPayload(), false);
  }

}
