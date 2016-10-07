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
package com.adaptris.core.ftp;

public class FtpHelper {
  public static final String FORWARD_SLASH = "/";
  public static final String BACK_SLASH = "\\";

  /**
   * Get the filename part from an NLST command.
   * 
   * @param s the result of a NLST.
   * @param windowsWorkaround {@link FileTransferConnection#setWindowsWorkAround(Boolean)}.
   * @return the filename.
   */
  public static String getFilename(String s, boolean windowsWorkaround) {
    String result = s;
    int slashPos = -1;
    if (windowsWorkaround) {
      slashPos = s.lastIndexOf(BACK_SLASH);
    }
    else {
      slashPos = s.lastIndexOf(FORWARD_SLASH);
    }
    if (slashPos >= 0 && s.length() > slashPos) {
      result = s.substring(slashPos + 1);
    }
    return result;
  }

  public static String getFilename(String fullPath) {
    return getFilename(fullPath, false);
  }

}
