/*
 * $RCSfile: ConfirmServiceImp.java,v $
 * $Revision: 1.3 $
 * $Date: 2008/05/01 09:51:03 $
 * $Author: lchan $
 */
package com.adaptris.core.services.confirmation;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreException;
import com.adaptris.core.MessageEventGenerator;
import com.adaptris.core.ServiceImp;

/**
 * <p>
 * Utilty class which can be extended by <code>Service</code>s which need to
 * confirm confirmations.
 * </p>
 */
public abstract class ConfirmServiceImp extends ServiceImp {

  /**
   * <p>
   * Verifies that a metadata key to obtain the confirmation ID from has been
   * set, and that <code>getIsConfirmation</code> returns true. We could
   * hard code <code>isConfirmation</code> to true, but I think that is 
   * potentially more confusing. 
   * </p> 
   *  @see com.adaptris.core.AdaptrisComponent#init() 
   */
  @Override
  public void init() throws CoreException {
    if (!isConfirmation()) {
      throw new CoreException("isConfirmation must be true");
    }
  }
  
  /**
   * <p>
   * Sets the passed <code>confirmationId</code> against the configured key.
   * </p>
   */
  protected void confirm(AdaptrisMessage msg, String confirmationId) {
    msg.addObjectMetadata
      (MessageEventGenerator.CONFIRMATION_ID_KEY, confirmationId);
  }
  
  /** @see com.adaptris.core.AdaptrisComponent#close() */
  @Override
  public void close() {
    // n/a
  }
}
