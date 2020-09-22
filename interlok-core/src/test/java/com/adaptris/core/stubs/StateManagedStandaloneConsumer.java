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

import com.adaptris.core.AdaptrisConnection;
import com.adaptris.core.AdaptrisMessageConsumer;
import com.adaptris.core.ClosedState;
import com.adaptris.core.ComponentState;
import com.adaptris.core.CoreException;
import com.adaptris.core.InitialisedState;
import com.adaptris.core.StartedState;
import com.adaptris.core.StateManagedComponent;
import com.adaptris.core.StoppedState;

public class StateManagedStandaloneConsumer extends MockStandaloneConsumer implements StateManagedComponent {
  private ComponentState state = ClosedState.getInstance();

  public StateManagedStandaloneConsumer(AdaptrisConnection c, AdaptrisMessageConsumer amc) throws Exception {
    super(c, amc);
  }

  @Override
  public void requestInit() throws CoreException {
    state.requestInit(this);
  }

  @Override
  public void requestStart() throws CoreException {
    state.requestStart(this);
  }

  @Override
  public void requestStop() {
    state.requestStop(this);
  }

  @Override
  public void requestClose() {
    state.requestClose(this);
  }
  
  @Override
  public void changeState(ComponentState newState) {
    state = newState;
  }

  @Override
  public ComponentState retrieveComponentState() {
    return state;
  }

  @Override
  public void init() throws CoreException {
    super.init();
    state = InitialisedState.getInstance();
  }

  @Override
  public void start() throws CoreException {
    super.start();
    state = StartedState.getInstance();
  }

  @Override
  public void stop() {
    super.stop();
    state = StoppedState.getInstance();
  }

  @Override
  public void close() {
    super.close();
    state = ClosedState.getInstance();
  }

}
