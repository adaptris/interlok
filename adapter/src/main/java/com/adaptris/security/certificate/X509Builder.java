/*
 * $Author: lchan $
 * $RCSfile: X509Builder.java,v $
 * $Revision: 1.6 $
 * $Date: 2006/10/30 13:35:33 $
 */
package com.adaptris.security.certificate;

import java.io.OutputStream;
import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Random;

import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.X509v3CertificateBuilder;
import org.bouncycastle.jce.provider.X509CertificateObject;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.perf4j.aop.Profiled;

import com.adaptris.security.exc.AdaptrisSecurityException;
import com.adaptris.security.exc.CertException;
import com.adaptris.security.util.SecurityUtil;

/**
 * Concrete implementation of CertificateMaker.
 *
 * @see CertificateMaker
 * @author $Author: lchan $
 */
final class X509Builder implements CertificateBuilder {

  /** The underlying certificate parameters */
  private CertificateParameter certificateParm = null;
  /** The public key */
  private PublicKey publicKey = null;
  /** The private key */
  private PrivateKey privateKey = null;
  /** The certificate */
  private X509Certificate certificate = null;
  /** The random number generator for serial */
  private Random rand = null;

  /** Default Constructor */
  X509Builder() {
    rand = new Random();
  }

  /**
   * @see CertificateBuilder#setCertificateParameters(CertificateParameter)
   */
  public void setCertificateParameters(CertificateParameter cm) {
    certificateParm = cm;
  }

  /**
   * @see CertificateBuilder#getPrivateKey()
   */
  public PrivateKey getPrivateKey() {
    return privateKey;
  }

  /**
   * @see CertificateBuilder#getPublicKey()
   */
  public PublicKey getPublicKey() {
    return publicKey;
  }

  /**
   * @see CertificateBuilder#reset()
   */
  public void reset() {
    certificate = null;
    publicKey = null;
    privateKey = null;
  }

  /**
   * @see CertificateBuilder#createSelfSignedCertificate()
   */
  public Certificate createSelfSignedCertificate()
      throws AdaptrisSecurityException {
    try {
      if (certificate == null) {
        createCertificate();
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
  public void createSelfSignedCertificate(OutputStream output)
      throws AdaptrisSecurityException {
    try {
      if (certificate == null) {
        createCertificate();
      }
      output.write(certificate.getEncoded());
    }
    catch (Exception e) {
      throw new CertException(e);
    }
  }

  /**
   * Create a key pair.
   *
   * @throws NoSuchAlgorithmException if the specified algorithm can't be used
   */
  @Profiled(tag = "CertificateBuilder#createKeyPair()", logger = "com.adaptris.perf4j.security.certificate.TimingLogger")
  private void createKeyPair() throws NoSuchAlgorithmException {

    KeyPairGenerator kpg = KeyPairGenerator.getInstance(certificateParm.getKeyAlgorithm());
    kpg.initialize(certificateParm.getKeySize(), SecurityUtil.getSecureRandom());
    KeyPair kp = kpg.generateKeyPair();
    publicKey = kp.getPublic();
    privateKey = kp.getPrivate();
  }

  /**
   * Create a certificate.
   *
   * @throws NoSuchAlgorithmException if the specified algorithm can't be used
   * @throws InvalidKeyException if an invalid key was used
   * @throws IllegalArgumentException if an illegal argument was used to create
   *           the certificate
   * @throws CertificateException if the certificate couldn't be created
   */
  @Profiled(tag = "CertificateBuilder#createCertificate()", logger = "com.adaptris.perf4j.security.certificate.TimingLogger")
  private void createCertificate()
      throws NoSuchAlgorithmException, CertificateException, OperatorCreationException {
    if (privateKey == null) {
      this.createKeyPair();
    }

    // The certificate is self-signed, so use the current
    // subject as the issuer
    X500Name name = certificateParm.getSubjectInfo();

    // The certificate is self-signed, do we exactly care what
    // the serial number that uniquely identifies is
    BigInteger serial = BigInteger.valueOf(new Integer(rand.nextInt(10000)).longValue());

    GregorianCalendar valid = new GregorianCalendar();
    Date notBefore = valid.getTime();
    valid.add(Calendar.MONTH, 12);
    Date notAfter = valid.getTime();

    SubjectPublicKeyInfo pubKeyInfo = new SubjectPublicKeyInfo(ASN1Sequence.getInstance(publicKey.getEncoded()));

    X509v3CertificateBuilder certGen = new X509v3CertificateBuilder(name, serial, notBefore, notAfter, name, pubKeyInfo);
    String alg = certificateParm.getSignatureAlgorithm();
    JcaContentSignerBuilder builder = new JcaContentSignerBuilder(alg);
 
    // build and sign the certificate
    X509CertificateHolder certHolder = certGen.build(builder.build(privateKey));

    certificate = new X509CertificateObject(certHolder.toASN1Structure());

    return;
  }
}