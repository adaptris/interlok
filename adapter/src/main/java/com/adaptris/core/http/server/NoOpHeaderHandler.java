package com.adaptris.core.http.server;

import javax.servlet.http.HttpServletRequest;

import com.adaptris.core.AdaptrisMessage;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * {@link ParameterHandler} implementation that ignores HTTP headers.
 * 
 * @config http-ignore-headers
 * 
 */
@XStreamAlias("http-ignore-headers")
public class NoOpHeaderHandler implements HeaderHandler {

  @Override
  public void handleHeaders(AdaptrisMessage message, HttpServletRequest request, String itemPrefix) {
    // No operation
  }

  @Override
  public void handleHeaders(AdaptrisMessage message, HttpServletRequest request) {
  }

}
