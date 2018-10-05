/*
 * Copyright 2017 Adaptris Ltd.
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
package com.adaptris.core.http.jetty;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.servlet.http.HttpServletResponse;

import com.adaptris.annotation.InputFieldDefault;
import com.adaptris.core.http.server.HttpStatusProvider;
import com.adaptris.core.http.server.HttpStatusProvider.HttpStatus;
import com.adaptris.util.TimeInterval;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Configure the behaviour that should occur when the max mait time is exceeded
 * 
 * @config jetty-http-timeout-action
 *
 */
@XStreamAlias("jetty-http-timeout-action")
public class TimeoutAction {

  private static final TimeInterval DEFAULT_MAX_WAIT_TIME = new TimeInterval(10L, TimeUnit.MINUTES);

  @InputFieldDefault(value = "10 minutes")
  private TimeInterval maxWaitTime;
  @InputFieldDefault(value = "202 (ACCEPTED)")
  private HttpStatus status;

  public TimeoutAction() {

  }

  public TimeoutAction(TimeInterval ti) {
    this();
    setMaxWaitTime(ti);
  }

  public TimeoutAction(TimeInterval ti, HttpStatus s) {
    this();
    setMaxWaitTime(ti);
    setStatus(s);
  }

  /**
   * @return the maxWaitTime
   */
  public TimeInterval getMaxWaitTime() {
    return maxWaitTime;
  }

  /**
   * Set the max wait time for an individual worker in a workflow to finish.
   * <p>
   * This setting only has an impact if the consumer is the entry point for a {@link com.adaptris.core.PoolingWorkflow} instance. In
   * the event that the wait time is exceeded, then the internal {@link javax.servlet.http.HttpServlet} instance commits the
   * response in its current state and returns control back to the Jetty engine.
   * </p>
   * 
   * @param t the maxWaitTime to set (default 10 minutes)
   */
  public void setMaxWaitTime(TimeInterval t) {
    maxWaitTime = t;
  }


  long maxWaitTime() {
    return getMaxWaitTime() != null ? getMaxWaitTime().toMilliseconds() : DEFAULT_MAX_WAIT_TIME.toMilliseconds();
  }

  public HttpStatusProvider.HttpStatus getStatus() {
    return status;
  }

  int status() {
    return (getStatus() != null ? getStatus() : HttpStatus.ACCEPTED_202).getStatusCode();
  }

  /**
   * Set the HTTP status code to be returned to the client.
   * <p>
   * By default we use {@code 202 Accepted} which seems to fit the semantics of the behaviour; the workflow is just taking a long
   * time (it has not yet failed). The request has been accepted for processing, but the processing has not been completed. The
   * request might or might not eventually be acted upon, as it might be disallowed when processing actually takes places.
   * </p>
   * <p>
   * You can choose something different here; a sensible option might be {@code 500} or {@code 503} if you want to indicate that
   * there is no chance of it succeeding after the timeout has been exceeded.
   * </p>
   * 
   * @param s the status, default is {@code HttpStatus#ACCEPTED_202}
   */
  public void setStatus(HttpStatus s) {
    this.status = s;
  }

  public void handleTimeout(HttpServletResponse response) throws IOException {
    response.setStatus(status());
    response.flushBuffer();
  }

  public void checkTimeout(JettyConsumerMonitor monitor) throws TimeoutException {
    long elapsed = System.currentTimeMillis() - monitor.getStartTime();
    if (elapsed > maxWaitTime()) {
      throw new TimeoutException(elapsed + "ms exceeds configured max [" + maxWaitTime() + "]ms");
    }
  }

}
