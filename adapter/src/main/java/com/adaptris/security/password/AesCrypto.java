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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.io.IOUtils;

import com.adaptris.security.exc.PasswordException;
import com.adaptris.security.util.SecurityUtil;
import com.adaptris.util.text.Base64ByteTranslator;

class AesCrypto extends PasswordImpl {

  private static final String ALG = "AES";
  private static final String CIPHER = "AES/CBC/PKCS5Padding";
  private static final int KEY_LEN = 128;

  private Base64ByteTranslator base64;

  public AesCrypto() {
    base64 = new Base64ByteTranslator();
  }

  public String decode(String encrypted, String charset) throws PasswordException {

    String encryptedString = encrypted;
    String result;

    if (encrypted.startsWith(Password.PORTABLE_PASSWORD)) {
      encryptedString = encrypted.substring(Password.PORTABLE_PASSWORD.length());
    }
    try {
      Input input = new Input(encryptedString);
      input.read();
      SecretKey sessionKey = new SecretKeySpec(input.getSessionKey(), ALG);
      Cipher cipher = Cipher.getInstance(CIPHER);
      if (input.getSessionVector() != null) {
        IvParameterSpec spec = new IvParameterSpec(input.getSessionVector());
        cipher.init(Cipher.DECRYPT_MODE, sessionKey, spec);
      }
      else {
        cipher.init(Cipher.DECRYPT_MODE, sessionKey);
      }
      byte[] decrypted = cipher.doFinal(input.getEncryptedData());
      result = unseed(decrypted, charset);
    }
    catch (Exception e) {
      throw new PasswordException(e);
    }
    return result;
  }

  public String encode(String plainText, String charset) throws PasswordException {
    String result = null;
    try {
      KeyGenerator kg = KeyGenerator.getInstance(ALG);
      kg.init(KEY_LEN, SecurityUtil.getSecureRandom());
      SecretKey sessionKey = kg.generateKey();
      Cipher dataCipher = Cipher.getInstance(CIPHER);
      dataCipher.init(Cipher.ENCRYPT_MODE, sessionKey);
      byte[] encryptedBody = dataCipher.doFinal(seed(plainText, charset));
      Output output = new Output();
      output.setSessionKey(sessionKey.getEncoded());
      output.setSessionVector(dataCipher.getIV());
      output.setEncryptedData(encryptedBody);
      result = Password.PORTABLE_PASSWORD + output.write();
    }
    catch (Exception e) {
      throw new PasswordException(e);
    }
    return result;
  }

  private class Input {

    String encrypted = null;
    private byte[] sessionKey;
    private byte[] sessionVector;
    private byte[] encryptedData;

    Input(String s) {
      this.encrypted = s;
    }

    void read() throws IOException {
      DataInputStream in = new DataInputStream(new ByteArrayInputStream(base64.translate(encrypted)));
      try {
        sessionVector = read(in);
        sessionKey = read(in);
        encryptedData = read(in);
      }
      finally {
        IOUtils.closeQuietly(in);
      }
    }

    private byte[] read(DataInputStream in) throws IOException {
      byte[] bytes = new byte[in.readInt()];
      if (bytes.length > 0) {
        in.read(bytes, 0, bytes.length);
      }
      else {
        bytes = null;
      }
      return bytes;
    }

    byte[] getSessionKey() {
      return sessionKey;
    }

    byte[] getSessionVector() {
      return sessionVector;
    }

    byte[] getEncryptedData() {
      return encryptedData;
    }
  }

  private class Output {
    private byte[] sessionKey;
    private byte[] sessionVector;
    private byte[] encryptedData;

    void setSessionKey(byte[] b) {
      this.sessionKey = b;
    }

    void setSessionVector(byte[] b) {
      this.sessionVector = b;
    }

    void setEncryptedData(byte[] b) {
      this.encryptedData = b;
    }

    String write() throws IOException {
      DataOutputStream out = null;
      ByteArrayOutputStream byteStream = null;
      String result = null;
      try {
        byteStream = new ByteArrayOutputStream();
        out = new DataOutputStream(byteStream);
        write(out, sessionVector);
        write(out, sessionKey);
        write(out, encryptedData);
        out.flush();
        result = base64.translate(byteStream.toByteArray());
      }
      finally {
        IOUtils.closeQuietly(out);
      }
      return result;
    }

    private void write(DataOutputStream out, byte[] bytes) throws IOException {
      if (bytes == null) {
        out.writeInt(0);
      }
      else {
        out.writeInt(bytes.length);
        out.write(bytes, 0, bytes.length);
      }
    }
  }

}
