package com.adaptris.core.services.confirmation;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreException;
import com.adaptris.core.MessageEventGenerator;
import com.adaptris.core.ServiceImp;

/**
 * <p>
 * Utilty class which can be extended by <code>Service</code>s which need to
 * set up confirmations.
 * </p>
 */
public abstract class SetUpConfirmationServiceImp extends ServiceImp {

  /**
   * <p>
   * Sets the passed <code>confirmationId</code> against the configured key.
   * </p>
   */
  protected void registerConfirmationId
    (AdaptrisMessage msg, String confirmationId) {
    
    msg.addObjectMetadata
      (MessageEventGenerator.CONFIRMATION_ID_KEY, confirmationId);
  }
  
  /** @see com.adaptris.core.AdaptrisComponent#init() */
  @Override
  public void init() throws CoreException {
    // n/a...  
  }

  /** @see com.adaptris.core.AdaptrisComponent#close() */
  @Override
  public void close() {
    // n/a
  }
}
