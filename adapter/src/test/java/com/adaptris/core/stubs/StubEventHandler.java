package com.adaptris.core.stubs;

import com.adaptris.core.ClosedState;
import com.adaptris.core.ComponentState;
import com.adaptris.core.CoreException;
import com.adaptris.core.Event;
import com.adaptris.core.EventHandler;
import com.adaptris.core.ProduceDestination;
import com.adaptris.util.license.License;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * This is dummy for marshalling purposes.
 *
 * @author lchan
 *
 */
@XStreamAlias("dummy-event-handler")
public class StubEventHandler implements EventHandler {

  private transient ComponentState state;

  public StubEventHandler() {
    changeState(ClosedState.getInstance());
  }

  @Override
  public void send(Event evt) throws CoreException {
  }

  @Override
  public void send(Event evt, ProduceDestination destination)
      throws CoreException {
  }

  @Override
  public void registerSourceId(String sourceId) {
  }

  @Override
  public String retrieveSourceId() {
    return null;
  }

  @Override
  public void close() {
  }

  @Override
  public void init() throws CoreException {
  }

  @Override
  public boolean isEnabled(License license) throws CoreException {
    return false;
  }

  @Override
  public void start() throws CoreException {
  }

  @Override
  public void stop() {
  }

  /**
   * @see com.adaptris.core.StateManagedComponent#retrieveComponentState()
   */
  @Override
  public ComponentState retrieveComponentState() {
    return state;
  }

  /**
   * @see com.adaptris.core.StateManagedComponent#getUniqueId()
   */
  @Override
  public String getUniqueId() {
    return null;
    // return this.getClass().getSimpleName();
  }

  public void changeState(ComponentState c) {
    state = c;
  }

  /**
   * @see com.adaptris.core.StateManagedComponent#requestInit()
   */
  @Override
  public void requestInit() throws CoreException {
    state.requestInit(this);
  }

  /**
   * @see com.adaptris.core.StateManagedComponent#requestStart()
   */
  @Override
  public void requestStart() throws CoreException {
    state.requestStart(this);
  }

  /**
   * @see com.adaptris.core.StateManagedComponent#requestStop()
   */
  @Override
  public void requestStop() {
    state.requestStop(this);
  }

  /**
   * @see com.adaptris.core.StateManagedComponent#requestClose()
   */
  @Override
  public void requestClose() {
    state.requestClose(this);
  }

}
