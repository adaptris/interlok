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

import java.net.HttpURLConnection;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.NotBlank;

import com.adaptris.annotation.AdapterComponent;
import com.adaptris.annotation.AutoPopulated;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.annotation.InputFieldDefault;
import com.adaptris.annotation.InputFieldHint;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreException;
import com.adaptris.core.NullConnection;
import com.adaptris.core.ServiceException;
import com.adaptris.core.ServiceImp;
import com.adaptris.core.StandaloneProducer;
import com.adaptris.core.http.RawContentTypeProvider;
import com.adaptris.core.http.server.RawStatusProvider;
import com.adaptris.core.http.server.ResponseHeaderProvider;
import com.adaptris.core.util.Args;
import com.adaptris.core.util.ExceptionHelper;
import com.adaptris.core.util.LifecycleHelper;
import com.adaptris.validation.constraints.NumberExpression;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Send a response via HTTP as a service rather than having to wrap in a {@link StandaloneProducer}.
 * 
 * <p>
 * Note that this service just wraps a {@link StandardResponseProducer} instance but doesn't expose all the possible settings
 * available. If you need those features, than continue using the producer wrapped as a {@link StandaloneProducer}.
 * <ul>
 * <li>It will also always send the current payload (i.e. {@link StandardResponseProducer#setSendPayload(Boolean)} is true)</li>
 * <li>{@link StandardResponseProducer#setForwardConnectionException(Boolean)} is always false.</li>
 * <li>{@link StandardResponseProducer#setFlushBuffer(Boolean)} is always true.</li>
 * </ul>
 * </p>
 * <p>
 * String parameters in this service will use the {@link AdaptrisMessage#resolve(String)} which allows you to specify metadata
 * values as part of a constant string e.g. {@code setUrl("%message{http_url}")} will use the metadata value associated with the key
 * {@code http_url}.
 * </p>
 * 
 * @since 3.6.5
 */
@XStreamAlias("jetty-response-service")
@AdapterComponent
@ComponentProfile(summary = "Send a HTTP Response", tag = "service,http,https,jetty", since = "3.6.5")
public class JettyResponseService extends ServiceImp {

  @NotBlank
  @InputFieldDefault(value = "500")
  @InputFieldHint(expression = true)
  @AutoPopulated
  @NumberExpression
  private String httpStatus;
  @NotBlank
  @InputFieldDefault(value = "text/plain")
  @InputFieldHint(expression = true)
  @AutoPopulated
  private String contentType;
  @NotNull
  @AutoPopulated
  @Valid
  private ResponseHeaderProvider<HttpServletResponse> responseHeaderProvider;


  public JettyResponseService() {
    setHttpStatus(String.valueOf(HttpURLConnection.HTTP_INTERNAL_ERROR));
    setResponseHeaderProvider(new NoOpResponseHeaderProvider());
    setContentType("text/plain");
  }

  public JettyResponseService(int status, String contentType) {
    this();
    setHttpStatus(String.valueOf(status));
    setContentType(contentType);
  }

  @Override
  public void doService(AdaptrisMessage msg) throws ServiceException {
    StandardResponseProducer p = buildProducer(msg);
    try {
      LifecycleHelper.initAndStart(p, false);
      p.produce(msg);
    }
    catch (CoreException e) {
      throw ExceptionHelper.wrapServiceException(e);
    }
    finally {
      LifecycleHelper.stopAndClose(p, false);
    }
  }


  @Override
  public void prepare() throws CoreException {
  }

  @Override
  protected void initService() throws CoreException {
  }

  @Override
  protected void closeService() {
  }

  protected StandardResponseProducer buildProducer(AdaptrisMessage msg) {
    StandardResponseProducer p = new StandardResponseProducer()
        .withContentTypeProvider(new RawContentTypeProvider(msg.resolve(getContentType())))
        .withResponseHeaderProvider(getResponseHeaderProvider()).withSendPayload(true).withForwardConnectionException(false)
        .withFlushBuffer(true)
        .withStatusProvider(new RawStatusProvider(Integer.parseInt(msg.resolve(getHttpStatus()))))
        .withMessageFactory(msg.getFactory());
    p.registerConnection(new NullConnection());
    return p;
  }

  public String getHttpStatus() {
    return httpStatus;
  }

  /**
   * 
   * @param s the status, supports metadata resolution via {@link AdaptrisMessage#resolve(String)}.
   */
  public void setHttpStatus(String s) {
    this.httpStatus = Args.notBlank(s, "httpStatus");
  }

  public JettyResponseService withHttpStatus(String status) {
    setHttpStatus(status);
    return this;
  }

  public String getContentType() {
    return contentType;
  }

  /**
   * 
   * @param ct the content-type, supports metadata resolution via {@link AdaptrisMessage#resolve(String)}.
   */
  public void setContentType(String ct) {
    this.contentType = Args.notBlank(ct, "contentType");
  }

  public JettyResponseService withContentType(String type) {
    setContentType(type);
    return this;
  }

  public ResponseHeaderProvider<HttpServletResponse> getResponseHeaderProvider() {
    return responseHeaderProvider;
  }

  public void setResponseHeaderProvider(ResponseHeaderProvider<HttpServletResponse> provider) {
    this.responseHeaderProvider = Args.notNull(provider, "responseHeaderProvider");
  }

  public JettyResponseService withResponseHeaderProvider(ResponseHeaderProvider<HttpServletResponse> provider) {
    setResponseHeaderProvider(provider);
    return this;
  }
}
