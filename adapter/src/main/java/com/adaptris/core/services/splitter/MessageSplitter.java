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

package com.adaptris.core.services.splitter;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreException;

/**
 * Interface for creating multiple messages from a single {@link AdaptrisMessage} instance.
 */
public interface MessageSplitter {

  /**
   * <p>
   * Splits an {@link AdaptrisMessage} into some number of AdaptrisMessage objects. Preservation of metadata is down to the
   * implementation.
   * </p>
   * <p>
   * If this method returns a {@link CloseableIterable}, it must be closed by the caller! This contract cannot be clearly 
   * expressed in Java code without breaking the API in uncomfortable ways for things just returning a List, which is why 
   * this method is only declared to return Iterable.
   * </p>
   *
   * @param msg the msg to split
   * @return an {@link Iterable} of {@link AdaptrisMessage}
   * @throws CoreException wrapping any other exception
   */
  Iterable<AdaptrisMessage> splitMessage(AdaptrisMessage msg) throws CoreException;

}
