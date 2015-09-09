package com.adaptris.security.keystore;

import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adaptris.security.util.SecurityUtil;

/**
 * @author lchan
 * @author $Author: lchan $
 */
abstract class KeystoreLocationImp implements KeystoreLocation {

  protected static Logger logR = LoggerFactory.getLogger(KeystoreLocation.class);
  private char[] keyStorePassword;

  private String keyStoreType;
  private Properties additionalParams;

  /**
   * @see Object#Object()
   *
   *
   */
  public KeystoreLocationImp() {
    SecurityUtil.addProvider();
    setAdditionalParams(new Properties());
  }

  /**
   * Get the type of keystore.
   * <p>
   * Natively jdk1.4 supports the <b>JKS </b> and <b>JCEKS </b> types, <b>JCEKS
   * </b> being more secure.
   * </p>
   * <p>
   * In addition to these two types, we also support the <b>BKS </b> keystore
   * type which is part of the BC JCE implementation
   * </p>
   *
   * @return the keystore type.
   */
  public String getKeyStoreType() {
    return keyStoreType;
  }

  /**
   * Set the keystore type.
   *
   * @see #getKeyStoreType()
   * @param s
   *          the keystore type
   */
  public void setKeystoreType(String s) {
    keyStoreType = s;
  }


  /**
   * Set the keystore password.
   *
   * @param s
   *          the keystore password.
   */
  public void setKeystorePassword(String s) {
    if (s != null) {
      keyStorePassword = s.toCharArray();
    }
  }

  /**
   * Set the keystore password.
   *
   * @param c the keystore password.
   */
  public void setKeystorePassword(char[] c) {
    keyStorePassword = c;
  }

  /**
   * Return the keystore password.
   *
   * @return the keystore password
   */
  public char[] getKeystorePassword() {
    return keyStorePassword;
  }

  /**
   *
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public abstract boolean equals(Object o);

  /**
   *
   * @see java.lang.Object#hashCode()
   */
  @Override
  public abstract int hashCode();

  /**
   * @return the additionalParams
   */
  public Properties getAdditionalParams() {
    return additionalParams;
  }

  /**
   * @param p the additionalParams to set
   */
  public void setAdditionalParams(Properties p) {
    this.additionalParams = p;
  }
}