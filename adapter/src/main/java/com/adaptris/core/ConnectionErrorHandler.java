package com.adaptris.core;


/**
 * <p>
 * Implementations of this class encapsualte behaviour that is invoked when an
 * <code>Exception</code> relating to a connection is encountered.
 * </p>
 */
public interface ConnectionErrorHandler extends ComponentLifecycle {

  /**
   * <p>
   * Sets the {@link AdaptrisConnection} to handle errors for.
   * </p>
   *
   * @param connection the <code>AdaptrisConnection</code> to handle errors for
   */
  void registerConnection(AdaptrisConnection connection);

  /**
   * Return this components underlying connection.
   * 
   * @param type the type of connection
   * @return the connection
   */
  <T> T retrieveConnection(Class<T> type);

  /**
   * Is this error handler allowed to work with this error handler.
   *
   * @param ceh other error handler.
   * @return true if the two error handlers can work together.
   */
  boolean allowedInConjunctionWith(ConnectionErrorHandler ceh);

  /**
   * Handle the error.
   *
   */
  void handleConnectionException();

}
