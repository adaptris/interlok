package com.adaptris.core.services.dynamic;

import java.io.IOException;
import java.io.InputStream;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.ServiceException;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Default {@link ServiceExtractor} implementation that treats the entire payload as the service.
 * 
 * @config dynamic-default-service-extractor
 * @author lchan
 * 
 */
@XStreamAlias("dynamic-default-service-extractor")
public class DefaultServiceExtractor implements ServiceExtractor {

  @Override
  public InputStream getInputStream(AdaptrisMessage m) throws ServiceException, IOException {
    return m.getInputStream();
  }

}
