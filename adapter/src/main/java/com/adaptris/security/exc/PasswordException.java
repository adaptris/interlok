/*
 * $Author: lchan $
 * $RCSfile: KeystoreException.java,v $
 * $Revision: 1.2 $
 * $Date: 2006/09/27 19:18:19 $
 */
package com.adaptris.security.exc;

/**
 * Wraps any exceptions encountered during Password operations.
 * 
 * @author lchan
 * @author $Author: lchan $
 */
public class PasswordException extends AdaptrisSecurityException {

  /**
   * <code>serialVersionUID</code>
   */
  private static final long serialVersionUID = 2010020801L;

  /** @see Exception#Exception()
   *
   *
   */
  public PasswordException() {
    super();
  }

  /** @see Exception#Exception(String)
   *
   *
   */
  public PasswordException(String s) {
    super(s);
  }

  /** @see Exception#Exception(String, Throwable)
   *
   *
   */
  public PasswordException(String s, Throwable t) {
    super(s, t);
  }

  /** @see Exception#Exception(Throwable)
   *
   *
   */
  public PasswordException(Throwable t) {
    super(t);
  }

}
