package com.adaptris.core;

import java.util.concurrent.TimeUnit;

import javax.validation.Valid;

import com.adaptris.util.TimeInterval;

/**
 * Abstract class for connections that have the ability to retry connections.
 *
 * <p>
 * Generally speaking for connections that physically require initialisation
 * such as a connection to a JMS Broker or a Database, it should be possible to
 * configure the connection to retry the connection if it initially fails. For
 * other types of connection such as binding on a socket, this isn't so valid.
 * </p>
 *
 * <p>
 * Previously AdaptrisConnectionImp exposed connectionAttemps and connectionWait
 * however, this class was introduced reduce the number of un-used fields in the
 * connection hierarchy.
 * </p>
 *
 * @see com.adaptris.core.jms.JmsConnection
 * @see com.adaptris.core.jdbc.DatabaseConnection
 * @author lchan
 * @author $Author: lchan $
 *
 */
public abstract class AllowsRetriesConnection extends AdaptrisConnectionImp {

  private Integer connectionAttempts;
  @Valid
  private TimeInterval connectionRetryInterval;
  private static final TimeInterval DEFAULT_CONNECTION_RETRY_INTERVAL = new TimeInterval(1L, TimeUnit.MINUTES);
  private static final Integer DEFAULT_CONNECTION_ATTEMPTS = Integer.valueOf(-1);

  /**
   * Default Constructor with the following default values.
   * <ul>
   * <li>connectionAttempts is -1</li>
   * <li>setConnectionWait is 60000</li>
   * </ul>
   */
  public AllowsRetriesConnection() {
    super();
  }

  /**
   * <p>
   * Sets the number of connection attempts to make. -1 means keep trying
   * indefinitely.
   * </p>
   *
   * @param i the number of connection attempts to make
   */
  public void setConnectionAttempts(Integer i) {
    connectionAttempts = i;
  }

  /**
   * <p>
   * Returns the number of connection attempts to make.
   * </p>
   *
   * @return the number of connection attempts to make
   */
  public Integer getConnectionAttempts() {
    return connectionAttempts;
  }

  public int connectionAttempts() {
    return getConnectionAttempts() != null ? getConnectionAttempts().intValue() : DEFAULT_CONNECTION_ATTEMPTS.intValue();
  }

  /**
   * Returns the wait between connection attempts in milliseconds.
   *
   * @return the connection retry interval in ms.
   */
  public long connectionRetryInterval() {
    return getConnectionRetryInterval() != null ? getConnectionRetryInterval().toMilliseconds() : DEFAULT_CONNECTION_RETRY_INTERVAL
        .toMilliseconds();
  }

  protected String createLoggingStatement(int attempt) {
    StringBuffer result = new StringBuffer();
    result.append("pausing for [");
    result.append(connectionRetryInterval() / 1000);
    result.append("] seconds before connection attempt [");
    result.append(attempt + 1);
    if (connectionAttempts() != -1) {
      result.append("] of [");
      result.append(connectionAttempts());
    }
    result.append("]");
    return result.toString();
  }

  public TimeInterval getConnectionRetryInterval() {
    return connectionRetryInterval;
  }

  /**
   * Set the interval between each retry attempt.
   *
   * @param interval the interval between each retry attempt.
   */
  public void setConnectionRetryInterval(TimeInterval interval) {
    connectionRetryInterval = interval;
  }
}
