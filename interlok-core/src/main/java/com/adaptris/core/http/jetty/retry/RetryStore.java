package com.adaptris.core.http.jetty.retry;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.adaptris.core.AdaptrisConnection;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.ComponentLifecycle;
import com.adaptris.core.ComponentLifecycleExtension;
import com.adaptris.core.CoreException;
import com.adaptris.interlok.InterlokException;
import com.adaptris.interlok.cloud.RemoteBlob;

public interface RetryStore extends ComponentLifecycle, ComponentLifecycleExtension {
  /**
   * Report on a list of blobs that is present in the store (optional operation).
   *
   * @implNote The default implementation just returns an empty list.
   */
  default Iterable<RemoteBlob> report() throws InterlokException {
    return Collections.emptyList();
  }

  /**
   * Write a message to the store.
   *
   */
  void write(AdaptrisMessage msg) throws InterlokException;

  /**
   * Retrieve the message id from the store.
   *
   * @implNote The default implementation delegates to {@link #buildForRetry(String, Map)} via
   *           {@link #getMetadata(String)}
   */
  default AdaptrisMessage buildForRetry(String msgId) throws InterlokException {
    return buildForRetry(msgId, getMetadata(msgId));
  }

  /**
   * Retrieve the message id from the store.
   *
   * @implNote The default implementation delegates to
   *           {@link #buildForRetry(String, Map, AdaptrisMessageFactory)} via
   *           {@link #getMetadata(String)}
   *
   */
  default AdaptrisMessage buildForRetry(String msgId, Map<String, String> metadata)
      throws InterlokException {
    return buildForRetry(msgId, metadata, null);
  }


  /**
   * Build the message for retrying from the store.
   *
   * @param msgId the message id.
   * @param metadata the metadata you want to apply to the message
   * @param factory the message factory to use
   * @return a message.
   * @throws CoreException
   */
  AdaptrisMessage buildForRetry(String msgId, Map<String, String> metadata,
      AdaptrisMessageFactory factory) throws InterlokException;

  /**
   * Retrieve the metadata associated with the msgId the store.
   * <p>
   * This is used to assert that the workflow exists in this instance for that message; there is no
   * point building the whole message only to fail because the workflow doesn't exist.
   * </p>
   * 
   * @throws InterlokException if no metadata could be retrieved (e.g the msgId doesn't exist)
   */
  Map<String, String> getMetadata(String msgId) throws InterlokException;


  /**
   * Delete a message from the store (optional operation).
   *
   * @implNote The default implementation throws an instance of
   *           {@link UnsupportedOperationException} and performs no other action.
   */
  default boolean delete(String msgId) throws InterlokException {
    throw new UnsupportedOperationException("delete(String)");
  }

  @Override
  default void prepare() throws CoreException {
  }
  
  /**
   * <p>
   * Acknowledge that the message with the passed ID has now been successfully
   * processed and should not be retried again. NB this method does not throw an
   * Exception if the acknowledge ID does not exist in the store.
   * </p>
   * 
   * @param acknowledgeId the acknowledge ID of the message to acknowledge
   * @throws InterlokException wrapping any <code>Exception</code> which occurs
   */
  void acknowledge(String acknowledgeId) throws InterlokException;
  
  /**
   * Delete any messages that have been successfully Acknowledged.
   *
   * @throws InterlokException wrapping any <code>Exception</code> which occurs
   */
  void deleteAcknowledged() throws InterlokException;
  
  /**
   * <p>
   * Obtain a list of <code>AdaptrisMessage</code>s which meet the expiration
   * criteria.
   * In the most abstract sense, expired messages are those that have exceeded
   * their max retry count but not yet been acknowledged.
   * </p>
   * 
   * @return a list of <code>AdaptrisMessage</code>s which meet the expiration
   * criteria.
   * @implNote The default implementation throws an instance of
   * {@link UnsupportedOperationException} and performs no other action.
   * @throws InterlokException wrapping any <code>Exception</code> which occurs
   */
  default List<AdaptrisMessage> obtainExpiredMessages() throws InterlokException {
    throw new UnsupportedOperationException("Not supported by implementation");
  }
  
  /**
   * <p>
   * Obtain a list of <code>AdaptrisMessage</code>s which meet the criteria
   * for retrying.
   * </p>
   *
   * @return a list of <code>AdaptrisMessage</code>s which meet the criteria
   * for retrying
   * @implNote The default implementation throws an instance of
   * {@link UnsupportedOperationException} and performs no other action.
   * @throws InterlokException wrapping any <code>Exception</code> which occurs
   */
  default List<AdaptrisMessage> obtainMessagesToRetry() throws InterlokException {
    throw new UnsupportedOperationException("Not supported by implementation");
  }
  
  /**
   * <p>
   * Update the number of retries which have taken place for the message with
   * the passed ID. NB this method does not throw an Exception if an attempt is
   * made to update the retry count for a message ID which does not exist in the
   * store.
   * </p>
   *
   * @param messageId the ID of the message to update
   * @throws InterlokException wrapping any <code>Exception</code> which occurs
   */
  void updateRetryCount(String messageId) throws InterlokException;
  
  /**
   * <p>
   * Used for any implementations that have a connected RetryStore
   * </p>
   */
  void makeConnection(AdaptrisConnection connection);

}