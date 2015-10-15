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
