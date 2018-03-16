/*
 * Copyright 2015 Adaptris Ltd.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/

package com.adaptris.security.certificate;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.NoSuchProviderException;
import java.security.PublicKey;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateFactory;
import java.security.cert.CertificateNotYetValidException;
import java.security.cert.X509Certificate;
import java.util.Calendar;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adaptris.security.exc.AdaptrisSecurityException;
import com.adaptris.security.util.Constants;
import com.adaptris.security.util.SecurityUtil;

/**
 * X509CertificateHandler.
 * <p>
 * A concrete implementation of the Certificate Handler interface capable of handling X.509 certificates.
 * <p>
 * More information on X509 Certificates can be found from <a href="http://www.ietf.org/rfc/rfc2459.txt">RFC2459 </a>
 *
 * @see CertificateHandler
 * @author $Author: lchan $
 */
final class X509Handler implements CertificateHandler {

  private transient Logger logR = LoggerFactory.getLogger(CertificateHandler.class);
  private X509Certificate x509 = null;
  private boolean checkRevocation = false;
  private RevocationService revocationService;

  /** Default constructor */
  private X509Handler() {
    SecurityUtil.addProvider();
    revocationService = RevocationService.getDefaultInstance();
    checkRevocation = !Boolean.valueOf(System.getProperty(Constants.IGNORE_REV, "false")).booleanValue();
  }

  /**
   * Constructor using a pre-existing Certificate object .
   *
   * @param c the Certificate
   * @throws CertificateException if an error was encountered during the parse of the certificate
   * @throws IOException if there was an error reading the cert
   */
  X509Handler(Certificate c) throws CertificateException, IOException {
    this(c.getEncoded());
  }

  /**
   * Constructor using an InputStream .
   * <p>
   * The inputstream is expected to contain the certificate in either DER format or PEM format. The contents of the inputstream is
   * expected to only contain a single certificate.
   *
   * @param input the inputstream containing the certificate
   * @throws CertificateException if an error was encountered during the parse of the certificate
   * @throws IOException if there was an error reading the cert
   */
  X509Handler(InputStream input) throws CertificateException, IOException {
    this();
    x509 = parseCertificate(input);
  }

  /**
   * Constructor using a byte array (DER/PEM) .
   * <p>
   * The byte array is expected to contain the certificate in either DER format or PEM format. The contents of the byte array is
   * expected to only contain a single certificate.
   *
   * @param bytes the bytes representing the certificate
   * @throws CertificateException if an error was encountered during the parse of the certificate
   * @throws IOException if there was an error reading the cert
   */
  X509Handler(byte[] bytes) throws CertificateException, IOException {
    this();
    try (InputStream is = new ByteArrayInputStream(bytes)) {
      x509 = parseCertificate(is);
    }
  }

  private X509Certificate parseCertificate(InputStream input) throws CertificateException, IOException {
    try {
      CertificateFactory certFactory = CertificateFactory.getInstance("X.509", BouncyCastleProvider.PROVIDER_NAME);
      X509Certificate cert = (X509Certificate) certFactory.generateCertificate(input);
      logR.trace("Handling [{}] SerialNumber: {}", cert.getSubjectDN().toString(), cert.getSerialNumber());
      return cert;
      // X509CertParser x509parser = new X509CertParser();
      // x509parser.engineInit(input);
      // return (X509CertificateObject)x509parser.engineRead();
    } catch (NoSuchProviderException e) {
      throw new IOException("Could not parse certificate!", e);
    }
  }

  /**
   * @see CertificateHandler#getPublicKey()
   */
  public PublicKey getPublicKey() {
    return x509.getPublicKey();
  }

  /**
   * @see CertificateHandler#getSignatureAlgorithm()
   */
  public String getSignatureAlgorithm() {
    return x509.getSigAlgName();
  }

  /**
   * @see CertificateHandler#getSignatureAlgorithmObjectId()
   */
  public String getSignatureAlgorithmObjectId() {
    return x509.getSigAlgOID();
  }

  /**
   * @see CertificateHandler#getKeyAlgorithm()
   */
  public String getKeyAlgorithm() {
    return x509.getPublicKey().getAlgorithm();
  }

  /**
   * @see CertificateHandler#isExpired()
   */
  public boolean isExpired() {

    boolean rc = true;

    if (Constants.DEBUG) {
      logR.trace("Checking expiry date on Certificate SerialNumber :{}", x509.getSerialNumber());
    }
    try {
      x509.checkValidity();
      rc = false;
    }
    catch (CertificateExpiredException e) {
      logR.error("SerialNumber : {}; Certificate has Expired", x509.getSerialNumber());
    }
    catch (CertificateNotYetValidException e) {
      logR.error("SerialNumber : {}; Certificate is not yet valid", x509.getSerialNumber());
    }
    return rc;
  }

  /**
   * @see CertificateHandler#isRevoked()
   */
  public boolean isRevoked() throws AdaptrisSecurityException {

    boolean rc = false;
    if (checkRevocation) {
      if (Constants.DEBUG) {
        logR.trace("Checking revocation status on Certificate SerialNumber : {}", x509.getSerialNumber());
      }
      rc = revocationService.isRevoked(x509);
    }
    return rc;
  }

  /**
   * @see CertificateHandler#isValid()
   */
  public boolean isValid() throws AdaptrisSecurityException {
    return !isExpired() && !isRevoked();
  }

  /**
   * @see CertificateHandler#setCheckRevocation(boolean)
   */
  public void setCheckRevocation(boolean b) {
    checkRevocation = b;
  }

  /**
   * @see CertificateHandler#getCertificate()
   */
  public Certificate getCertificate() {
    return x509;
  }

  /**
   * @see CertificateHandler#getIssuer()
   */
  public String getIssuer() {
    return x509.getIssuerDN().getName();
  }

  /**
   * @see CertificateHandler#getLastRevocationCheck()
   */
  public Calendar getLastRevocationCheck() {
    return revocationService.getLastRevocationCheck(x509);
  }
}
