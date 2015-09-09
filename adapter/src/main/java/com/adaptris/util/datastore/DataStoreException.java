package com.adaptris.util.datastore;


/** DataStoreException.
 *  <p>Container class for all exceptions related to the datastore * </p>
 
 * @author $Author: lchan $
 */
public class DataStoreException extends Exception {
  /**
   * 
   */
  private static final long serialVersionUID = 1L;

  /** @see Exception#Exception(String)
   */
  public DataStoreException(String msg) {
    super(msg);
  }

  /** @see Exception#Exception(String, Throwable)
   */
  public DataStoreException(String s, Throwable t) {
    super(s, t);
  }

  /** @see Exception#Exception(Throwable)
   */
  public DataStoreException(Throwable t) {
    super(t);
  }
}
