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

package com.adaptris.core.ftp;

import java.io.IOException;

import com.adaptris.annotation.AdapterComponent;
import com.adaptris.annotation.AdvancedConfig;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.annotation.DisplayOrder;
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
 * <pre>
 * {@code
      ssl_enable=YES
      allow_anon_ssl=NO
      force_local_data_ssl=NO
      force_local_logins_ssl=NO
      ssl_tlsv1=YES
      ssl_sslv2=NO
      ssl_sslv3=NO
   }
 * </pre> 
 * </pre>
 * </code>
 * </p>
 * <p>
 * <strong>Implicit FTP/SSL connections have not been tested; but can be enabled using {@link #setImplicitSsl(Boolean)}</strong>
 * </p>
 * 
 * @config ftp-ssl-connection
 * 
 */
@XStreamAlias("ftp-ssl-connection")
@AdapterComponent
@ComponentProfile(summary = "Connect to a FTP Server with explicit SSL with a username and password", tag = "connections,ftps")
@DisplayOrder(order = {"defaultUserName", "defaultPassword", "transferType", "ftpDataMode", "defaultControlPort", "implicitSsl"})
public class FtpSslConnection extends FtpConnectionImp {

  private static final String SCHEME_FTPS = "ftps";
  @AdvancedConfig
  private Boolean implicitSsl;

  public FtpSslConnection() {
    super();
  }

  @Override
  protected boolean acceptProtocol(String s) {
    return SCHEME_FTPS.equalsIgnoreCase(s);
  }

  @Override
  protected ApacheFtpClientImpl createFtpClient(String remoteHost, int port, int timeoutSecs) throws IOException {
    return new CommonsNetFtpSslClient(remoteHost, port, timeoutSecs, implicitSSL());
  }

  public Boolean getImplicitSsl() {
    return implicitSsl;
  }

  public void setImplicitSsl(Boolean implicitSsl) {
    this.implicitSsl = implicitSsl;
  }

  private boolean implicitSSL() {
    return getImplicitSsl() != null ? getImplicitSsl().booleanValue() : false;
  }

}
