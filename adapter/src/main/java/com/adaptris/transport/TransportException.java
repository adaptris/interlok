package com.adaptris.transport;

/** The Transport Exception container.
 *  @see Exception
 */
public class TransportException extends Exception {

  /**
   * <code>serialVersionUID</code>
   */
  private static final long serialVersionUID = 2006100601L;

  /** @see Exception#Exception(Throwable)
   * 
   */
  public TransportException(Throwable e) {
    super(e);
  }

  /** @see Exception#Exception(String)
   * 
   */
  public TransportException(String e) {
    super(e);
  }

  /** @see Exception#Exception(String, Throwable)
   * 
   */
  public TransportException(String e, Throwable t) {
    super(e, t);
  }

}