package com.adaptris.core.http.jetty;

import javax.servlet.http.HttpServletRequest;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.http.server.HeaderHandler;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * {@link HeaderHandler} implementation that ignores HTTP headers.
 * 
 * @config jetty-http-ignore-headers
 * 
 */
@XStreamAlias("jetty-http-ignore-headers")
public class NoOpHeaderHandler implements HeaderHandler<HttpServletRequest> {

  @Override
  public void handleHeaders(AdaptrisMessage message, HttpServletRequest request, String itemPrefix) {
    // No operation
  }

  @Override
  public void handleHeaders(AdaptrisMessage message, HttpServletRequest request) {
  }

}
