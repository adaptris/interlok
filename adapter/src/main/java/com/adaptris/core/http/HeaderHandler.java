package com.adaptris.core.http;

import javax.servlet.http.HttpServletRequest;

import com.adaptris.core.AdaptrisMessage;

/**
 * Interface for handling behaviour for HTTP headers.
 * 
 * 
 */
public interface HeaderHandler {
  
  /**
   * Handle the headers from the {@link HttpServletRequest}.
   * 
   * @param message the target {@link AdaptrisMessage}
   * @param request the request
   * @param itemPrefix Any prefix that needs to be applied
   */
  public void handleHeaders(AdaptrisMessage message, HttpServletRequest request, String itemPrefix);

}
