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
 * Implementation of the {@link OutOfStateHandler} which does no checks.
 * @config null-out-of-state-handler
 *
 */
@XStreamAlias("null-out-of-state-handler")
public class NullOutOfStateHandler implements OutOfStateHandler {

  public NullOutOfStateHandler() {}

  public boolean isInCorrectState(StateManagedComponent component) throws OutOfStateException {
    return true;
  }

  @Override
  public void handleOutOfState(StateManagedComponent state) throws OutOfStateException {
  }
}
