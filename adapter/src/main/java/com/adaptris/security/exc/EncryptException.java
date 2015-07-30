/*
 * $Author: lchan $
 * $RCSfile: EncryptException.java,v $
 * $Revision: 1.2 $
 * $Date: 2006/09/27 19:18:19 $
 */
package com.adaptris.security.exc;


/** Wraps any exception encountered during encryption operations.
 * @author lchan
 * @author $Author: lchan $
 */
public class EncryptException extends AdaptrisSecurityException {

  /**
   * <code>serialVersionUID</code>
   */
  private static final long serialVersionUID = 2006092701L;

  /** @see Exception#Exception()
   * 
   *
   */
  public EncryptException() {
    super();
  }

  /** @see Exception#Exception(String)
   * 
   *
   */
  public EncryptException(String s) {
    super(s);
  }

  /** @see Exception#Exception(String, Throwable)
   * 
   *
   */
  public EncryptException(String s, Throwable t) {
    super(s, t);
  }

  /** @see Exception#Exception(Throwable)
   * 
   *
   */
  public EncryptException(Throwable t) {
    super(t);
  }

}
