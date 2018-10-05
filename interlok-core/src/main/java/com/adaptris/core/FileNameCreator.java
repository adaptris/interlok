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
 * Creates a file name for an <code>AdaptrisMessage</code>.  Implementations
 * may create this name dynamically based on message contents or metadata, or 
 * may allow the name to be configured, etc. 
 * </p>
 */
public interface FileNameCreator {

  /**
   * <p>
   * Returns a file name for the passed <code>AdaptrisMessage</code>.
   * </p>
   * @param msg the <code>AdaptrisMessage</code> to create a file name for
   * @return a file name for the message
   * @throws CoreException wrapping any <code>Exception</code> that may occur
   */
  String createName(AdaptrisMessage msg) throws CoreException;
}
