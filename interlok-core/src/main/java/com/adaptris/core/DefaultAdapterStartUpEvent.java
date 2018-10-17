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

import com.adaptris.core.event.StandardAdapterStartUpEvent;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * <p>
 * Event containing <code>Adapter</code> start-up information..
 * </p>
 * 
 * @config default-adapter-start-up-event
 * 
 * @see AdapterStartUpEvent
 */
@XStreamAlias("default-adapter-start-up-event")
@Deprecated
public class DefaultAdapterStartUpEvent extends StandardAdapterStartUpEvent {
  private static final long serialVersionUID = 2014012301L;

  // DO NOT REMOVE THIS CLASS UNTIL WE GET RID OF ALL V2 CLIENTS TO A V3 HUB
  public DefaultAdapterStartUpEvent() throws Exception {
    super();
  }
}
