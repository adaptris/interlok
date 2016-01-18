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
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.ftp.ApacheFtpClientImpl;
import com.adaptris.ftp.CommonsNetFtpClient;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Class containing configuration for both FTP Consumers and producers.
 * 
 * @config ftp-connection
 * 
 * @see FtpConnectionImp
 * @author lchan
 * @author $Author: lchan $
 */
@XStreamAlias("ftp-connection")
@AdapterComponent
@ComponentProfile(summary = "Connect to a FTP server using a username and password", tag = "connections,ftp")
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
