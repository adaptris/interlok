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
 * New interface so that we can re-use producer style functionality without all the additional requirements surrounds
 * AdaptrisMessageWorker.
 *
 * @author lchan
 *
 */
public interface AdaptrisMessageSender extends ComponentLifecycle {
  /**
   * Produces the {@link com.adaptris.core.AdaptrisMessage} to the default destination.
   * 
   * @param msg the @link AdaptrisMessage} to produce
   * @throws ProduceException wrapping any underlying Exceptions
   */
  void produce(AdaptrisMessage msg) throws ProduceException;

  /**
   * Produce the {@link com.adaptris.core.AdaptrisMessage} to the supplied {@link ProduceDestination} , over-riding any configured destinations.
   *
   * @param msg the {@link com.adaptris.core.AdaptrisMessage} to produce
   * @param destination the {@link ProduceDestination} to produce to
   * @throws ProduceException wrapping any underlying Exceptions
   */
  void produce(AdaptrisMessage msg, ProduceDestination destination) throws ProduceException;

}
