package com.adaptris.core.security;

import com.adaptris.security.Output;
import com.adaptris.security.exc.AdaptrisSecurityException;
import com.adaptris.security.keystore.Alias;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Perform encryption and signing.
 * 
 * @config encrypt-and-sign-service
 * @license BASIC
 * @author lchan / $Author: lchan $
 */
@XStreamAlias("encrypt-and-sign-service")
public class EncryptionSigningService extends EncryptionService {

  /**
   * @see EncryptionService#doEncryption(byte[], Alias)
   */
  @Override
  protected Output doEncryption(byte[] payload, Alias remoteAlias)
      throws AdaptrisSecurityException {
    Output output = retrieveSecurityImplementation().encrypt(payload,
        retrieveLocalPartner(), remoteAlias);
    output = retrieveSecurityImplementation().sign(payload,
        retrieveLocalPartner(), output);
    return output;
  }
}
