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


import com.adaptris.validation.constraints.ConfigDeprecated;

/**
 * <p>
 * Implementations of this interface return a <code>String</code> destination
 * (e.g. queue name, URL) to be used by <code>AdaptrisMessageProducer</code>.
 * </p>
 *
 */
@Deprecated(forRemoval = true)
@ConfigDeprecated(removalVersion = "4.0.0", groups = Deprecated.class)
@FunctionalInterface
public interface ProduceDestination {
  /**
   * <p>
   * Returns a <code>String</code> destination name. Implementations may or may not use the <code>AdaptrisMessage</code> to
   * dynamically generate the name.
   * </p>
   *
   * @param msg the <code>AdaptrisMessage</code> for which the name is being generated
   * @return the destination name to use
   * @throws CoreException wrapping any underlying <code>Exception</code>
   */
  String getDestination(AdaptrisMessage msg) throws CoreException;
}
