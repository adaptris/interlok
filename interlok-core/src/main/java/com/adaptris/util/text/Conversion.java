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
   * @return the String
   * @deprecated since 3.10, use {@link java.util.Base64} instead.
   */
  @Deprecated
  public static String byteArrayToBase64String(byte[] b) {
    return java.util.Base64.getEncoder().encodeToString(b);
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
  public static byte[] base64StringToByteArray(String s) {
    return java.util.Base64.getDecoder().decode(s);
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
