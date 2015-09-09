package com.adaptris.security.exc;


/** Wraps any exception encountered during certificate operations.
 * @author lchan
 * @author $Author: lchan $
 */
public class CertException extends AdaptrisSecurityException {
  /**
   * <code>serialVersionUID</code>
   */
  private static final long serialVersionUID = 2006092701L;
  
  /** @see Exception#Exception()
   * 
   *
   */
  public CertException() {
    super();
  }

  /** @see Exception#Exception(String)
   * 
   *
   */
  public CertException(String s) {
    super(s);
  }

  /** @see Exception#Exception(String, Throwable)
   * 
   *
   */
  public CertException(String s, Throwable t) {
    super(s, t);
  }

  /** @see Exception#Exception(Throwable)
   * 
   *
   */
  public CertException(Throwable t) {
    super(t);
  }

}
