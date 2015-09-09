package com.adaptris.core;

import com.adaptris.util.license.License;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * <p>
 * Null implementation of <code>Service</code>.
 * </p>
 * 
 * @config null-service
 */
@XStreamAlias("null-service")
public class NullService extends ServiceImp {

  public NullService() {
    super();
  }

  public NullService(String uniqueId) {
    this();
    setUniqueId(uniqueId);
  }

  /** @see com.adaptris.core.Service#doService
   *   (com.adaptris.core.AdaptrisMessage) */
  public void doService(AdaptrisMessage msg) throws ServiceException {
    // do nothing
  }

  /** @see com.adaptris.core.AdaptrisComponent#init() */
  public void init() throws CoreException {
    // do nothing
  }

  /** @see com.adaptris.core.AdaptrisComponent#close() */
  public void close() {
    // do nothing
  }

  @Override
  public boolean isEnabled(License license) throws CoreException {
    return true;
  }

}
