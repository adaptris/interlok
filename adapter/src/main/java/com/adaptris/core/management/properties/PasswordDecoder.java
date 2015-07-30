package com.adaptris.core.management.properties;

import com.adaptris.security.password.Password;

/**
 * Decodes a password using {@link Password#decode(String)}
 * <p>
 * Decodes system properties that are stored with the {password} scheme.
 * </p>
 * <code>
 * <pre>
 sysprop.encrypted={password}PW:AAAAEDNPp8M3xBUiU+goN1cmjBYAAAAQorWHploKWvTb5bmjjgiCWQAAABCa6cnOef76qd67FXsgN4nV
 * </pre>
 * </code>
 * 
 * @author lchan
 * 
 */
public class PasswordDecoder implements Decoder {

  @Override
  public String decode(String value) throws Exception {
    return Password.decode(value);
  }

}
