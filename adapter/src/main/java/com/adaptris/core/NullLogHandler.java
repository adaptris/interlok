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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import com.adaptris.util.license.License;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Null Implemention of the <code>LogHandler</code>.
 * <p>
 * This implementation merely presents the fixed text <code>No
 * implementation of the Logging Handler configured</code> as the InputStream when <code>retrieveLog()</code> is invoked.
 * </p>
 * 
 * @config null-log-handler
 * @see FileLogHandler
 * @author lchan / $Author: hfraser $
 */
@XStreamAlias("null-log-handler")
public class NullLogHandler extends LogHandlerImp {

  private static final String DEFAULT_STRING =
    "No implementation of the Logging Handler configured";

  /**
   * @see com.adaptris.core.LogHandler#retrieveLog(LogFileType)
   */
  public InputStream retrieveLog(LogFileType type) throws IOException {
    return new ByteArrayInputStream(DEFAULT_STRING.getBytes("UTF-8"));
  }

  /**
   * @see com.adaptris.core.LogHandler#clean()
   */
  public void clean() throws IOException {
    return;
  }

  /**
   *
   * @see com.adaptris.core.LogHandler#isCompressed()
   */
  public boolean isCompressed() {
    return false;
  }

  @Override
  public boolean isEnabled(License license) throws CoreException {
    return true;
  }
}
