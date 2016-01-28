/*
 * Copyright 2015 Adaptris Ltd.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/

package com.adaptris.core;

import com.adaptris.annotation.AdapterComponent;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.util.TimeInterval;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * MessageErrorHandler implementation that allows automatic retries for a problem message.
 * 
 * <p>
 * This implementation keeps the {@link com.adaptris.core.AdaptrisMessage} that was consumed in memory, and periodically retries the message in the
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
 * note that if a RetryMessageErrorHandler is configured as a direct child of {@link com.adaptris.core.Channel} or {@link Workflow} then an exception
 * that causes a restart of the entire channel will force all messages to 'fail' as this implementation will be stopped as part of
 * the parent component restart.
 * </p>
 * 
 * @config retry-message-error-handler
 */
@XStreamAlias("retry-message-error-handler")
@AdapterComponent
@ComponentProfile(summary = "An exception handler instance that supports automated retries defined by schedule",
    tag = "error-handling,base")
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
