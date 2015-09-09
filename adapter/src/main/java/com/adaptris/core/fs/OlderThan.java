package com.adaptris.core.fs;

import java.io.File;
import java.io.FileFilter;
import java.util.Date;

import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.Duration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link FileFilter} accepts files based on the last modified time of the file..
 * <p>
 * Files are accepted based on whether or not the last modified time is older than the specified interval. This interval follows the
 * ISO8601 convention for durations.
 * </p>
 * <p>
 * The interval is represented by the format <strong>[+-]P[n]Y[n]M[n]DT[n]H[n]M[n]S</strong>. In these representations, the [n] is
 * replaced by the value for each of the date and time elements that follow the [n]. Leading zeros are not required. The capital
 * letters 'P', 'Y', 'M', 'W', 'D', 'T', 'H', 'M', and 'S' are designators for each of the date and time elements and are not
 * replaced.
 * </p>
 * <p>
 * <ul>
 * <li>P is the duration designator (historically called "period") placed at the start of the duration representation.</li>
 * <li>Y is the year designator that follows the value for the number of years.</li>
 * <li>M is the month designator that follows the value for the number of months.</li>
 * <li>W is the week designator that follows the value for the number of weeks.</li>
 * <li>D is the day designator that follows the value for the number of days.</li>
 * <li>T is the time designator that precedes the time components of the representation.</li>
 * <li>H is the hour designator that follows the value for the number of hours.</li>
 * <li>M is the minute designator that follows the value for the number of minutes.</li>
 * <li>S is the second designator that follows the value for the number of seconds.
 * </p>
 * <p>
 * For example, <code>P3Y6M4DT12H30M5S</code> represents a duration of three years, six months, four days, twelve hours, thirty
 * minutes, and five seconds . Date and time elements including their designator may be omitted if their value is zero, and lower
 * order elements may also be omitted for reduced precision. For example, "P23DT23H" and "P4Y" are both acceptable duration
 * representations.
 * </p>
 * <p>
 * To resolve ambiguity, "P1M" is a one-month duration and "PT1M" is a one-minute duration (note the time designator, T, that
 * precedes the time value). The seconds value used may also have a decimal fraction, as in "PT0.5S" to indicate half a
 * second."PT36H" could be used as well as "P1DT12H" for representing the same duration.
 * </p>
 * <p>
 * A negative period will become some date in the past; a positive or unspecified period will resolve to some time in the future.
 * For example <code>-P30D</code> will resolve to 30 days ago, whereas <code>P30D</code> will resolve to 30 days in the future.
 * </p>
 *
 * @see Duration
 * @author lchan
 * @author $Author: lchan $
 */
public class OlderThan implements FileFilter {
  private transient Logger logR = LoggerFactory.getLogger(this.getClass().getName());

  private String iso8601duration;
  private boolean additionalDebug;

  /** Default constructor
   */
  protected OlderThan() {
    iso8601duration = "-P30D";
  }

  /**
   * Create the filefilter using an ISO8601 formatted interval.
   *
   * @param iso8601 the iso8601 interval.
   */
  public OlderThan(String iso8601) {
    this();
    iso8601duration = iso8601;
  }


  // For unit testing.
  void setAdditionalDebug(boolean b) {
    additionalDebug = b;
  }

  /**
   * @see java.io.FileFilter#accept(java.io.File)
   */
  @Override
  public boolean accept(File pathname) {
    Date filterDate = new Date();
    Date fileDate = new Date(pathname.lastModified());
    try {
      Duration duration = DatatypeFactory.newInstance().newDuration(
          iso8601duration);
      duration.addTo(filterDate);
      if (additionalDebug) {
        logR.trace("Accepting files older than " + filterDate);
      }
    }
    catch (Exception e) {
      return false;
    }

    return fileDate.before(filterDate);
  }
}
