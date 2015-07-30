/*
 * $Author: lchan $
 * $RCSfile: InlineKeystore.java,v $
 * $Revision: 1.5 $
 * $Date: 2006/10/27 11:28:43 $
 */
package com.adaptris.security.keystore;

import java.util.Properties;

import com.adaptris.security.exc.AdaptrisSecurityException;
import com.adaptris.security.util.Constants;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Specifically presents an embedded encoded Certificate string as a KeystoreLocation object.
 * 
 * @config inline-keystore
 * 
 * @author lchan
 * @author $Author: lchan $
 */
@XStreamAlias("inline-keystore")
public class InlineKeystore extends ConfiguredKeystore {

  private String certificate;
  private transient InlineKeystoreLocation keystoreLocation;
  private String type;
  private String alias;

  public InlineKeystore() {
    super();
    setType(Constants.KEYSTORE_XMLKEYINFO);
  }

  /**
   *
   * @see com.adaptris.security.keystore.ConfiguredKeystore#asKeystoreLocation()
   */
  @Override
  public synchronized KeystoreLocation asKeystoreLocation()
      throws AdaptrisSecurityException {
    if (keystoreLocation == null) {
      keystoreLocation = new InlineKeystoreLocation(getCertificate().getBytes());
      keystoreLocation.setKeystoreType(getType());
      keystoreLocation.setKeystorePassword("".toCharArray());
      Properties p = new Properties();
      p.setProperty(Constants.KEYSTORE_ALIAS, getAlias());
      keystoreLocation.setAdditionalParams(p);
    }
    return keystoreLocation;
  }

  /**
   * @return the certificate
   */
  public String getCertificate() {
    return certificate;
  }

  /**
   * @param c the certificate to set
   */
  public void setCertificate(String c) {
    certificate = c;
  }

  /**
   *
   * @see ConfiguredKeystore#equals(java.lang.Object)
   */
  @Override
  public boolean equals(Object o) {
    boolean result = false;
    if (o instanceof InlineKeystore) {
      result = certificate.equals(((InlineKeystore) o).certificate);
    }
    return result;
  }

  /**
   *
   * @see com.adaptris.security.keystore.ConfiguredKeystore#hashCode()
   */
  @Override
  public int hashCode() {
    if (certificate != null) {
      return certificate.hashCode();
    }
    return 0;
  }

  /**
   *
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return "InlineKeystore=[" + alias + "][" + type + "]";
  }

  /**
   * @return the type
   */
  public String getType() {
    return type;
  }

  /**
   * Set the type of keystore this is.
   * <p>
   * Supported types are
   * <ul>
   * <li>XMLKEYINFO</li>
   * <li>X509</li>
   * </ul>
   *
   * @param type the type to set
   */
  public void setType(String type) {
    this.type = type;
  }

  /**
   * @return the alias
   */
  public String getAlias() {
    return alias;
  }

  /**
   * @param alias the alias to set
   */
  public void setAlias(String alias) {
    this.alias = alias;
  }

}
