package com.adaptris.core.http;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreException;

/**
 * Interface to provide a {@code Content-Type} header for the HTTP request or response.
 * 
 * @author lchan
 * 
 */
public interface ContentTypeProvider {

  /**
   * Get the content type.
   * 
   * @param msg the Adaptris Message
   * @return the content type.
   * @throws CoreException wrapping other exceptions
   */
  String getContentType(AdaptrisMessage msg) throws CoreException;

}
