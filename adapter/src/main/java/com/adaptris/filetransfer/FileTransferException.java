package com.adaptris.filetransfer;

import java.io.IOException;


/**
 *  Exception encapsulating an file transfer error.
 */
public class FileTransferException extends IOException {


  /**
   * <code>serialVersionUID</code>
   */
  private static final long serialVersionUID = 2006080401L;
  /**
   * 
   * @see Exception#Exception()
   */
  public FileTransferException() {
    super();
  }
  
  /**
   * 
   * @see Exception#Exception(String)
   */
  public FileTransferException(String msg) {
    super(msg);
  }
  
  /**
   * 
   * @see Exception#Exception(String, java.lang.Throwable)
   */
  public FileTransferException(String msg, Throwable t) {
    super(msg);
    super.initCause(t);
  }
  /**
   * 
   * @see Exception#Exception(java.lang.Throwable)
   */
  public FileTransferException(Throwable t) {
    super();
    super.initCause(t);
  }


}
