/*
 * Copyright 2015 Adaptris Ltd.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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

  static SftpException wrapException(Throwable t) {
    return wrapException(t.getMessage(), t);
  }

  static SftpException wrapException(String msg, Throwable t) {
    if (t instanceof SftpException) {
      return (SftpException) t;
    }
    return new SftpException(msg, t);
  }
}
