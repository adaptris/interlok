package com.adaptris.security.password;

import java.util.ArrayList;

import com.adaptris.core.management.ClasspathInitialiser;
import com.adaptris.security.exc.PasswordException;

/**
 * Handles simple encryption and decryption of passwords that may be stored in XML configuration.
 *
 * @author lchan
 * 
 */
public abstract class Password {

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
   * Alternative password style which is not portable across environments and
   * machines
   * <p>
   * It is not considered especially secure, but is enough to stop casual
   * interrogation
   * </p>
   */
  public static final String NON_PORTABLE_PASSWORD = "ALTPW:";

  private static final String[] STYLES =
  {
      MSCAPI_STYLE, PORTABLE_PASSWORD, NON_PORTABLE_PASSWORD
  };

  private enum Codecs {
    DefaultPassword(PORTABLE_PASSWORD) {
      @Override
      PasswordCodec create() throws PasswordException {
        return new AesCrypto();
      }
    },
    AlternativePassword(NON_PORTABLE_PASSWORD) {
      @Override
      PasswordCodec create() throws PasswordException {
        return new PbeCrypto();
      }
    },
    MicrosoftCrypto(MSCAPI_STYLE) {
      @Override
      PasswordCodec create() throws PasswordException {
        return new MicrosoftCrypto();
      }
    };

    private String thisType;

    Codecs(String s) {
      thisType = s;
    }

    abstract PasswordCodec create() throws PasswordException;

    boolean ofType(String type) {
      return type != null && type.startsWith(thisType);
    }
  }

  /**
   * Create a password implementation of the specified type.
   *
   * @param type the type
   * @return the password implementation
   * @throws Exception
   * @see #MSCAPI_STYLE
   * @see #NON_PORTABLE_PASSWORD
   * @see #PORTABLE_PASSWORD
   */
  public static PasswordCodec create(String type) throws PasswordException {
    PasswordCodec ph = null;
    for (Codecs f : Codecs.values()) {
      if (f.ofType(type)) {
        ph = f.create();
        break;
      }
    }
    if (ph == null) {
      ph = new PlainText();
    }
    return ph;
  }

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
   * @see #NON_PORTABLE_PASSWORD
   * @see #PORTABLE_PASSWORD
   */
  public static String encode(String plain, String type) throws PasswordException {
    return create(type).encode(plain);
  }

  private static void encrypt(String[] argv) throws Exception {
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
    ClasspathInitialiser.init(new ArrayList<String>(), false);
    encrypt(argv);
  }

}
