package com.adaptris.core.http.jetty.retry;

import java.util.Collections;
import java.util.Map;
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
    return Collections.EMPTY_LIST;
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
}