package com.adaptris.core.http.server;

import javax.servlet.http.HttpServletRequest;

import com.adaptris.core.AdaptrisMessage;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * {@link ParameterHandler} implementation that ignores HTTP parameters.
 * 
 * @config http-ignore-parameters
 * 
 */
@XStreamAlias("http-ignore-parameters")
public class NoOpParameterHandler implements ParameterHandler {

  @Override
  public void handleParameters(AdaptrisMessage message, HttpServletRequest request, String itemPrefix) {
    // No operation
  }

  @Override
  public void handleParameters(AdaptrisMessage message, HttpServletRequest request) {}

}
