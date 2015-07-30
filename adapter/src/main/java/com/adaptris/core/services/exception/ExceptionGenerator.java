package com.adaptris.core.services.exception;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.ServiceException;

/**
 * Interface for generating an exception from an {@link AdaptrisMessage} object.
 *
 * @author lchan
 * 
 */
public interface ExceptionGenerator {

  /**
   * Generate a service exception from the AdaptrisMessage.
   *
   * @param msg the message
   * @return a ServiceException ready to be thrown, or null if no exception is
   *         appropriate
   */
  ServiceException create(AdaptrisMessage msg);

}
