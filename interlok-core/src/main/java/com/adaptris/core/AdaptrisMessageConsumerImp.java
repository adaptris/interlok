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

import javax.validation.Valid;

import org.apache.commons.lang.StringUtils;

import com.adaptris.core.util.Args;

/**
 * <p>
 * Implementation of behaviour common to <code>AdaptrisMessageConsumer</code>s.
 * </p>
 */
public abstract class AdaptrisMessageConsumerImp extends
    AdaptrisMessageWorkerImp implements AdaptrisMessageConsumer, StateManagedComponent {

  private transient ComponentState consumerState;
  
  private transient AdaptrisMessageListener listener;
  @Valid
  private ConsumeDestination destination;

  
  public AdaptrisMessageConsumerImp() {
    changeState(ClosedState.getInstance());
  }
  /**
   * <p>
   * Sets the <code>AdaptrisMessageListener</code> to use.
   * </p>
   *
   * @param l the <code>AdaptrisMessageListener</code> to use
   */
  @Override
  public void registerAdaptrisMessageListener(AdaptrisMessageListener l) {
    listener = l;
  }

  /**
   * <p>
   * Returns the <code>AdaptrisMessageListener</code> to use.
   * </p>
   *
   * @return the <code>AdaptrisMessageListener</code> to use
   */
  public AdaptrisMessageListener retrieveAdaptrisMessageListener() {
    return listener;
  }

  /**
   * <p>
   * Returns the <code>ConsumeDestination</code> to use.
   * </p>
   *
   * @return the <code>ConsumeDestination</code> to use
   */
  @Override
  public ConsumeDestination getDestination() {
    return destination;
  }

  /**
   * <p>
   * Sets the <code>ConsumeDestination</code> to use.
   * </p>
   *
   * @param dest the <code>ConsumeDestination</code> to use
   */
  @Override
  public void setDestination(ConsumeDestination dest) {
    destination = Args.notNull(dest, "destination");
  }

  /**
   * Rename the thread to match the {@link ConsumeDestination#getDeliveryThreadName()}.
   *
   * @return the old thread name if you want it.
   */
  protected String renameThread() {
    String oldName = Thread.currentThread().getName();
    String newName = getDestination().getDeliveryThreadName();
    if (StringUtils.isEmpty(newName)) {
      newName = retrieveAdaptrisMessageListener().friendlyName();
    }
    Thread.currentThread().setName(newName);
    return oldName;
  }
  
  public void changeState(ComponentState newState) {
    consumerState = newState;
  }

  public ComponentState retrieveComponentState() {
    return consumerState;
  }

  public void requestInit() throws CoreException {
    checkStateExists();
    consumerState.requestInit(this);
  }

  public void requestStart() throws CoreException {
    checkStateExists();
    consumerState.requestStart(this);
  }

  public void requestStop() {
    checkStateExists();
    consumerState.requestStop(this);
  }

  public void requestClose() {
    checkStateExists();
    consumerState.requestClose(this);
  }
  
  private void checkStateExists() {
    if(this.consumerState == null)
      this.changeState(ClosedState.getInstance());
  }
}
