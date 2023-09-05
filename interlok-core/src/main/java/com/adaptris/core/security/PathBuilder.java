package com.adaptris.core.security;

import java.util.Map;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.ComponentLifecycle;
import com.adaptris.core.ComponentLifecycleExtension;
import com.adaptris.core.ServiceException;


/**
 * 
 * @author jwickham
 * 
 * Interface for handling paths to be used.
 * Used to validate paths, extract data from paths and insert data back into paths.
 *
 */

public interface PathBuilder extends ComponentLifecycle, ComponentLifecycleExtension {
  
  /**
   * Extract from a payloads path.
   * 
   * @return a Map<String, String> containing key/value pairs. 
   * <strong>key = payloads path.<strong>
   * <strong>value = path's content.<strong>
   */
  
  Map<String, String> extract(AdaptrisMessage msg) throws ServiceException;
  
  /**
   * Insert values back into a payload.
   * 
   * <p>
   *  The Map<String, String> 
   *  param should contain:
   *  <strong>key = payloads path.<strong>
   *  <strong>value = path's content to be inserted.<strong>
   *  <p>
   */
   
  void insert(AdaptrisMessage msg, Map<String, String> pathKeyValuePairs) throws ServiceException;
  
}
