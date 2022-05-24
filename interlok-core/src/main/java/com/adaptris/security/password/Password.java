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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;
import java.util.ServiceLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.adaptris.annotation.Removal;
import com.adaptris.security.exc.PasswordException;

/**
 * Handles simple encryption and decryption of passwords that may be stored in XML configuration.
 *
 * @author lchan
 *
 */
public abstract class Password {


  private static transient Password INSTANCE = new PasswordLoader();

  private static Logger log = LoggerFactory.getLogger(Password.class);
  /**
   * Password obfuscation using Microsoft Crypto API which is only available on
   * windows.
   * <p>
   * Use of this means that the password is encrypted with the current
   * username's private key and certificate.
   * </p>
   *
   */
  public static final String MSCAPI_STYLE = "MSCAPI:";
  /**
   * Standard password style which is portable across environments.
   * <p>
   * It is not considered especially secure, but is enough to stop casual
   * interrogation
   * </p>
   *
   */
  public static final String PORTABLE_PASSWORD = "PW:";
  /**
   * Standard password style which is portable across environments.
   * <p>
   * It uses the Advanced Encryption Standard (AES) algorithm in
   * Galois/Counter Mode (GCM) to perform the encryption. GCM has the
   * benefit of providing authenticity (integrity) in addition to
   * confidentiality.
   * </p>
   *
   */
  public static final String PORTABLE_PASSWORD_2 = "AES_GCM:";
  /**
   * Alternative password style which is portable across environments
   * and machines if the seed file is present. Uses a similar password
   * based encryption scheme to the deprecated/removed non-portable
   * PBEWithSHA1AndDESede. This uses the more up-to-date and secure
   * PBEWithHmacSHA256AndAES_128.
   */
  public static final String SEEDED_BATCH = "SEED:";

  private static final String[] STYLES =
  {
          MSCAPI_STYLE, PORTABLE_PASSWORD, PORTABLE_PASSWORD_2, SEEDED_BATCH
  };

  /**
   * Create a password implementation of the specified type.
   *
   * @param type the type
   * @return the password implementation
   * @throws PasswordException wrapping other exceptions.
   * @see #MSCAPI_STYLE
   * @see #SEEDED_BATCH
   * @see #PORTABLE_PASSWORD
   */
  public static PasswordCodec create(String type) throws PasswordException {
    return INSTANCE.createCodec(type);
  }

  protected abstract PasswordCodec createCodec(String type) throws PasswordException;

  /**
   * Convenience method to decrypt a password.
   *
   * @param encoded the encrypted password
   * @return the decrypted password
   */
  public static String decode(String encoded) throws PasswordException {
    return create(encoded).decode(encoded);
  }

  /**
   * Convenience method to encode a password.
   *
   * @param plain the plain password
   * @param type the type of encryption to use.
   * @return the encoded password
   * @see #MSCAPI_STYLE
   * @see #SEEDED_BATCH
   * @see #PORTABLE_PASSWORD
   */
  public static String encode(String plain, String type) throws PasswordException {
    return create(type).encode(plain);
  }

  static void generatePassword(String[] argv) throws Exception {
    if (argv == null || argv.length < 2) {
      System.out.println("Usage :");
      System.err.println("  java " + Password.class.getCanonicalName() + " <style> <password>");
      System.err.println("    where style is one of (trailing colon is required):");
      for (String s : STYLES) {
        System.err.println("      " + s);
      }
      return;
    }
    PasswordCodec ph = create(argv[0]);
    String plainText = argv[1];
    System.err.println("Cut and paste the following value where passwords are supported");
    System.err.println("(should be a single line, with no spaces)");
    System.err.println(ph.encode(plainText));
  }

  public static void main(String[] argv) throws Exception {
    generatePassword(argv);
    System.exit(0);
  }



  private static class PasswordLoader extends Password {
    private Collection<PasswordCodec> passwordImpls = new ArrayList<>();

    PasswordLoader() {
      for (PasswordCodec r : ServiceLoader.load(PasswordCodec.class)) {
        passwordImpls.add(r);
      }
    }

    @Override
    protected PasswordCodec createCodec(String type) throws PasswordException {
      Optional<PasswordCodec> impl =
          passwordImpls.stream().filter((r) -> r.canHandle(type)).findFirst();
      return impl.orElse(new PlainText());
    }

  }
}
