package com.adaptris.security.password;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.apache.commons.io.IOUtils;

import com.adaptris.security.exc.PasswordException;
import com.adaptris.security.util.SecurityUtil;

abstract class PasswordImpl implements PasswordCodec {

  private static final int SEED = 4;

  protected PasswordImpl() {
    SecurityUtil.addProvider();
  }

  public String decode(String encrypted) throws PasswordException {
    return decode(encrypted, null);
  }

  public String encode(String plainText) throws PasswordException {
    return encode(plainText, null);
  }

  protected String getEncodingToUse(String encoding) {
    return encoding == null ? "UTF-8" : encoding;
  }

  byte[] seed(String plainText, String charset) throws IOException {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    try {
      out.write(SecurityUtil.getSecureRandom().generateSeed(SEED));
      out.write(plainText.getBytes(getEncodingToUse(charset)));
    }
    finally {
      IOUtils.closeQuietly(out);
    }
    return out.toByteArray();
  }

  String unseed(byte[] decrypted, String charset) throws IOException {
    return new String(decrypted, SEED, decrypted.length - SEED, getEncodingToUse(charset));
  }
}
