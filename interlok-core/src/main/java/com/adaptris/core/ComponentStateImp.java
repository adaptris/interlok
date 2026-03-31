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

import static com.adaptris.core.util.LoggingHelper.friendlyName;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>
 * Default implementation of <code>ComponentState</code>. Each default method
 * implementation does nothing other than request the same operation on any
 * <code>StateManagedComponent</code>s which are sub-components of
 * <code>comp</code>. This is required if for example an Adapter is started and
 * one of its Channels is stopped. <code>requestStart</code> has nothing to do
 * at the Adapter level but must still be propogated to the stopped Channel
 * which does require manipulation.
 * </p>
 */
public abstract class ComponentStateImp implements ComponentState {
  private static final long serialVersionUID = 2012022101L;
  protected transient Logger log = LoggerFactory.getLogger(this.getClass().getName());

  /**
   * @see ComponentState#requestInit (com.adaptris.core.StateManagedComponent)
   */
  @Override
  public void requestInit(StateManagedComponent comp) throws CoreException {
    if (comp instanceof StateManagedComponentContainer) {
      ((StateManagedComponentContainer) comp).requestChildInit();
    }
  }

  /**
   * @see ComponentState#requestStart (com.adaptris.core.StateManagedComponent)
   */
  @Override
  public void requestStart(StateManagedComponent comp) throws CoreException {
    if (comp instanceof StateManagedComponentContainer) {
      ((StateManagedComponentContainer) comp).requestChildStart();
    }
  }

  /**
   * @see ComponentState#requestStop (com.adaptris.core.StateManagedComponent)
   */
  @Override
  public void requestStop(StateManagedComponent comp) {
    if (comp instanceof StateManagedComponentContainer) {
      ((StateManagedComponentContainer) comp).requestChildStop();
    }
  }

  /**
   * @see ComponentState#requestClose (com.adaptris.core.StateManagedComponent)
   */
  @Override
  public void requestClose(StateManagedComponent comp) {
    if (comp instanceof StateManagedComponentContainer) {
      ((StateManagedComponentContainer) comp).requestChildClose();
    }
  }

  /** @see java.lang.Object#toString() */
  @Override
  public String toString() {
    return this.getClass().getSimpleName();
  }

  /**
   * @see ComponentState#requestRestart(com.adaptris.core.StateManagedComponent)
   */
  @Override
  public void requestRestart(StateManagedComponent comp) throws CoreException {
    synchronized (comp) {
      requestClose(comp);
      comp.retrieveComponentState().requestStart(comp);
    }
  }

  void logActivity(String action, StateManagedComponent comp) {
    log.trace("{} [{}]", action, friendlyName(comp));
  }

  boolean continueRequest(StateManagedComponent comp, ComponentState state) {
    boolean result = false;
    if (comp.retrieveComponentState().equals(state)) {
      result = true;
    }
    else {
      log.trace("State Change detected; [{}] now [{}], not [{}]", friendlyName(comp), comp.retrieveComponentState(), state);
    }
    return result;
  }
}
