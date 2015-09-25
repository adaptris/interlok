package com.adaptris.core.http.jetty;

import javax.servlet.http.HttpServletRequest;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.http.server.ParameterHandler;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * {@link ParameterHandler} implementation that ignores HTTP parameters.
 * 
 * @config jetty-http-ignore-parameters
 * 
 */
@XStreamAlias("jetty-http-ignore-parameters")
public class NoOpParameterHandler implements ParameterHandler<HttpServletRequest> {

  @Override
  public void handleParameters(AdaptrisMessage message, HttpServletRequest request, String itemPrefix) {
    // No operation
  }

  @Override
  public void handleParameters(AdaptrisMessage message, HttpServletRequest request) {}

}
