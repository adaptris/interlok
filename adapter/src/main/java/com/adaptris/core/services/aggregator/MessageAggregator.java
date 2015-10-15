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

package com.adaptris.core.services.aggregator;

import java.util.Collection;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreException;
import com.adaptris.core.services.splitter.SplitJoinService;

/**
 * Interface for creating a single {@link AdaptrisMessage} instance from multiple Messages.
 * 
 * @see SplitJoinService
 */
public interface MessageAggregator {

  /**
   * <p>
   * Joins multiple {@link AdaptrisMessage}s into a single AdaptrisMessage objects. Preservation of metadata is down to the
   * implementation.
   * </p>
   * 
   * @param msg the msg to insert all the messages into
   * @param msgs the list of messages to join.
   * @throws CoreException wrapping any other exception
   */
  void joinMessage(AdaptrisMessage msg, Collection<AdaptrisMessage> msgs) throws CoreException;

}
