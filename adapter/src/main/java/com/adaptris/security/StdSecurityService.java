package com.adaptris.security;

import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.cert.Certificate;
import java.util.Hashtable;
import java.util.Map;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.perf4j.aop.Profiled;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adaptris.security.certificate.CertificateHandler;
import com.adaptris.security.certificate.CertificateHandlerFactory;
import com.adaptris.security.exc.AdaptrisSecurityException;
import com.adaptris.security.exc.CertException;
import com.adaptris.security.exc.DecryptException;
import com.adaptris.security.exc.EncryptException;
import com.adaptris.security.exc.KeystoreException;
import com.adaptris.security.exc.VerifyException;
import com.adaptris.security.keystore.Alias;
import com.adaptris.security.keystore.ConfiguredKeystore;
import com.adaptris.security.keystore.KeystoreProxy;
import com.adaptris.security.util.Constants;
import com.adaptris.security.util.SecurityUtil;

/**
 * This is the standard security service, as used by F4F.
 *
 * @see SecurityService
 * @author $Author: lchan $
 */
final class StdSecurityService implements SecurityService {

  private transient Logger logR = LoggerFactory.getLogger(SecurityService.class);;
  private Hashtable<ConfiguredKeystore, KeystoreProxy> keystores;
  private EncryptionAlgorithm alg = null;

  StdSecurityService() {
    keystores = new Hashtable<>();
    return;
  }

  /**
   * @see SecurityService#setEncryptionAlgorithm(EncryptionAlgorithm)
   */
  public void setEncryptionAlgorithm(EncryptionAlgorithm a)
      throws AdaptrisSecurityException {
    alg = a;
    return;
  }

  /**
   * @see SecurityService#registerKeystore(ConfiguredKeystore)
   */
  public void registerKeystore(ConfiguredKeystore keystore)
      throws AdaptrisSecurityException {
    try {
      KeystoreProxy ksh = keystore.asKeystoreProxy();
      keystores.put(keystore, ksh);
      logKeystores();
    }
    catch (Exception e) {
      throw new KeystoreException(e);
    }
    return;
  }

  /**
   * @see SecurityService#removeKeystore(ConfiguredKeystore)
   */
  public void removeKeystore(ConfiguredKeystore k)
      throws AdaptrisSecurityException {
    try {
      keystores.remove(k);
    }
    catch (Exception e) {
      throw new KeystoreException(e);
    }
    return;
  }

  /**
   * @see SecurityService#encrypt(String, Alias, Alias)
   */
  public Output encrypt(String payload, Alias us, Alias them)
      throws AdaptrisSecurityException {
    return this.encrypt(payload.getBytes(), us, them);
  }

  /**
   * @see SecurityService#encrypt(byte[], Alias, Alias)
   */
  @Profiled(tag = "StandardSecurityService.encrypt()", logger = "com.adaptris.perf4j.security.TimingLogger")
  public Output encrypt(byte[] payload, Alias sender, Alias receiver)
      throws AdaptrisSecurityException {

    PrivateKey us = null;
    Output output = null;

    if (alg == null) {
      throw new EncryptException("Encryption requires an "
          + "EncryptionAlgorithm object");
    }
    us = getPrivateKey(sender.getAlias(), sender.getAliasPassword());
    CertificateHandler them = createCertificateHandler(getCertificate(receiver
        .getAlias()));
    output = encrypt(payload, us, them);
    return output;
  }

  /**
   * @see SecurityService#sign(String, Alias, Output)
   */
  public Output sign(String payload, Alias us, Output output)
      throws AdaptrisSecurityException {
    return this.sign(payload.getBytes(), us, output);
  }

  /**
   * @see SecurityService#sign(byte[], Alias)
   */
  public Output sign(byte[] payload, Alias ourAlias)
      throws AdaptrisSecurityException {
    return this.sign(payload, ourAlias, null);
  }

  /**
   * @see SecurityService#sign(String, Alias)
   */
  public Output sign(String payload, Alias ourAlias)
      throws AdaptrisSecurityException {
    return this.sign(payload, ourAlias, null);
  }

  /**
   * @see SecurityService#sign(byte[], Alias, Output)
   */
  @Profiled(tag = "StandardSecurityService.sign()", logger = "com.adaptris.perf4j.security.TimingLogger")
  public Output sign(byte[] payload, Alias us, Output output)
      throws AdaptrisSecurityException {

    PrivateKey pk = null;
    StdOutput target = null;
    CertificateHandler ch = null;
    try {
      target = output == null
          ? new StdOutput(Output.PLAIN)
          : (StdOutput) output;
      target.setType(target.getType() | Output.SIGNED);
    }
    catch (ClassCastException e) {
      if(output != null)
        throw new EncryptException("Class " + output.getClass() + " not recognised", e);
      else
        throw new EncryptException("Output null, therefore not recognised", e);
    }
    pk = getPrivateKey(us.getAlias(), us.getAliasPassword());
    ch = createCertificateHandler(getCertificate(us.getAlias()));

    try {
      Signature sig = getSignatureInstance(ch);
      sig.initSign(pk, SecurityUtil.getSecureRandom());
      sig.update(payload);
      target.setSignature(sig.sign());
      target.setDecryptedData(payload);
    }
    catch (Exception e) {
      throw new EncryptException(e);
    }
    return target;
  }

  /**
   * @see SecurityService#verify(String, Alias, Alias)
   */
  public Output verify(String payload, Alias us, Alias them)
      throws AdaptrisSecurityException {
    return this.verify(payload.getBytes(), us, them);
  }

  /**
   * @see SecurityService#verify(byte[], Alias, Alias)
   */
  @Profiled(tag = "StandardSecurityService.verify()", logger = "com.adaptris.perf4j.security.TimingLogger")
  public Output verify(byte[] payload, Alias receiver, Alias sender)
      throws AdaptrisSecurityException {

    StdOutput target = null;
    PrivateKey pk = null;
    CertificateHandler them = null;
    if (alg == null) {
      throw new VerifyException("Decrypt / Verify requires an "
          + "EncryptionAlgorithm object");
    }

    pk = getPrivateKey(receiver.getAlias(), receiver.getAliasPassword());
    them = createCertificateHandler(getCertificate(sender.getAlias()));
    target = decrypt(payload, pk);
    if (!verify(target, them)) {
      throw new VerifyException("Payload signature could not be verified");
    }

    return target;
  }

  private StdOutput decrypt(byte[] payload, PrivateKey pk)
      throws AdaptrisSecurityException {
    StdOutput target = new StdOutput(Output.PLAIN);
    target.split(payload);
    try {
      if (target.getSessionKey() != null) {
        String cipherName = getCipherName(alg.getAlgorithm());
        Cipher keyCipher = Cipher.getInstance(pk.getAlgorithm());/*,
            Constants.SECURITY_PROVIDER);*/
        keyCipher.init(Cipher.DECRYPT_MODE, pk);
        byte[] sessionKeyBytes = keyCipher.doFinal(target.getSessionKey());
        SecretKeyFactory skf = SecretKeyFactory.getInstance(cipherName);/*,
            Constants.SECURITY_PROVIDER);*/

        SecretKeySpec key = new SecretKeySpec(sessionKeyBytes, cipherName);
        SecretKey sessionKey = skf.generateSecret(key);
        Cipher payloadCipher = Cipher.getInstance(alg.getAlgorithm());/*,
            Constants.SECURITY_PROVIDER);*/
        if (target.getSessionVector() != null) {
          IvParameterSpec spec = new IvParameterSpec(target.getSessionVector());
          payloadCipher.init(Cipher.DECRYPT_MODE, sessionKey, spec);
        }
        else {
          payloadCipher.init(Cipher.DECRYPT_MODE, sessionKey);
        }
        target.setDecryptedData(payloadCipher.doFinal(target
            .getEncryptedData(true)));
      }
      else {
        target.setDecryptedData(target.getEncryptedData(true));
      }
    }
    catch (Exception e) {
      throw new DecryptException("Payload could not be decrypted", e);
    }
    return target;
  }

  private boolean verify(StdOutput target, CertificateHandler ch)
      throws AdaptrisSecurityException {
    boolean rc = false;
    try {
      if (target.getSignature() != null) {
        Signature sig = getSignatureInstance(ch);
        sig.initVerify(ch.getPublicKey());
        sig.update(target.getDecryptedData(true));
        rc = sig.verify(target.getSignature());
      }
      else {
        rc = true;
      }
    }
    catch (Exception e) {
      throw new VerifyException("Exception during signature verfication", e);
    }
    return rc;
  }

  private Output encrypt(byte[] payload, PrivateKey pk, CertificateHandler ch)
      throws AdaptrisSecurityException {

    StdOutput output = new StdOutput(Output.ENCRYPTED);
    try {
      KeyGenerator kg = KeyGenerator.getInstance(getCipherName(alg
          .getAlgorithm()));//, Constants.SECURITY_PROVIDER);
      kg.init(alg.getKeyLength(), SecurityUtil.getSecureRandom());
      SecretKey sessionKey = kg.generateKey();
      Cipher dataCipher = Cipher.getInstance(alg.getAlgorithm());/*,
          Constants.SECURITY_PROVIDER);*/
      dataCipher.init(Cipher.ENCRYPT_MODE, sessionKey);
      byte[] encryptedBody = dataCipher.doFinal(payload);
      Cipher keyCipher = Cipher.getInstance(ch.getKeyAlgorithm());/*,
          Constants.SECURITY_PROVIDER);*/
      keyCipher.init(Cipher.ENCRYPT_MODE, ch.getPublicKey(), SecurityUtil
          .getSecureRandom());
      byte[] encryptedSessionKey = keyCipher.doFinal(sessionKey.getEncoded());
      output.setSessionKey(encryptedSessionKey);
      output.setSessionVector(dataCipher.getIV());
      output.setEncryptedData(encryptedBody);
    }
    catch (Exception e) {
      throw new EncryptException(e);
    }
    return output;
  }

  private Certificate getCertificate(String alias)
      throws AdaptrisSecurityException {

    Certificate c = null;
    for (Map.Entry<ConfiguredKeystore,KeystoreProxy> set : keystores.entrySet()) {
      ConfiguredKeystore ksi = set.getKey();
      KeystoreProxy ksm = set.getValue();
      if (ksm.containsAlias(alias)) {
        if (logR.isDebugEnabled()) {
          logR.debug("Certificate Alias " + alias + " found in " + ksi);
        }
        c = ksm.getCertificate(alias);
        break;
      }
    }
    if (c == null) {
      throw new KeystoreException("Alias " + alias
          + " not found in registered keystores");
    }
    return c;
  }

  private PrivateKey getPrivateKey(String alias, char[] password)
      throws AdaptrisSecurityException {

    PrivateKey pk = null;
    for (Map.Entry<ConfiguredKeystore,KeystoreProxy> set : keystores.entrySet()) {
      ConfiguredKeystore ksi = set.getKey();
      KeystoreProxy ksm = set.getValue();
      if (ksm.containsAlias(alias)) {
        pk = ksm.getPrivateKey(alias, password);
        if (logR.isDebugEnabled()) {
          logR.debug("Private key alias " + alias + " found in " + ksi);
        }
        break;
      }
    }
    if (pk == null) {
      throw new KeystoreException("Private Key Alias " + alias
          + " not found in registered keystores");
    }
    return pk;
  }

  private void logKeystores() {
    if (logR.isDebugEnabled() && Constants.DEBUG) {
      StringBuffer sb = new StringBuffer("Registered Keystores :");
      for (ConfiguredKeystore ksi : keystores.keySet()) {
        sb.append("[");
        sb.append(ksi.toString());
        sb.append("]");
      }
    }
  }

  private static String getCipherName(String s) {
    String[] ss = s.split("/");
    return ss[0];
  }

  private static CertificateHandler createCertificateHandler(Certificate c)
      throws AdaptrisSecurityException {

    CertificateHandler h = null;
    if (c == null) {
      throw new CertException("No certificate available");
    }
    try {
      h = CertificateHandlerFactory.getInstance().generateHandler(c);
    }
    catch (Exception e) {
      throw new CertException("Couldn't handle certificate", e);
    }
    if (!h.isValid()) {
      throw new CertException("Invalid Certificate, certificate has either expired or been revoked");
    }
    return h;
  }

  private static Signature getSignatureInstance(CertificateHandler h)
      throws NoSuchAlgorithmException, NoSuchProviderException {

    Signature sig = null;
    try {
      sig = Signature.getInstance(h.getSignatureAlgorithm());/*,
          Constants.SECURITY_PROVIDER);*/
    }
    catch (NoSuchAlgorithmException e) {
      sig = Signature.getInstance(h.getSignatureAlgorithmObjectId());/*,
          Constants.SECURITY_PROVIDER);*/
    }
    return sig;
  }

}