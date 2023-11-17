package com.adaptris.core.security;

import java.util.Map;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.ServiceException;

/**
 * Interface for {@link PathBuilder}.
 */
public interface PathBuilder {
  
  /**
   * Extract from a payload's path.
   * 
   * @return a 'Map&lt;String, String&gt;' containing key/value pairs. 
   * <strong>key = payloads path.<strong>
   * <strong>value = path's content.<strong>
   */
  Map<String, String> extract(AdaptrisMessage msg) throws ServiceException;
  
  /**
   * Insert values back into a payload.
   * 
   * <p>
   *  The 'Map&lt;String, String&gt;'
   *  param should contain:
   *  <strong>key = payloads path.<strong>
   *  <strong>value = path's content to be inserted.<strong>
   *  <p>
   */
  void insert(AdaptrisMessage msg, Map<String, String> pathKeyValuePairs) throws ServiceException;
  
}
