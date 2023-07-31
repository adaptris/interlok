package com.adaptris.core.jdbc.retry;

/**
 * <p>
 * Constants relating to retrying messages.
 * </p>
 */

public final class Constants {

  /**
   * <p>
   * Metadata key for message acknowledgement ID.
   * </p>
   */
  public static final String ACKNOWLEDGE_ID_KEY = "retryAckId";

  /**
   * <p>
   * Metadata key for message retry interval.
   * </p>
   */
  public static final String RETRY_INTERVAL_KEY = "retryAckInterval";

  /**
   * <p>
   * Metadata key for the number of message retries.
   * </p>
   */
  public static final String RETRIES_KEY = "retryRetries";

  /**
   * <p>
   * Metadata key for marshalled <code>Service</code>.
   * </p>
   */
  public static final String MARSHALLED_SERVICE_KEY = "retryService";

  /**
   * <p>
   * Metadata key for marshalled <code>Service</code> class name.
   * </p>
   */
  public static final String MARSHALLED_CLASS_NAME_KEY = "retryServiceClass";

  /**
   * <p>
   * Metadata key indicating whether asynchronous acknowledgment is required.
   * </p>
   */
  public static final String ASYNCHRONOUS_KEY = "retryAsynch";

  /**
   * <p>
   * Metadata key indicating whether we should treat exceptions from the service
   * as normal behaviour.
   * </p>
   */
  public static final String ASYNC_AUTO_RETRY = "retryAsyncRetry";

  protected static final String ACKNOWLEDGED = "T";
  protected static final String NOT_ACKNOWLEDGED = "F";

  private Constants() {
    // no instances
  }
}

