/*
 * $RCSfile: MockStateManagedComponent.java,v $
 * $Revision: 1.5 $
 * $Date: 2009/01/20 13:56:33 $
 * $Author: lchan $
 */
package com.adaptris.core.stubs;

import com.adaptris.core.ClosedState;
import com.adaptris.core.ComponentState;
import com.adaptris.core.CoreException;
import com.adaptris.core.InitialisedState;
import com.adaptris.core.StartedState;
import com.adaptris.core.StateManagedComponent;
import com.adaptris.core.StoppedState;
import com.adaptris.util.license.License;

/**
 * <p>
 * Mock implementation for testing.
 * </p>
 */
public class MockStateManagedComponent implements StateManagedComponent {
  private ComponentState state = ClosedState.getInstance();

  public MockStateManagedComponent() {

  }

  /**
   * @see com.adaptris.core.AdaptrisComponent#isEnabled
   *      (com.adaptris.util.license.License)
   */
  public boolean isEnabled(License license) throws CoreException {
    return true;
  }

  public String getUniqueId() {
    return null;
  }

  public void requestInit() throws CoreException {
    state.requestInit(this);
  }

  public void requestStart() throws CoreException {
    state.requestStart(this);
  }

  public void requestStop() {
    state.requestStop(this);
  }

  public void requestClose() {
    state.requestClose(this);
  }
  
  public void changeState(ComponentState newState) {
    state = newState;
  }

  public ComponentState retrieveComponentState() {
    return state;
  }

  public void init() throws CoreException {
    state = InitialisedState.getInstance();
  }

  public void start() throws CoreException {
    state = StartedState.getInstance();
  }

  public void stop() {
    state = StoppedState.getInstance();
  }

  public void close() {
    state = ClosedState.getInstance();
  }
}
