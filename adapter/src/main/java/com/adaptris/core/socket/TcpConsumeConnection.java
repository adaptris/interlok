package com.adaptris.core.socket;

import java.io.IOException;
import java.net.ServerSocket;

import com.adaptris.core.CoreException;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Vanilla TCP Socket consume connection.
 * 
 * @config tcp-consume-connection
 * 
 * @license STANDARD
 * @author lchan
 * @author $Author: lchan $
 */
@XStreamAlias("tcp-consume-connection")
public class TcpConsumeConnection extends ConsumeConnection {

  /**
   * @see ConsumeConnection#createServerSocket()
   */
  public ServerSocket createServerSocket() throws IOException {
    return new ServerSocket(getListenPort(), getBacklog());
  }

  /**
   * 
   * @see com.adaptris.core.AdaptrisConnectionImp#initConnection()
   */
  @Override
  protected void initConnection() throws CoreException {
  }

}
