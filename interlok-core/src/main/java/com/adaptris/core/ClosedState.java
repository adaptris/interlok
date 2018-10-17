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
 * Represents closed <code>StateManagedComponent</code>s and implements permitted transitions.
 * </p>
 */
public final class ClosedState extends ComponentStateImp {

  private static final long serialVersionUID = 2012022101L;
  private static ClosedState instance;

  /**
   * @see com.adaptris.core.ComponentState#requestInit (com.adaptris.core.StateManagedComponent)
   */
  @Override
  public void requestInit(StateManagedComponent comp) throws CoreException {
    synchronized (comp) {
      if (continueRequest(comp, this)) {
        comp.init();
        logActivity("Initialised", comp);
        comp.changeState(InitialisedState.getInstance());
      }
    }
  }

  /**
   * @see com.adaptris.core.ComponentState#requestStart (com.adaptris.core.StateManagedComponent)
   */
  @Override
  public void requestStart(StateManagedComponent comp) throws CoreException {
    synchronized (comp) {
      if (continueRequest(comp, this)) {
        requestInit(comp);
        comp.start();
        logActivity("Started", comp);
        comp.changeState(StartedState.getInstance());
      }
    }
  }

  /**
   * <p>
   * Returns the single instance of this class.
   * </p>
   * 
   * @return the single instance of this class
   */
  public static ClosedState getInstance() {
    if (instance == null) {
      instance = new ClosedState();
    }

    return instance;
  }

  /**
   * <p>
   * Creates a new instance.
   * </p>
   */
  private ClosedState() {
    // na
  }
}
