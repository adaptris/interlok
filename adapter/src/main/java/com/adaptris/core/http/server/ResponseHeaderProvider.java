package com.adaptris.core.http.server;

import com.adaptris.core.AdaptrisMessage;

/**
 * Interface to generate http response headers.
 * 
 * 
 */
public interface ResponseHeaderProvider<T> {

  /**
   * Apply any additional headers required.
   * 
   * @param msg the {@link AdaptrisMessage} to source the headers from
   * @param target the target object to configure
   * @return the modified target object
   */
  T addHeaders(AdaptrisMessage msg, T target);
}
