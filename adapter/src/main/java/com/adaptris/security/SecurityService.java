package com.adaptris.security;

import com.adaptris.security.exc.AdaptrisSecurityException;
import com.adaptris.security.keystore.Alias;
import com.adaptris.security.keystore.ConfiguredKeystore;
import com.adaptris.security.keystore.KeystoreLocation;

/**
 * Interface for handling encryption, signing and verification requests.
 * <p>
 * Before using this service, there are a number pre-requisites that have to be fulfilled. They are, in no particular order
 * <ul>
 * <li>A keystore containing the local certificate (+key) and the remote party's certificate must exist</li>
 * <li>If a symmetric key algorithm such as DESede is to be used, then it must be agreed with the remote party</li>
 * <li>The Certificates in the keystore must be valid (i.e. not expired and not revoked by the CA</li>
 * </ul>
 * </p>
 * <p>
 * A code example follows
 * </p>
 * 
 * <pre>
 * {@code 
 *    SecurityService s = SecurityFactory.createService();
 *    KeyStoreLocation ksi = KeystoreFactory.getDefault()
 *            .create("file:///testKeyStore?type=JKS", 
 *                    "password".toCharArray());
 *    s.registerKeyStore(ksi);
 *    Alias src = new Alias("myalias", "mypassword");
 *    Alias partner = new Alias("theiralias");
 *    Output target = s.encrypt("abcdefg".getBytes(), src, partner);
 *    System.out.println(target.getAsString());
 * }
 * </pre>
 * 
 * @see KeystoreLocation
 * @author $Author: lchan $
 */
public interface SecurityService {

  /**
   * Set the encryption algorithm.
   * <p>
   * Using this method will override the algorithm that is specified by the
   * partner's public key. Generally, assymmetric keys are considered
   * computationally intensive, so a symmetric key is used to encrypt the
   * payload and this key is encrypted assymmetrically and sent along with the
   * message itself.
   * </p>
   * 
   * @param a
   *          the algorithm to be used for encryption purposes.
   * @see EncryptionAlgorithm
   * @throws AdaptrisSecurityException
   *           wrapping any underlying exception
   */
  void setEncryptionAlgorithm(EncryptionAlgorithm a)
      throws AdaptrisSecurityException;

  /**
   * Register a keystore object for use during encryption sign and verify.
   * <p>
   * If this method is called multiple times, then all keystores that have been
   * registered will be searched for a matching certificate or private key.
   * </p>
   * 
   * @param k
   *          the KeystoreLocation object to register.
   * @see KeystoreLocation
   * @throws AdaptrisSecurityException
   *           wrapping any underlying exception
   */
  void registerKeystore(ConfiguredKeystore k) throws AdaptrisSecurityException;

  /**
   * Remove a keystore from the underlying map.
   * 
   * @param k
   *          the KeystoreLocation object to remove.
   * @throws AdaptrisSecurityException
   *           wrapping any underlying exception
   */
  void removeKeystore(ConfiguredKeystore k) throws AdaptrisSecurityException;

  /**
   * Encrypt the given string into the Output object.
   * 
   * @param payload
   *          the unencrypted payload.
   * @param us
   *          a reference to the alias within the keystore to be used
   * @param partner
   *          a reference to the alias within the keystore to be used
   * @return Output object appropriate for this service type
   * @see Alias
   * @see Output
   * @throws AdaptrisSecurityException
   *           wrapping any exception
   */
  Output encrypt(byte[] payload, Alias us, Alias partner)
      throws AdaptrisSecurityException;

  /**
   * Encrypt the given string into the Output object.
   * <p>
   * This is a convenience method that simply returns <code>
   *  this.encrypt(payload.getBytes(), us, partner)</code>.
   * No attempt is made to handle character encoding, the default is used.
   * 
   * @param payload
   *          the unencrypted payload.
   * @param us
   *          a reference to the alias within the keystore to be used
   * @param partner
   *          a reference to the alias within the keystore to be used
   * @return Output object appropriate for this service type
   * @see Alias
   * @see Output
   * @see #encrypt(byte[], Alias, Alias)
   * @throws AdaptrisSecurityException
   *           wrapping any exception
   */
  Output encrypt(String payload, Alias us, Alias partner)
      throws AdaptrisSecurityException;

  /**
   * Decrypt the given string into the Output object.
   * <p>
   * This is a convenience method that simply returns <code>
   *  this.verify(payload.getBytes(), us, partner)</code>.
   * No attempt is made to handle character encoding, the default is used.
   * </p>
   * 
   * @param payload
   *          the payload in the appropriate format for this
   *          <code>SecurityService </code> instance.
   * @param us
   *          a reference to the alias within the keystore to be used
   * @param partner
   *          a reference to the alias within the keystore to be used
   * @return Output object appropriate for this service type
   * @see Alias
   * @see Output
   * @see #verify(byte[], Alias, Alias)
   * @throws AdaptrisSecurityException
   *           for any exception
   */
  Output verify(String payload, Alias us, Alias partner)
      throws AdaptrisSecurityException;

  /**
   * Decrypt the given bytes into the Output object.
   * <p>
   * This will also verify that the signature (if provided is correct)
   * </p>
   * 
   * @param payload
   *          the payload in the appropriate format for this
   *          <code>SecurityService </code> instance.
   * @param us
   *          a reference to the alias within the keystore to be used
   * @param partner
   *          a reference to the alias within the keystore to be used
   * @return Output object appropriate for this service type
   * @see Alias
   * @see Output
   * @throws AdaptrisSecurityException
   *           for any exception
   */
  Output verify(byte[] payload, Alias us, Alias partner)
      throws AdaptrisSecurityException;

  /**
   * Sign the payload with the private key specified by <code>Alias
   *  </code>.
   * 
   * @param payload
   *          the unencrypted data to be signed.
   * @param us
   *          a reference to the alias within the keystore to be used for
   *          signing
   * @param target
   *          the target which was returned by a previous encryption request
   * @return Output object appropriate for this service type
   * @see Alias
   * @see Output
   * @throws AdaptrisSecurityException
   *           for any exception
   */
  Output sign(byte[] payload, Alias us, Output target)
      throws AdaptrisSecurityException;

  /**
   * Sign the payload with the private key specified by <code>Alias
   *  </code>.
   * <p>
   * This is a convenience method that simply returns <code>
   *  this.sign(payload.getBytes(), us, target)</code>.
   * No attempt is made to handle character encoding, the default is used.
   * </p>
   * 
   * @param payload
   *          the unencrypted data to be signed.
   * @param us
   *          a reference to the alias within the keystore to be used for
   *          signing
   * @param target
   *          the target which was returned by a previous encryption request
   * @return Output object appropriate for this service type
   * @see Alias
   * @see Output
   * @see #sign(byte[],Alias,Output)
   * @throws AdaptrisSecurityException
   *           for any exception
   */
  Output sign(String payload, Alias us, Output target)
      throws AdaptrisSecurityException;

  /**
   * Sign the payload with the private key specified by <code>Alias
   *  </code>.
   * 
   * @param payload
   *          the unencrypted data to be signed.
   * @param us
   *          a reference to the alias within the keystore to be used for
   *          signing
   * @return Output object appropriate for this service type
   * @see Alias
   * @see Output
   * @throws AdaptrisSecurityException
   *           for any exception
   */
  Output sign(byte[] payload, Alias us) throws AdaptrisSecurityException;

  /**
   * Sign the payload with the private key specified by <code>Alias
   *  </code>.
   * <p>
   * This is a convenience method that simply returns <code>
   *  this.sign(payload.getBytes(), us)</code>.
   * No attempt is made to handle character encoding, the default is used.
   * </p>
   * 
   * @param payload
   *          the unencrypted data to be signed.
   * @param us
   *          a reference to the alias within the keystore to be used for
   *          signing
   * @return Output object appropriate for this service type
   * @see Alias
   * @see Output
   * @see #sign(byte[], Alias)
   * @throws AdaptrisSecurityException
   *           for any exception
   */
  Output sign(String payload, Alias us) throws AdaptrisSecurityException;

}