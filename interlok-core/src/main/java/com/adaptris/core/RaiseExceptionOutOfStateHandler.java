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

import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * <p>
 * This implementation of the {@link OutOfStateHandler} will simply throw an {@link OutOfStateException} every time when a
 * {@link StateManagedComponent} is not in the correct/expected state.
 * </p>
 * <p>
 * Example configuration:
 * 
 * <pre>
 * {@code 
 * <raise-exception-out-of-state-handler>
 *    <correct-state>STARTED</correct-state>
 * </raise-exception-out-of-state-handler>
 * }
 * </pre>
 * </p>
 * 
 * @config raise-exception-out-of-state-handler
 * 
 * @author Aaron
 * 
 */
@XStreamAlias("raise-exception-out-of-state-handler")
public class RaiseExceptionOutOfStateHandler extends OutOfStateHandlerImp {
  
  public RaiseExceptionOutOfStateHandler() {
    super();
  }
  
  @Override
  public void handleOutOfState(StateManagedComponent component) throws OutOfStateException {
    if(!this.isInCorrectState(component))
      throw new OutOfStateException("Expected state: " + this.getCorrectState().getClass().getSimpleName() + " but got " + component.retrieveComponentState().getClass().getSimpleName());
  }

}
