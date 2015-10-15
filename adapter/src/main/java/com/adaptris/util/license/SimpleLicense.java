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

package com.adaptris.util.license;

import java.io.ByteArrayInputStream;
import java.io.Closeable;
import java.io.DataInputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Adaptris license key object.
 * <p>
 * This is used to verify that target hostname at installation, at run time, and what adapters are available for
 * configuration/execution.
 * </p>
 * 
 */
final class SimpleLicense implements License {
  private static final int MY_LICENSE_VERSION = 3;
  private static final byte[] SALT =
  {
      (byte) 0xE1, (byte) 0x1D, (byte) 0x2B, (byte) 0xE2, (byte) 0x89, (byte) 0x45, (byte) 0x53, (byte) 0xF7, (byte) 0x7F,
      (byte) 0xE2, (byte) 0x7D, (byte) 0xF3, (byte) 0x9E, (byte) 0x68, (byte) 0x0B, (byte) 0x64, (byte) 0x7E, (byte) 0x69,
      (byte) 0x5B, (byte) 0x22, (byte) 0xBF, (byte) 0x18, (byte) 0xF2, (byte) 0xCD, (byte) 0x4C, (byte) 0x89, (byte) 0x96,
      (byte) 0x9E, (byte) 0x5F, (byte) 0x18, (byte) 0xC8, (byte) 0x7D
  };
  private static final int ITERATIONS = 1000;
  private static final String ALGORITHM = "PBEWithSHA1AndDESede";
  private static final int SEED_LENGTH = 4;
  private static final String KEY_TYPE = "Adaptris License Key";

  private static final EnumSet<LicenseType> BASIC_LICENSE = EnumSet.of(LicenseType.Basic);
  private static final EnumSet<LicenseType> STANDARD_LICENSE = EnumSet.of(LicenseType.Basic, LicenseType.Standard);
  private static final EnumSet<LicenseType> ENTERPRISE_LICENSE = EnumSet.of(LicenseType.Basic, LicenseType.Standard,
      LicenseType.Enterprise);
  private static final EnumSet<LicenseType> RESTRICTED_LICENSE = EnumSet.of(LicenseType.Restricted);
  private static final Map<Integer, EnumSet<LicenseType>> LICENSE_MAP;

  static {
    Map<Integer, EnumSet<LicenseType>> map = new HashMap<>();
    map.put(0, RESTRICTED_LICENSE);
    map.put(1, BASIC_LICENSE);
    map.put(2, STANDARD_LICENSE);
    map.put(4, ENTERPRISE_LICENSE);
    LICENSE_MAP = Collections.unmodifiableMap(map);
  }

  private transient Logger log = LoggerFactory.getLogger(License.class);
  private Calendar expiryDate = null;
  private String license;
  private EnumSet<LicenseType> licenseSet = RESTRICTED_LICENSE;

  SimpleLicense(String s) throws LicenseException {
    license = s.replaceAll(" ", "");
    initialise();
  }

  public void verify() throws LicenseException {
    if (hasExpired()) {
      throw new LicenseException("License has expired");
    }
  }

  public boolean isEnabled(LicenseType type) {
    return licenseSet.contains(type);
  }

  /**
   * 
   * @see com.adaptris.util.license.License#hasExpired()
   */
  public boolean hasExpired() throws LicenseException {
    Date today = new Date();
    return today.after(getExpiry());
  }

  /**
   * 
   * @see com.adaptris.util.license.License#getExpiry()
   */
  public Date getExpiry() throws LicenseException {
    if (expiryDate == null) {
      return new Date(1);
    }
    return expiryDate.getTime();
  }

  private void initialise() throws LicenseException {
    DataInputStream in = null;
    try {
      Cipher cipher = buildCipher();
      in = getInputStream(fromb64(license), cipher);
      licenseSet = parse(new Byte(in.readByte()).intValue());
      setExpiryDate(in.readInt());
    }
    catch (Exception e) {
      rethrow(e);
    }
    finally {
      closeQuietly(in);
    }
    log.trace("License is valid; expires on " + getExpiry());
  }

  private void rethrow(Exception e) throws LicenseException {
    if (e instanceof LicenseException) {
      throw (LicenseException) e;
    }
    throw new LicenseException(e);
  }

  private byte[] decrypt(byte[] bytes, Cipher c) throws IllegalBlockSizeException, BadPaddingException {
    byte[] decrypted = c.doFinal(bytes);
    return Arrays.copyOfRange(decrypted, SEED_LENGTH, decrypted.length);
  }

  private DataInputStream getInputStream(byte[] in, Cipher c) throws LicenseException, IOException {
    DataInputStream result = null;
    try {
      byte[] decrypted = decrypt(in, c);
      result = new DataInputStream(new ByteArrayInputStream(decrypted));
      int version = new Byte(result.readByte()).intValue();
      if (version != MY_LICENSE_VERSION) {
        throw new LicenseException("License version mismatch");
      }
    }
    catch (IllegalBlockSizeException | BadPaddingException e) {
      throw new LicenseException("Could not interpret license");
    }
    return result;
  }

  private Cipher buildCipher() throws Exception {
    PBEParameterSpec pbeParamSpec = new PBEParameterSpec(SALT, ITERATIONS);
    PBEKeySpec pbeKeySpec = new PBEKeySpec(KEY_TYPE.toCharArray());
    SecretKeyFactory keyFac = SecretKeyFactory.getInstance(ALGORITHM);
    SecretKey pbeKey = keyFac.generateSecret(pbeKeySpec);
    Cipher pbeCipher = Cipher.getInstance(ALGORITHM);
    pbeCipher.init(Cipher.DECRYPT_MODE, pbeKey, pbeParamSpec);
    return pbeCipher;
  }

  @Override
  public String toString() {
    return license;
  }

  private void setExpiryDate(int dateInt) throws IOException {
    try {
      DateFormat df = new SimpleDateFormat("yyyyMMdd");
      expiryDate = Calendar.getInstance();
      expiryDate.setTime(df.parse(Integer.toString(dateInt)));
    }
    catch (Exception edate) {
      log.warn("Unparseable expiry date, assuming license has expired!");
      expiryDate = Calendar.getInstance();
      expiryDate.add(Calendar.DAY_OF_YEAR, -1);
    }
  }

  private static EnumSet<LicenseType> parse(int i) {
    EnumSet<LicenseType> map = LICENSE_MAP.get(i);
    return map != null ? map : RESTRICTED_LICENSE;
  }

  private static void closeQuietly(Closeable input) {
    try {
      if (input != null) input.close();
    }
    catch (IOException ioe) {
    }
  }

  private static byte[] fromb64(String s) throws NumberFormatException {

    String t = "";

    for (int i = 0; i < s.length(); ++i) {

      char c = s.charAt(i);

      if (c == '\n') {
        continue;
      }
      else if (c >= 'A' && c <= 'Z' || c >= 'a' && c <= 'z' || c >= '0' && c <= '9' || c == '+' || c == '/') {
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
    case 1:
      throw new NumberFormatException();
    case 2:
      len += 2;
      n += 1;
      t += "==";

      break;
    case 3:
      ++len;
      n += 2;
      t += "=";

      break;
    default:
      break;
    }

    byte[] b = new byte[n];

    for (int i = 0; i < len / 4; ++i) {

      byte[] temp = fromb642(t.substring(4 * i, 4 * (i + 1)));
      for (int j = 0; j < temp.length; ++j) {
        b[3 * i + j] = temp[j];
      }
    }

    return b;
  }

  private static byte[] fromb642(String s) {

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
    case 4:
      b[2] = (byte) ((digit[2] & 0x03) << 6 | digit[3]);
    case 3:
      b[1] = (byte) ((digit[1] & 0x0F) << 4 | (digit[2] & 0x3C) >>> 2);
    case 2:
      b[0] = (byte) (digit[0] << 2 | (digit[1] & 0x30) >>> 4);
    default:
      break;
    }

    return b;
  }

}
