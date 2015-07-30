package com.adaptris.core;

import com.adaptris.util.TimeInterval;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * MessageErrorHandler implementation that allows automatic retries for a problem message.
 * 
 * <p>
 * This implementation keeps the {@link AdaptrisMessage} that was consumed in memory, and periodically retries the message in the
 * workflow that failed; the retry schedule and maximum number of retries is determined by {@link #setRetryInterval(TimeInterval)}
 * and {@link #setRetryLimit(Integer)} respectively. If the retry count exceeds the maximum number of retries then the message is
 * deemed to have failed, and passed off to any configured {@link #getProcessingExceptionService()}.
 * </p>
 * <p>
 * In the event that the {@link AdaptrisComponent} that owns this implementation is stopped or closed (using
 * {@link AdaptrisComponent#stop()} or {@link AdaptrisComponent#close()} then all messages are deemed to have failed, and treated as
 * a message that has failed.
 * </p>
 * <p>
 * Note that messages are not guaranteed to be in order (even if they were originally) once a message has entered retry mode. Also
 * note that if a RetryMessageErrorHandler is configured as a direct child of {@link Channel} or {@link Workflow} then an exception
 * that causes a restart of the entire channel will force all messages to 'fail' as this implementation will be stopped as part of
 * the parent component restart.
 * </p>
 * 
 * @config retry-message-error-handler
 */
@XStreamAlias("retry-message-error-handler")
public class RetryMessageErrorHandler extends RetryMessageErrorHandlerImp {

  public RetryMessageErrorHandler() {
    super();
  }

  public RetryMessageErrorHandler(Service... services) {
    this();
    setProcessingExceptionService(new ServiceList(services));
  }

  public RetryMessageErrorHandler(Integer limit, TimeInterval retryInterval, Service... services) {
    this();
    setRetryLimit(limit);
    setRetryInterval(retryInterval);
    setProcessingExceptionService(new ServiceList(services));
  }

}
