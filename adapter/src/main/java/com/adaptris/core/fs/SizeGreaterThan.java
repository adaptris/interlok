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

package com.adaptris.core.fs;

import java.io.File;
import java.io.FileFilter;

/**
 * {@link FileFilter} accepts files based on whether the size of the file is greater than the specified value.
 *
 * @author lchan
 * @author $Author: lchan $
 */
public class SizeGreaterThan extends SizeBasedFileFilter {

  /**
   * Create the filefilter using the specified size.
   *
   * @param size the size of the file in bytes.
   */
  public SizeGreaterThan(String size) {
    super(size);
  }

  /**
   * Create the filefilter using the specified size.
   *
   * @param fileSize the size of the file in bytes.
   */
  public SizeGreaterThan(long fileSize) {
    super(fileSize);
  }

  /**
   * @see java.io.FileFilter#accept(java.io.File)
   */
  @Override
  public boolean accept(File pathname) {
    return pathname.length() > getFilesize();
  }
}
