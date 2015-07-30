/*
 * $RCSfile: DynamicFailingService.java,v $
 * $Revision: 1.1 $
 * $Date: 2008/03/03 16:47:12 $
 * $Author: lchan $
 */
package com.adaptris.core.services.dynamic;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreException;
import com.adaptris.core.ServiceException;
import com.adaptris.util.license.License;

/**
 * <p>
 * Test failing service that records the state. This is purely to test
 * functionality for DynamicServiceLocator.
 * </p>
 */
public class DynamicFailingService extends DynamicService {

  public enum WhenToFail {
    NEVER, ON_INIT, ON_START, ON_LICENSE
  };

  private WhenToFail whenToFail = WhenToFail.NEVER;

  public DynamicFailingService() {
    super();
  }

  public DynamicFailingService(WhenToFail wtf) {
    this();
    whenToFail = wtf;
  }

  @Override
  public void init() throws CoreException {
    if (whenToFail.equals(WhenToFail.ON_INIT)) {
      throw new CoreException(WhenToFail.ON_INIT + " failure specified");
    }
    super.init();
  }

  @Override
  public void start() throws CoreException {
    if (whenToFail.equals(WhenToFail.ON_START)) {
      throw new CoreException(WhenToFail.ON_START + " failure specified");
    }
    super.start();
  }

  @Override
  public boolean isEnabled(License l) {
    if (whenToFail.equals(WhenToFail.ON_LICENSE)) {
      return false;
    }
    return true;
  }

  /**
   * @see com.adaptris.core.Service
   *      #doService(com.adaptris.core.AdaptrisMessage)
   */
  @Override
  public void doService(AdaptrisMessage msg) throws ServiceException {
    throw new ServiceException("As Configured");
  }

  public String getWhenToFail() {
    return whenToFail.name();
  }

  public void setWhenToFail(String wtf) {
    this.whenToFail = WhenToFail.valueOf(wtf);
  }

}
