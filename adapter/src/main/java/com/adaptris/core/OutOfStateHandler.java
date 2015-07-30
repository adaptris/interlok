package com.adaptris.core;

/**
 * <p>
 * Implementations will be able to test if a {@link StateManagedComponent} is in the expected state.
 * </p>
 * <p>
 * Also, all implementations will have a mechanism to handle any component that is not in the expected state.
 * </p>
 * <p>
 * Use implementations of this interface when you need/expect a particular StateManagedComponent to be in specific state at a given time.
 * </p>
 * @author Aaron
 *
 */
public interface OutOfStateHandler {
  
  boolean isInCorrectState(StateManagedComponent state) throws OutOfStateException;
  
  void handleOutOfState(StateManagedComponent state) throws OutOfStateException;

}
