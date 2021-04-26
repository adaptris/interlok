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

public abstract class FtpHelper {
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
    int slashPos = lastSlash(s, windowsWorkaround);
    if (slashPos >= 0 && s.length() > slashPos) {
      result = s.substring(slashPos + 1);
    }
    return result;
  }

  public static String getDirectory(String s, boolean windows) {
    String result = s;
    int slashPos = lastSlash(s, windows);
    if (slashPos >= 0) {
      result = s.substring(0, slashPos);
    }
    return result;
  }

  private static int lastSlash(String s, boolean windows) {
    if (windows) {
      return s.lastIndexOf(BACK_SLASH);
    } else {
      return s.lastIndexOf(FORWARD_SLASH);
    }
  }

  public static String getFilename(String fullPath) {
    return getFilename(fullPath, false);
  }

  public static String getDirectory(String fullPath) {
    return getDirectory(fullPath, false);
  }

  public static String getParentDirectoryName(String fullPath, boolean windows) {
    String result = fullPath;
    int slash = lastSlash(result, windows);
    if (slash >= 0) {
      result = result.substring(0, slash); // this is now the full path to the parent
    }
    return getFilename(result, windows);
  }

  public static String getParentDirectoryName(String fullPath) {
    return getParentDirectoryName(fullPath, false);
  }
}
