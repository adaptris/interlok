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

package com.adaptris.security;

import static org.junit.Assert.assertEquals;
import java.security.KeyStore;
import java.security.cert.X509Certificate;
import org.junit.Test;
import com.adaptris.interlok.junit.scaffolding.util.Os;
import com.adaptris.security.certificate.CertificateBuilder;
import com.adaptris.security.password.Password;
import com.adaptris.security.password.PasswordCodec;

/**
 */
public class TestPassword {
  private static final String CHARSET = "ISO-8859-1";
  private static final String PW = "Goodbye Cruel World.";

  @Test
  public void testPortableStyle() throws Exception {
    PasswordCodec pw = Password.create(Password.PORTABLE_PASSWORD);
    assertEquals(PW, pw.decode(pw.encode(PW)));
  }

  @Test
  public void testPortableStyleWithCharset() throws Exception {
    PasswordCodec pw = Password.create(Password.PORTABLE_PASSWORD);
    assertEquals(PW, pw.decode(pw.encode(PW, CHARSET), CHARSET));
  }

  @Test
  public void testNonPortableStyle() throws Exception {
    PasswordCodec pw = Password.create(Password.NON_PORTABLE_PASSWORD);
    assertEquals(PW, pw.decode(pw.encode(PW)));
  }

  @Test
  public void testNonPortaableStyleWithCharset() throws Exception {
    PasswordCodec pw = Password.create(Password.NON_PORTABLE_PASSWORD);
    assertEquals(PW, pw.decode(pw.encode(PW, CHARSET), CHARSET));
  }

  @Test
  public void testMicrosoftCrypto() throws Exception {
    if (Os.isFamily(Os.WINDOWS_NT_FAMILY)) {
      specificMicrosoftSetup();
      PasswordCodec pw = Password.create(Password.MSCAPI_STYLE);
      assertEquals(PW, pw.decode(pw.encode(PW)));
    }
    else {
      System.out.println("Not a MS platform! for testMicrosoftCrypto()");
    }
  }

  @Test
  public void testMicrosoftCryptoWithCharset() throws Exception {
    if (Os.isFamily(Os.WINDOWS_NT_FAMILY)) {
      specificMicrosoftSetup();
      PasswordCodec pw = Password.create(Password.MSCAPI_STYLE);
      assertEquals(PW, pw.decode(pw.encode(PW, CHARSET), CHARSET));
    }
    else {
      System.out.println("Not a MS platform! for testMicrosoftCryptoWithCharset()");
    }
  }

  @Test
  public void testPlainText() throws Exception {
    PasswordCodec pw = Password.create(PW);
    assertEquals(PW, pw.decode(pw.encode(PW)));
  }

  /*
   * Ensure that the Windows-MY keystore actually has a certificate
   * chain and a private key
   */
  private void specificMicrosoftSetup() throws Exception {
    String username = System.getProperty("user.name");
    KeyStore ks = KeyStore.getInstance("Windows-MY");
    ks.load(null, null);
    if (!ks.containsAlias(username)) {
      CertificateBuilder builder = Config.getInstance().getBuilder(username);
      X509Certificate[] chain = new X509Certificate[1];
      chain[0] = (X509Certificate)builder.createSelfSignedCertificate();
      ks.setKeyEntry(username, builder.getPrivateKey(), username.toCharArray(), chain);
    }
  }
}
