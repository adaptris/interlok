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

package com.adaptris.core.services;

import com.adaptris.core.AdaptrisMessage;

/**
 * Restart strategy for {@link StatelessServiceWrapper}.
 * 
 * @author amcgrath
 * 
 */
public interface RestartStrategy {

  /**
   * Mark the current message as processed.
   * 
   * @param msg the message currently being processed.
   */
  public void messageProcessed(AdaptrisMessage msg);
  
  /**
   * Whether or not a restart of the underlying service is required.
   * 
   * @return true if a restart is required.
   */
  public boolean requiresRestart();
  
}
