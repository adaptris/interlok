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

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPSClient;
import org.apache.commons.net.util.TrustManagerUtils;

/** FTP/SSL client.
 * 
 * @author lchan
 *
 */
public class CommonsNetFtpSslClient extends ApacheFtpClientImpl<FTPSClient> {

  private static final boolean DEFAULT_IMPLICIT_SSL = false;

  private boolean implicitSSL = DEFAULT_IMPLICIT_SSL;
  

  /**
   * Constructor.
   * 
   * @param remoteHost the remote hostname
   * @param port port for control stream
   * @param timeout the length of the timeout, in milliseconds
   * @throws IOException if a comms error occurs
   */
  public CommonsNetFtpSslClient(String remoteHost, int port, int timeout, boolean implicitSSL) throws IOException {
    super(remoteHost, port, timeout);
    this.implicitSSL = implicitSSL;
  }

  public CommonsNetFtpSslClient(String remoteHost) throws IOException {
    this(remoteHost, FTPClient.DEFAULT_PORT);
  }

  public CommonsNetFtpSslClient(String remoteHost, int port) throws IOException {
    this(remoteHost, port, 0, DEFAULT_IMPLICIT_SSL);
  }

  @Override
  protected FTPSClient createFTPClient() {
    return new FTPSClient(implicitSSL);
  }

  @Override
  protected void preConnectSettings(FTPSClient client) throws IOException {
    client.setTrustManager(TrustManagerUtils.getAcceptAllTrustManager());
    ClientSettings.preConnectSettings(client, ClientSettings.FTP.values(), additionalSettings);
    ClientSettings.preConnectSettings(client, ClientSettings.FTPS.values(), additionalSettings);
  }

  @Override
  protected void postConnectSettings(FTPSClient client) throws IOException {
    // With ImplicitSSL we still need to set the protection level; as by default
    // the data channel is "Clear"...

    // Set protection buffer size
    client.execPBSZ(0);
    // Set data channel protection to private
    client.execPROT("P");
  }


}
