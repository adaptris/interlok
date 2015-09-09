package com.adaptris.core.stubs;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.BranchingServiceImp;
import com.adaptris.core.CoreException;
import com.adaptris.core.ServiceException;

/**
 * <p>
 * Example branching <code>Service</code>.  Selects a configurable ID for the 
 * next <code>Service</code> to apply based on a whether a random number is
 * greater than or less than 0.5.
 * </p>
 */
public class ExampleBranchingService extends BranchingServiceImp {
  
  private String lowerServiceId;
  private String higherServiceId;

  /** @see com.adaptris.core.Service
   *   #doService(com.adaptris.core.AdaptrisMessage) */
  public void doService(AdaptrisMessage msg) throws ServiceException {
    if (Math.random() < 0.5) {
      msg.setNextServiceId(lowerServiceId);
    }
    else {
      msg.setNextServiceId(higherServiceId);
    }
  }

  /** @see com.adaptris.core.AdaptrisComponent#init() */
  public void init() throws CoreException {
    // na
  }

  /** @see com.adaptris.core.AdaptrisComponent#close() */
  public void close() {
    // na
  }
  
  /**
   * <p>
   * Returns the unique Id of the next <code>Service</code> to apply if the 
   * random number is greater than 0.5.
   * </p>
   * @return the unique Id of the next <code>Service</code> to apply if the 
   * random number is greater than 0.5
   */
  public String getHigherServiceId() {
    return higherServiceId;
  }

  /**
   * <p>
   * Returns the unique Id of the next <code>Service</code> to apply if the 
   * random number is less than 0.5.
   * </p>
   * @return the unique Id of the next <code>Service</code> to apply if the 
   * random number is less than 0.5
   */
  public String getLowerServiceId() {
    return lowerServiceId;
  }

  /**
   * <p>
   * Sets the unique Id of the next <code>Service</code> to apply if the 
   * random number is greater than 0.5.
   * </p>
   * @param string the unique Id of the next <code>Service</code> to apply if 
   * the random number is greater than 0.5
   */
  public void setHigherServiceId(String string) {
    higherServiceId = string;
  }

  /**
   * <p>
   * Sets the unique Id of the next <code>Service</code> to apply if 
   * the random number is less than 0.5.
   * </p>
   * @param string the unique Id of the next <code>Service</code> to 
   * apply if the random number is less than 0.5
   */
  public void setLowerServiceId(String string) {
    lowerServiceId = string;
  }
}
