/*
 * $Author: lchan $
 * $RCSfile: FtpConnection.java,v $
 * $Revision: 1.12 $
 * $Date: 2009/07/01 12:55:28 $
 */
package com.adaptris.core.ftp;

import java.io.IOException;

import com.adaptris.ftp.ApacheFtpClientImpl;
import com.adaptris.ftp.CommonsNetFtpClient;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Class containing configuration for both FTP Consumers and producers.
 * 
 * @config ftp-connection
 * @license BASIC
 * @see FtpConnectionImp
 * @author lchan
 * @author $Author: lchan $
 */
@XStreamAlias("ftp-connection")
public class FtpConnection extends FtpConnectionImp {

  private static final String SCHEME_FTP = "ftp";

  public FtpConnection() {
    super();
  }

  public FtpConnection(String uniqueId) {
    this();
    setUniqueId(uniqueId);
  }

  @Override
  protected boolean acceptProtocol(String s) {
    return SCHEME_FTP.equalsIgnoreCase(s);
  }

  @Override
  protected ApacheFtpClientImpl createFtpClient(String remoteHost, int port, int timeoutSecs) throws IOException {
    return new CommonsNetFtpClient(remoteHost, port, timeoutSecs);
  }


}
