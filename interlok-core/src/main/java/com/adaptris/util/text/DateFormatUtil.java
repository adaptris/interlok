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

package com.adaptris.util.text;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.apache.commons.lang3.ObjectUtils;

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
 * @see DateFormat
 */
public final class DateFormatUtil {

  // These are all the date formats we know about.
  private static final String[] KNOWN_DATE_FORMATS =
  {
      "yyyy-MM-dd'T'HH:mm:ssZ", "yyyyMMdd HH:mm:ss zzz", "yyyyMMdd HH:mm:ss",
      "yyyyMMdd", "yyyy-MM-dd HH:mm:ss zzz", "yyyy-MM-dd HH:mm:ss",
      "yyyy-MM-dd", "dd.MM.yyyy HH:mm:ss zzz", "dd.MM.yyyy HH:mm:ss",
      "dd.MM.yyyy", "dd/MM/yyyy HH:mm:ss zzz", "dd/MM/yyyy HH:mm:ss",
      "dd/MM/yyyy HH:mm:ss", "dd/MM/yyyy",
  };

  private static final ThreadLocal<List<SimpleDateFormat>> FORMATTERS = ThreadLocal.withInitial(
      () -> Arrays.stream(KNOWN_DATE_FORMATS).map((p) -> strictFormatter(p)).collect(Collectors.toUnmodifiableList()));

  private static final ThreadLocal<SimpleDateFormat> TO_STRING_FORMATTER = ThreadLocal.withInitial(
      () -> strictFormatter(KNOWN_DATE_FORMATS[0]));

  /**
   * Custom date formats over and above {@link  java.text.SimpleDateFormat}.
   *
   */
  public static enum CustomDateFormat {
    SECONDS_SINCE_EPOCH, MILLISECONDS_SINCE_EPOCH
  };

  static enum CustomDateFormatter {

    /**
     * A formatter using {@link  java.text.SimpleDateFormat}.
     *
     */
    SIMPLE() {

      @Override
      Date toDate(String stringRep, String format) throws ParseException {
        return strictFormatter(format).parse(stringRep);
      }

      @Override
      String toString(Date date, String format) {
        return strictFormatter(format).format(date);
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
    return parse(s, new Date());
  }

  /**
   * Return a date object from a given string returning a default if it could not.
   *
   * @param s the date in string format
   * @param defaultDate the default Date (which could be null)
   * @return the date
   */
  public static Date parse(String s, Date defaultDate) {
    Date date = useDefaultFormatter(s);
    Iterator<SimpleDateFormat> itr = FORMATTERS.get().iterator();
    while (date == null && itr.hasNext()) {
      date = parse(s, itr.next());
    }
    return ObjectUtils.defaultIfNull(date, defaultDate);
  }


  /**
   * Return the default formatted String for a given date.
   *
   * @param d the date
   * @return the formatted string yyyy-MM-dd'T'HH:mm:ssZ
   */
  public static String format(Date d) {
    return TO_STRING_FORMATTER.get().format(d);
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

  private static Date useDefaultFormatter(String str) {
    return Optional.ofNullable(str).map((s) -> DateFormat.getDateTimeInstance().parse(s, new ParsePosition(0)))
        .orElse(null);
  }

  private static Date parse(String s, SimpleDateFormat format) {
    try {
      return format.parse(s);
    }
    catch (Exception e) {
    }
    return null;
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

  /** Return a {@code SimpleDateFormat} instance that is not lenient.
   *
   * @param pattern the pattern
   * @return a SimpleDateFormat.
   */
  public static SimpleDateFormat strictFormatter(String pattern) {
    SimpleDateFormat f = new SimpleDateFormat(pattern);
    f.setLenient(false);
    return f;
  }
}
