/*
 * $RCSfile: ExampleConfirmService.java,v $
 * $Revision: 1.1 $
 * $Date: 2006/05/12 05:10:37 $
 * $Author: hfraser $
 */
package com.adaptris.core.stubs;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreException;
import com.adaptris.core.ServiceException;
import com.adaptris.core.services.confirmation.ConfirmServiceImp;
import com.adaptris.util.license.License;

/**
 * <p>
 * Example <code>Service</code> which makes a confirmation based on the 
 * payload of the msg containg the confirmation ID.
 * </p>
 */
public class MockConfirmService extends ConfirmServiceImp {

  /** @see com.adaptris.core.Service
   *   #doService(com.adaptris.core.AdaptrisMessage) */
  public void doService(AdaptrisMessage msg) throws ServiceException {
    this.confirm(msg, msg.getStringPayload());
  }

  @Override
  public boolean isEnabled(License license) throws CoreException {
    return true;
  }

}
