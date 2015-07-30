package com.adaptris.core.http;

import javax.servlet.http.HttpServletRequest;

import com.adaptris.core.AdaptrisMessage;

/**
 * Interface for handling HTTP Parameters.
 * 
 * 
 */
public interface ParameterHandler {

  /**
   * Handle the parameters from the HTTP request.
   * 
   * @param message the target {@link AdaptrisMessage}
   * @param request the request
   * @param itemPrefix any prefix that needs to be applied.
   */
  public void handleParameters(AdaptrisMessage message, HttpServletRequest request, String itemPrefix);
  
}
