package com.adaptris.core.jdbc.retry;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.apache.commons.lang3.BooleanUtils;

import com.adaptris.annotation.AdapterComponent;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.annotation.DisplayOrder;
import com.adaptris.annotation.InputFieldDefault;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreException;
import com.adaptris.core.NullService;
import com.adaptris.core.Service;
import com.adaptris.core.ServiceException;
import com.adaptris.interlok.InterlokException;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * <p>
 * Service which stores unacknowledged messages for future retry.
 * </p>
 * <p>
 * This class supports both synchronous and asynchronous acknowledgement. This is controlled by the field
 * {@linkplain StoreMessageForRetryServiceTest#setAsynchronousAcknowledgment(boolean)}.
 * </p>
 * <p>
 * A message is deemed to be synchronously acknowledged if the wrapped service completes normally. In such cases it is not added to
 * the retry store. If an exception occurs the message is added to the retry store and normal error handling is not invoked.
 * </p>
 * <p>
 * Where asynchronous acknowledgment is required and a message causes an exception, then the behaviour is determined by the
 * async-auto-retry-on-fail setting. If set to true, then the message is added to the retry store for future retrying and message
 * error handling is not invoked. If set to false, then normal message error handling is invoked. Where no exception occurs the
 * message is added to the retry store to wait for asynchronous acknowledgment.
 * </p>
 * <p>
 * The following metadata keys control the behaviour of the underlying datastore:
 * <ul>
 * <li>{@value com.adaptris.core.jdbc.retry.Constants#ACKNOWLEDGE_ID_KEY} contains the ID that will be used to correlate this
 * message against any asynchronous confirmations; if not specified will default to the messages unique-id</li>
 * <li>{@value com.adaptris.core.jdbc.retry.Constants#RETRY_INTERVAL_KEY} contains the number of milliseconds between each retry
 * attempt; if not specified then this service will fail.</li>
 * <li>{@value com.adaptris.core.jdbc.retry.Constants#RETRIES_KEY} contains the maximum number of retries (-1 for forever); if
 * not specified then this service will fail.</li>
 * </ul>
 * </p>
 */

@XStreamAlias("store-message-for-retry-service")
@AdapterComponent
@ComponentProfile(summary = "Wraps an interlok service and gives the option to store the message in a retry store in the event of an exception.",
    since = "4.9.0", tag = "retry")
@DisplayOrder(order = {"asynchronousAcknowledgment", "asyncAutoRetryOnFail", "retryStore"})
public class StoreMessageForRetryService extends RetryServiceImp {

  // persistent
  @NotNull
  @Valid
  private Service service;
  
  @InputFieldDefault(value = "false")
  private boolean asynchronousAcknowledgment;
  @InputFieldDefault(value = "true")
  private boolean asyncAutoRetryOnFail;

  /**
   * <p>
   * Creates a new instance.
   * </p>
   * <ul>
   * <li>Default service is <code>NullService</code></li>
   * <li>asynchronousAcknowledgment is false</li>
   * <li>asyncAutoRetryOnFail is true</li> </p>
   */
  public StoreMessageForRetryService() {
    super();
    setService(new NullService());
    setAsyncAutoRetryOnFail(true);
  }

  /** @see com.adaptris.core.ServiceImp#start() */
  @Override
  public void start() throws CoreException {
    super.start();
    getService().start();
  }

  /** @see com.adaptris.core.ServiceImp#stop() */
  @Override
  public void stop() {
    getService().stop();
    super.stop();
  }


  /**
   *
   * @see RetryServiceImpTest#performService(com.adaptris.core.AdaptrisMessage)
   */
  @Override
  protected void performService(AdaptrisMessage msg) throws ServiceException {
    if (isAsynchronousAcknowledgment()) {
      handleAsynchronous(msg);
    }
    else {
      handleSynchronous(msg);
    }
  }

  private void handleAsynchronous(AdaptrisMessage msg) throws ServiceException {
    try {
      if (isAsyncAutoRetryOnFail()) {
        insertMessageForRetry(msg);
      }

      getService().doService(msg);

      if (!isAsyncAutoRetryOnFail()) {
        insertMessageForRetry(msg);
      }
    }
    catch (ServiceException e) {
      if (isAsyncAutoRetryOnFail()) {
        log.warn("Inserted " + msg.getUniqueId() + " for retrying and ignoring ServiceException: " + e.getMessage());
      }
      else {
        throw e;
      }
    }
  }

  private void handleSynchronous(AdaptrisMessage msg) throws ServiceException {
    try {
      getService().doService(msg);
    }
    catch (Exception e) { // inc. runtime
      insertMessageForRetry(msg); // store only if msg fails
    }
  }

  private String applyMetadata(AdaptrisMessage msg) throws ServiceException {
    String ackId;
    try {
      msg.addMetadata(Constants.MARSHALLED_SERVICE_KEY, marshaller.marshal(getService()));
      msg.addMetadata(Constants.MARSHALLED_CLASS_NAME_KEY, getService().getClass().getName());
      msg.addMetadata(Constants.ASYNCHRONOUS_KEY, Boolean.valueOf(getAsynchronousAcknowledgment()).toString());
      msg.addMetadata(Constants.ASYNC_AUTO_RETRY, Boolean.valueOf(getAsyncAutoRetryOnFail()).toString());

      ackId = msg.getMetadataValue(Constants.ACKNOWLEDGE_ID_KEY);
      if (ackId == null || ackId.isEmpty()) {
        msg.addMetadata(Constants.ACKNOWLEDGE_ID_KEY, msg.getUniqueId());
        ackId = msg.getUniqueId();
      }
    }
    catch (CoreException e) {
      throw new ServiceException(e);
    }
    return ackId;
  }

  private void insertMessageForRetry(AdaptrisMessage msg) throws ServiceException {
    try {
      String ackId = applyMetadata(msg);
      getRetryStore().write(msg);
      log.debug("Storing [" + ackId + "] for future acknowledgement");

    }
    catch (InterlokException e) {
      log.warn("exception storing message for retry", e);
      throw new ServiceException(e);
    }
  }

  // properties

  /**
   * <p>
   * Returns the <code>Service</code> to use.
   * </p>
   *
   * @return the <code>Service</code> to use
   */
  public Service getService() {
    return service;
  }

  /**
   * Set the service which will initially produce the message to the remote system.
   *
   * @param s the <code>Service</code> to use
   */
  public void setService(Service s) {
    if (s == null) {
      throw new IllegalArgumentException("null param");
    }
    service = s;
  }

  /**
   * <p>
   * Returns true if asynchronous acknowledgment is required, otherwise false.
   * </p>
   *
   * @return true if asynchronous acknowledgment is required, otherwise false
   */
  public boolean getAsynchronousAcknowledgment() {
    return asynchronousAcknowledgment;
  }

  /**
   * <p>
   * Sets whether asynchronous acknowledgment is required.
   * </p>
   *
   * @param b true if asynchronous acknowledgment is required, otherwise false
   */
  public void setAsynchronousAcknowledgment(boolean b) {
    asynchronousAcknowledgment = b;
  }
  
  private boolean isAsynchronousAcknowledgment() {
    return BooleanUtils.toBooleanDefaultIfNull(getAsynchronousAcknowledgment(), false);
  }

  /**
   * @return the retryOnFail
   */
  public boolean getAsyncAutoRetryOnFail() {
    return asyncAutoRetryOnFail;
  }

  /**
   * Flag for setting whether to store asynchronous acknolwedgement messages for retrying rather than invoking normal message error
   * handling.
   *
   * @param b the retryOnFail to set
   */
  public void setAsyncAutoRetryOnFail(boolean b) {
    asyncAutoRetryOnFail = b;
  }
  
  private boolean isAsyncAutoRetryOnFail() {
    return BooleanUtils.toBooleanDefaultIfNull(getAsyncAutoRetryOnFail(), true);
  }

  @Override
  protected void stopService() {
    // TODO Auto-generated method stub
    
  }

}