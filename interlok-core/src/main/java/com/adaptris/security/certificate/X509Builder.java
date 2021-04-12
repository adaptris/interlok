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

import java.io.IOException;
import java.io.OutputStream;
import java.math.BigInteger;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.X509v3CertificateBuilder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import com.adaptris.security.exc.AdaptrisSecurityException;
import com.adaptris.security.exc.CertException;
import com.adaptris.security.util.SecurityUtil;

/**
 * Concrete implementation of CertificateMaker.
 *
 * @see CertificateBuilder
 */
final class X509Builder implements CertificateBuilder {

  private transient CertificateParameter certificateParm = null;
  private transient PublicKey publicKey = null;
  private transient PrivateKey privateKey = null;
  private transient X509Certificate certificate = null;

  X509Builder() {
  }

  /**
   * @see CertificateBuilder#setCertificateParameters(CertificateParameter)
   */
  @Override
  public void setCertificateParameters(CertificateParameter cm) {
    certificateParm = cm;
  }

  /**
   * @see CertificateBuilder#getPrivateKey()
   */
  @Override
  public PrivateKey getPrivateKey() {
    return privateKey;
  }

  /**
   * @see CertificateBuilder#getPublicKey()
   */
  @Override
  public PublicKey getPublicKey() {
    return publicKey;
  }

  /**
   * @see CertificateBuilder#reset()
   */
  @Override
  public void reset() {
    certificate = null;
    publicKey = null;
    privateKey = null;
  }

  /**
   * @see CertificateBuilder#createSelfSignedCertificate()
   */
  @Override
  public Certificate createSelfSignedCertificate()
      throws AdaptrisSecurityException {
    try {
      if (certificate == null) {
        certificate = build();
      }
    }
    catch (Exception e) {
      throw new CertException(e);
    }
    return certificate;
  }

  /**
   * @see CertificateBuilder#createSelfSignedCertificate(OutputStream)
   */
  @Override
  public void createSelfSignedCertificate(OutputStream output)
      throws AdaptrisSecurityException {
    try {
      output.write(createSelfSignedCertificate().getEncoded());
    }
    catch (IOException | CertificateEncodingException e) {
      throw new CertException(e);
    }
  }

  /**
   * Create a key pair.
   *
   * @throws NoSuchAlgorithmException if the specified algorithm can't be used
   */
  private void createKeyPair() throws NoSuchAlgorithmException {

    KeyPairGenerator kpg = KeyPairGenerator.getInstance(certificateParm.getKeyAlgorithm());
    kpg.initialize(certificateParm.getKeySize(), SecurityUtil.getSecureRandom());
    KeyPair kp = kpg.generateKeyPair();
    publicKey = kp.getPublic();
    privateKey = kp.getPrivate();
  }


  private X509Certificate build()
      throws NoSuchAlgorithmException, CertificateException, OperatorCreationException {
    X509Certificate result = null;
    if (privateKey == null) {
      createKeyPair();
    }

    // The certificate is self-signed, so use the current
    // subject as the issuer
    X500Name name = certificateParm.getSubjectInfo();

    // The certificate is self-signed, do we exactly care what
    // the serial number that uniquely identifies is
    BigInteger serial = BigInteger
        .valueOf(Integer.valueOf(SecurityUtil.getSecureRandom().nextInt(10000)).longValue());

    GregorianCalendar valid = new GregorianCalendar();
    Date notBefore = valid.getTime();
    valid.add(Calendar.MONTH, 12);
    Date notAfter = valid.getTime();

    SubjectPublicKeyInfo pubKeyInfo = SubjectPublicKeyInfo.getInstance(ASN1Sequence.getInstance(publicKey.getEncoded()));

    X509v3CertificateBuilder certGen = new X509v3CertificateBuilder(name, serial, notBefore, notAfter, name, pubKeyInfo);
    String alg = certificateParm.getSignatureAlgorithm();
    JcaContentSignerBuilder builder = new JcaContentSignerBuilder(alg);

    // build and sign the certificate
    X509CertificateHolder certHolder = certGen.build(builder.build(privateKey));

    result = new JcaX509CertificateConverter().getCertificate(certHolder);
    // result = new X509CertificateObject(certHolder.toASN1Structure());

    return result;
  }
}
