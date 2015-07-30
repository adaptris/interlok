/*
 * $Author: lchan $
 * $RCSfile: KeystoreException.java,v $
 * $Revision: 1.2 $
 * $Date: 2006/09/27 19:18:19 $
 */
package com.adaptris.security.exc;


/** Wraps any exceptions encountered during keystore operations.
 * @author lchan
 * @author $Author: lchan $
 */
public class KeystoreException extends AdaptrisSecurityException {
  
  /**
   * <code>serialVersionUID</code>
   */
  private static final long serialVersionUID = 2006092701L;

  /** @see Exception#Exception()
   * 
   *
   */
  public KeystoreException() {
    super();
  }

  /** @see Exception#Exception(String)
   * 
   *
   */
  public KeystoreException(String s) {
    super(s);
  }

  /** @see Exception#Exception(String, Throwable)
   * 
   *
   */
  public KeystoreException(String s, Throwable t) {
    super(s, t);
  }

  /** @see Exception#Exception(Throwable)
   * 
   *
   */
  public KeystoreException(Throwable t) {
    super(t);
  }

}
