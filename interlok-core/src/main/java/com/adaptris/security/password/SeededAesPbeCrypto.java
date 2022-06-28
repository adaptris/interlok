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

package com.adaptris.security.password;

import static com.adaptris.security.password.Password.SEEDED_BATCH;

import java.io.FileInputStream;
import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.bouncycastle.crypto.digests.SHA384Digest;

import com.adaptris.security.exc.PasswordException;
import com.adaptris.util.text.Base64ByteTranslator;
import com.adaptris.util.text.HexStringByteTranslator;

import lombok.Getter;
import lombok.Setter;

public class SeededAesPbeCrypto extends PasswordImpl {
  private static final byte[] SALT = { (byte) 0xE1, (byte) 0x1D, (byte) 0x2B, (byte) 0xE2, (byte) 0x89, (byte) 0x45, (byte) 0x53,
      (byte) 0xF7, (byte) 0x7F, (byte) 0x94, (byte) 0x7D, (byte) 0xF3, (byte) 0x9E, (byte) 0x68, (byte) 0x0B, (byte) 0x64, (byte) 0x7E,
      (byte) 0x20, (byte) 0x5B, (byte) 0x22, (byte) 0xB9, (byte) 0x18, (byte) 0xC5, (byte) 0xCD, (byte) 0x4C, (byte) 0x0F, (byte) 0x96,
      (byte) 0x3F, (byte) 0x8F, (byte) 0x18, (byte) 0xC8, (byte) 0x7C };
  private static final int IV_LENGTH = 16;
  private static final int ITERATIONS = 20000;
  private static final String ALGORITHM = "PBEWithHmacSHA256AndAES_128";
  private Base64ByteTranslator base64;

  public static final String SYSTEM_PROPERTY = "interlok.password.seed";

  /**
   * Location of the seed file used to to do the encryption. Should be left empty if the system property <i>interlok.password.seed</i> is
   * used.
   */
  @Getter
  @Setter
  private String seedFile;

  public SeededAesPbeCrypto() {
    this(System.getProperty(SYSTEM_PROPERTY));
  }

  public SeededAesPbeCrypto(String seedFile) {
    base64 = new Base64ByteTranslator();
    this.seedFile = seedFile;
  }

  @Override
  public boolean canHandle(String type) {
    return StringUtils.startsWith(type, SEEDED_BATCH);
  }

  @Override
  public String decode(String encrypted, String charset) throws PasswordException {
    String encryptedString = encrypted;
    if (encrypted.startsWith(SEEDED_BATCH)) {
      encryptedString = encrypted.substring(SEEDED_BATCH.length());
    }
    try {
      int mode = Cipher.DECRYPT_MODE;
      Cipher pbeCipher = configureCipher(mode);

      byte[] decrypted = pbeCipher.doFinal(base64.translate(encryptedString));
      return unseed(decrypted, charset);
    } catch (Exception e) {
      throw new PasswordException(e);
    }
  }

  @Override
  public String encode(String plainText, String charset) throws PasswordException {
    try {
      int mode = Cipher.ENCRYPT_MODE;
      byte[] secret = seedAndIv();
      byte[] iv = ArrayUtils.subarray(secret, 0, IV_LENGTH);
      char[] seed = new HexStringByteTranslator().translate(ArrayUtils.subarray(secret, IV_LENGTH, secret.length)).toCharArray();

      PBEParameterSpec pbeParamSpec = new PBEParameterSpec(SALT, ITERATIONS, new IvParameterSpec(iv));
      PBEKeySpec pbeKeySpec = new PBEKeySpec(seed);
      SecretKeyFactory keyFac = SecretKeyFactory.getInstance(ALGORITHM);
      SecretKey pbeKey = keyFac.generateSecret(pbeKeySpec);
      Cipher pbeCipher = Cipher.getInstance(ALGORITHM);
      pbeCipher.init(mode, pbeKey, pbeParamSpec);

      byte[] encrypted = pbeCipher.doFinal(seed(plainText, charset));
      return SEEDED_BATCH + base64.translate(encrypted);
    } catch (Exception e) {
      throw new PasswordException(e);
    }
  }

  private Cipher configureCipher(int mode) throws PasswordException, IOException, NoSuchAlgorithmException, InvalidKeySpecException,
  NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException {
    byte[] secret = seedAndIv();
    byte[] iv = ArrayUtils.subarray(secret, 0, IV_LENGTH);
    char[] seed = new HexStringByteTranslator().translate(ArrayUtils.subarray(secret, IV_LENGTH, secret.length)).toCharArray();

    PBEParameterSpec pbeParamSpec = new PBEParameterSpec(SALT, ITERATIONS, new IvParameterSpec(iv));
    PBEKeySpec pbeKeySpec = new PBEKeySpec(seed);
    SecretKeyFactory keyFac = SecretKeyFactory.getInstance(ALGORITHM);
    SecretKey pbeKey = keyFac.generateSecret(pbeKeySpec);
    Cipher pbeCipher = Cipher.getInstance(ALGORITHM);
    pbeCipher.init(mode, pbeKey, pbeParamSpec);

    return pbeCipher;
  }

  private byte[] seedAndIv() throws PasswordException {
    SHA384Digest sha = new SHA384Digest();
    try {
      if (seedFile == null) {
        if ((seedFile = System.getProperty(SYSTEM_PROPERTY)) == null) {
          throw new PasswordException("No seed file specified in system properties \"" + SYSTEM_PROPERTY + "\"");
        }
      }
      try (FileInputStream in = new FileInputStream(seedFile)) {
        byte[] b = IOUtils.readFully(in, in.available());
        sha.update(b, 0, b.length);
      }
      byte[] s = new byte[sha.getDigestSize()];
      sha.doFinal(s, 0);
      return s;
    } catch (Exception e) {
      throw new PasswordException(e);
    }
  }

}
