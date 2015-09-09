package com.adaptris.util.text;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Contains convenience method to format date strings into Date objects. <br>
 * The current date formats that this utility class knows about are (in order of
 * precedence).
 * <ul>
 * <li>The format understood by {@link DateFormat#getDateTimeInstance()}</li>
 * <li>yyyy-MM-dd'T'HH:mm:ssZ</li>
 * <li>yyyyMMdd HH:mm:ss zzz</li>
 * <li>yyyyMMdd HH:mm:ss</li>
 * <li>yyyyMMdd</li>
 * <li>yyyy-MM-dd HH:mm:ss zzz</li>
 * <li>yyyy-MM-dd HH:mm:ss</li>
 * <li>yyyy-MM-dd</li>
 * <li>dd.MM.yyyy HH:mm:ss zzz</li>
 * <li>dd.MM.yyyy HH:mm:ss</li>
 * <li>dd.MM.yyyy</li>
 * <li>dd/MM/yyyy HH:mm:ss zzz</li>
 * <li>dd/MM/yyyy HH:mm:ss</li>
 * <li>dd/MM/yyyy</li>
 * </ul>
 * <p>
 * When formatting a Date object into a String then yyyy-MM-dd'T'HH:mm:ssZ will
 * always be used.
 * </p>
 *
 * @author lchan / $Author: lchan $
 * @see DateFormat
 */
public final class DateFormatUtil {

  // These are all the date formats we know about.
  private static final String[] DATE_FORMATS =
  {
      "yyyy-MM-dd'T'HH:mm:ssZ", "yyyyMMdd HH:mm:ss zzz", "yyyyMMdd HH:mm:ss",
      "yyyyMMdd", "yyyy-MM-dd HH:mm:ss zzz", "yyyy-MM-dd HH:mm:ss",
      "yyyy-MM-dd", "dd.MM.yyyy HH:mm:ss zzz", "dd.MM.yyyy HH:mm:ss",
      "dd.MM.yyyy", "dd/MM/yyyy HH:mm:ss zzz", "dd/MM/yyyy HH:mm:ss",
      "dd/MM/yyyy HH:mm:ss", "dd/MM/yyyy",
  };

  /**
   * Custom date formats over and above {@link SimpleDateFormat}.
   * 
   */
  public static enum CustomDateFormat {
    SECONDS_SINCE_EPOCH, MILLISECONDS_SINCE_EPOCH
  };

  static enum CustomDateFormatter {

    /**
     * A formatter using {@link SimpleDateFormat}.
     * 
     */
    SIMPLE() {

      @Override
      Date toDate(String stringRep, String format) throws ParseException {
        return new SimpleDateFormat(format).parse(stringRep);
      }

      @Override
      String toString(Date date, String format) {
        return new SimpleDateFormat(format).format(date);
      }
    },
    /**
     * A formatter using the number of seconds since 00:00 Jan 1 1970 GMT.
     * 
     */
    SECONDS_SINCE_EPOCH() {

      @Override
      Date toDate(String stringRep, String format) {
        return new Date(new BigDecimal(stringRep).longValue() * 1000);
      }

      @Override
      String toString(Date date, String format) {
        return String.valueOf(new BigDecimal(date.getTime()).divide(new BigDecimal(1000), RoundingMode.HALF_UP).longValue());
      }

    },
    /**
     * A formatter using the number of milliseconds since 00:00 Jan 1 1970 GMT.
     * 
     */
    MILLISECONDS_SINCE_EPOCH() {

      @Override
      Date toDate(String stringRep, String format) {
        return new Date(new BigDecimal(stringRep).longValue());
      }

      @Override
      String toString(Date date, String format) {
        return String.valueOf(date.getTime());
      }

    };

    abstract Date toDate(String stringRep, String format) throws ParseException;

    abstract String toString(Date date, String format);

  }

  private DateFormatUtil() {
  }

  /**
   * Return a date object from a given string. If the date could not be parsed,
   * the current time is used.
   *
   * @param s the date in string format
   * @return the date
   */
  public static Date parse(String s) {
    if (s == null) {
      return new Date();
    }
    Date date = useDefaultFormatter(s);
    int i = 0;
    while (date == null && i < DATE_FORMATS.length) {
      date = getDate(s, DATE_FORMATS[i++]);
    }
    return date == null ? new Date() : date;
  }

  /**
   * Return the default formatted String for a given date.
   *
   * @param d the date
   * @return the formatted string yyyy-MM-dd'T'HH:mm:ssZ
   */
  public static String format(Date d) {
    String format = DATE_FORMATS[0];
    SimpleDateFormat sdf = new SimpleDateFormat(format);
    return sdf.format(d);
  }

  /**
   * Convert a date to a String.
   * 
   * @param d the date to format.
   * @param format the format which might be one of {@link CustomDateFormat}
   * @return a string matching the format.
   */
  public static String toString(Date d, String format) {
    return getFormatter(format).toString(d, format);
  }

  /**
   * Convert a string into a date.
   * 
   * @param stringRep the string to convert.
   * @param format the format of the date, which might be one of {@link CustomDateFormat}
   * @return the {@link java.util.Date}
   * @throws ParseException if the format was invalid.
   */
  public static Date toDate(String stringRep, String format) throws ParseException {
    return getFormatter(format).toDate(stringRep, format);
  }


  private static Date useDefaultFormatter(String s) {

    DateFormat df = DateFormat.getDateTimeInstance();
    return df.parse(s, new ParsePosition(0));
  }

  private static Date getDate(String s, String format) {
    Date d = null;

    SimpleDateFormat sdf = new SimpleDateFormat(format);
    try {
      d = sdf.parse(s);
    }
    catch (ParseException e) {
      d = null;
    }
    return d;
  }

  private static CustomDateFormatter getFormatter(String pattern) {
    CustomDateFormatter format = CustomDateFormatter.SIMPLE;
    try {
      format = CustomDateFormatter.valueOf(CustomDateFormat.valueOf(pattern).name());
    }
    catch (IllegalArgumentException e) {

    }
    return format;
  }

}
