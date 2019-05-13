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

import static org.apache.commons.lang.StringUtils.isEmpty;
import java.net.Authenticator;
import java.net.HttpURLConnection;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import org.hibernate.validator.constraints.NotBlank;
import com.adaptris.annotation.AdvancedConfig;
import com.adaptris.annotation.AutoPopulated;
import com.adaptris.annotation.InputFieldDefault;
import com.adaptris.annotation.InputFieldHint;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.ConfiguredProduceDestination;
import com.adaptris.core.CoreException;
import com.adaptris.core.NullConnection;
import com.adaptris.core.ServiceImp;
import com.adaptris.core.StandaloneProducer;
import com.adaptris.core.StandaloneRequestor;
import com.adaptris.core.http.RawContentTypeProvider;
import com.adaptris.core.http.auth.AdapterResourceAuthenticator;
import com.adaptris.core.http.auth.HttpAuthenticator;
import com.adaptris.core.http.auth.NoAuthentication;
import com.adaptris.core.http.client.ConfiguredRequestMethodProvider;
import com.adaptris.core.http.client.RequestHeaderProvider;
import com.adaptris.core.http.client.RequestMethodProvider.RequestMethod;
import com.adaptris.core.http.client.ResponseHeaderHandler;
import com.adaptris.core.util.Args;

/**
 * Direct HTTP support as a service rather than wrapped via {@link StandaloneProducer} or {@link StandaloneRequestor}.
 * 
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
 */
public abstract class HttpRequestServiceImpl extends ServiceImp {

  @NotBlank
  @InputFieldHint(expression = true)
  private String url;
  @NotBlank
  @AutoPopulated
  @InputFieldDefault(value = "text/plain")
  @InputFieldHint(expression = true)
  private String contentType;
  @NotBlank
  @AutoPopulated
  @InputFieldDefault(value = "POST")
  @InputFieldHint(expression = true)
  private String method;

  @AdvancedConfig
  @Valid
  @NotNull
  @AutoPopulated
  private ResponseHeaderHandler<HttpURLConnection> responseHeaderHandler;

  @AdvancedConfig
  @Valid
  @NotNull
  @AutoPopulated
  private RequestHeaderProvider<HttpURLConnection> requestHeaderProvider;
  @Valid
  @AdvancedConfig
  @NotNull
  @AutoPopulated
  private HttpAuthenticator authenticator = new NoAuthentication();

  public HttpRequestServiceImpl() {
    super();
    Authenticator.setDefault(AdapterResourceAuthenticator.getInstance());
    setResponseHeaderHandler(new DiscardResponseHeaders());
    setRequestHeaderProvider(new NoRequestHeaders());
    setContentType("text/plain");
    setMethod("POST");
  }

  @Override
  public void prepare() throws CoreException {
    if (isEmpty(url)) {
      throw new CoreException("Empty URL param");
    }
  }

  @Override
  protected void initService() throws CoreException {

  }

  @Override
  protected void closeService() {
  }

  protected StandardHttpProducer buildProducer(AdaptrisMessage msg) {
    StandardHttpProducer p = new StandardHttpProducer();
    p.setMessageFactory(msg.getFactory());
    p.setDestination(new ConfiguredProduceDestination(msg.resolve(getUrl())));
    p.setContentTypeProvider(new RawContentTypeProvider(msg.resolve(getContentType())));
    p.setMethodProvider(new ConfiguredRequestMethodProvider(RequestMethod.valueOf(msg.resolve(getMethod()).toUpperCase())));
    p.setAuthenticator(getAuthenticator());
    p.setRequestHeaderProvider(getRequestHeaderProvider());
    p.setResponseHeaderHandler(getResponseHeaderHandler());
    p.registerConnection(new NullConnection());
    return p;
  }

  /**
   * @return the responseHeaderHandler
   */
  public ResponseHeaderHandler<HttpURLConnection> getResponseHeaderHandler() {
    return responseHeaderHandler;
  }

  /**
   * Specify how we handle headers from the HTTP response.
   * 
   * @param handler the handler, default is a {@link DiscardResponseHeaders}.
   */
  public void setResponseHeaderHandler(ResponseHeaderHandler<HttpURLConnection> handler) {
    this.responseHeaderHandler = Args.notNull(handler, "ResponseHeaderHandler");
  }

  /**
   * @since 3.9.0
   * 
   */
  public <T extends HttpRequestServiceImpl> T withResponseHeaderHandler(
      ResponseHeaderHandler<HttpURLConnection> s) {
    setResponseHeaderHandler(s);
    return (T) this;
  }

  public RequestHeaderProvider<HttpURLConnection> getRequestHeaderProvider() {
    return requestHeaderProvider;
  }

  /**
   * Specify how we want to generate the initial set of HTTP Headers.
   * 
   * @param handler the handler, default is a {@link NoRequestHeaders}
   */
  public void setRequestHeaderProvider(RequestHeaderProvider<HttpURLConnection> handler) {
    this.requestHeaderProvider = Args.notNull(handler, "Request Header Provider");
  }

  /**
   * @since 3.9.0
   * 
   */
  public <T extends HttpRequestServiceImpl> T withRequestHeaderProvider(
      RequestHeaderProvider<HttpURLConnection> s) {
    setRequestHeaderProvider(s);
    return (T) this;
  }

  /**
   * @return the url
   */
  public String getUrl() {
    return url;
  }

  /**
   * @param s the url to set; can be of the form {@code %message{key1}} to use the metadata value associated with {@code key1}
   */
  public void setUrl(String s) {
    this.url = s;
  }

  /**
   * @since 3.9.0
   * 
   */
  public <T extends HttpRequestServiceImpl> T withUrl(String s) {
    setUrl(s);
    return (T) this;
  }

  /**
   * @return the contentType
   */
  public String getContentType() {
    return contentType;
  }

  /**
   * @param ct the contentType to set; can be of the form {@code %message{key1}} to use the metadata value associated with
   *          {@code key1}
   */
  public void setContentType(String ct) {
    this.contentType = ct;
  }

  /**
   * @since 3.9.0
   * 
   */
  public <T extends HttpRequestServiceImpl> T withContentType(String s) {
    setContentType(s);
    return (T) this;
  }

  /**
   * @return the method
   */
  public String getMethod() {
    return method;
  }

  /**
   * @param m the method to set; can be of the form {@code %message{key1}} to use the metadata value associated with
   *          {@code key1}
   */
  public void setMethod(String m) {
    this.method = m;
  }

  /**
   * @since 3.9.0
   * 
   */
  public <T extends HttpRequestServiceImpl> T withMethod(String s) {
    setMethod(s);
    return (T) this;
  }

  /**
   * @return the authenticator
   */
  public HttpAuthenticator getAuthenticator() {
    return authenticator;
  }

  /**
   * @param auth the authenticator to set
   */
  public void setAuthenticator(HttpAuthenticator auth) {
    this.authenticator = auth;
  }

  /**
   * @since 3.9.0
   * 
   */
  public <T extends HttpRequestServiceImpl> T withAuthenticator(HttpAuthenticator auth) {
    setAuthenticator(auth);
    return (T) this;
  }
}
