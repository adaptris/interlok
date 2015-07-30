package com.adaptris.core;

public enum ConfiguredComponentState {

  STARTED {
    @Override
    public ComponentState getComponentState() {
      return StartedState.getInstance();
    }
  },
  
  STOPPED {
    @Override
    public ComponentState getComponentState() {
      return StoppedState.getInstance();
    }
  },
  
  CLOSED {
    @Override
    public ComponentState getComponentState() {
      return ClosedState.getInstance();
    }
  },
  
  INITIALISED {
    @Override
    public ComponentState getComponentState() {
      return InitialisedState.getInstance();
    }
  };
  
  public abstract ComponentState getComponentState();
  
}
