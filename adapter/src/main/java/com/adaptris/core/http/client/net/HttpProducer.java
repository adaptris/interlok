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

import static com.adaptris.core.http.HttpConstants.DEFAULT_SOCKET_TIMEOUT;
import static org.apache.commons.lang.StringUtils.isEmpty;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.PasswordAuthentication;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import com.adaptris.annotation.AdvancedConfig;
import com.adaptris.annotation.AutoPopulated;
import com.adaptris.annotation.InputFieldDefault;
import com.adaptris.annotation.InputFieldHint;
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
import com.adaptris.core.util.Args;
import com.adaptris.security.password.Password;

/**
 * 
 * @author lchan
 * 
 */
public abstract class HttpProducer extends RequestReplyProducerImp {

  @NotNull
  @AutoPopulated
  @Valid
  private RequestMethodProvider methodProvider;

  @Deprecated
  private String username = null;
  
  @Deprecated
  @InputFieldHint(style = "PASSWORD")
  private String password = null;

  @NotNull
  @Valid
  @AutoPopulated
  private ContentTypeProvider contentTypeProvider;

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

  @AdvancedConfig
  @InputFieldDefault(value = "false")
  private Boolean ignoreServerResponseCode;
  @AdvancedConfig
  @InputFieldDefault(value = "true")
  private Boolean allowRedirect;

  private transient String authString = null;
  private transient PasswordAuthentication passwordAuth;

  public HttpProducer() {
    super();
    setContentTypeProvider(new ConfiguredContentTypeProvider());
    setResponseHeaderHandler(new DiscardResponseHeaders());
    setRequestHeaderProvider(new NoRequestHeaders());
    setMethodProvider(new ConfiguredRequestMethodProvider(RequestMethod.POST));
  }

  @Override
  public void start() throws CoreException {}

  @Override
  public void stop() {}

  @Override
  public void close() {}

  @Override
  public void init() throws CoreException {
    try {
      if (!isEmpty(username)) {
        passwordAuth = new PasswordAuthentication(username, Password.decode(password).toCharArray());
      }
    } catch (Exception e) {
      throw new CoreException(e);
    }

  }


  /**
   * 
   * @see com.adaptris.core.RequestReplyProducerImp#defaultTimeout()
   */
  @Override
  protected long defaultTimeout() {
    return DEFAULT_SOCKET_TIMEOUT;
  }


  /**
   * 
   * @see RequestReplyProducerImp#produce(AdaptrisMessage, ProduceDestination)
   */
  @Override
  public void produce(AdaptrisMessage msg, ProduceDestination dest) throws ProduceException {
    doRequest(msg, dest, defaultTimeout());
  }

  /**
   * 
   * @param s the user name
   */
  @Deprecated
  public void setUsername(String s) {
    username = s;
  }

  /**
   * Set the password.
   * <p>
   * In additional to plain text passwords, the passwords can also be encoded using the appropriate {@link com.adaptris.security.password.Password}
   * </p>
   * 
   * @param s the password
   */
  @Deprecated
  public void setPassword(String s) {
    password = s;
  }

  /**
   * Get the username.
   * 
   * @return username
   */
  @Deprecated
  public String getUsername() {
    return username;
  }

  /**
   * Get the password.
   * 
   * @return the password
   */
  @Deprecated
  public String getPassword() {
    return password;
  }

  /**
   * Specify whether to automatically handle redirection.
   * 
   * @param b true or false, default is null (true)
   */
  public void setAllowRedirect(Boolean b) {
    allowRedirect = b;
  }

  boolean handleRedirection() {
    return allowRedirect != null ? allowRedirect.booleanValue() : true;
  }

  /**
   * Get the handle redirection flag.
   * 
   * @return true or false.
   */
  public Boolean getAllowRedirect() {
    return allowRedirect;
  }

  /**
   * Get the currently configured flag for ignoring server response code.
   * 
   * @return true or false
   * @see #setIgnoreServerResponseCode(Boolean)
   */
  public Boolean getIgnoreServerResponseCode() {
    return ignoreServerResponseCode;
  }

  boolean ignoreServerResponseCode() {
    return ignoreServerResponseCode != null ? ignoreServerResponseCode.booleanValue() : false;
  }

  /**
   * Set whether to ignore the server response code.
   * <p>
   * In some cases, you may wish to ignore any server response code (such as 500) as this may return meaningful data that you wish
   * to use. If that's the case, make sure this flag is true. It defaults to false.
   * </p>
   * <p>
   * In all cases the metadata key {@link com.adaptris.core.CoreConstants#HTTP_PRODUCER_RESPONSE_CODE} is populated with the last
   * server response.
   * </p>
   * 
   * @see com.adaptris.core.CoreConstants#HTTP_PRODUCER_RESPONSE_CODE
   * @param b true
   */
  public void setIgnoreServerResponseCode(Boolean b) {
    ignoreServerResponseCode = b;
  }

  protected PasswordAuthentication getPasswordAuthentication() {
    return passwordAuth;
  }

  public ContentTypeProvider getContentTypeProvider() {
    return contentTypeProvider;
  }

  /**
   * Specify the Content-Type header associated with the HTTP operation.
   * 
   * @param ctp
   */
  public void setContentTypeProvider(ContentTypeProvider ctp) {
    this.contentTypeProvider = ctp;
  }


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


  public RequestMethodProvider getMethodProvider() {
    return methodProvider;
  }

  /**
   * Specify how the HTTP Request Method is generated.
   * 
   * @param p the request method provider.
   */
  public void setMethodProvider(RequestMethodProvider p) {
    this.methodProvider = Args.notNull(p, "Method Provider");
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

}
