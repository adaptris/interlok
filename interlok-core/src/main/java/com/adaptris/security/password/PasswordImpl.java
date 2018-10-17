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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

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
    try (OutputStream writer = out) {
      writer.write(SecurityUtil.getSecureRandom().generateSeed(SEED));
      writer.write(plainText.getBytes(getEncodingToUse(charset)));
    }
    return out.toByteArray();
  }

  String unseed(byte[] decrypted, String charset) throws IOException {
    return new String(decrypted, SEED, decrypted.length - SEED, getEncodingToUse(charset));
  }
}
