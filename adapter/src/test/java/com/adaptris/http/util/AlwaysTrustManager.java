/* 
 * $Id: AlwaysTrustManager.java,v 1.2 2003/10/27 07:10:26 lchan Exp $
 */
package com.adaptris.http.util;

import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.X509TrustManager;

/** Always trust manager.
 * <p>The purpose of this class is to implement an X509TrustManager that
 * always returns true, regardless of the situation.
 * <p>Its use is primary during the test phase, when the Certificate Authority
 * or the certificates themselves have not been agreed upon.  It will allow
 * any certificate to be used, ensuring the encryption of the data, but not
 * anything else.
 * @see X509TrustManager
 */
public class AlwaysTrustManager implements X509TrustManager {
  /** Default Constructor.
   */
  public AlwaysTrustManager() {
  }

  /** @see X509TrustManager#getAcceptedIssuers()
   */
  public X509Certificate[] getAcceptedIssuers() {

    X509Certificate[] x = new X509Certificate[0];
    return x;
  }

  /** Check if the client is trusted.
   *  @param chain the certificate chain
   *  @return true if the chain is trusted/
   */
  public boolean isClientTrusted(X509Certificate[] chain) {
    return true;
  }

  /** Check if the server is trusted.
   *  @param chain the certificate chain
   *  @return true if the chain is trusted/
   */
  public boolean isServerTrusted(X509Certificate[] chain) {
    return true;
  }

  /** @see Object#toString()
   */
  public String toString() {
    return "AlwaysTrustManager";
  }

  /** @see X509TrustManager#checkClientTrusted(X509Certificate[], String)
   */
  public void checkClientTrusted(X509Certificate[] x509Certificate, String str)
    throws CertificateException {
    return;
  }

  /** @see X509TrustManager#checkServerTrusted(X509Certificate[], String)
   */
  public void checkServerTrusted(X509Certificate[] x509Certificate, String str)
    throws CertificateException {
    return;
  }
}