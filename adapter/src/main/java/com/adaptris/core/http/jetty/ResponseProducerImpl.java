package com.adaptris.core.http.jetty;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import com.adaptris.annotation.AdvancedConfig;
import com.adaptris.annotation.AutoPopulated;
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
  private Boolean sendPayload;

  @AdvancedConfig
  private Boolean forwardConnectionException;
  @AdvancedConfig
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

  public ResponseHeaderProvider<HttpServletResponse> getResponseHeaderProvider() {
    return responseHeaderProvider;
  }

  public void setResponseHeaderProvider(ResponseHeaderProvider<HttpServletResponse> p) {
    this.responseHeaderProvider = p;
  }

  public Boolean getForwardConnectionException() {
    return forwardConnectionException;
  }

  /**
   * Set to true to throw an exception if producing the response fails.
   * 
   * <p>
   * When producing the reply to a client; it may be that they have already terminated the connection. By default client
   * disconnections will not generate a {@link ServiceException} so normal processing continues. Set this to be true if you want
   * error handling to be triggered in this situation.
   * </p>
   * 
   * @param b true to throw a ServiceException if producing the response fails., default null (false).
   */
  public void setForwardConnectionException(Boolean b) {
    this.forwardConnectionException = b;
  }

  protected boolean forwardConnectionException() {
    return getForwardConnectionException() != null ? getForwardConnectionException().booleanValue() : false;    
  }

  public Boolean getFlushBuffer() {
    return flushBuffer;
  }

  public void setFlushBuffer(Boolean flush) {
    this.flushBuffer = flush;
  }

  protected boolean flushBuffers() {
    return getFlushBuffer() != null ? getFlushBuffer().booleanValue() : true;
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

  /**
   * @return the sendPayload
   */
  public Boolean getSendPayload() {
    return sendPayload;
  }

  /**
   * Whether or not to send the {@link AdaptrisMessage#getPayload()} as part of the reply.
   *
   * @param b the sendPayload to set defaults true.
   */
  public void setSendPayload(Boolean b) {
    sendPayload = b;
  }

  protected boolean sendPayload() {
    return getSendPayload() != null ? getSendPayload().booleanValue() : true;
  }


}
