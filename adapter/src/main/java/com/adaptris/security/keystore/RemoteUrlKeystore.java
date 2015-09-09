package com.adaptris.security.keystore;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Properties;

import com.adaptris.security.exc.AdaptrisSecurityException;
import com.adaptris.util.stream.StreamUtil;

/**
 * A Keystore that resides on a remote machine.
 *
 * <p>
 * Because this keystore is on a remote machine, it is implicitly read-only.
 * </p>
 *
 * @author lchan
 * @author $Author: lchan $
 */
final class RemoteUrlKeystore extends ReadonlyKeystore {

  private URL keyStoreUrl = null;
  private InputStream urlInput;

  /**
   * @see Object#Object()
   *
   *
   */
  RemoteUrlKeystore() {
    super();
  }

  /**
   * Constructor.
   *
   * @param t the type of keystore
   * @param u the keystore URL
   * @param pw the password to access the keystore
   * @param p any additional properties.
   * @throws IOException if we could not undrestand this keystore.
   */
  RemoteUrlKeystore(String t, String u, char[] pw, Properties p) throws IOException {
    this();
    this.setKeystoreUrl(u);
    this.setKeystoreType(t);
    this.setKeystorePassword(pw);
    setAdditionalParams(p);
  }

  /**
   * Set the keystore URL.
   *
   * @param s The filename
   * @throws IOException if it could not be turned into a url.
   */
  public void setKeystoreUrl(String s) throws IOException {
    keyStoreUrl = new URL(s);
  }

  /**
   * Get tye keystore URL.
   *
   * @return the keystore url.
   */
  public String getKeyStoreUrl() {
    return keyStoreUrl.toExternalForm();
  }

  /** @see Object#toString() */
  @Override
  public String toString() {
    return "[" + getKeyStoreType() + "][" + keyStoreUrl.toString() + "]";
  }

  /**
   * @see com.adaptris.security.keystore.KeystoreLocation#openInput()
   */
  public InputStream openInput() throws IOException, AdaptrisSecurityException {
    if (keyStoreUrl == null) {
      throw new IOException("Keystore URL is null");
    }
    urlInput = urlInput == null ? readUrl(keyStoreUrl) : urlInput;
    return urlInput;
  }

  public boolean exists() {
    try {
      urlInput = urlInput == null ? readUrl(keyStoreUrl) : urlInput;
    }
    catch (IOException e) {
      urlInput = null;
    }
    return urlInput != null;
  }

  /**
   * @see KeystoreLocationImp#equals(java.lang.Object)
   */
  @Override
  public boolean equals(Object o) {
    boolean rc = false;
    if (o instanceof RemoteUrlKeystore) {
      RemoteUrlKeystore rhs = (RemoteUrlKeystore) o;
      rc = getKeyStoreUrl().equals(rhs.getKeyStoreUrl())
          && getKeyStoreType().equals(rhs.getKeyStoreType());
    }
    return rc;
  }

  /**
   * @see KeystoreLocationImp#hashCode()
   */
  @Override
  public int hashCode() {
    return getKeyStoreUrl().hashCode() + getKeyStoreType().hashCode();
  }

  private static InputStream readUrl(URL u) throws IOException {
    URLConnection urlC = u.openConnection();
    InputStream urlIn = urlC.getInputStream();
    InputStream in = StreamUtil.makeCopy(urlIn);
    urlIn.close();
    return in;
  }

}