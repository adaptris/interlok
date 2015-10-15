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


/**
 * <p>
 * Implementation of behaviour common to <code>AdaptrisMessageConsumer</code>s.
 * </p>
 */
public abstract class AdaptrisMessageConsumerImp extends
    AdaptrisMessageWorkerImp implements AdaptrisMessageConsumer {

  private transient AdaptrisMessageListener listener;
  @Valid
  private ConsumeDestination destination;

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
    if (dest == null) {
      throw new IllegalArgumentException();
    }
    destination = dest;
  }

  /**
   * Rename the thread to match the {@link ConsumeDestination#getDeliveryThreadName()}.
   *
   * @return the old thread name if you want it.
   */
  protected String renameThread() {
    String oldName = Thread.currentThread().getName();
    StringBuffer newName = new StringBuffer();
    newName.append(getDestination().getDeliveryThreadName());
    Thread.currentThread().setName(newName.toString());
    return oldName;
  }
}
