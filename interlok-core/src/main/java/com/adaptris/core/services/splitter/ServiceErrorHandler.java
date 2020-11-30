package com.adaptris.core.services.splitter;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.ServiceException;

public interface ServiceErrorHandler extends Thread.UncaughtExceptionHandler {

  void throwExceptionAsRequired() throws ServiceException;

  /**
   * @implNote The default implementation is no-op
   * @param msg the message that was successful
   */
  default void markSuccessful(AdaptrisMessage msg) {

  }


}
