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
   * Produce the {@link com.adaptris.core.AdaptrisMessage} to the configured destination and blocks indefinitely for a reply.
   *
   * @param msg the {@link com.adaptris.core.AdaptrisMessage} to produce
   * @return a reply {@link com.adaptris.core.AdaptrisMessage} which is generally the same as supplied parameter
   * @throws ProduceException wrapping any underlying Exceptions
   */
  AdaptrisMessage request(AdaptrisMessage msg) throws ProduceException;

  /**
   * Produces the {@link com.adaptris.core.AdaptrisMessage} and blocks for the specified timeout for
   * a reply.
   *
   * @param msg the {@link com.adaptris.core.AdaptrisMessage} to produce
   * @param timeout the time to wait for a reply in milliseconds
   * @return a reply {@link com.adaptris.core.AdaptrisMessage} which is generally the same as
   *         supplied parameter
   * @throws ProduceException wrapping any underlying Exceptions
   */
  AdaptrisMessage request(AdaptrisMessage msg, long timeout) throws ProduceException;

}
