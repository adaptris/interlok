/*
 * $Author: lchan $
 * $RCSfile: MailException.java,v $
 * $Revision: 1.3 $
 * $Date: 2006/07/21 13:31:55 $
 */
package com.adaptris.mail;

/** This is the container class for any exceptions that occur
 * 
 * @author lchan
 * @author $Author: lchan $
 */
public class MailException extends Exception {
  
  /**
   * <code>serialVersionUID</code>
   */
  private static final long serialVersionUID = 2006072101L;

  /** @see Exception#Exception() */
  public MailException() {
    super();
  }

  /** @see Exception#Exception(String) */
  public MailException(String s) {
    super(s);
  }

  /** @see Exception#Exception(Throwable) */
  public MailException(Throwable t) {
    super(t);
  }

  /** @see Exception#Exception(String, Throwable) */
  public MailException(String s, Throwable t) {
    super(s, t);
  }
}