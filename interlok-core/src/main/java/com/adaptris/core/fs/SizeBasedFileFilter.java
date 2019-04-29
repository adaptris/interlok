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

import java.io.FileFilter;

/**
 * A {@link FileFilter} that works based on the size of a file.
 * 
 * @author lchan
 * @author $Author: lchan $
 */
abstract class SizeBasedFileFilter implements FileFilter {

  private long filesize;

  /**
   * Default constructor
   */
  private SizeBasedFileFilter() {
    filesize = 0;
  }

  /**
   * Create the filefilter using the specified size.
   *
   * @param size the size of the file in bytes.
   */
  public SizeBasedFileFilter(String size) {
    this(Long.parseLong(size));
  }

  /**
   * Create the filefilter using the specified size.
   *
   * @param size the size of the file in bytes.
   */
  public SizeBasedFileFilter(long size) {
    this();
    filesize = size;
  }

  protected long getFilesize() {
    return filesize;
  }
}
