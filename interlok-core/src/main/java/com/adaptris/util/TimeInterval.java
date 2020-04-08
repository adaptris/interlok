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

import java.util.Arrays;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import org.apache.commons.lang3.ObjectUtils;
import com.adaptris.annotation.DisplayOrder;
import com.adaptris.annotation.InputFieldDefault;
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
@DisplayOrder(order = {"interval", "unit"})
public class TimeInterval {
  @InputFieldDefault(value = "SECONDS")
  private TimeUnit unit;
  @InputFieldDefault(value = "10")
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
    return ObjectUtils.defaultIfNull(getUnit(), DEFAULT_UNIT)
        .toMillis(NumberUtils.toLongDefaultIfNull(getInterval(), DEFAULT_INTERVAL));
  }

  private static TimeUnit parse(String s) {
    Optional<TimeUnit> parsed = Arrays.stream(TimeUnit.values()).filter((t) -> t.name().equalsIgnoreCase(s)).findFirst();
    return parsed.orElse(TimeUnit.SECONDS);
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

  public static long toMillisecondsDefaultIfNull(TimeInterval t, long defaultMs) {
    return t != null ? t.toMilliseconds() : defaultMs;
  }

  public static long toMillisecondsDefaultIfNull(TimeInterval t, TimeInterval defaultInterval) {
    return toMillisecondsDefaultIfNull(t, defaultInterval.toMilliseconds());
  }

  public static long toSecondsDefaultIfNull(TimeInterval t, TimeInterval defaultInterval) {
    return TimeUnit.MILLISECONDS
        .toSeconds(TimeInterval.toMillisecondsDefaultIfNull(t, defaultInterval));
  }

  public static long toSecondsDefaultIfNull(TimeInterval t, long defaultMs) {
    return TimeUnit.MILLISECONDS
        .toSeconds(TimeInterval.toMillisecondsDefaultIfNull(t, defaultMs));
  }
}
