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

import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.Certificate;

import javax.crypto.Cipher;

import com.adaptris.security.exc.PasswordException;
import com.adaptris.util.text.Base64ByteTranslator;

class MicrosoftCrypto extends PasswordImpl {

  private PrivateKey privateKey;
  private Certificate certificate;
  private String username;
  private Base64ByteTranslator base64;

  public MicrosoftCrypto() throws PasswordException {
    try {
      base64 = new Base64ByteTranslator();
      username = System.getProperty("user.name");
      KeyStore ks = KeyStore.getInstance("Windows-MY");
      ks.load(null, null);
      privateKey = (PrivateKey) ks.getKey(username, null);
      certificate = ks.getCertificate(username);
    }
    catch (Exception e) {
      throw new PasswordException(e);
    }
  }

  public String decode(String encrypted, String charset) throws PasswordException {
    String encryptedString = encrypted;
    String result;

    if (encrypted.startsWith(Password.MSCAPI_STYLE)) {
      encryptedString = encrypted.substring(Password.MSCAPI_STYLE.length());
    }
    try {
      Cipher cipher = Cipher.getInstance("RSA");
      cipher.init(Cipher.DECRYPT_MODE, privateKey);
      byte[] encryptedBytes = base64.translate(encryptedString);
      byte[] decrypted = cipher.doFinal(encryptedBytes);
      result = new String(decrypted, getEncodingToUse(charset));
    }
    catch (Exception e) {
      throw new PasswordException(e);
    }
    return result;
  }

  public String encode(String plainText, String charset) throws PasswordException {
    byte[] encryptedBody = new byte[0];
    try {
      Cipher cipher = Cipher.getInstance("RSA");
      cipher.init(Cipher.ENCRYPT_MODE, certificate);
      encryptedBody = cipher.doFinal(plainText.getBytes(getEncodingToUse(charset)));
    }
    catch (Exception e) {
      throw new PasswordException(e);
    }
    return Password.MSCAPI_STYLE + base64.translate(encryptedBody);
  }

}
