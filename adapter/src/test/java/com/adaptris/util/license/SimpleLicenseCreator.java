package com.adaptris.util.license;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;

import org.apache.commons.io.IOUtils;

import com.adaptris.security.util.SecurityUtil;
import com.adaptris.util.license.License.LicenseType;
import com.adaptris.util.text.Base64ByteTranslator;

public class SimpleLicenseCreator {

  // These values are directly copied from SimpleLicense.
  // They are marked as private, so they get obfuscated...
  // So we can't use them if this class gets copied elsewhere.
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

  private static final Map<LicenseType, Integer> LICENSE_TO_INT;

  static {
    Map<LicenseType, Integer> map = new HashMap<>();
    map.put(LicenseType.Basic, 1);
    map.put(LicenseType.Standard, 2);
    map.put(LicenseType.Enterprise, 4);
    LICENSE_TO_INT = Collections.unmodifiableMap(map);
  }

  private transient LicenseType licenseType;
  private transient Date expiryDate;
  private transient Base64ByteTranslator b64 = new Base64ByteTranslator();

  public SimpleLicenseCreator(LicenseType type) {
    this(type, nextYear());
  }

  public SimpleLicenseCreator(LicenseType type, Date expiry) {
    this.licenseType = type;
    this.expiryDate = expiry;
  }

  /**
   * Create the license key.
   * 
   * @return the license key which is different every time.
   */
  public String create() throws Exception {
    return b64.translate(newCipher().doFinal(seed(getBytes(licenseType, expiryDate))));
  }

  private static byte[] getBytes(LicenseType type, Date expiry) throws Exception {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    DataOutputStream dataOut = new DataOutputStream(out);
    try {
      dataOut.writeByte(MY_LICENSE_VERSION);
      dataOut.writeByte(LICENSE_TO_INT.get(type));
      dataOut.writeInt(getExpiryDate(expiry));
    }
    finally {
      IOUtils.closeQuietly(dataOut);
      IOUtils.closeQuietly(out);
    }
    return out.toByteArray();
  }

  private Cipher newCipher() throws Exception {
    PBEParameterSpec pbeParamSpec = new PBEParameterSpec(SALT, ITERATIONS);
    PBEKeySpec pbeKeySpec = new PBEKeySpec(KEY_TYPE.toCharArray());
    SecretKeyFactory keyFac = SecretKeyFactory.getInstance(ALGORITHM);
    SecretKey pbeKey = keyFac.generateSecret(pbeKeySpec);
    Cipher pbeCipher = Cipher.getInstance(ALGORITHM);
    pbeCipher.init(Cipher.ENCRYPT_MODE, pbeKey, pbeParamSpec);
    return pbeCipher;
  }

  private static int getExpiryDate(Date expiry) {
    DateFormat df = new SimpleDateFormat("yyyyMMdd");
    return Integer.parseInt((df.format(expiry)));
  }

  private static byte[] seed(byte[] plainText) throws Exception {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    try {
      out.write(SecurityUtil.getSecureRandom().generateSeed(SEED_LENGTH));
      out.write(plainText);
    }
    finally {
      IOUtils.closeQuietly(out);
    }
    return out.toByteArray();
  }

  private static Date nextYear() {
    Calendar cal = Calendar.getInstance();
    cal.add(Calendar.YEAR, 1);
    return cal.getTime();
  }

  private static Date nextQuarter() {
    Calendar cal = Calendar.getInstance();
    cal.add(Calendar.DAY_OF_YEAR, 90);
    return cal.getTime();

  }

  private static void printLicenseDetails(SimpleLicense license) throws Exception {
    System.out.println("----------------------------");
    System.out.println("Raw Key    : " + license);
    System.out.println("Expiry     : " + license.getExpiry());
    System.out.println("Enterprise : " + license.isEnabled(SimpleLicense.LicenseType.Enterprise));
    System.out.println("Standard   : " + license.isEnabled(SimpleLicense.LicenseType.Standard));
    System.out.println("Basic      : " + license.isEnabled(SimpleLicense.LicenseType.Basic));
  }

  public static void main(String[] argv) throws Exception {
    Calendar cal = Calendar.getInstance();
    cal.set(Calendar.DAY_OF_YEAR, 365);

    Calendar ten = Calendar.getInstance();
    ten.add(Calendar.YEAR, 10);

    Calendar yesterday = Calendar.getInstance();
    yesterday.add(Calendar.DAY_OF_YEAR, -1);
    printLicenseDetails(new SimpleLicense(new SimpleLicenseCreator(SimpleLicense.LicenseType.Enterprise, cal.getTime()).create()));
    printLicenseDetails(new SimpleLicense(new SimpleLicenseCreator(SimpleLicense.LicenseType.Standard, cal.getTime()).create()));
    printLicenseDetails(new SimpleLicense(new SimpleLicenseCreator(SimpleLicense.LicenseType.Basic, cal.getTime()).create()));
    printLicenseDetails(new SimpleLicense(
        new SimpleLicenseCreator(SimpleLicense.LicenseType.Enterprise, yesterday.getTime()).create()));
    printLicenseDetails(new SimpleLicense(new SimpleLicenseCreator(SimpleLicense.LicenseType.Enterprise, nextQuarter()).create()));

    // System.out.println(create(SimpleLicense.LicenseType.Base, nextYear()));
    // System.out.println(create(SimpleLicense.LicenseType.Standard, nextYear()));
  }

}
