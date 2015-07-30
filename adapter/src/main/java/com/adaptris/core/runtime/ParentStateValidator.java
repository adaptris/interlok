package com.adaptris.core.runtime;

import com.adaptris.core.ComponentState;
import com.adaptris.core.CoreException;

final class ParentStateValidator {

  private enum ParentState {
    InitialisedState() {
      @Override
      boolean validAsNextChildState(ComponentState s) {
        return s == com.adaptris.core.InitialisedState.getInstance() || s == com.adaptris.core.ClosedState.getInstance();
      }
    },
    StartedState() {
      @Override
      boolean validAsNextChildState(ComponentState s) {
        return true;
      }
    },
    StoppedState() {
      @Override
      boolean validAsNextChildState(ComponentState s) {
        return s == com.adaptris.core.StoppedState.getInstance() || s == com.adaptris.core.ClosedState.getInstance();
      }
    },
    ClosedState() {
      @Override
      boolean validAsNextChildState(ComponentState s) {
        return s == com.adaptris.core.ClosedState.getInstance();
      }
    };
    abstract boolean validAsNextChildState(ComponentState s);

  };

  static void checkTransitionTo(ComponentState newState, ComponentState parentState) throws CoreException {
    ParentState validator = ParentState.valueOf(parentState.toString());
    if (!validator.validAsNextChildState(newState)) {
      throw new CoreException("Container Component State is [" + parentState + "]; not suitable for member transition to ["
          + newState + "]");
    }
  }
}
