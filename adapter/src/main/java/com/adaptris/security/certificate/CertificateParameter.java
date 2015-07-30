package com.adaptris.security.certificate;

import java.security.NoSuchAlgorithmException;

import org.bouncycastle.asn1.x500.X500Name;

/**
 * Container class with enough information to create a certificate.
 * 
 * <pre>
 * {@code 
 *  CertificateParameter cp = new CertificateParameter();
    X500NameBuilder subject = new X500NameBuilder();

    subject.addRDN(X509ObjectIdentifiers.countryName, "GB");
    subject.addRDN(X509ObjectIdentifiers.stateOrProvinceName, "Middlesex");
    subject.addRDN(X509ObjectIdentifiers.localityName, "Uxbridge");
    subject.addRDN(X509ObjectIdentifiers.organization, "Adaptris");
    subject.addRDN(X509ObjectIdentifiers.organizationalUnitName, "Development");
    subject.addRDN(X509ObjectIdentifiers.commonName, "My Name");
    subject.addRDN(PKCSObjectIdentifiers.pkcs_9_at_emailAddress, "myname@mycompany.com");
    cp.setSignatureAlgorithm("Md5WithRSAencryption");
    cp.setKeyAlgorithm("RSA", 2048);
    cp.setSubjectInfo(subject.build());
   }
 *  </pre>
 * 
 * @see CertificateBuilder
 */
public final class CertificateParameter {

  /** The signature algorithm */
  private String signatureAlgorithm = null;
  /** The key algorithm */
  private String keyAlgorithm = null;
  /** The keysize */
  private int keySize = 0;
  /** The subject Info */
  private X500Name subject;

  /** Default constructor */
  public CertificateParameter() {
  }

  /** Set the signature algorithm for a certificate.
   *  <p>Common certificate signature algorithms are
   *  <code>MD5withRSAencryption, SHA1withRSAEncryption, 
   *  MD4withRSAEncryption</code>
   *  @param sigAlg the signature algorithm to use
   *  @throws NoSuchAlgorithmException if the algorithm is not available
   */
  public void setSignatureAlgorithm(String sigAlg)
  throws NoSuchAlgorithmException {
    signatureAlgorithm = sigAlg;
  }

  /** Set the key algorithm for a certificate.
   *  <p>The most common key algorithm is <code>RSA</code> with a bit size
   *  of either 1024/2048 bits
   *  @param keyAlg the key algorithm to use
   *  @param bits the number of bits for the key algorithm
   *  @throws NoSuchAlgorithmException if the algorithm is not available
   */
  public void setKeyAlgorithm(String keyAlg, int bits)
  throws NoSuchAlgorithmException {
    keyAlgorithm = keyAlg;
    keySize = bits;
  }

  /** Return the signatureAlgorithm.
   *  @return the Signature Algorithm in its core form
   */
  public String getSignatureAlgorithm() {
    return signatureAlgorithm;
  }

  /** Return the keyAlgorithm.
   *  @return the key Algorithm in its core form
   */
  public String getKeyAlgorithm() {
    return keyAlgorithm;
  }

  /** Return the key size.
   *  @return the key size
   */
  public int getKeySize() {
    return keySize;
  }

  /** Set the subject of the certificate.
   *  @param name the subject info for a certificate
   *  @see X500Name
   */
  public void setSubjectInfo(X500Name name) {
    subject = name;
  }

  /** Return the subject.
   *  @return the Subject Info
   */
  public X500Name getSubjectInfo() {
    return subject;
  }

}