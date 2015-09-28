package com.adaptris.core;

import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Implementation of the {@link OutOfStateHandler} which does no checks.
 * @config null-out-of-state-handler
 *
 */
@XStreamAlias("null-out-of-state-handler")
public class NullOutOfStateHandler implements OutOfStateHandler {

  public NullOutOfStateHandler() {}

  public boolean isInCorrectState(StateManagedComponent component) throws OutOfStateException {
    return true;
  }

  @Override
  public void handleOutOfState(StateManagedComponent state) throws OutOfStateException {
  }
}
