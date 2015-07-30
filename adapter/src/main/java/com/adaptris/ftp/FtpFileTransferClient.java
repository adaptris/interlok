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
