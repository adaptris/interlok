package com.adaptris.core.ftp;

import java.io.IOException;

import com.adaptris.ftp.ApacheFtpClientImpl;
import com.adaptris.ftp.CommonsNetFtpSslClient;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Allows connections to FTP (Explicit) SSL connections.
 * 
 * <p>
 * This was tested against vsftpd (2.0.5) which was configured with the following additional parameters.
 * </p>
 * <p>
 * <code>
 * <pre>
 * ssl_enable=YES
 * allow_anon_ssl=NO
 * force_local_data_ssl=NO
 * force_local_logins_ssl=NO
 * ssl_tlsv1=YES
 * ssl_sslv2=NO
 * ssl_sslv3=NO
 * </pre>
 * </code>
 * </p>
 * <p>
 * <strong>Implicit FTP/SSL connections have not been tested.</strong>
 * </p>
 * 
 * @config ftp-ssl-connection
 * @license BASIC
 */
@XStreamAlias("ftp-ssl-connection")
public class FtpSslConnection extends FtpConnectionImp {

  private static final String SCHEME_FTPS = "ftps";

  public FtpSslConnection() {
    super();
  }

  @Override
  protected boolean acceptProtocol(String s) {
    return SCHEME_FTPS.equalsIgnoreCase(s);
  }

  @Override
  protected ApacheFtpClientImpl createFtpClient(String remoteHost, int port, int timeoutSecs) throws IOException {
    return new CommonsNetFtpSslClient(remoteHost, port, timeoutSecs);
  }

}
