package com.adaptris.core.http;

import com.adaptris.core.AdaptrisMessage;

/**
 * Interface for providing a HTTP method.
 * 
 */
public interface RequestMethodProvider {

  /**
   * Valid methods as defined by RFC2616 & RFC5789 (PATCH method).
   * <p>Note that this is simply a list of methods, and there may be limited/no support for those methods within configured
   * components that make use of the {@link MethodProvider} interface.</p>
   */
  public static enum RequestMethod {
    CONNECT,
    DELETE,
    GET,
    HEAD,
    OPTIONS,
    PATCH,
    PUT,
    POST,
    TRACE
  }

  /**
   * Get the method that should be used with the HTTP request.
   * 
   * @param msg the {@link AdaptrisMessage} if required to derive the method.
   * @return the method.
   */
  RequestMethod getMethod(AdaptrisMessage msg);
}
