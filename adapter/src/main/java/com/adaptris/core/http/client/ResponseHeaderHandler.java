package com.adaptris.core.http.client;

import com.adaptris.core.AdaptrisMessage;

/**
 * Interface to handle the headers from the HTTP response.
 * 
 * 
 */
public interface ResponseHeaderHandler<T> {

  /**
   * Do something with the response headers
   * 
   * @param src the object containing the headers
   * @param msg the AdaptrisMessage.
   * @return the modified message.
   */
  AdaptrisMessage handle(T src, AdaptrisMessage msg);

}
