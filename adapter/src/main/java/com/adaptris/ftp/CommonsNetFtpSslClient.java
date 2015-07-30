package com.adaptris.ftp;

import java.io.IOException;
import java.net.InetAddress;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPSClient;
import org.apache.commons.net.util.TrustManagerUtils;

/** FTP/SSL client.
 * 
 * @author lchan
 *
 */
public class CommonsNetFtpSslClient extends ApacheFtpClientImpl<FTPSClient> {

  /**
   * Constructor.
   * 
   * @param remoteHost the remote hostname
   * @param port port for control stream
   * @param timeout the length of the timeout, in milliseconds
   * @throws IOException if a comms error occurs
   */
  public CommonsNetFtpSslClient(String remoteHost, int port, int timeout) throws IOException {
    super(remoteHost, port, timeout);
  }

  /**
   * @see CommonsNetFtpSslClient#CommonsNetFtpSslClient(String, int, int)
   */
  public CommonsNetFtpSslClient(String remoteHost) throws IOException {
    this(remoteHost, FTPClient.DEFAULT_PORT);
  }

  /**
   * @see CommonsNetFtpSslClient#CommonsNetFtpSslClient(String, int, int)
   */
  public CommonsNetFtpSslClient(String remoteHost, int port) throws IOException {
    this(remoteHost, port, 0);
  }

  /**
   * @see CommonsNetFtpSslClient#CommonsNetFtpSslClient(String, int, int)
   */
  public CommonsNetFtpSslClient(InetAddress remoteAddr) throws IOException {
    this(remoteAddr, FTPClient.DEFAULT_PORT);
  }

  /**
   * @see CommonsNetFtpSslClient#CommonsNetFtpSslClient(String, int, int)
   */
  public CommonsNetFtpSslClient(InetAddress remoteAddr, int port) throws IOException {
    this(remoteAddr.getHostAddress(), port, 0);
  }

  @Override
  protected FTPSClient createFTPClient() {
    FTPSClient ftps = new FTPSClient(false);
    ftps.setTrustManager(TrustManagerUtils.getAcceptAllTrustManager());
    return ftps;
  }

  @Override
  protected void additionalSettings(FTPSClient client) throws IOException {
    // Set protection buffer size
    client.execPBSZ(0);
    // Set data channel protection to private
    client.execPROT("P");
  }

}
