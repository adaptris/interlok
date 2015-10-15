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

import java.io.IOException;

import com.adaptris.filetransfer.FileTransferClient;
import com.adaptris.filetransfer.FileTransferException;

/**
 * Extension to {@link FileTransferClient} specifically for FTP.
 * 
 * @author lchan
 * 
 */
public interface FtpFileTransferClient extends FileTransferClient {

  /**
   * Connect and login into an account on the FTP server. This completes the entire login process
   * 
   * @param user user name
   * @param password user's password
   * @param account the account
   * @throws FileTransferException if an FTP specific exception occurs
   * @throws IOException if a comms error occurs
   */
  void connect(String user, String password, String account) throws IOException, FileTransferException;
}
