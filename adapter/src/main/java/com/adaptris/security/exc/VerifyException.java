package com.adaptris.security.exc;


/** Wraps any exception during verify of signatures.
 * @author lchan
 * @author $Author: lchan $
 */
public class VerifyException extends AdaptrisSecurityException {
  
  /**
   * <code>serialVersionUID</code>
   */
  private static final long serialVersionUID = 2006092701L;

  /** @see Exception#Exception()
   * 
   *
   */
  public VerifyException() {
    super();
  }

  /** @see Exception#Exception(String)
   * 
   *
   */
  public VerifyException(String s) {
    super(s);
  }

  /** @see Exception#Exception(String, Throwable)
   * 
   *
   */
  public VerifyException(String s, Throwable t) {
    super(s, t);
  }

  /** @see Exception#Exception(Throwable)
   * 
   *
   */
  public VerifyException(Throwable t) {
    super(t);
  }

}
