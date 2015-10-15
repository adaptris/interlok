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

package com.adaptris.core.socket;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

import com.adaptris.core.CoreException;
import com.adaptris.util.URLString;
import com.adaptris.util.license.License;
import com.adaptris.util.license.License.LicenseType;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Basic vanilla TCP produce connection.
 * 
 * @config tcp-produce-connection
 * 
 * @license STANDARD
 * @author lchan
 * @author $Author: hfraser $
 */
@XStreamAlias("tcp-produce-connection")
public class TcpProduceConnection extends ProduceConnection {

  @Override
  public boolean isEnabled(License license) throws CoreException {
    return license.isEnabled(LicenseType.Standard);
  }

  /**
   * @see ProduceConnection#createSocket(java.lang.String)
   */
  @Override
  public Socket createSocket(String dest)
    throws IOException, UnsupportedOperationException {
    URLString url = new URLString(dest);
    Socket s = null;
    if ("tcp".equals(url.getProtocol())) {
      s = new Socket();
      InetSocketAddress addr = new InetSocketAddress(url.getHost(), url.getPort());
      s.connect(addr, getSocketTimeout());
    }
    else {
      throw new IOException(
        "Unhandled connection type " + "for TcpProduceConnection");
    }
    s.setSoTimeout(getSocketTimeout());
    return s;
  }

}
