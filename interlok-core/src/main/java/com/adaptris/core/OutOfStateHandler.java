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

/**
 * <p>
 * Implementations will be able to test if a {@link StateManagedComponent} is in the expected state.
 * </p>
 * <p>
 * Also, all implementations will have a mechanism to handle any component that is not in the expected state.
 * </p>
 * <p>
 * Use implementations of this interface when you need/expect a particular StateManagedComponent to be in specific state at a given time.
 * </p>
 * @author Aaron
 *
 */
public interface OutOfStateHandler {
  
  boolean isInCorrectState(StateManagedComponent state) throws OutOfStateException;
  
  void handleOutOfState(StateManagedComponent state) throws OutOfStateException;

}
