/*
 * $Author: lchan $
 * $RCSfile: CertificateBuilderFactory.java,v $
 * $Revision: 1.4 $
 * $Date: 2005/08/05 10:08:23 $
 */
package com.adaptris.security.certificate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adaptris.security.util.SecurityUtil;

/** Another factory for building certificates.
 *  <p>Only X.509 certificates are supported, but in the future, maybe more
 *  who knows</p>
 */
public final class CertificateBuilderFactory {

  protected transient static final Logger log = LoggerFactory.getLogger(CertificateBuilderFactory.class.getName());
  
  private static CertificateBuilderFactory f = new CertificateBuilderFactory();

  private CertificateBuilderFactory() {
    SecurityUtil.addProvider();
  }

  /** Get the instance of the CertificateBuilderFactory.
   * 
   * @return a certificate builder.
   */
  public static CertificateBuilderFactory getInstance() {
    return f;
  }

  /** Return the default instance of a Certificatebuilder.
   *  <p>The default instance is one suitable for X.509 certificates
   *  @see CertificateBuilder
   *  @return something to build certificates with
   */
  public CertificateBuilder createBuilder() {
    return new X509Builder();
  }

}