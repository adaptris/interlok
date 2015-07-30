package com.adaptris.util;

import java.security.SecureRandom;

import com.adaptris.util.text.Conversion;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Basic {@linkplain IdGenerator} implementation
 * <p>
 * The ID is generated from the a combination of {@linkplain SecureRandom#nextBytes(byte[])}, the hashcode of the object, and
 * current timestamp. No guarantees are made for the uniqueness of the ID generated.
 * </p>
 * 
 * @config plain-id-generator
 */
@XStreamAlias("plain-id-generator")
public class PlainIdGenerator implements IdGenerator {

  private String separator;
  private static SecureRandom random = new SecureRandom();

  public PlainIdGenerator() {
    setSeparator(".");
  }

  public PlainIdGenerator(String separator) {
    this();
    setSeparator(separator);
  }

  @Override
  public String create(Object msg) {
    byte[] bytes = new byte[8];
    random.nextBytes(bytes);
    return Conversion.byteArrayToBase64String(bytes).replaceAll("=", "") + getSeparator()
        + (msg == null ? "0" : Integer.toHexString(msg.hashCode()))
        + getSeparator() + System.currentTimeMillis();
  }

  /**
   * @return the separator
   */
  public String getSeparator() {
    return separator;
  }

  /**
   * @param s the separator to set
   */
  public void setSeparator(String s) {
    separator = s;
  }

}
