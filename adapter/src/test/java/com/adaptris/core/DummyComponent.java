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

public class DummyComponent extends AdaptrisComponentImp implements StateManagedComponent {

  private ComponentState state;
  
  @Override
  public void prepare() throws CoreException {}

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
