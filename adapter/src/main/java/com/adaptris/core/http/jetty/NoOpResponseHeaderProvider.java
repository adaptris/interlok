package com.adaptris.core.http.jetty;

import javax.servlet.http.HttpServletResponse;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.http.server.ResponseHeaderProvider;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * {@link ResponseHeaderProvider} implementation that does not add any HTTP response headers.
 * 
 * @config jetty-no-response-headers
 * 
 */
@XStreamAlias("jetty-no-response-headers")
public class NoOpResponseHeaderProvider implements ResponseHeaderProvider<HttpServletResponse> {

  @Override
  public HttpServletResponse addHeaders(AdaptrisMessage msg, HttpServletResponse target) {
    return target;
  }

}
