package com.adaptris.core.stubs;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreException;
import com.adaptris.core.ServiceException;
import com.adaptris.core.services.confirmation.SetUpConfirmationServiceImp;
import com.adaptris.util.license.License;

/**
 * <p>
 * Example <code>Service</code> which sets up a confirmation using
 * the current time as the confirmation ID.
 * </p>
 */
public class MockSetUpConfirmationService extends
    SetUpConfirmationServiceImp {

  /** 
   * @see com.adaptris.core.Service#doService(com.adaptris.core.AdaptrisMessage)
   */
  public void doService(AdaptrisMessage msg) throws ServiceException {
    
    this.registerConfirmationId
      (msg, new Long(System.currentTimeMillis()).toString());
  }

  @Override
  public boolean isEnabled(License license) throws CoreException {
    return true;
  }

}
