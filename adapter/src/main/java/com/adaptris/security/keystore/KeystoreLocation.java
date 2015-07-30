/*
 * $Author: lchan $
 * $RCSfile: KeystoreLocation.java,v $
 * $Revision: 1.4 $
 * $Date: 2006/10/18 12:59:54 $
 */
package com.adaptris.security.keystore;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;

import com.adaptris.security.exc.AdaptrisSecurityException;

/**
 * Wrapper that is used handle the IO to a keystore.
 *
 * @author $Author: lchan $
 */
public interface KeystoreLocation {

  /**
   * Open the input stream that this is represented by this object.
   *
   * @return the inputstream containing the keystore
   * @throws IOException if we could not open the keystore
   * @throws AdaptrisSecurityException wrapping any other exception.
   */
  InputStream openInput() throws IOException, AdaptrisSecurityException;

  /**
   * Open an outputstream ready for writing..
   *
   * @return the outputstream to be written
   * @throws IOException if we could not open the keystore
   * @throws AdaptrisSecurityException wrapping any other exception.
   */
  OutputStream openOutput() throws IOException, AdaptrisSecurityException;

  /**
   * Can this keystore be used for updates.
   *
   * @return true if we can write to the keystore.
   */
  boolean isWriteable();

  /**
   * Return the keystore password.
   *
   * @return the keystore password
   */
  char[] getKeystorePassword();

  /**
   * Set the password of the keystore.
   *
   * @param pw the password.
   */
  void setKeystorePassword(char[] pw);

  /**
   * Set the type of keystore.
   * <p>
   * Natively jdk1.4 supports the <b>JKS </b> and <b>JCEKS </b> types, <b>JCEKS
   * </b> being more secure.
   * </p>
   * <p>
   * In addition to these two types, we also support the following types
   * </p>
   * <ul>
   * <li>PKCS12 - where you wish to proxy a single PKCS12 file containing a
   * private key </li>
   * <li>X509 - where you wish to proxy a single file containing a certificate.
   * </li>
   * <li>XmlKeyInfo - where you wish to proxy a XML KeyInfo element that
   * contains certifcate information </li>
   * </ul>
   *
   * @param s the type of keystore.
   */
  void setKeystoreType(String s);

  /**
   * Get the type of keystore.
   *
   * @return the keystore type.
   */
  String getKeyStoreType();

  /**
   * Does the location wrapped by this KeystoreLocation object exist.
   *
   * @return true if the location exists.
   */
  boolean exists();

  /**
   * @return the additionalParams
   */
  public Properties getAdditionalParams();

  /**
   * @param p the additionalParams to set
   */
  public void setAdditionalParams(Properties p);

}