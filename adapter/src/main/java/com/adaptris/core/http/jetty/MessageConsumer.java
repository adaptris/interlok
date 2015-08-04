package com.adaptris.core.http.jetty;

import static com.adaptris.core.AdaptrisMessageFactory.defaultIfNull;

import java.io.IOException;
import java.io.OutputStream;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.apache.commons.io.IOUtils;

import com.adaptris.annotation.AdvancedConfig;
import com.adaptris.annotation.AutoPopulated;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.ConsumeDestination;
import com.adaptris.core.CoreException;
import com.adaptris.core.http.HeaderHandler;
import com.adaptris.core.http.NoOpHeaderHandler;
import com.adaptris.core.http.NoOpParameterHandler;
import com.adaptris.core.http.ParameterHandler;
import com.adaptris.util.stream.StreamUtil;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * This is the standard class that receives documents via HTTP.
 * <p>
 * You should configure the {@link #setDestination(com.adaptris.core.ConsumeDestination)} to contain the URI that should trigger
 * this consumer (e.g. {@code /path/to/my/workflow}). The value from {@link ConsumeDestination#getFilterExpression()} is used to
 * determine which HTTP methods are appropriate for this consumer and should be a comma separated list. In the event that the filter
 * expression is empty / null then all HTTP methods are acceptable (
 * {@code "GET", "POST", "HEAD", "OPTIONS", "PUT", "DELETE", "TRACE", "CONNECT"}).
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
 * 
 * @config jetty-message-consumer
 * @license BASIC
 * @author lchan
 * @author $Author: lchan $
 */
@XStreamAlias("jetty-message-consumer")
public class MessageConsumer extends BasicJettyConsumer {

  @AdvancedConfig
  private String headerPrefix;
  @AdvancedConfig
  private String paramPrefix;
  
  @AutoPopulated
  @Valid
  @NotNull
  @AdvancedConfig
  private ParameterHandler parameterHandler;
  @AutoPopulated
  @Valid
  @NotNull
  @AdvancedConfig
  private HeaderHandler headerHandler;

  public MessageConsumer() {
    super();
    
    this.setParameterHandler(new NoOpParameterHandler());
    this.setHeaderHandler(new NoOpHeaderHandler());
  }


  /**
   * Set the header prefix for any stored headers.
   * 
   * @param s the header prefix
   */
  public void setHeaderPrefix(String s) {
    headerPrefix = s;
  }

  /**
   * get the header prefix for any stored headers.
   *
   * @return the header prefix
   */
  public String getHeaderPrefix() {
    return headerPrefix;
  }

  /**
   *
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    StringBuffer sb = new StringBuffer("[");
    sb.append(this.getClass().getName());
    sb.append(",parameterHandler=[").append(this.getParameterHandler().getClass().getSimpleName());
    sb.append("],headerHandler=[").append(this.getHeaderHandler().getClass().getSimpleName());
    sb.append("],headerPrefix=[").append(getHeaderPrefix());
    sb.append("]]");
    return sb.toString();
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
      msg.setCharEncoding(request.getCharacterEncoding());
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

  private void addParamMetadata(AdaptrisMessage msg, HttpServletRequest request) {
    this.getParameterHandler().handleParameters(msg, request, this.getParamPrefix());
  }

  private void addHeaderMetadata(AdaptrisMessage msg, HttpServletRequest request) {
    this.getHeaderHandler().handleHeaders(msg, request, this.getHeaderPrefix());
  }

  /**
   * @return the paramPrefix
   */
  public String getParamPrefix() {
    return paramPrefix;
  }

  /**
   * Set the prefix for any parameters that are added as metadata.
   * 
   * @param s the paramPrefix to set, defaults to 'null'.
   * @see #setHeaderPrefix(String)
   */
  public void setParamPrefix(String s) {
    paramPrefix = s;
  }

  public ParameterHandler getParameterHandler() {
    return parameterHandler;
  }

  public void setParameterHandler(ParameterHandler parameterHandler) {
    this.parameterHandler = parameterHandler;
  }

  public HeaderHandler getHeaderHandler() {
    return headerHandler;
  }

  public void setHeaderHandler(HeaderHandler headerHandler) {
    this.headerHandler = headerHandler;
  }
}
