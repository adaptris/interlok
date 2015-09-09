package com.adaptris.core.socket;

import java.io.IOException;
import java.net.ServerSocket;

import com.adaptris.annotation.AdvancedConfig;
import com.adaptris.core.AdaptrisConnectionImp;
import com.adaptris.core.CoreException;
import com.adaptris.util.license.License;
import com.adaptris.util.license.License.LicenseType;

/**
 * Basic ConsumeConnection class for the Socket Adapter.
 *
 * @author lchan
 * @author $Author: lchan $
 */
public abstract class ConsumeConnection extends AdaptrisConnectionImp {

  @AdvancedConfig
  private int backlog;
  private int listenPort;
  @AdvancedConfig
  private int timeout;

  /**
   * @see Object#Object()
   *
   *
   */
  public ConsumeConnection() {
    backlog = 256;
    timeout = 6000;
    listenPort = 9000;
  }

  /**
   *
   * @see com.adaptris.core.AdaptrisConnectionImp#closeConnection()
   */
  @Override
  protected void closeConnection() {
    return;
  }

  /**
   *
   * @see com.adaptris.core.AdaptrisConnectionImp#startConnection()
   */
  @Override
  protected void startConnection() throws CoreException {
  }

  /**
   *
   * @see com.adaptris.core.AdaptrisConnectionImp#stopConnection()
   */
  @Override
  protected void stopConnection() {
  }

  /**
   * @see com.adaptris.core.AdaptrisComponent#isEnabled(License)
   */
  @Override
  public boolean isEnabled(License license) throws CoreException {
    return license.isEnabled(LicenseType.Standard);
  }

  /**
   * Create the server socket that will be used by Socket consumers.
   *
   * @return the server socket.
   * @throws IOException wrapping any underlying Exception
   */
  public abstract ServerSocket createServerSocket() throws IOException;

  /**
   * Get the server socket timeout.
   * <p>
   * This timeout controls the amount of time a <code>socket.accept()</code>
   * blocks for
   * </p>
   *
   * @return the timeout.
   */
  public int getServerSocketTimeout() {
    return timeout;
  }

  /**
   * Get the server socket timeout.
   * <p>
   * This timeout controls the amount of time a <code>socket.accept()</code>
   * blocks for
   * </p>
   *
   * @param ms the timeout.
   */
  public void setServerSocketTimeout(int ms) {
    timeout = ms;
  }

  /**
   * Set the listen port.
   *
   * @param port the listen port.
   */
  public void setListenPort(int port) {
    listenPort = port;
  }

  /**
   * Get the listen port.
   *
   * @return the listen port.
   */
  public int getListenPort() {
    return listenPort;
  }

  /**
   * Set the socket backlog for this connection.
   *
   * @param i the backlog.
   * @see ServerSocket#ServerSocket(int, int)
   */
  public void setBacklog(int i) {
    backlog = i;
  }

  /**
   * Get the backlog for this connection.
   *
   * @return the backlog.
   * @see ServerSocket#ServerSocket(int, int)
   */
  public int getBacklog() {
    return backlog;
  }
}
