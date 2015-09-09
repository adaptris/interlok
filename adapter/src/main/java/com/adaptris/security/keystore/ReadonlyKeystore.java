package com.adaptris.security.keystore;

import java.io.IOException;
import java.io.OutputStream;

import com.adaptris.security.exc.AdaptrisSecurityException;


/** A Readonly keystore.
 * 
 * @author lchan
 * @author $Author: lchan $
 */
abstract class ReadonlyKeystore extends KeystoreLocationImp {

  /**
   * @see com.adaptris.security.keystore.KeystoreLocation#isWriteable()
   */
  public boolean isWriteable() {
    return false;
  }

  /**
   * @see com.adaptris.security.keystore.KeystoreLocation#openOutput()
   */
  public OutputStream openOutput()
      throws IOException, AdaptrisSecurityException {
    throw new IOException("Cannot open output to ReadOnly keystore");
  }

}