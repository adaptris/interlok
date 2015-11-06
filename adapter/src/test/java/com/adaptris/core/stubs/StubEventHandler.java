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

import com.adaptris.core.ClosedState;
import com.adaptris.core.ComponentState;
import com.adaptris.core.CoreException;
import com.adaptris.core.Event;
import com.adaptris.core.EventHandler;
import com.adaptris.core.ProduceDestination;
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
  public void prepare() throws CoreException {
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
