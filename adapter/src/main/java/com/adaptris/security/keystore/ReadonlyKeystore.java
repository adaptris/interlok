/*
 * $Author: lchan $
 * $RCSfile: ReadonlyKeystore.java,v $
 * $Revision: 1.1 $
 * $Date: 2005/08/16 21:43:56 $
 */
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