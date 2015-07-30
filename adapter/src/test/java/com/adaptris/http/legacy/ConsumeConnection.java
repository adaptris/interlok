package com.adaptris.http.legacy;

import java.util.Iterator;

import com.adaptris.core.AdaptrisConnectionImp;
import com.adaptris.core.CoreException;
import com.adaptris.http.Http;
import com.adaptris.http.HttpException;
import com.adaptris.http.Listener;
import com.adaptris.http.RequestProcessor;
import com.adaptris.security.password.Password;
import com.adaptris.util.KeyValuePair;
import com.adaptris.util.KeyValuePairSet;
import com.adaptris.util.license.License;

/**
 * This class is thee base class that all Http Consume Connections extend.
 *
 * @author lchan
 * @author $Author: lchan $
 */
public abstract class ConsumeConnection extends AdaptrisConnectionImp {

  private KeyValuePairSet passwords;
  private transient Listener httpListener;
  private int serverSocketTimeout = Http.DEFAULT_SERVER_SOCKET_TIMEOUT;
  private int socketTimeout = Http.DEFAULT_SOCKET_TIMEOUT;

  /**
   * @see AdaptrisConnectionImp#AdaptrisConnectionImp()
   *
   *
   */
  public ConsumeConnection() {
    super();
    passwords = new KeyValuePairSet();
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
    try {
      httpListener = initialiseListener();

      for (Iterator itr = retrieveMessageConsumers().iterator(); itr.hasNext();) {
        RequestProcessor rp = (RequestProcessor) itr.next();
        httpListener.addRequestProcessor(rp);
      }
      httpListener.start();
    }
    catch (Exception e) {
      throw new CoreException(e);
    }
  }

  /**
   *
   * @see com.adaptris.core.AdaptrisConnectionImp#stopConnection()
   */
  @Override
  protected void stopConnection() {
    try {
      httpListener.stop();
    }
    catch (Exception e) {
      log.trace("Failed to shutdown component cleanly, logging exception for informational purposes only", e);
    }
  }

  /**
   * Set the server socket timeout.
   * <p>
   * This timeout controls the amount of time a <code>socket.accept()</code>
   * blocks for
   * </p>
   *
   * @param i the amount of time in milliseconds.
   * @see Http#DEFAULT_SERVER_SOCKET_TIMEOUT
   */
  public void setServerSocketTimeout(int i) {
    serverSocketTimeout = i;
  }

  /**
   * Return the configured server socket timeout.
   *
   * @return the timeout in ms
   */
  public int getServerSocketTimeout() {
    return serverSocketTimeout;
  }

  /**
   * Set the socket timeout.
   * <p>
   * For any given socket that has been accepted, this is value that is used to
   * control the timeout for operations on the socket.
   *
   * @param i the timeout in milliseconds
   * @see java.net.Socket#setSoTimeout(int)
   * @see Http#DEFAULT_SOCKET_TIMEOUT
   */
  public void setSocketTimeout(int i) {
    socketTimeout = i;
  }

  /**
   * Get the configured socket timeout.
   *
   * @return the timeout in ms.
   */
  public int getSocketTimeout() {
    return socketTimeout;
  }

  /**
   * Initialise the listener from configuration.
   *
   * @return a Listener object that can be started.
   * @throws HttpException on error.
   */
  abstract Listener initialiseListener() throws HttpException;

  /**
   * @see com.adaptris.core.AdaptrisComponent#isEnabled(License)
   */
  @Override
  public boolean isEnabled(License license) throws CoreException {
    return true;
  }

  /**
   * Return the set of configured passwords.
   * <p>
   * In additional to plain text passwords, the passwords can also be encrypted
   * using the appropriate {@link Password}
   * </p>
   *
   * @return the set.
   * @see Password
   */
  public KeyValuePairSet getPasswords() {
    return passwords;
  }

  /**
   * Set the ACL list for this connection.
   * <p>
   * In additional to plain text passwords, the passwords can also be encrypted
   * using the appropriate {@link Password}
   * </p>
   *
   * @param kps a keyvalue pair set that contains user and password combinations
   */
  public void setPasswords(KeyValuePairSet kps) {
    if (kps == null) {
      throw new IllegalArgumentException("Password set cannot be null");
    }
    passwords = kps;
  }

  /**
   * Check that the user and password combination is valid.
   *
   * @param user the username
   * @param password the password
   * @return true if the combination is valid, false otherwise.
   * @see #isValid(KeyValuePair)
   */
  public boolean isValid(String user, String password) {
    return isValid(new KeyValuePair(user, password));
  }

  /**
   * Check that the user and password combination is valid.
   *
   * @param user the <code>KeyValuePair</code> where the key is the user and the
   *          value is the password.
   * @return true if the combination is valid, false otherwise.
   */
  public boolean isValid(KeyValuePair user) {
    boolean rc = false;
    KeyValuePair empty = new KeyValuePair();
    if (passwords == null || passwords.getKeyValuePairs().size() == 0) {
      // No passwords set up means always true.
      return true;
    }
    if (user == null || empty.equals(user)) {
      return false;
    }
    try {
      if (passwords.contains(user)) {
        KeyValuePair verify = passwords.getKeyValuePair(user.getKey());
        String password = Password.decode(verify.getValue());
        if (user.getValue().equals(password)) {
          rc = true;
        }
      }
    }
    catch (Exception e) {
      log.trace("Failed to verify password");
      rc = false;
    }
    return rc;
  }
}
