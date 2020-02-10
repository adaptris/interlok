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
 * Implementations of <code>AdaptrisMessageConsumer</code>
 * obtain data, convert it into an <code>AdaptrisMessage</code> and pass
 * the message to the registered <code>AdaptrisMessageListener</code>.
 * </p>
 */
public interface AdaptrisMessageConsumer extends AdaptrisMessageWorker {

  /**
   * <p>
   * Sets the <code>AdaptrisMessageListener</code> to use.
   * </p>
   * @param listener the <code>AdaptrisMessageListener</code> to use
   */
  void registerAdaptrisMessageListener(AdaptrisMessageListener listener);

  /**
   * <p>
   * Returns the <code>ConsumeDestination</code> to use.
   * </p>
   * @return this <code>Object</code>'s <code>ConsumeDestination</code>
   */
  ConsumeDestination getDestination();

  /**
   * <p>
   * Sets the <code>ConsumeDestination</code> to use.
   * </p>
   * @param destination the name of the destination
   */
  void setDestination(ConsumeDestination destination);

  /**
   * Return the specific metadata key that contains the consume location.
   * <p>
   * if specified; then the workflow will attempt to set {@value com.adaptris.core.CoreConstants#MESSAGE_CONSUME_LOCATION} as a
   * standard metadata key. If not explicitly overriden, then this metadata key is not available.
   * </p>
   * 
   * @implNote The default implementation returns null, indicating there is no way to standardise the consume location.
   * 
   * @return the metadata key;
   * @since 3.9.0
   */
  default String consumeLocationKey() {
    return null;
  }

}
