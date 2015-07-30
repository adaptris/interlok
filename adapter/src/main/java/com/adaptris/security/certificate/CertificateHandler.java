/*
 * $Author: lchan $
 * $RCSfile: CertificateHandler.java,v $
 * $Revision: 1.7 $
 * $Date: 2006/11/02 21:49:13 $
 */
package com.adaptris.security.certificate;

import java.security.PublicKey;
import java.security.cert.Certificate;
import java.util.Calendar;

import com.adaptris.security.exc.AdaptrisSecurityException;

/**
 * A wrapper around a certificate object.
 * <p>
 * This interface exposes some simple methods that can be used to get information out of a certificate
 * </p>
 * <p>
 * A certificate can be imagined as some kind of "digital identity card" attesting that a particular public key belongs to a
 * particular entity. Certificates have a limited period of validity and are digitally signed by some trusted authority.
 * Certificates can be verified by anyone having access to the signing authority public key. Each certification authority has to
 * take care to label every handled certificate with a unique serial number for unequivocally identifying it. Certification
 * authorities also have to maintain certification revocation lists of certificates that heve been expired for some reason and are
 * no longer valid.
 * </p>
 * <p>
 * If the System property defined by {@link com.adaptris.security.util.Constants#IGNORE_REV} is set to true, then any
 * CRLDistribution points in a given certificate are ignored for the purposes of checking validity and revocation status. This
 * behaviour can be overridden by using the appropriate value for {@link #setCheckRevocation(boolean)}
 * </p>
 * 
 * @author $Author: lchan $
 */
public interface CertificateHandler {

  /** Extract the PublicKey from the given Certificate.
   *  @return the Public Key
   */
  PublicKey getPublicKey();

  /**
   * This method extracts the Signature Algorithm from the given Certificate.
   * <p>
   * Generally the signature will be stored in the certificate as a string, with the string representing some specific ASN.1 Object
   * identifier
   * </p>
   * 
   * @return sigAlg e.g. <code>"MD5withRSA"</code>
   */
  String getSignatureAlgorithm();

  /**
   * This method extracts the Signature Algorithm OID from the given Certificate.
   * <p>
   * Sometimes it is possible to get a signature algorithm name that is not recognised by the Security Provider. e.g.
   * <code>"MD5withRSAencryption"</code> is not known by the IAIK provider. In instances like this, this method can be used to get
   * the ASN.1 Object Identifier for the signature algorithm. The returned ASN.1 Object Id string can be used with most providers.
   * e.g. <code>1.2.840.10040.4.3</code> would be returned for a <code>"SHA1withDSA"</code> signature algorithm
   * </p>
   * <p>
   * A concise definition of an ASN.1 Object ID is that "It consists of a sequence of integer components and is used for identifying
   * some abstract information object (for instance an algorithm, an attribute type, or even a registration authority that defines
   * other object identifiers)."
   * </p>
   * 
   * @return sigAlg e.g. <code>"1.2.840.10040.4.3"</code>
   */
  String getSignatureAlgorithmObjectId();

  /**
   * Extract the KeyAlgorithm from the certificate.
   * <p>
   * This will return the algorithm type stored in the publickey.
   * </p>
   * 
   * @return the String form of the algorithm type in the public key
   */
  String getKeyAlgorithm();

  /**
   * Check expiry on the certificate.
   * <p>
   * If a certificate is not yet valid, then it should be considered to be expired.
   * </p>
   * 
   * @return true if the certificate has expired
   */
  boolean isExpired();

  /**
   * Check revocation status on the certificate.
   * <p>
   * Check if a certificate has been revoked by the CA
   * </p>
   * <p>
   * This depends on whether the checkRevocation flag has been set and also if there is information available within the certificate
   * to actually get a Certificate revocation list. As revocation lists are held centrally the CA, it is a requirement that the
   * process in question has http access to the host specified in the CRL distribution point.
   * </p>
   * 
   * @return true if the certificate has been revoked, false otherwise
   * @see #setCheckRevocation(boolean)
   * @throws AdaptrisSecurityException on error
   */
  boolean isRevoked() throws AdaptrisSecurityException;

  /**
   * Set the flag for checking revocation.
   * <p>
   * Checking revocation could take some time, so with this option being set ,an {@link #isRevoked()} call will only actually check
   * for revocation on some pre-determined schedule currently set at once a day.{@link #isRevoked()} is implicitly called if an
   * {@link #isValid()} call is made.
   * </p>
   * 
   * @param b true or false
   * @see #isRevoked()
   * @see #isValid()
   * @see #getLastRevocationCheck()
   */
  void setCheckRevocation(boolean b);

  /**
   * Check the overall validity of this certificate.
   * <p>
   * This involves checking expiry and revocation as required.
   * </p>
   * 
   * @return true if the certificate is valid.
   * @throws AdaptrisSecurityException on error
   */
  boolean isValid() throws AdaptrisSecurityException;

  /** Get the certificate contained within this handler for further manual
   *  querying.
   *  @return the certificate
   */
  Certificate getCertificate();

  /** Get the Issuer of this certificate.
   *  @return the issuer
   */
  String getIssuer();

  /** Return a Calendar object that indicates the date that a revocation check
   *  was last performed.
   *  <p>This is dependent on a number of things...
   *  <ul>
   *  <li>setCheckRevocation(true)</li>
   *  <li>isRevoked() having been previously invoked</li>
   *  </ul>
   *  @see #isRevoked()
   *  @see #isValid()
   *  @see #setCheckRevocation(boolean)
   *  @see Calendar
   *  @return a Calendar object, or null if a revocation check was never made.
   */
  Calendar getLastRevocationCheck();
}
