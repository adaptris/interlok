package com.adaptris.core.services;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreException;
import com.adaptris.core.ServiceException;
import com.adaptris.core.ServiceImp;
import com.adaptris.core.services.exception.ThrowExceptionService;
import com.adaptris.util.license.License;
import com.adaptris.util.license.License.LicenseType;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Always fail.
 * 
 * @config always-fail-service
 * 
 * @license BASIC
 * @author lchan
 * @deprecated consider using {@link ThrowExceptionService} instead (since 2.6.2) which wil give you a better exception message.
 */
@Deprecated
@XStreamAlias("always-fail-service")
public class AlwaysFailService extends ServiceImp {

  private static transient boolean warningLogged;

  public AlwaysFailService() {
    super();
    if (!warningLogged) {
      log.warn("[{}] is deprecated, use [{}] instead", this.getClass().getSimpleName(), ThrowExceptionService.class.getName());
      warningLogged = true;
    }
  }

  @Override
  public void doService(AdaptrisMessage msg) throws ServiceException {
    throw new ServiceException(this.getClass().getName());
  }

  @Override
  public void close() {
  }

  @Override
  public void init() throws CoreException {
  }

  @Override
  public boolean isEnabled(License license) throws CoreException {
    return license.isEnabled(LicenseType.Basic);
  }
}
