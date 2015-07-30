/*
 * $Id: LicenseException.java,v 1.1 2009/04/28 13:20:25 lchan Exp $
 */
package com.adaptris.util.license;


/** Container for exceptions thrown by a license object.
 */
public class LicenseException extends java.lang.Exception {
  /**
   * 
   */
  private static final long serialVersionUID = 1L;

  /** @see Exception#Exception() 
   */
  public LicenseException() {
    super();
  }

  /** @see Exception#Exception(String) 
   */
  public LicenseException(String msg) {
    super(msg);
  }

  /** @see Exception#Exception(String, Throwable) 
   */
  public LicenseException(String msg, Throwable t) {
    super(msg, t);
  }

  /** @see Exception#Exception(Throwable) 
   */
  public LicenseException(Throwable t) {
    super(t);
  }
}
