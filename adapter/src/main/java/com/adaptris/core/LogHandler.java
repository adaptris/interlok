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
import java.io.InputStream;

/**
 * Defines methods for handling <code>Adapter </code> log files.
 *
 * @author lchan / $Author: hfraser $
 */
public interface LogHandler extends AdaptrisComponent {

  /**
   * Standard types of logfile.
   *
   */
  public static enum LogFileType {
    /** Standard log file */
    Standard,
    /** Statistics */
    Statistics,
    /** Google graph API log file. */
    Graphing
  }

  /**
   * Retrieve the log file and present it as an InputStream.
   * 
   * @return the log file.
   * @param type the Logfile type
   * @throws IOException if the input stream could not be returned.
   * @deprecated since 3.0.6; there is no replacement.
   */
  @Deprecated
  InputStream retrieveLog(LogFileType type) throws IOException;

  /**
   * Clean up any logfiles.
   *
   * @throws IOException if there was an error.
   */
  void clean() throws IOException;

  /**
   * Get the compression status for the log file.
   *
   * @return the compression flag.
   * @deprecated since 3.0.6; there is no replacement.
   */
  @Deprecated
  boolean isCompressed();
}
