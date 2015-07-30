package com.adaptris.core;

import com.adaptris.util.license.License;

public class DummyComponent implements StateManagedComponent {

  private ComponentState state;
  
  @Override
  public boolean isEnabled(License license) throws CoreException {
    return true;
  }

  @Override
  public void init() throws CoreException {}

  @Override
  public void start() throws CoreException {}

  @Override
  public void stop() {}

  @Override
  public void close() {}

  @Override
  public String getUniqueId() {return "id";}

  @Override
  public ComponentState retrieveComponentState() {
    return state;
  }

  @Override
  public void changeState(ComponentState newState) {
    state = newState;
  }

  @Override
  public void requestInit() throws CoreException {}

  @Override
  public void requestStart() throws CoreException {}

  @Override
  public void requestStop() {}

  @Override
  public void requestClose() {}
  
}