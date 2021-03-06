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

package com.adaptris.core;

import javax.validation.constraints.NotNull;

import com.adaptris.annotation.AutoPopulated;

/**
 * <p>
 * Abstract implementation of the {@link OutOfStateHandler}.
 * </p>
 * <p>
 * All extending classes will be expected to "setup" by configuring the expected (correct) state.
 * If this is not configured, then we will auto-populate the expected state to be "STARTED".
 * </p>
 * <p>
 * The valid states are the following; "STARTED", "STOPPED", "CLOSED" and "INITIALISED".
 * </p>
 * <p>
 * Extending classes will only need to implement the handleOutOfState method.
 * </p>
 * @author Aaron
 *
 */
public abstract class OutOfStateHandlerImp implements OutOfStateHandler {
  
  @NotNull
  @AutoPopulated
  private ConfiguredComponentState correctState;
  
  public OutOfStateHandlerImp() {
    correctState = ConfiguredComponentState.STARTED;
  }
  
  public boolean isInCorrectState(StateManagedComponent component) throws OutOfStateException {
    return component.retrieveComponentState().equals(this.getCorrectState().getComponentState());
  }
  
  public ConfiguredComponentState getCorrectState() {
    return correctState;
  }

  public void setCorrectState(ConfiguredComponentState state) {
    this.correctState = state;
  }
}
