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

package com.adaptris.util;

import java.util.concurrent.TimeUnit;

import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Class that describes a time unit for use within the adapter.
 * 
 * @config time-interval
 * 
 * @author lchan
 * @see TimeUnit
 */
@XStreamAlias("time-interval")
public class TimeInterval {

  private TimeUnit unit;
  private Long interval;

  private static final long DEFAULT_INTERVAL = 10L;
  private static final TimeUnit DEFAULT_UNIT = TimeUnit.SECONDS;

  public TimeInterval() {
  }

  public TimeInterval(Long interval, String unit) {
    this(interval, parse(unit));
  }

  public TimeInterval(Long interval, TimeUnit unit) {
    setUnit(unit);
    setInterval(interval);
  }

  public long toMilliseconds() {
    TimeUnit unit = getUnit() != null ? getUnit() : DEFAULT_UNIT;
    return unit.toMillis(getInterval() != null ? getInterval().longValue() : DEFAULT_INTERVAL);
  }

  private static TimeUnit parse(String s) {
    TimeUnit result = TimeUnit.SECONDS;
    for (TimeUnit tu : TimeUnit.values()) {
      if (tu.name().equalsIgnoreCase(s)) {
        result = tu;
        break;
      }
    }
    return result;
  }

  @Override
  public String toString() {
    return TimeUnit.MILLISECONDS.toSeconds(toMilliseconds()) + " Seconds";
  }

  public TimeUnit getUnit() {
    return unit;
  }

  /**
   * Define the unit for this interval.
   * 
   * @param unit the unit, if not specified (or unknown) defaults to {@link TimeUnit#SECONDS}
   */
  public void setUnit(TimeUnit unit) {
    this.unit = unit;
  }

  public Long getInterval() {
    return interval;
  }

  /**
   * Define the actual interval for this interval.
   * 
   * @param interval the interval, if not specified defaults to 10.
   */
  public void setInterval(Long interval) {
    this.interval = interval;
  }
}
