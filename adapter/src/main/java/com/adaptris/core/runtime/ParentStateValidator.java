/*
 * Copyright 2015 Adaptris Ltd.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/

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
