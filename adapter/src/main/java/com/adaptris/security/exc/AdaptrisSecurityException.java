/*
 * $Id: AdaptrisSecurityException.java,v 1.2 2006/09/27 19:18:19 lchan Exp $
 */
package com.adaptris.security.exc;

/**
 * This is the root container class for any exceptions that occur in the
 * security library.
 * 
 * @author $Author: lchan $
 */
public class AdaptrisSecurityException extends Exception {

  /**
   * <code>serialVersionUID</code>
   */
  private static final long serialVersionUID = 2006092701L;

  /**
   * @see Exception#Exception()
   * 
   *  
   */
  public AdaptrisSecurityException() {
    super();
  }

  /**
   * @see Exception#Exception(String)
   * 
   *  
   */
  public AdaptrisSecurityException(String s) {
    super(s);
  }

  /**
   * @see Exception#Exception(String, Throwable)
   * 
   *  
   */
  public AdaptrisSecurityException(String s, Throwable t) {
    super(s, t);
  }

  /**
   * @see Exception#Exception(Throwable)
   * 
   *  
   */
  public AdaptrisSecurityException(Throwable t) {
    super(t);
  }

}