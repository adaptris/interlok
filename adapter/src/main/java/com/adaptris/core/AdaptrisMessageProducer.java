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
 * Implementations of <code>AdaptrisMessageProducer</code> produce messages to a destination (such as a JMS Queue or file system
 * directory).
 * </p>
 *
 * @see AdaptrisMessageSender
 */
public interface AdaptrisMessageProducer extends AdaptrisMessageWorker, AdaptrisMessageSender {

  /**
   * Produce the {@link AdaptrisMessage} to the configured destination and blocks indefinitely for a reply.
   *
   * @param msg the {@link AdaptrisMessage} to produce
   * @return a reply {@link AdaptrisMessage} which is generally the same as supplied parameter
   * @throws ProduceException wrapping any underlying Exceptions
   */
  AdaptrisMessage request(AdaptrisMessage msg) throws ProduceException;

  /**
   * Produces the {@link AdaptrisMessage} to the passed supplied {@link ProduceDestination} and blocks indefinitely for a reply.
   *
   * @param msg the {@link AdaptrisMessage} to produce
   * @param destination the {@link ProduceDestination} to produce to
   * @return a reply {@link AdaptrisMessage} which is generally the same as supplied parameter
   * @throws ProduceException wrapping any underlying Exceptions
   */
  AdaptrisMessage request(AdaptrisMessage msg, ProduceDestination destination) throws ProduceException;

  /**
   * Produces the {@link AdaptrisMessage} to the passed supplied {@link ProduceDestination} and blocks for the specified timeout for
   * a reply.
   *
   *
   * @param msg the {@link AdaptrisMessage} to produce
   * @param destination the {@link ProduceDestination} to produce to
   * @param timeoutMs the time to wait for a reply in milliseconds
   * @return a reply {@link AdaptrisMessage} which is generally the same as supplied parameter
   * @throws ProduceException wrapping any underlying Exceptions
   */
  AdaptrisMessage request(AdaptrisMessage msg, ProduceDestination destination, long timeoutMs) throws ProduceException;

  /**
   * Produces the {@link AdaptrisMessage} to the configured {@link ProduceDestination} and blocks for the specified timeout for a
   * reply.
   * 
   * @param msg the {@link AdaptrisMessage} to produce
   * @param timeout the time to wait for a reply in milliseconds
   * @return a reply {@link AdaptrisMessage} which is generally the same as supplied parameter
   * @throws ProduceException wrapping any underlying Exceptions
   */
  AdaptrisMessage request(AdaptrisMessage msg, long timeout) throws ProduceException;

  /**
   * Returns the default {@link ProduceDestination} for this instance.
   *
   * @return the {@link ProduceDestination} used in the absence of any other information.
   */
  ProduceDestination getDestination();

  /**
   * Set the default {@link ProduceDestination}.
   *
   * @param destination the {@link ProduceDestination} to use
   */
  void setDestination(ProduceDestination destination);
}
