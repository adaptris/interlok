package com.adaptris.ftp;

import java.io.IOException;
import java.net.InetAddress;

import org.apache.commons.net.ftp.FTPClient;

/**
 * FTP Client implementation.#
 *
 * @author dsefton
 *
 */
public class CommonsNetFtpClient extends ApacheFtpClientImpl<FTPClient> {

  /**
   * Constructor.
   *
   * @param remoteHost the remote hostname
   * @param port the port for control stream
   * @param timeout the length of the timeout, in milliseconds
   * @throws IOException if a comms error occurs
   */
  public CommonsNetFtpClient(String remoteHost, int port, int timeout) throws IOException {
    super(remoteHost, port, timeout);
  }

  /**
   * @see CommonsNetFtpClient#CommonsNetFtpClient(String, int, int)
   */
  public CommonsNetFtpClient(String remoteHost) throws IOException {
    this(remoteHost, FTPClient.DEFAULT_PORT);
  }

  /**
   * @see CommonsNetFtpClient#CommonsNetFtpClient(String, int, int)
   */
  public CommonsNetFtpClient(String remoteHost, int port) throws IOException {
    this(remoteHost, port, 0);
  }

  /**
   * @see CommonsNetFtpClient#CommonsNetFtpClient(String, int, int)
   */
  public CommonsNetFtpClient(InetAddress remoteAddr) throws IOException {
    this(remoteAddr, FTPClient.DEFAULT_PORT);
  }

  /**
   * @see CommonsNetFtpClient#CommonsNetFtpClient(String, int, int)
   */
  public CommonsNetFtpClient(InetAddress remoteAddr, int port) throws IOException {
    this(remoteAddr.getHostAddress(), port, 0);
  }

  @Override
  protected FTPClient createFTPClient() {
    return new FTPClient();
  }

  @Override
  protected void additionalSettings(FTPClient client) throws IOException {
  }
}
