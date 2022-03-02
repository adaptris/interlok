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
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * A {@link FileFilter} that works based on the size of a file.
 *
 */
@Slf4j
abstract class SizeBasedFileFilter implements FileFilter {

  @Getter(AccessLevel.PROTECTED)
  private long filesize;
  private static final String UNITS = "BKMGTPEZY";
  private static final Pattern UNITS_PATTERN = Pattern.compile("[A-Za-z]");
  // That's right, No sir, we don't care about SI Units here.
  private static final int FACTOR = 1024;

  /**
   * Default constructor
   */
  private SizeBasedFileFilter() {
    filesize = 0;
  }

  /**
   * Create the filefilter using the specified size.
   *
   * @param size the size of the file in bytes or something like 1.1G
   */
  public SizeBasedFileFilter(String size) {
    this(humanReadbleToBytes(size));
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

  private static int indexOfUnitSpecifier(String s) {
    Matcher matcher = UNITS_PATTERN.matcher(s);
    return matcher.find() ? matcher.start() : -1;
  }

  private static long humanReadbleToBytes(String str) {
    int index = indexOfUnitSpecifier(str);
    if (index == -1) {
      return Long.parseLong(str);
    }
    double rawValue = Double.parseDouble(str.substring(0, index));
    int p = UNITS.indexOf(str.substring(index).toUpperCase().charAt(0));
    int power = (p == -1) ? 0 : p;
    long computedSize = Double.valueOf(rawValue * Math.pow(FACTOR, power)).longValue();
    log.trace("{} converts to {}bytes", str, computedSize);
    return computedSize;
  }
}
