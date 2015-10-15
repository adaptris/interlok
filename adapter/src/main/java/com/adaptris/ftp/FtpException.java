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

package com.adaptris.ftp;

import com.adaptris.filetransfer.FileTransferException;

/**
 *  FTP specific exceptions
 *
 *  @author     Bruce Blackshaw
 */
public class FtpException extends FileTransferException {


  /**
   * <code>serialVersionUID</code>
   */
  private static final long serialVersionUID = 2006080401L;

  /**
   *  Integer reply code
   */
  private int replyCode = -1;

  /**
   *   Constructor. Delegates to super.
   *
   *   @param   msg   Message that the user will be
   *                  able to retrieve
   */
  public FtpException(String msg) {
    super(msg);
  }

  /**
   *  Constructor. Permits setting of reply code
   *
   *   @param   msg        message that the user will be
   *                       able to retrieve
   *   @param   replyCode  string form of reply code
   */
  public FtpException(String msg, String replyCode) {

    super(msg);

    // extract reply code if possible
    try {
      this.replyCode = Integer.parseInt(replyCode);
    } catch (NumberFormatException ex) {
      this.replyCode = -1;
    }
  }

  /**
   *   Get the reply code if it exists
   *
   *   @return  reply if it exists, -1 otherwise
   */
  public int getReplyCode() {
    return replyCode;
  }

}
