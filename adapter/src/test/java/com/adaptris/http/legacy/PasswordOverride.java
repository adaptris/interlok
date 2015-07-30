/*
 * $RCSfile: PasswordOverride.java,v $
 * $Revision: 1.1 $
 * $Date: 2007/08/30 17:17:53 $
 * $Author: lchan $
 */
package com.adaptris.http.legacy;

import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adaptris.core.security.PrivateKeyPasswordProvider;
import com.adaptris.security.exc.AdaptrisSecurityException;
import com.adaptris.security.keystore.KeystoreLocation;
import com.adaptris.security.util.Constants;

/**
 * @author lchan
 *
 */
class PasswordOverride {

  private static final Logger log = LoggerFactory.getLogger(PasswordOverride.class);

  private static final String[] CUSTOM_TYPES =
  {
      Constants.KEYSTORE_XMLKEYINFO, Constants.KEYSTORE_PKCS12
  };

  private static final List CUSTOM_KEYSTORES = Arrays.asList(CUSTOM_TYPES);

  static char[] discoverPrivateKeyPassword(KeystoreLocation ksl, PrivateKeyPasswordProvider pkpp) throws AdaptrisSecurityException {
    if (CUSTOM_KEYSTORES.contains(ksl.getKeyStoreType())) {
      log.info("Keystore is a custom XMLKEYINFO or PKCS12 Certificate; treating keystore password as private key password");
      return ksl.getKeystorePassword();
    }
    return pkpp.retrievePrivateKeyPassword();
  }
}
