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



/**
 * Concrete implementations of this <code>Event</code> contains details of the {@link Adapter} configuration.
 * <p>
 * The earliest point in the adapter lifecycle that an event can be emitted is after <code>init</code>, therefore this event is
 * always emitted at the end of the initialisation phase.
 * </p>
 * <p>
 * Concrete implementations of this class provide solution-specific behaviour, such as extracting valid receive destinations,
 * sending the entire adapter config, etc.
 * </p>
 * 
 * @see StandardAdapterStartUpEvent
 */
public abstract class AdapterStartUpEvent extends AdapterLifecycleEvent {
  private static final long serialVersionUID = 2014012301L;

  public AdapterStartUpEvent() {
    super(EventNameSpaceConstants.ADAPTER_START_UP);
  }

  /**
   * <p>
   * Sets the <code>Adapter</code> that generated this start up event.
   * Solution-specific sub-classes should implement this method and extract
   * information that they need (such as valid consume destinations) from the
   * <code>Adapter</code> object (hence the ability to throw
   * <code>Exception</code>s from a set).
   * </p>
   * @param adapter the <code>Adapter</code> that generated this start-up event
   * @throws CoreException wrapping any underlying Exceptions that may occur
   */
  public abstract void setAdapter(Adapter adapter) throws CoreException;
}
