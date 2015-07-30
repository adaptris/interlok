/*
 * $Id: DesLicense.java,v 1.2 2009/04/28 13:39:28 lchan Exp $
 */
package com.adaptris.util.license;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.security.MessageDigest;
import java.security.spec.KeySpec;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.StringTokenizer;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Adaptris license key object.
 * <p>
 * This is used to verify that target hostname at installation, at run time, and
 * what adapters are available for configuration/execution.
 * </p>
 *
 * @author $Author: lchan $
 */
final class DesLicense implements License {

  private transient Logger logR = LoggerFactory.getLogger(License.class);
  private Calendar expiryDate = null;
  private int components = 0;
  private int maxChannels = 0;
  private String license;
  private boolean initialised = false;

  /**
   * Construct with the specified key.
   *
   * @param s the license key
   */
  DesLicense(String s) throws LicenseException {
    license = s.replaceAll(" ", "");
    initialise();
  }

  @Override
  public boolean isEnabled(LicenseType type) {
    return false;
  }

  /**
   * @see License#isEnabled(int)
   */
  public boolean isEnabled(int l) {
    return (l & components) > 0 ? true : false;
  }

  /**
   *
   * @see com.adaptris.util.license.License#verify()
   */
  public void verify() throws LicenseException {
    if (!isValid()) {
      throw new LicenseException("License is not valid for this machine");
    }
    if (hasExpired()) {
      throw new LicenseException("License has expired");
    }
  }

  /**
   * @see License#isValid()
   */
  public boolean isValid() throws LicenseException {
    return initialised;
  }

  /**
   * @see License#exceedsMaxChannels(int)
   */
  public boolean exceedsMaxChannels(int channelCount) {
    return maxChannels == 0 ? false : maxChannels < channelCount;
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
      byte[] bytes = fromb64(license);
      Cipher cipher = buildCipher();
      try {
        in = decrypt(new ByteArrayInputStream(bytes), cipher);
      }
      catch (LicenseException e) {
        // This is really fucked up, but jeff wants to have a key
        // that Progress can use for any machine.
        // I think this will come back and bite us in the ass.
        cipher = buildCipher("PROGRESS");
        in = decrypt(new ByteArrayInputStream(bytes), cipher);
      }
      maxChannels = new Byte(in.readByte()).intValue();
      components = in.readInt();
      setExpiryDate(in.readInt());
      initialised = true;
    }
    catch (LicenseException e) {
      throw e;
    }
    catch (Exception e) {
      throw new LicenseException(e.getMessage(), e);
    }
  }

  private DataInputStream decrypt(ByteArrayInputStream in, Cipher c)
      throws LicenseException, IOException {
    DataInputStream result = null;
    CipherInputStream cIn = new CipherInputStream(in, c);
    result = new DataInputStream(cIn);
    int version = result.readInt();
    return result;
  }

  private Cipher buildCipher() throws Exception {
    InetAddress address = InetAddress.getLocalHost();
    return buildCipher(getHostName(address.getHostName().toUpperCase()));
  }

  private Cipher buildCipher(String key) throws Exception {
    Cipher cipher = null;
    MessageDigest md = MessageDigest.getInstance("MD5");
    byte[] digest = md.digest(key.getBytes("ISO-8859-1"));
    KeySpec sks = new DESKeySpec(digest);
    SecretKeyFactory skf = SecretKeyFactory.getInstance("DES");
    SecretKey sk = skf.generateSecret(sks);
    cipher = Cipher.getInstance("DES");
    cipher.init(Cipher.DECRYPT_MODE, sk);
    return cipher;
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
      logR.warn("Unparseable expiry date, assuming license has expired!");
      expiryDate = Calendar.getInstance();
      expiryDate.add(Calendar.DAY_OF_YEAR, -1);
    }
  }

  private byte[] fromb64(String s) throws NumberFormatException {

    String t = "";

    for (int i = 0; i < s.length(); ++i) {

      char c = s.charAt(i);

      if (c == '\n') {
        continue;
      }
      else if (c >= 'A' && c <= 'Z' || c >= 'a' && c <= 'z'
          || c >= '0' && c <= '9' || c == '+' || c == '/') {
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

  private static String getHostName(String fqdn) {
    String host = "";
    StringTokenizer st;
    try {
      st = new StringTokenizer(fqdn, ".");
      host = st.nextToken();
    }
    catch (Exception e) {
      ;
    }
    return host;
  }

}
