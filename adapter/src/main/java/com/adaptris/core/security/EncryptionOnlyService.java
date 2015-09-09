package com.adaptris.core.security;

import com.adaptris.security.Output;
import com.adaptris.security.exc.AdaptrisSecurityException;
import com.adaptris.security.keystore.Alias;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Perform encryption only.
 * 
 * @config encrypt-only-service
 * @license BASIC
 * @author lchan / $Author: lchan $
 */
@XStreamAlias("encrypt-only-service")
public class EncryptionOnlyService extends EncryptionService {

  /**
   * @see EncryptionService#doEncryption(byte[], Alias)
   */
  @Override
  protected Output doEncryption(byte[] payload, Alias remoteAlias)
      throws AdaptrisSecurityException {
    return retrieveSecurityImplementation().encrypt(payload,
        retrieveLocalPartner(),
        remoteAlias);
  }

}
