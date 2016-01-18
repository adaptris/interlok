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

import static com.adaptris.core.AdaptrisMessageFactory.defaultIfNull;
import static org.apache.commons.lang.StringUtils.isBlank;

import java.io.IOException;
import java.io.OutputStream;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.apache.commons.io.IOUtils;

import com.adaptris.annotation.AdapterComponent;
import com.adaptris.annotation.AdvancedConfig;
import com.adaptris.annotation.AutoPopulated;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreException;
import com.adaptris.core.http.server.HeaderHandler;
import com.adaptris.core.http.server.ParameterHandler;
import com.adaptris.util.stream.StreamUtil;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * This is the standard class that receives documents via HTTP.
 * <p>
 * You should configure the {@link #setDestination(com.adaptris.core.ConsumeDestination)} to contain the URI that should trigger
 * this consumer (e.g. {@code /path/to/my/workflow}). The value from {@link
 * com.adaptris.core.ConsumeDestination#getFilterExpression()} is used to determine which HTTP methods are appropriate for this
 * consumer and should be a comma separated list. In the event that the filter
 * expression is empty / null then all HTTP methods are acceptable ({@code "GET", "POST", "HEAD", "OPTIONS", "PUT", "DELETE",
 * "TRACE", "CONNECT"}).
 * <p>
 * If you want to preserve the http request headers or parameters simply configure a handler for either or both the headers and
 * parameters.
 * </p>
 * <p>
 * See the following javadoc for the following configuration items for headers/parameters;
 * <ul>
 * <li>{@link ParameterHandler parameter-handler}</li>
 * <li>{@link HeaderHandler header-handler}</li>
 * </ul>
 * </p>
 * <p>Note that if you intend for this class to be consumer withing a {@link com.adaptris.core.PoolingWorkflow} then you should
 * consider configuring a {@link JettyPoolingWorkflowInterceptor} as part of that workflow.
 * </p>
 * @config jetty-message-consumer
 * 
 * @author lchan
 * @author $Author: lchan $
 */
@XStreamAlias("jetty-message-consumer")
@AdapterComponent
@ComponentProfile(summary = "Listen for HTTP traffic on the specified URI", tag = "consumer,http,https")
public class MessageConsumer extends BasicJettyConsumer {

  @AdvancedConfig
  @Deprecated
  private String headerPrefix;
  @AdvancedConfig
  @Deprecated
  private String paramPrefix;
  
  @AutoPopulated
  @Valid
  @NotNull
  @AdvancedConfig
  private ParameterHandler<HttpServletRequest> parameterHandler;
  @AutoPopulated
  @Valid
  @NotNull
  @AdvancedConfig
  private HeaderHandler<HttpServletRequest> headerHandler;

  public MessageConsumer() {
    super();
    
    this.setParameterHandler(new NoOpParameterHandler());
    this.setHeaderHandler(new NoOpHeaderHandler());
  }


  /**
   * Set the header prefix for any stored headers.
   * 
   * @param s the header prefix
   * @deprecated since 3.0.6, configure the behaviour on the {@link HeaderHandler} directly.
   * 
   */
  @Deprecated
  public void setHeaderPrefix(String s) {
    headerPrefix = s;
  }

  /**
   * get the header prefix for any stored headers.
   *
   * @return the header prefix
   * @deprecated since 3.0.6, configure the behaviour on the {@link HeaderHandler} directly.
   */
  @Deprecated
  public String getHeaderPrefix() {
    return headerPrefix;
  }


  @Override
  public AdaptrisMessage createMessage(HttpServletRequest request,
                                       HttpServletResponse response)
      throws IOException, ServletException {
    AdaptrisMessage msg = null;
    OutputStream out = null;
    try {
      logHeaders(request);
      if (getEncoder() != null) {
        msg = getEncoder().readMessage(request);
      }
      else {
        msg = defaultIfNull(getMessageFactory()).newMessage();
        out = msg.getOutputStream();
        if (request.getContentLength() == -1) {
          IOUtils.copy(request.getInputStream(), out);
        }
        else {
          StreamUtil.copyStream(request.getInputStream(), out, request
              .getContentLength());
        }
        out.flush();
      }
      msg.setContentEncoding(request.getCharacterEncoding());
      addParamMetadata(msg, request);
      addHeaderMetadata(msg, request);
    }
    catch (CoreException e) {
      IOException ioe = new IOException(e.getMessage());
      ioe.initCause(e);
      throw ioe;
    }
    finally {
      IOUtils.closeQuietly(out);
    }
    return msg;
  }

  @SuppressWarnings("deprecation")
  private void addParamMetadata(AdaptrisMessage msg, HttpServletRequest request) {
    if (!isBlank(getParamPrefix())) {
      log.warn("Deprecated Config Warning:: configured using setParamPrefix(), configure the handler instead.");
      this.getParameterHandler().handleParameters(msg, request, this.getParamPrefix());
    } else {
      this.getParameterHandler().handleParameters(msg, request);
    }
  }

  @SuppressWarnings("deprecation")
  private void addHeaderMetadata(AdaptrisMessage msg, HttpServletRequest request) {
    if (!isBlank(getHeaderPrefix())) {
      log.warn("Deprecated Config Warning:: configured using setHeaderPrefix(), configure the handler instead.");
      this.getHeaderHandler().handleHeaders(msg, request, this.getHeaderPrefix());
    } else {
      this.getHeaderHandler().handleHeaders(msg, request);
    }
  }

  /**
   * @return the paramPrefix
   * @deprecated since 3.0.6, configure the behaviour on the {@link ParameterHandler} directly.
   */
  @Deprecated
  public String getParamPrefix() {
    return paramPrefix;
  }

  /**
   * Set the prefix for any parameters that are added as metadata.
   * 
   * @param s the paramPrefix to set, defaults to 'null'.
   * @deprecated since 3.0.6, configure the behaviour on the {@link ParameterHandler} directly.
   */
  @Deprecated
  public void setParamPrefix(String s) {
    paramPrefix = s;
  }

  public ParameterHandler<HttpServletRequest> getParameterHandler() {
    return parameterHandler;
  }

  public void setParameterHandler(ParameterHandler<HttpServletRequest> parameterHandler) {
    this.parameterHandler = parameterHandler;
  }

  public HeaderHandler<HttpServletRequest> getHeaderHandler() {
    return headerHandler;
  }

  public void setHeaderHandler(HeaderHandler<HttpServletRequest> headerHandler) {
    this.headerHandler = headerHandler;
  }

  @Override
  public void prepare() throws CoreException {
  }

}
