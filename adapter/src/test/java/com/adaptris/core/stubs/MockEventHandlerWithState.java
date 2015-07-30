/*
 * $RCSfile: MockEventHandlerWithState.java,v $
 * $Revision: 1.1 $
 * $Date: 2009/03/10 13:44:39 $
 * $Author: lchan $
 */
package com.adaptris.core.stubs;

import com.adaptris.core.CoreException;
import com.adaptris.core.DefaultEventHandler;

/**
 * @author lchan
 * @author $Author: lchan $
 */
public class MockEventHandlerWithState extends DefaultEventHandler {

  private String uniqueId;

  /**
   * @throws CoreException
   */
  public MockEventHandlerWithState() throws CoreException {
    super();
    setProducer(new MockMessageProducer());
  }

  public MockEventHandlerWithState(String id) throws CoreException {
    this();
    uniqueId = id;
  }

  @Override
  public String toString() {
    return uniqueId != null ? uniqueId : super.toString();
  }
}
