package com.adaptris.security.exc;


/** Wraps any exception during decryption.
 * @author lchan
 * @author $Author: lchan $
 */
public class DecryptException extends AdaptrisSecurityException {

  /**
   * <code>serialVersionUID</code>
   */
  private static final long serialVersionUID = 2006092701L;
  
  /** @see Exception#Exception()
   * 
   *
   */
  public DecryptException() {
    super();
  }

  /** @see Exception#Exception(String)
   * 
   *
   */
  public DecryptException(String s) {
    super(s);
  }

  /** @see Exception#Exception(String, Throwable)
   * 
   *
   */
  public DecryptException(String s, Throwable t) {
    super(s, t);
  }

  /** @see Exception#Exception(Throwable)
   * 
   *
   */
  public DecryptException(Throwable t) {
    super(t);
  }

}
