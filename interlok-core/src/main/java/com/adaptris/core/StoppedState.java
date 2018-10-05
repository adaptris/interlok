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
 * Represents stopped <code>StateManagedComponent</code>s and implements permitted transitions.
 * </p>
 */
public final class StoppedState extends ComponentStateImp {
  private static final long serialVersionUID = 2012022101L;
  private static StoppedState instance;

  /**
   * @see com.adaptris.core.ComponentState#requestStart (com.adaptris.core.StateManagedComponent)
   */
  @Override
  public void requestStart(StateManagedComponent comp) throws CoreException {
    synchronized (comp) {
      if (continueRequest(comp, this)) {
        comp.start();
        logActivity("Started", comp);
        comp.changeState(StartedState.getInstance());
      }
    }
  }

  /**
   * @see com.adaptris.core.ComponentState#requestClose (com.adaptris.core.StateManagedComponent)
   */
  @Override
  public void requestClose(StateManagedComponent comp) {
    synchronized (comp) {
      if (continueRequest(comp, this)) {
        comp.close();
        logActivity("Closed", comp);
        comp.changeState(ClosedState.getInstance());
      }
    }
  }

  /**
   * @see com.adaptris.core.ComponentState#requestInit (com.adaptris.core.StateManagedComponent)
   */
  @Override
  public void requestInit(StateManagedComponent comp) throws CoreException {
    logActivity("Ignoring requestInit()", comp);
  }

  /**
   * <p>
   * Returns the single instance of this class.
   * </p>
   * 
   * @return the single instance of this class
   */
  public static StoppedState getInstance() {
    if (instance == null) {
      instance = new StoppedState();
    }

    return instance;
  }

  /**
   * <p>
   * Creates a new instance.
   * </p>
   */
  private StoppedState() {
    // na
  }
}
