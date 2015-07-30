/*
 * $Author: lchan $
 * $RCSfile: SftpException.java,v $
 * $Revision: 1.1 $
 * $Date: 2006/08/08 14:43:02 $
 */
package com.adaptris.sftp;

import com.adaptris.filetransfer.FileTransferException;

/** Exception wrapping any specific SFTP Exception.
 * 
 * @author lchan
 * @author $Author: lchan $
 */
public class SftpException extends FileTransferException {

  /**
   * <code>serialVersionUID</code>
   */
  private static final long serialVersionUID = 2006080801L;

  /**
   * @see Exception#Exception(String)
   * 
   */
  public SftpException(String msg) {
    super(msg);
  }

  /**
   * @see Exception#Exception()
   * 
   */
  public SftpException() {
    super();
  }

  /**
   * @see Exception#Exception(Throwable)
   * 
   */
  public SftpException(Throwable t) {
    super(t);
  }

  /**
   * @see Exception#Exception(String, Throwable)
   * 
   */
  public SftpException(String msg, Throwable t) {
    super(msg, t);
  }
}
