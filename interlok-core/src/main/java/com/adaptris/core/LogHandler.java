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

import java.io.IOException;
import com.adaptris.annotation.Removal;

/**
 * Defines methods for handling <code>Adapter </code> log files.
 *
 */
@Deprecated
@Removal(version = "4.0.0", message = "Defunct and should not be used.")
public interface LogHandler extends AdaptrisComponent {

  /**
   * Clean up any logfiles.
   *
   * @throws IOException if there was an error.
   */
  void clean() throws IOException;

}
