package com.adaptris.core;

import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * <p>
 * This implementation of the {@link OutOfStateHandler} will simply throw an {@link OutOfStateException} every time when a
 * {@link StateManagedComponent} is not in the correct/expected state.
 * </p>
 * <p>
 * Example configuration:
 * 
 * <pre>
 * {@code 
 * <raise-exception-out-of-state-handler>
 *    <correct-state>STARTED</correct-state>
 * </raise-exception-out-of-state-handler>
 * }
 * </pre>
 * </p>
 * 
 * @config raise-exception-out-of-state-handler
 * 
 * @author Aaron
 * 
 */
@XStreamAlias("raise-exception-out-of-state-handler")
public class RaiseExceptionOutOfStateHandler extends OutOfStateHandlerImp {
  
  public RaiseExceptionOutOfStateHandler() {
    super();
  }
  
  @Override
  public void handleOutOfState(StateManagedComponent component) throws OutOfStateException {
    if(!this.isInCorrectState(component))
      throw new OutOfStateException("Expected state: " + this.getCorrectState().getClass().getSimpleName() + " but got " + component.retrieveComponentState().getClass().getSimpleName());
  }

}
