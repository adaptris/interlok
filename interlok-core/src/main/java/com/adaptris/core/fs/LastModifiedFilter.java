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
import java.util.Date;

import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.Duration;

import org.apache.commons.lang.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract {@link FileFilter} that accepts files based on the last modified time of the file.
 */
public abstract class LastModifiedFilter implements FileFilter {
  protected transient Logger logR = LoggerFactory.getLogger(this.getClass().getName());

  private String when;

  /**
   * Default constructor
   */
  private LastModifiedFilter() {
    when = "-P30D";
  }

  /**
   * Create the filefilter using an ISO8601 formatted interval.
   *
   * @param s the iso8601 interval or the absolute time (MS since epoch)
   */
  public LastModifiedFilter(String s) {
    this();
    when = s;
  }

  protected Date filterDate() throws Exception {
    Date filterDate = new Date();
    if (NumberUtils.isDigits(when)) {
      filterDate.setTime(Long.parseLong(when));
    }
    else {
      Duration duration = DatatypeFactory.newInstance().newDuration(when);
      duration.addTo(filterDate);
    }
    return filterDate;
  }
}
