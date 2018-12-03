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


/** Text justification.
 *  @author $Author: lchan $
 */
public final class Justify {
  private Justify() {
  }

  /** Left Justify a string with spaces until len.
   *  <p>If the string in question is greater than len, then it is truncated
   *  so that it is exactly len chars long
   *  @param s The string to pad
   *  @param len the length to pad to
   *  @return the justified string
   */
  public static String left(String s, int len) {
    return trailing(s.trim(), len, ' ');
  }

  /** Pad a string with a trailing pad character until len.
   *  <p>If the string in question is greater than len, then it is truncated
   *  so that it is exactly len chars long
   *  @param s The string to pad
   *  @param len the length to pad to
   *  @param pad the pad character
   *  @return the justified string
   */
  public static String trailing(String s, int len, char pad) {

    String input = s == null ? "" : s;
    StringBuffer sb = new StringBuffer(input);
    sb.setLength(len);

    if (input.length() < len) {
      for (int i = input.length(); i < len; i++) {
        sb.setCharAt(i, pad);
      }
    }

    return sb.toString();
  }

  /** Right justify a string to a given length, padded with spaces.
   * @param input - the string to be justified
   * @param size  - the length to pad the string out to
   *  @return the justified string
   */
  public static String right(String input, int size) {
    return leading(input.trim(), size, ' ');
  }

  /** Pad a string with a leading pad character until len.
   *  <p>If the string in question is greater than len, then it is truncated
   *  so that it is exactly len chars long
   * @param s - the string to be justified
   * @param size  - the length to pad the string out to
   * @param pad   - the character to pad the string with
   *  @return the justified string
   */
  public static String leading(String s, int size, char pad) {

    StringBuffer sb = new StringBuffer();
    String input = s == null ? "" : s;

    if (input.length() < size) {
      for (int i = 0; i < size - input.length(); i++) {
        sb.append(pad);
      }

      sb.append(input);
    }
    else {
      sb.append(input);
      sb.setLength(size);
    }

    return sb.toString();
  }
}
