/*
 * $RCSfile: StubMessageFactory.java,v $
 * $Revision: 1.1 $
 * $Date: 2008/08/14 13:40:52 $
 * $Author: lchan $
 */
package com.adaptris.core.stubs;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.DefaultMessageFactory;

/**
 * <p>
 * The stub factory which returns implementations of
 * <code>AdaptrisMessage</code>.
 * </p>
 * <p>
 * Functionally the same as DefaultMessageFactory but we want to check that the
 * message implementations that are created can be different.
 * </p>
 */
public final class DefectiveMessageFactory extends DefaultMessageFactory {

  @Override
  public AdaptrisMessage newMessage() {
    AdaptrisMessage result = new DefectiveAdaptrisMessage(uniqueIdGenerator, this);
    return result;
  }

}