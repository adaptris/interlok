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
