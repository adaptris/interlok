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

import com.adaptris.core.Adapter;
import com.adaptris.core.AdapterStartUpEvent;
import com.adaptris.core.CoreException;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * <p>
 * Event containing <code>Adapter</code> start-up information..
 * </p>
 *
 * @see AdapterStartUpEvent
 */
@XStreamAlias("stub-adapter-startup-event")
public class StubAdapterStartUpEvent extends AdapterStartUpEvent {
  private static final long serialVersionUID = 2014012301L;

  public StubAdapterStartUpEvent() throws Exception {
  }

  /**
   *
   * @see AdapterStartUpEvent#setAdapter(Adapter)
   */
  @Override
  public void setAdapter(Adapter param) throws CoreException {
  }
}
