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
import java.io.IOException;
import java.io.InputStream;
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
import com.adaptris.annotation.DisplayOrder;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreConstants;
import com.adaptris.core.CoreException;
import com.adaptris.core.http.server.HeaderHandler;
import com.adaptris.core.http.server.ParameterHandler;
import com.adaptris.util.stream.StreamUtil;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * This is the standard class that receives documents via HTTP.
 * <p>
 * You should configure the {@link #setDestination(com.adaptris.core.ConsumeDestination)} to contain the URI that should trigger
 * this consumer (e.g. {@code /path/to/my/workflow}). The value from
 * {@link com.adaptris.core.ConsumeDestination#getFilterExpression()} is used to determine which HTTP methods are appropriate for
 * this consumer and should be a comma separated list. In the event that the filter expression is empty / null then all HTTP methods
 * are acceptable ({@code "GET", "POST", "HEAD", "PUT", "DELETE",
 * "TRACE", "CONNECT", "PATCH"}) and will need to be handled directly by your workflow. {@code OPTIONS} will be automatically.
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
 * <p>
 * Note that if you intend for this class to be consumer withing a {@link com.adaptris.core.PoolingWorkflow} then you should
 * consider configuring a {@link JettyPoolingWorkflowInterceptor} as part of that workflow.
 * </p>
 * 
 * @config jetty-message-consumer
 * 
 * @author lchan
 * @author $Author: lchan $
 */
@XStreamAlias("jetty-message-consumer")
@AdapterComponent
@ComponentProfile(summary = "Listen for HTTP traffic on the specified URI", tag = "consumer,http,https", metadata =
{
        JettyConstants.JETTY_QUERY_STRING, JettyConstants.JETTY_URI, JettyConstants.JETTY_URL,
        CoreConstants.HTTP_METHOD, CoreConstants.MESSAGE_CONSUME_LOCATION
}, recommended =
{
    EmbeddedConnection.class, JettyConnection.class
})
@DisplayOrder(order = {"destination"})
public class JettyMessageConsumer extends BasicJettyConsumer {

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

  public JettyMessageConsumer() {
    super();
    
    this.setParameterHandler(new NoOpParameterHandler());
    this.setHeaderHandler(new NoOpHeaderHandler());
  }


  @Override
  public AdaptrisMessage createMessage(HttpServletRequest request,
                                       HttpServletResponse response)
      throws IOException, ServletException {
    AdaptrisMessage msg = null;
    try {
      logHeaders(request);
      if (getEncoder() != null) {
        msg = getEncoder().readMessage(request);
      }
      else {
        msg = defaultIfNull(getMessageFactory()).newMessage();
        try (InputStream in = request.getInputStream(); OutputStream out = msg.getOutputStream()) {
          if (request.getContentLength() == -1) {
            IOUtils.copy(request.getInputStream(), out);
          } else {
            StreamUtil.copyStream(request.getInputStream(), out, request.getContentLength());
          }
        }
      }
      msg.setContentEncoding(request.getCharacterEncoding());
      addParamMetadata(msg, request);
      addHeaderMetadata(msg, request);
    }
    catch (CoreException e) {
      throw new IOException(e.getMessage(), e);
    }
    return msg;
  }

  private void addParamMetadata(AdaptrisMessage msg, HttpServletRequest request) {
    this.getParameterHandler().handleParameters(msg, request);
  }

  private void addHeaderMetadata(AdaptrisMessage msg, HttpServletRequest request) {
    this.getHeaderHandler().handleHeaders(msg, request);
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
