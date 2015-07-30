/*
 * $RCSfile: SigningService.java,v $
 * $Revision: 1.11 $
 * $Date: 2008/07/30 08:16:46 $
 * $Author: lchan $
 */
package com.adaptris.core.security;

import com.adaptris.security.Output;
import com.adaptris.security.exc.AdaptrisSecurityException;
import com.adaptris.security.keystore.Alias;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Perform Signing.
 * 
 * @config signing-service
 * @license BASIC
 * @author lchan / $Author: lchan $
 */
@XStreamAlias("signing-service")
public class SigningService extends EncryptionService {

  /**
   * @see EncryptionService#doEncryption(byte[], Alias)
   */
  protected Output doEncryption(byte[] payload, Alias remoteAlias)
      throws AdaptrisSecurityException {
    return retrieveSecurityImplementation().sign(payload,
        retrieveLocalPartner());
  }

}
