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

import java.io.IOException;


/**
 * Simple Data Conversion methods.
 * <p>
 * Only small amounts of data should be converted using this class, to write a large number of bytes as a Base64 String, an
 * alternative method such as IAIK's Base64OutputStream should be used, or javax.mail.internet.MimeUtility.
 * </p>
 * 
 */
public final class Conversion {

  private static String[] hexDigits =
    {
      "0",
      "1",
      "2",
      "3",
      "4",
      "5",
      "6",
      "7",
      "8",
      "9",
      "a",
      "b",
      "c",
      "d",
      "e",
      "f" };

  private Conversion() {
  }

  /** Convert a byte to a hexadecimal String.
   *  @param b a single byte
   *  @return the hex String
   */
  public static String byteToHexString(byte b) {

    int n = b;

    if (n < 0) {
      n = 256 + n;
    }

    int d1 = n / 16;
    int d2 = n % 16;

    return hexDigits[d1] + hexDigits[d2];
  }

  /** Convert a byte array to a hexadecimal String.
   *  @param b the bytes
   *  @return the hex String
   */
  public static String byteArrayToHexString(byte[] b) {

    String result = "";

    for (int i = 0; i < b.length; ++i) {
      result += byteToHexString(b[i]);
    }

    return result;
  }

  /**
   * Convert a byte array to a base 64 string (see RFC 1421).
   * 
   * @param b the bytes
   * @param len the length
   * @return the hex String
   * @deprecated since 3.10, use {@link java.util.Base64} instead.
   */
  @Deprecated
  public static String byteArrayToBase64String(byte[] b, int len) {

    String s = "";

    // Organize into three byte groups and convert
    int n = len / 3;
    int m = len % 3;

    for (int i = 0; i < n; ++i) {

      int j = i * 3;
      s += toBase64(b[j], b[j + 1], b[j + 2]);
    }

    if (m == 1) {
      s += toBase64(b[len - 1]);
    }
    else if (m == 2) {
      s += toBase64(b[len - 2], b[len - 1]);
    }

    // Insert a new line every 64 characters
    String result = "";
    len = s.length();
    n = len / 64;
    m = len % 64;

    for (int i = 0; i < n; ++i) {
      result += s.substring(i * 64, (i + 1) * 64);
    }

    if (m > 0) {
      result += s.substring(n * 64, len);
    }

    return result;
  }

  /**
   * Convert a byte array to a base 64 string (see RFC 1421).
   * 
   * @param b the bytes
   * @return the String
   * @deprecated since 3.10, use {@link java.util.Base64} instead.
   */
  @Deprecated
  public static String byteArrayToBase64String(byte[] b) {
    return byteArrayToBase64String(b, b.length);
  }

  // Perform the base64 transformation
  @Deprecated
  private static String toBase64(byte b1, byte b2, byte b3) {

    int[] digit = new int[4];
    digit[0] = (b1 & 0xFC) >>> 2;
    digit[1] = (b1 & 0x03) << 4;
    digit[1] |= (b2 & 0xF0) >> 4;
    digit[2] = (b2 & 0x0F) << 2;
    digit[2] |= (b3 & 0xC0) >> 6;
    digit[3] = b3 & 0x3F;

    String result = "";

    for (int i = 0; i < digit.length; ++i) {
      result += base64Digit(digit[i]);
    }

    return result;
  }

  /** Perform a padded base64 transformation. */
  @Deprecated
  private static String toBase64(byte b1, byte b2) {

    int[] digit = new int[3];
    digit[0] = (b1 & 0xFC) >>> 2;
    digit[1] = (b1 & 0x03) << 4;
    digit[1] |= (b2 & 0xF0) >> 4;
    digit[2] = (b2 & 0x0F) << 2;

    String result = "";

    for (int i = 0; i < digit.length; ++i) {
      result += base64Digit(digit[i]);
    }

    result += "=";

    return result;
  }

  /** Perform a padded base64 transformation */
  @Deprecated
  private static String toBase64(byte b1) {

    int[] digit = new int[2];
    digit[0] = (b1 & 0xFC) >>> 2;
    digit[1] = (b1 & 0x03) << 4;

    String result = "";

    for (int i = 0; i < digit.length; ++i) {
      result += base64Digit(digit[i]);
    }

    result += "==";

    return result;
  }

  @Deprecated
  private static char base64Digit(int i) {
    if (i < 26) {
      return (char) ('A' + i);
    }

    if (i < 52) {
      return (char) ('a' + (i - 26));
    }

    if (i < 62) {
      return (char) ('0' + (i - 52));
    }

    if (i == 62) {
      return '+';
    }
    else {
      return '/';
    }
  }

  /**
   * Convert a base 64 string to a byte array (see RFC 1421).
   * 
   * @param s the string
   * @return the byte array
   * @throws NumberFormatException if the stirng is invalid base64
   * @deprecated since 3.10, use {@link java.util.Base64} instead.
   */
  @Deprecated
  public static byte[] base64StringToByteArray(String s)
    throws NumberFormatException {

    String t = "";

    for (int i = 0; i < s.length(); ++i) {

      char c = s.charAt(i);

      if (c == '\n') {
        continue;
      }
      else if (
        c >= 'A' && c <= 'Z'
          || c >= 'a' && c <= 'z'
          || c >= '0' && c <= '9'
          || c == '+'
          || c == '/') {
        t += c;
      }
      else if (c == '=') {
        break;
      }
      else {
        throw new NumberFormatException();
      }
    }

    int len = t.length();
    int n = 3 * (len / 4);

    switch (len % 4) {
      case 1 :
        throw new NumberFormatException();
      case 2 :
        len += 2;
        n += 1;
        t += "==";

        break;
      case 3 :
        ++len;
        n += 2;
        t += "=";

        break;
      default :
        break;
    }

    byte[] b = new byte[n];

    for (int i = 0; i < len / 4; ++i) {

      byte[] temp = base64ToBytes(t.substring(4 * i, 4 * (i + 1)));

      for (int j = 0; j < temp.length; ++j) {
        b[3 * i + j] = temp[j];
      }
    }

    return b;
  }

  // Convert string to bytes
  @Deprecated
  private static byte[] base64ToBytes(String s) {

    int len = 0;

    for (int i = 0; i < s.length(); ++i) {
      if (s.charAt(i) != '=') {
        ++len;
      }
    }

    int[] digit = new int[len];

    for (int i = 0; i < len; ++i) {

      char c = s.charAt(i);

      if (c >= 'A' && c <= 'Z') {
        digit[i] = c - 'A';
      }
      else if (c >= 'a' && c <= 'z') {
        digit[i] = c - 'a' + 26;
      }
      else if (c >= '0' && c <= '9') {
        digit[i] = c - '0' + 52;
      }
      else if (c == '+') {
        digit[i] = 62;
      }
      else if (c == '/') {
        digit[i] = 63;
      }
    }

    byte[] b = new byte[len - 1];

    switch (len) {
      case 4 :
        b[2] = (byte) ((digit[2] & 0x03) << 6 | digit[3]);
      case 3 :
        b[1] = (byte) ((digit[1] & 0x0F) << 4 | (digit[2] & 0x3C) >>> 2);
      case 2 :
        b[0] = (byte) (digit[0] << 2 | (digit[1] & 0x30) >>> 4);
      default :
        break;
    }

    return b;
  }

  /**
   * Parses the supplied String into a byte[] - assumes that the text is in hex
   * format.
   *
   * @param s the hex String
   * @return the byte array.
   */
  public static byte[] hexStringToByteArray(String s) throws IOException {
    int len = s.length();
    if ((len & 0x01) != 0) {
      throw new IOException("Odd number of characters.");
    }
    byte[] data = new byte[len / 2];
    for (int i = 0; i < len; i += 2) {
      data[i / 2] = (byte) ((hexCharToDigit(s.charAt(i)) << 4) +
          hexCharToDigit(s.charAt(i + 1)));
    }
    return data;
  }

  private static int hexCharToDigit(char ch) throws IOException {
    int digit = Character.digit(ch, 16);
    if (digit == -1) {
      throw new IOException("Illegal hexadecimal character " + ch);
    }
    return digit;
  }

}
