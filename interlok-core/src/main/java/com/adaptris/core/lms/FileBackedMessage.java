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

package com.adaptris.core.lms;

import java.io.File;
import java.io.IOException;

import com.adaptris.core.AdaptrisMessage;

/**
 * <p>
 * Represents a <i>message</i> in the framework which is backed by a file on the
 * filesystem.
 * </p>
 */
public interface FileBackedMessage extends AdaptrisMessage {


  /**
   * Initialise this AdaptrisMessage from an existing object.
   * 
   * @param sourceObject the source file to initialise from.
   * @throws IOException wrapping any access error.
   */
  void initialiseFrom(File sourceObject) throws IOException, RuntimeException;

  
  /**
   * Returns the current file that is the source of the message.
   * 
   * @return the current source file.
   */
  File currentSource();

}
