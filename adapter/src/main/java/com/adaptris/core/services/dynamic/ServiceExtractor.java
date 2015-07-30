package com.adaptris.core.services.dynamic;

import java.io.IOException;
import java.io.InputStream;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.ServiceException;

/**
 * Interface for use with {@link DynamicServiceExecutor}.
 * 
 * @author lchan
 * 
 */
public interface ServiceExtractor {

  /**
   * Get an {@link InputStream} that can be unmarshalled into a service.
   * 
   * @param m the adaptris message.
   * @return an input stream.
   * @throws IOException on IO errors.
   * @throws ServiceException wrapping any other exceptions.
   */
  InputStream getInputStream(AdaptrisMessage m) throws ServiceException, IOException;
}
