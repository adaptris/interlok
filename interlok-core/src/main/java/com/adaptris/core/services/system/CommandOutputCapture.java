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

package com.adaptris.core.services.system;

import java.io.IOException;
import java.io.OutputStream;

import com.adaptris.core.AdaptrisMessage;

/**
 * Interface for capturing output from a process.
 * 
 * @author lchan
 * 
 */
public interface CommandOutputCapture {

  /**
   * Capture the output from the process.
   * 
   * @param msg the adaptris message.
   * @return an OutputStream capturing the command output.
   * @throws IOException if an outputstream couldn't be created.
   */
  OutputStream startCapture(AdaptrisMessage msg) throws IOException;
}
