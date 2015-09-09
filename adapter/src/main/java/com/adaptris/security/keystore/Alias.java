package com.adaptris.security.keystore;

/**
 * Alias in a keystore.
 * <p>
 * This object is simply a wrapper that holds the information required to access
 * the appropriate certificate / private keys out of the keystore.
 * </p>
 * @author $Author: lchan $
 */
public final class Alias {

  private String alias;
  private char[] aliasPassword;

  /**
   * @see Object#Object()
   * 
   * 
   */
  public Alias() {
  }

  /**
   * Constructor
   * 
   * @param a the alias
   * @param pw the password
   */
  public Alias(String a, String pw) {
    this();
    setKeyStoreAlias(a, pw);
  }

  /**
   * 
   * @param a the alias
   * @param pw the password
   */
  public Alias(String a, char[] pw) {
    this();
    setKeyStoreAlias(a, pw);
  }

  /**
   * Constructor
   * 
   * @param a the alias
   */
  public Alias(String a) {
    this(a, "");
  }

  /**
   * Set the keystore alias entry.
   * 
   * @param a the alias
   * @param pw the password
   */
  public void setKeyStoreAlias(String a, String pw) {
    if (pw != null) {
      setKeyStoreAlias(a, pw.toCharArray());
    } else {
      alias = a;
    }
    return;
  }

  /**
   * Set the keystore alias entry.
   * 
   * @param a the alias
   * @param pw the password
   */
  public void setKeyStoreAlias(String a, char[] pw) {
    alias = a;
    aliasPassword = pw;
    return;
  }

  /**
   * Get the alias associated with this object.
   * 
   * @return the alias
   */
  public String getAlias() {
    return alias;
  }

  /**
   * Get the password associated with this object.
   * 
   * @return the alias password
   */
  public char[] getAliasPassword() {
    return aliasPassword;
  }
}