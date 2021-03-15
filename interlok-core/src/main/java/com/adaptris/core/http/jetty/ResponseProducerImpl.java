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

import com.adaptris.annotation.AdvancedConfig;
import com.adaptris.annotation.AutoPopulated;
import com.adaptris.annotation.InputFieldDefault;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreException;
import com.adaptris.core.ProduceException;
import com.adaptris.core.ProduceOnlyProducerImp;
import com.adaptris.core.http.ContentTypeProvider;
import com.adaptris.core.http.NullContentTypeProvider;
import com.adaptris.core.http.server.ConfiguredStatusProvider;
import com.adaptris.core.http.server.HttpStatusProvider;
import com.adaptris.core.http.server.HttpStatusProvider.HttpStatus;
import com.adaptris.core.http.server.HttpStatusProvider.Status;
import com.adaptris.core.http.server.ResponseHeaderProvider;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import org.apache.commons.lang3.BooleanUtils;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

/**
 * @author lchan
 *
 */
public abstract class ResponseProducerImpl extends ProduceOnlyProducerImp {

  /**
   * The HTTP Status.
   *
   */
  @NotNull
  @AutoPopulated
  @Valid
  @Getter
  @Setter
  @NonNull
  private HttpStatusProvider statusProvider;

  /**
   * Additional HTTP headers that will be sent as part of the response.
   *
   */
  @NotNull
  @AutoPopulated
  @Valid
  @Getter
  @Setter
  @NonNull
  private ResponseHeaderProvider<HttpServletResponse> responseHeaderProvider;

  /**
   * The Content-Type to send with the response.
   *
   */
  @NotNull
  @AutoPopulated
  @Valid
  @Getter
  @Setter
  @NonNull
  private ContentTypeProvider contentTypeProvider;

  /**
   * Send the current payload as part of the response.
   * <p>
   * Defaults to true if not explicitly configured.
   * </p>
   */
  @InputFieldDefault(value = "true")
  @Getter
  @Setter
  private Boolean sendPayload;

  /**
   * Throw an exception if producing the response fails.
   *
   * <p>
   * When producing the reply to a client; it may be that they have already terminated the
   * connection. By default client disconnections will not generate a
   * {@link com.adaptris.core.ServiceException} so normal processing continues. Set this to be true
   * if you want error handling to be triggered in this situation. This defaults to {@code false}
   * unless explicitly configured.
   * </p>
   */
  @AdvancedConfig(rare = true)
  @InputFieldDefault(value = "false")
  @Getter
  @Setter
  private Boolean forwardConnectionException;
  @AdvancedConfig(rare = true)
  @InputFieldDefault(value = "true")
  @Getter
  @Setter
  private Boolean flushBuffer;

  public ResponseProducerImpl() {
    setResponseHeaderProvider(new NoOpResponseHeaderProvider());
    setContentTypeProvider(new NullContentTypeProvider());
    setStatusProvider(new ConfiguredStatusProvider(HttpStatus.INTERNAL_ERROR_500));
  }

  @Override
  public void prepare() throws CoreException
  {
  }

  @Override
  public String endpoint(AdaptrisMessage msg) throws ProduceException {
    return null;
  }

  public <T extends ResponseProducerImpl> T withStatusProvider(HttpStatusProvider p) {
    setStatusProvider(p);
    return (T) this;
  }

  public <T extends ResponseProducerImpl> T withResponseHeaderProvider(ResponseHeaderProvider<HttpServletResponse> p) {
    setResponseHeaderProvider(p);
    return (T) this;
  }

  public <T extends ResponseProducerImpl> T withForwardConnectionException(Boolean b) {
    setForwardConnectionException(b);
    return (T) this;
  }

  protected boolean forwardConnectionException() {
    return BooleanUtils.toBooleanDefaultIfNull(getForwardConnectionException(), false);
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

  public <T extends ResponseProducerImpl> T withContentTypeProvider(ContentTypeProvider b) {
    setContentTypeProvider(b);
    return (T) this;
  }

  public <T extends ResponseProducerImpl> T withSendPayload(Boolean b) {
    setSendPayload(b);
    return (T) this;
  }

  protected boolean sendPayload() {
    return BooleanUtils.toBooleanDefaultIfNull(getSendPayload(), true);
  }


}
