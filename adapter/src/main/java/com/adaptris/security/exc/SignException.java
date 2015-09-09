package com.adaptris.security.exc;


/** Wraps any exception during signing.
 * @author lchan
 * @author $Author: lchan $
 */
public class SignException extends AdaptrisSecurityException {

  
  /**
   * <code>serialVersionUID</code>
   */
  private static final long serialVersionUID = 2006092701L;

  /** @see Exception#Exception()
   * 
   *
   */
  public SignException() {
    super();
  }

  /** @see Exception#Exception(String)
   * 
   *
   */
  public SignException(String s) {
    super(s);
  }

  /** @see Exception#Exception(String, Throwable)
   * 
   *
   */
  public SignException(String s, Throwable t) {
    super(s, t);
  }

  /** @see Exception#Exception(Throwable)
   * 
   *
   */
  public SignException(Throwable t) {
    super(t);
  }

}
