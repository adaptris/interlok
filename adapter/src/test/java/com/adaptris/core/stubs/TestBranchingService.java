/*
 * $RCSfile: TestBranchingService.java,v $
 * $Revision: 1.4 $
 * $Date: 2005/10/04 23:40:34 $
 * $Author: hfraser $
 */
package com.adaptris.core.stubs;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.BranchingServiceImp;
import com.adaptris.core.CoreException;
import com.adaptris.core.ServiceException;

/**
 * <p>
 * Test branching <code>Service</code>.  Returns "001" and "002" alternately.
 * </p>
 */
public class TestBranchingService extends BranchingServiceImp {
  
  private String nextServiceId = "001"; 

  /** @see com.adaptris.core.Service
   *   #doService(com.adaptris.core.AdaptrisMessage) */
  public void doService(AdaptrisMessage msg) throws ServiceException {
    msg.setNextServiceId(nextServiceId);
    
    if (nextServiceId.equals("001")) {
      nextServiceId = "002";     
    }
    else {
      nextServiceId = "001";
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
}
