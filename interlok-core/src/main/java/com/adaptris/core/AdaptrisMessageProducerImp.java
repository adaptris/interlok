/*
 * Copyright 2015 Adaptris Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package com.adaptris.core;

import lombok.NoArgsConstructor;

/**
 * <p>
 * Implementation of behaviour common to <code>AdaptrisMessageProducer</code>s.
 * </p>
 */
@NoArgsConstructor
public abstract class AdaptrisMessageProducerImp extends AdaptrisMessageWorkerImp
    implements AdaptrisMessageProducer {

  @Override
  public String createName() {
    return this.getClass().getName();
  }

  /**
   * Return the endpoint that will be derived from the message.
   *
   * <p>
   * The purpose of this method is to mitigate the number of changes that need to happen because
   * {@code ProduceDestination} is being deprecated; boilerplate code can still be present in the
   * parent super-classes w/o impacting concrete sub-classes "too much".
   * </p>
   *
   * @see AdaptrisMessageProducer#request(AdaptrisMessage, ProduceDestination)
   * @see AdaptrisMessageProducer#request(AdaptrisMessage, ProduceDestination, long)
   * @see AdaptrisMessageSender#produce(AdaptrisMessage)
   * @see AdaptrisMessageSender#produce(AdaptrisMessage, ProduceDestination)
   */
  public abstract String endpoint(AdaptrisMessage msg) throws ProduceException;

}
