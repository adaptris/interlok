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
