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

import static com.adaptris.security.password.Password.NON_PORTABLE_PASSWORD;
import java.net.InetAddress;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;
import com.adaptris.annotation.Removal;
import com.adaptris.security.exc.PasswordException;
import com.adaptris.util.text.Base64ByteTranslator;

/**
 * deprecated since 3.11.1 since this uses PBEWithSHA1AndDESede which is a weak algorithm.
 *
 */
@Deprecated
@Removal(version = "4.0.0",
    message = "This uses PBEWithSHA1AndDESede which is now cryptographically weak")
public class PbeCrypto extends PasswordImpl {

    private static final byte[] SALT = { (byte)0xE1, (byte)0x1D, (byte)0x2B, (byte)0xE2, (byte)0x89, (byte)0x45, (byte)0x53, (byte)0xF7,
                                         (byte)0x7F, (byte)0x94, (byte)0x7D, (byte)0xF3, (byte)0x9E, (byte)0x68, (byte)0x0B, (byte)0x64,
                                         (byte)0x7E, (byte)0x20, (byte)0x5B, (byte)0x22, (byte)0xB9, (byte)0x18, (byte)0xC5, (byte)0xCD,
                                         (byte)0x4C, (byte)0x0F, (byte)0x96, (byte)0x3F, (byte)0x8F, (byte)0x18, (byte)0xC8, (byte)0x7C };

  private static final int ITERATIONS = 20;
  private static final String ALGORITHM = "PBEWithSHA1AndDESede"; // lgtm [java/weak-cryptographic-algorithm]
  private Base64ByteTranslator base64;

  private String hostname;

  public PbeCrypto() throws PasswordException {
    try {
      base64 = new Base64ByteTranslator();
      hostname = base64.translate(InetAddress.getLocalHost().getAddress());
    }
    catch (Exception e) {
      throw new PasswordException(e);
    }
  }

  @Override
  public boolean canHandle(String type) {
    return type != null && type.startsWith(NON_PORTABLE_PASSWORD);
  }

  @Override
  @SuppressWarnings({"lgtm [java/weak-cryptographic-algorithm]"})
  public String decode(String encrypted, String charset) throws PasswordException {
    String encryptedString = encrypted;
    String result = null;
    if (encrypted.startsWith(NON_PORTABLE_PASSWORD)) {
      encryptedString = encrypted.substring(NON_PORTABLE_PASSWORD.length());
    }
    try {
      PBEParameterSpec pbeParamSpec = new PBEParameterSpec(SALT, ITERATIONS);
      PBEKeySpec pbeKeySpec = new PBEKeySpec(hostname.toCharArray());
      SecretKeyFactory keyFac = SecretKeyFactory.getInstance(ALGORITHM);
      SecretKey pbeKey = keyFac.generateSecret(pbeKeySpec);
      Cipher pbeCipher = Cipher.getInstance(ALGORITHM);
      pbeCipher.init(Cipher.DECRYPT_MODE, pbeKey, pbeParamSpec);
      byte[] decrypted = pbeCipher.doFinal(base64.translate(encryptedString));
      result = unseed(decrypted, charset);
    }
    catch (Exception e) {
      throw new PasswordException(e);
    }
    return result;
  }

  @Override
  @SuppressWarnings({"lgtm [java/weak-cryptographic-algorithm]"})
  public String encode(String plainText, String charset) throws PasswordException {
    byte[] encrypted = new byte[0];
    try {
      PBEParameterSpec pbeParamSpec = new PBEParameterSpec(SALT, ITERATIONS);
      PBEKeySpec pbeKeySpec = new PBEKeySpec(hostname.toCharArray());
      SecretKeyFactory keyFac = SecretKeyFactory.getInstance(ALGORITHM);
      SecretKey pbeKey = keyFac.generateSecret(pbeKeySpec);
      Cipher pbeCipher = Cipher.getInstance(ALGORITHM);
      pbeCipher.init(Cipher.ENCRYPT_MODE, pbeKey, pbeParamSpec);
      encrypted = pbeCipher.doFinal(seed(plainText, charset));
    }
    catch (Exception e) {
      throw new PasswordException(e);
    }
    return NON_PORTABLE_PASSWORD + base64.translate(encrypted);
  }

}
