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

import static com.adaptris.security.password.Password.PORTABLE_PASSWORD_2;

import java.security.SecureRandom;
import java.util.Arrays;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import com.adaptris.security.exc.PasswordException;
import com.adaptris.util.text.Base64ByteTranslator;

public class AesGcmCrypto extends PasswordImpl {

  private static final String ALGORITHM = "AES";
  private static final String CIPHER = "AES/GCM/NoPadding";
  private static final int KEY_LENGTH = 128;
  private static final int KEY_SIZE = KEY_LENGTH / Byte.SIZE;
  private static final int TAG_LENGTH = 128;
  private static final int IV_LENGTH = 12;

  private final static SecureRandom SECURE_RAND = new SecureRandom();

  private Base64ByteTranslator base64;

  public AesGcmCrypto() {
    base64 = new Base64ByteTranslator();
  }

  @Override
  public boolean canHandle(String type) {
    return type != null && type.startsWith(PORTABLE_PASSWORD_2);
  }

  @Override
  public String decode(String b64CipherText, String charset) throws PasswordException {
    if (b64CipherText.startsWith(PORTABLE_PASSWORD_2)) {
      b64CipherText = b64CipherText.substring(PORTABLE_PASSWORD_2.length());
    }
    try {
      byte[] cipherText = base64.translate(b64CipherText);
      byte[] iv = Arrays.copyOfRange(cipherText, 0, IV_LENGTH);
      byte[] key = Arrays.copyOfRange(cipherText, IV_LENGTH, IV_LENGTH + KEY_SIZE);
      SecretKey secretKey = new SecretKeySpec(key, ALGORITHM);
      Cipher cipher = Cipher.getInstance(CIPHER);
      GCMParameterSpec spec = new GCMParameterSpec(TAG_LENGTH, iv);
      cipher.init(Cipher.DECRYPT_MODE, secretKey, spec);
      int offset = iv.length + key.length;
      byte[] plaintext = cipher.doFinal(cipherText, offset, cipherText.length - offset);
      return new String(plaintext, getEncodingToUse(charset));
    } catch (Exception e) {
      throw new PasswordException(e);
    }
  }

  @Override
  public String encode(String plainText, String charset) throws PasswordException {
    try {
      KeyGenerator keyGenerator = KeyGenerator.getInstance(ALGORITHM);
      keyGenerator.init(KEY_LENGTH);
      SecretKey secretKey = keyGenerator.generateKey();
      Cipher cipher = Cipher.getInstance(CIPHER);
      byte[] iv = new byte[IV_LENGTH];
      SECURE_RAND.nextBytes(iv);
      GCMParameterSpec spec = new GCMParameterSpec(TAG_LENGTH, iv);
      cipher.init(Cipher.ENCRYPT_MODE, secretKey, spec);
      byte[] bytes = plainText.getBytes(getEncodingToUse(charset));
      byte[] key = secretKey.getEncoded();
      byte[] cipherText = new byte[iv.length + key.length + cipher.getOutputSize(bytes.length)];
      int i = 0;
      for (byte v : iv) {
        cipherText[i++] = v;
      }
      for (byte k : key) {
        cipherText[i++] = k;
      }
      cipher.doFinal(bytes, 0, bytes.length, cipherText, i);
      secretKey.getEncoded();
      return PORTABLE_PASSWORD_2 + base64.translate(cipherText);
    } catch (Exception e) {
      throw new PasswordException(e);
    }
  }
}
