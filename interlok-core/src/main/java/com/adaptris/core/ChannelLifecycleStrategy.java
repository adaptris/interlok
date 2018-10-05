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

import java.util.List;

/**
 * Strategy for handling channel lifecycle within a {@link ChannelList}.
 * 
 * @author lchan
 * @author $Author: lchan $
 */
public interface ChannelLifecycleStrategy {

  /**
   * Start a list of channels.
   * 
   * @param channelList a list of channels that have already been initialised.
   * @throws CoreException wrapping any underlying exception.
   */
  void start(List<Channel> channelList) throws CoreException;

  /**
   * Initialise a list of channels.
   * 
   * @param channels a list of channels that require initialising.
   * @throws CoreException wrapping any underlying exception.
   */
  void init(List<Channel> channels) throws CoreException;

  /**
   * Stop a list of channels.
   * 
   * @param channels a list of channels that have previously been started.
   */
  void stop(List<Channel> channels);

  /**
   * Close a list of channels.
   * 
   * @param channels a list of channels.
   */
  void close(List<Channel> channels);
}
