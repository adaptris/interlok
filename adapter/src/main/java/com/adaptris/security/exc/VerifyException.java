/*
 * $Author: lchan $
 * $RCSfile: VerifyException.java,v $
 * $Revision: 1.2 $
 * $Date: 2006/09/27 19:18:19 $
 */
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
