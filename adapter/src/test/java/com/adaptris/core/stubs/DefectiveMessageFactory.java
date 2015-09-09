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