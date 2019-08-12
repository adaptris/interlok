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

package com.adaptris.core.http.jetty;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.apache.commons.lang3.BooleanUtils;

import com.adaptris.annotation.AdvancedConfig;
import com.adaptris.annotation.AutoPopulated;
import com.adaptris.annotation.InputFieldDefault;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.ProduceOnlyProducerImp;
import com.adaptris.core.http.ContentTypeProvider;
import com.adaptris.core.http.NullContentTypeProvider;
import com.adaptris.core.http.server.ConfiguredStatusProvider;
import com.adaptris.core.http.server.HttpStatusProvider;
import com.adaptris.core.http.server.HttpStatusProvider.HttpStatus;
import com.adaptris.core.http.server.HttpStatusProvider.Status;
import com.adaptris.core.http.server.ResponseHeaderProvider;

/**
 * @author lchan
 *
 */
public abstract class ResponseProducerImpl extends ProduceOnlyProducerImp {
  @NotNull
  @AutoPopulated
  @Valid
  private HttpStatusProvider statusProvider;

  @NotNull
  @AutoPopulated
  @Valid
  private ResponseHeaderProvider<HttpServletResponse> responseHeaderProvider;

  @NotNull
  @AutoPopulated
  @Valid
  private ContentTypeProvider contentTypeProvider;
  @InputFieldDefault(value = "true")
  private Boolean sendPayload;

  @AdvancedConfig
  @InputFieldDefault(value = "false")
  private Boolean forwardConnectionException;
  @AdvancedConfig
  @InputFieldDefault(value = "true")
  private Boolean flushBuffer;

  public ResponseProducerImpl() {
    setResponseHeaderProvider(new NoOpResponseHeaderProvider());
    setContentTypeProvider(new NullContentTypeProvider());
    setStatusProvider(new ConfiguredStatusProvider(HttpStatus.INTERNAL_ERROR_500));
  }

  public HttpStatusProvider getStatusProvider() {
    return statusProvider;
  }

  public void setStatusProvider(HttpStatusProvider p) {
    this.statusProvider = p;
  }

  public <T extends ResponseProducerImpl> T withStatusProvider(HttpStatusProvider p) {
    setStatusProvider(p);
    return (T) this;
  }

  public ResponseHeaderProvider<HttpServletResponse> getResponseHeaderProvider() {
    return responseHeaderProvider;
  }

  public void setResponseHeaderProvider(ResponseHeaderProvider<HttpServletResponse> p) {
    this.responseHeaderProvider = p;
  }

  public <T extends ResponseProducerImpl> T withResponseHeaderProvider(ResponseHeaderProvider<HttpServletResponse> p) {
    setResponseHeaderProvider(p);
    return (T) this;
  }

  public Boolean getForwardConnectionException() {
    return forwardConnectionException;
  }

  /**
   * Set to true to throw an exception if producing the response fails.
   * 
   * <p>
   * When producing the reply to a client; it may be that they have already terminated the connection. By default client
   * disconnections will not generate a {@link com.adaptris.core.ServiceException} so normal processing continues. Set this to be
   * true if you want
   * error handling to be triggered in this situation.
   * </p>
   * 
   * @param b true to throw a ServiceException if producing the response fails., default null (false).
   */
  public void setForwardConnectionException(Boolean b) {
    this.forwardConnectionException = b;
  }

  public <T extends ResponseProducerImpl> T withForwardConnectionException(Boolean b) {
    setForwardConnectionException(b);
    return (T) this;
  }

  protected boolean forwardConnectionException() {
    return BooleanUtils.toBooleanDefaultIfNull(getForwardConnectionException(), false);
  }

  public Boolean getFlushBuffer() {
    return flushBuffer;
  }

  public void setFlushBuffer(Boolean flush) {
    this.flushBuffer = flush;
  }

  protected boolean flushBuffers() {
    return BooleanUtils.toBooleanDefaultIfNull(getFlushBuffer(), true);
  }

  public <T extends ResponseProducerImpl> T withFlushBuffer(Boolean b) {
    setFlushBuffer(b);
    return (T) this;
  }

  protected Status getStatus(AdaptrisMessage msg) {
    return getStatusProvider().getStatus(msg);
  }

  public ContentTypeProvider getContentTypeProvider() {
    return contentTypeProvider;
  }

  /**
   * Set the Content-Type that will be returned as part of the HTTP Response.
   * 
   * @param ctp the content type provider
   */
  public void setContentTypeProvider(ContentTypeProvider ctp) {
    this.contentTypeProvider = ctp;
  }

  public <T extends ResponseProducerImpl> T withContentTypeProvider(ContentTypeProvider b) {
    setContentTypeProvider(b);
    return (T) this;
  }

  /**
   * @return the sendPayload
   */
  public Boolean getSendPayload() {
    return sendPayload;
  }

  /**
   * Whether or not to send the {@link com.adaptris.core.AdaptrisMessage#getPayload()} as part of the reply.
   *
   * @param b the sendPayload to set defaults true.
   */
  public void setSendPayload(Boolean b) {
    sendPayload = b;
  }

  public <T extends ResponseProducerImpl> T withSendPayload(Boolean b) {
    setSendPayload(b);
    return (T) this;
  }

  protected boolean sendPayload() {
    return BooleanUtils.toBooleanDefaultIfNull(getSendPayload(), true);
  }


}
