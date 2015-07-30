package com.adaptris.sftp;



/**
 * Interface allowing the adapter to configure the underlying {@link SftpClient} directly with specific behaviour.
 *
 * @author lchan
 *
 */
public interface SftpConnectionBehaviour {

  /**
   * Configure the {@link SftpClient}
   * 
   * @param c the SftpClient
   * @throws SftpException wrapping other exceptions.
   */
  void configure(SftpClient c) throws SftpException;
}
