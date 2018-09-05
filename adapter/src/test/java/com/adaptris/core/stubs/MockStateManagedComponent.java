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

package com.adaptris.core.stubs;

import com.adaptris.core.AdaptrisComponentImp;
import com.adaptris.core.ClosedState;
import com.adaptris.core.ComponentState;
import com.adaptris.core.CoreException;
import com.adaptris.core.InitialisedState;
import com.adaptris.core.StartedState;
import com.adaptris.core.StateManagedComponent;
import com.adaptris.core.StoppedState;

/**
 * <p>
 * Mock implementation for testing.
 * </p>
 */
public class MockStateManagedComponent extends AdaptrisComponentImp implements StateManagedComponent {
  private ComponentState state = ClosedState.getInstance();

  public MockStateManagedComponent() {

  }

  @Override
  public void prepare() throws CoreException {
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
