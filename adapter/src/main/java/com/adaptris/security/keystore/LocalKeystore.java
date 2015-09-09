package com.adaptris.security.keystore;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;

import com.adaptris.security.exc.AdaptrisSecurityException;

/**
 * A Keystore that resides on the local filesystem.
 *
 * @author lchan
 * @author $Author: lchan $
 */
final class LocalKeystore extends KeystoreLocationImp {

  private File keyStorePath;

  /**
   * @see Object#Object()
   *
   *
   */
  LocalKeystore() {
    super();
  }

  /**
   * Constructor.
   *
   * @param type the type of keystore
   * @param path the keystore file
   * @param password the password to access the keystore
   * @param p any additional properties.
   */
  LocalKeystore(String type, String path, char[] password, Properties p) {
    this();
    this.setKeyStorePath(path);
    this.setKeystoreType(type);
    this.setKeystorePassword(password);
    setAdditionalParams(p);
  }

  /**
   * Set the keystore file.
   *
   * @param s The filename
   */
  public void setKeyStorePath(String s) {
    keyStorePath = new File(s);
  }

  /**
   * Set the keystore file.
   *
   * @param f The File object
   */
  public void setKeyStorePath(File f) {
    keyStorePath = f.getAbsoluteFile();
  }

  /**
   * Return the keystore path.
   *
   * @return the keystore path
   */
  public String getKeyStorePath() {
    return keyStorePath.getAbsolutePath();
  }

  /** @see Object#toString() */
  @Override
  public String toString() {
    return "[" + getKeyStoreType() + "][" + keyStorePath.getAbsolutePath()
        + "]";
  }

  /**
   * @see com.adaptris.security.keystore.KeystoreLocation#openInput()
   */
  public InputStream openInput() throws IOException, AdaptrisSecurityException {
    return new FileInputStream(keyStorePath);
  }

  /**
   * @see com.adaptris.security.keystore.KeystoreLocation#openOutput()
   */
  public OutputStream openOutput()
      throws IOException, AdaptrisSecurityException {
    return new FileOutputStream(keyStorePath);
  }

  /**
   * @see com.adaptris.security.keystore.KeystoreLocation#isWriteable()
   */
  public boolean isWriteable() {
    boolean rc = false;
    if (!keyStorePath.exists()) {
      rc = keyStorePath.getParentFile().canWrite();
    }
    else {
      rc = keyStorePath.isFile() && keyStorePath.canWrite();
    }
    return rc;
  }

  /**
   *
   * @see com.adaptris.security.keystore.KeystoreLocation#exists()
   */
  public boolean exists() {
    return keyStorePath.exists();
  }

  /**
   * @see KeystoreLocationImp#equals(java.lang.Object)
   */
  @Override
  public boolean equals(Object o) {
    boolean rc = false;
    if (o instanceof LocalKeystore) {
      LocalKeystore rhs = (LocalKeystore) o;
      rc = getKeyStorePath().equals(rhs.getKeyStorePath())
          && getKeyStoreType().equalsIgnoreCase(rhs.getKeyStoreType());
    }
    return rc;
  }

  /**
   * @see KeystoreLocationImp#hashCode()
   */
  @Override
  public int hashCode() {
    return getKeyStorePath().hashCode() + getKeyStoreType().hashCode();
  }
}