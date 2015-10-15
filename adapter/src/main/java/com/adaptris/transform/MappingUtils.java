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

package com.adaptris.transform;

import java.text.SimpleDateFormat;
import java.util.Date;

import com.adaptris.util.text.Justify;

public class MappingUtils {
  /**
   * This method can be called to pad a value out to a given length.
   * 
   * @deprecated use {@link #leftJust(String, int)}or
   *             {@link #rightJust(String, int)}
   * @param input - value to be padded out
   * @param size - size of field to be returned
   * @param pad - the character to be used to pad the input
   * @param justify - L or R i.e. left or right justify the result
   */
  public static String padValue(String input, int size, char pad, char justify) {
    switch (justify) {
    case 'l':
      return leftJust(input, size, pad);
    case 'L':
      return leftJust(input, size, pad);
    case 'r':
      return rightJust(input, size, pad);
    case 'R':
      return rightJust(input, size, pad);
    default:
      return input;
    }
  }

  /**
   * @see Justify#left(String,int)
   */
  public static String leftJust(String input, int size) {
    return Justify.left(input, size);
  }

  /**
   * @see Justify#trailing(String, int, char)
   */
  public static String leftJust(String input, int size, char pad) {
    return Justify.trailing(input, size, pad);
  }

  /**
   * @see Justify#right(String, int)
   */

  public static String rightJust(String input, int size) {
    return Justify.right(input, size);
  }

  /**
   * @see Justify#leading(String, int, char)
   */

  public static String rightJust(String input, int size, char pad) {
    return Justify.leading(input, size, pad);
  }

  /**
   * Get the current system date.
   * 
   * @return the date in yyyyMMdd format
   */
  public static String getSystemDate() {
    return getFormattedDate("yyyyMMdd");
  }

  /**
   * Get the current system time.
   * 
   * @return the time in HH:mm:ss format.
   */
  public static String getSystemTime() {
    return getFormattedDate("HH:mm:ss");
  }

  /**
   * Get the system date and time.
   * 
   * @return the date and time in "yyyyMMdd HH:mm"
   */
  public static String getSystemDateTime() {
    return getFormattedDate("yyyyMMdd HH:mm");
  }

  private static String getFormattedDate(String format) {
    SimpleDateFormat sdf = new SimpleDateFormat(format);
    return sdf.format(new Date());
  }

  /**
   * Get the min of two numbers.
   * <p>
   * This method returns the smaller of two string representations of numbers.
   * All non-numeric characters are removed during comparison but the original
   * string is returned.
   * 
   * Example: 123xxx01 and 23+42 would be compared as 12301 and 2342
   * </p>
   * 
   * @param one - the first value
   * @param two - the second value
   */

  public static String getMinValue(String one, String two) {
    if (makeLong(one) > makeLong(two)) {
      return two;
    }
    else {
      return one;
    }
  }

  /**
   * Get the max of two numbers.
   * <p>
   * This method returns the larger of two string representations of numbers.
   * All non-numeric characters are removed during comparison but the original
   * string is returned.
   * 
   * Example: 123xxx01 and 23+42 would be compared as 12301 and 2342
   * </p>
   * 
   * @param one - the first value
   * @param two - the second value
   */

  public static String getMaxValue(String one, String two) {
    if (makeLong(one) > makeLong(two)) {
      return one;
    }
    else {
      return two;
    }
  }

  /**
   * Get the min of two dates.
   * <p>
   * This method returns the smaller of two string representations of dates. The
   * dates must be in either of the following formats: yyyyMMdd (e.g. 20011225)
   * yyyyMMdd HH:mm (e.g. 20011225 12:01)
   * </p>
   * 
   * @param one - the first date
   * @param two - the second date
   */

  public static String getMinDate(String one, String two) {
    if (makeLong(one.length() < 14 ? one + " 00:00" : one) > makeLong(two
        .length() < 14 ? two + " 00:00" : two)) {
      return two;
    }
    else {
      return one;
    }
  }

  /**
   * Get the max of two dates.
   * <p>
   * This method returns the larger of two string representations of dates. The
   * dates must be in either of the following formats: yyyyMMdd (e.g. 20011225)
   * yyyyMMdd HH:mm (e.g. 20011225 12:01)
   * </p>
   * 
   * @param one - the first date
   * @param two - the second date
   */

  public static String getMaxDate(String one, String two) {
    if (makeLong(one.length() < 14 ? one + " 00:00" : one) > makeLong(two
        .length() < 14 ? two + " 00:00" : two)) {
      return one;
    }
    else {
      return two;
    }
  }

  private static long makeLong(String input) {
    StringBuffer sb = new StringBuffer(input);

    for (int i = 0; i < sb.length(); i++) {
      char x = sb.charAt(i);
      if (x < '0' || x > '9') {
        sb.deleteCharAt(i);
        i--;
      }
    }

    return Long.parseLong(sb.toString());
  }
}
