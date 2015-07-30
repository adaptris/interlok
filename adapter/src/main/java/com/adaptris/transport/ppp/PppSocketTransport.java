/*
 * $Author: lchan $
 * $RCSfile: PppSocketTransport.java,v $
 * $Revision: 1.3 $
 * $Date: 2004/02/05 15:32:32 $
 */
package com.adaptris.transport.ppp;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.InterruptedIOException;

import com.adaptris.transport.TcpSocketTransport;
import com.adaptris.transport.TransportException;
import com.adaptris.transport.TransportLayer;

/** This is a generic way of initiating a system command to dial up your ISP
 *  and initiate a socket connection.
 * 
 *  <p>Any invoked system command is expected to complete before control is 
 *  returned to the calling method.  If the exit code of the process defined by 
 *  <code> getConnectCommand(), getDisconnectCommand(), 
 *  getCheckConnectionCommand() </code> is non-zero then an exception is thrown.
 *  </p>
 * 
 * @author lchan
 * @author $Author: lchan $
 */
public class PppSocketTransport extends TcpSocketTransport {

  private String connectCmd;
  private String disconnectCmd;
  private String checkConnectedCmd;
  private String noConnectionFilter;
  private boolean isConnected = false;

  /** @see Object#Object()
   * 
   *
   */
  public PppSocketTransport() {
    super();
  }

  /** Set the connect command to use to dial.
   *  <p>e.g. <code>rasdial "MyAccount" "MyUser" "MyPassword"</code></p>
   * @param s the connect command
   */
  public void setConnectCommand(String s) {
    connectCmd = s;
  }

  /** Return the configured connect command.
   * 
   * @return the connect command.
   */
  public String getConnectCommand() {
    return connectCmd;
  }

  /** Set the connect command to use to dial.
   *  <p>e.g. <code>rasdial /disconnect</code></p>
   * @param s the connect command
   */
  public void setDisconnectCommand(String s) {
    disconnectCmd = s;
  }

  /** Get the configured disconnect command.
   * 
   * @return the command.
   */
  public String getDisconnectCommand() {
    return disconnectCmd;
  }

  /** Set the command for checking a connection.
   *  <p>If this is null or empty, then no checks are performed prior to
   *  attempting the connect command.
   *  </p> 
   * @param s the command to check a connection
   */
  public void setCheckConnectionCommand(String s) {
    checkConnectedCmd = s;
  }

  /** Get the command for checking a connection.
   * 
   * @return the command.
   */
  public String getCheckConnectionCommand() {
    return checkConnectedCmd;
  }

  /** Set the filter for checkig there is no connection present.
   *  <p>If this is null, then no check is performed prior to attempting
   *  a connect command
   *  @see String#matches(String)
   * @param s the filter.
   */
  public void setNoConnectionFilter(String s) {
    noConnectionFilter = s;
  }

  /** Get the connection filter.
   * 
   * @return the filter.
   */
  public String getNoConnectionFilter() {
    return noConnectionFilter;
  }

  /**
   *  @see com.adaptris.transport.Transport#connect()
   */
  public TransportLayer connect()
    throws InterruptedIOException, TransportException, IllegalStateException {
    connectDialer();
    return (super.connect());
  }

  /**  
   *  @see com.adaptris.transport.Transport#listen(int)
   */
  public TransportLayer listen(int timeout)
    throws InterruptedIOException, TransportException, IllegalStateException {

    connectDialer();
    return super.listen(timeout);
  }

  /**
   *  @see com.adaptris.transport.Transport#close()
   */
  public void close() throws TransportException {
    disconnectDialer();
    super.close();
  }

  private void connectDialer() throws TransportException {
    int exitCode = 0;
    if (checkConnected()) {
      return;
    }
    if (isNull(getConnectCommand())) {
      throw new TransportException("No Connect Command available to trigger");
    }
    try {
      logR.trace("Attempting command [" + getConnectCommand() + "]");
      Process process = Runtime.getRuntime().exec(getConnectCommand());
      exitCode = process.waitFor();
    }
    catch (Exception e) {
      throw new TransportException(e.getMessage(), e);
    }
    if (exitCode != 0) {
      throw new TransportException(
        "[" + getConnectCommand() + "] exited with " + exitCode);
    }
    else {
      isConnected = true;
    }
  }

  private void disconnectDialer() throws TransportException {
    int exitCode = 0;
    if (!checkConnected()) {
      return;
    }
    if (isNull(getDisconnectCommand())) {
      // It is valid that we don't need to disconnect, it does it for us
      // after some timeout
      return;
    }
    try {
      logR.trace("Attempting command [" + getDisconnectCommand() + "]");
      Process process = Runtime.getRuntime().exec(getDisconnectCommand());
      exitCode = process.waitFor();

    }
    catch (Exception e) {
      throw new TransportException(e.getMessage(), e);
    }
    if (exitCode != 0) {
      throw new TransportException(
        "[" + getDisconnectCommand() + "] exited with " + exitCode);
    }
    else {
      isConnected = false;
    }
  }

  private boolean isNull(String s) {
    return (s == null || "".equals(s));
  }

  private boolean checkConnected() throws TransportException {
    boolean rc = false;
    int exitCode = 0;
    if (isConnected) {
      return true;
    }
    try {
      logR.trace(
        "Checking Connection State using ["
          + getCheckConnectionCommand()
          + "] matching against ["
          + getNoConnectionFilter()
          + "]");

      if (!isNull(getCheckConnectionCommand())
        && !isNull(getNoConnectionFilter())) {

        Process p = Runtime.getRuntime().exec(getCheckConnectionCommand());
        exitCode = p.waitFor();
        BufferedReader r =
          new BufferedReader(new InputStreamReader(p.getInputStream()));
        String s = r.readLine();
        // logR.trace("Read " + s);
        if (!s.matches(getNoConnectionFilter())) {
          rc = true;
        }
      }
    }
    catch (Exception e) {
      throw new TransportException(e.getMessage(), e);
    }
    if (exitCode != 0) {
      throw new TransportException(
        "[" + getCheckConnectionCommand() + "] exited with " + exitCode);
    }
    return rc;
  }
}
