/*
 * Copyright 2018 Adaptris Ltd.
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

import static com.adaptris.core.CoreConstants.KEY_WORKFLOW_SKIP_PRODUCER;
import static com.adaptris.core.CoreConstants.STOP_PROCESSING_KEY;
import static com.adaptris.core.CoreConstants.STOP_PROCESSING_VALUE;
import static com.adaptris.core.http.jetty.JettyWorkflowInterceptorImpl.messageComplete;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import javax.servlet.http.HttpServletResponse;

import com.adaptris.annotation.AdapterComponent;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreConstants;
import com.adaptris.core.CoreException;
import com.adaptris.core.PoolingWorkflow;
import com.adaptris.core.Workflow;
import com.adaptris.core.interceptor.WorkflowInterceptorImpl;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * WorkflowInterceptor that automatically returns a 503 if it knows there is nothing available to handle the inbound message in the
 * parent workflow.
 * 
 * <p>
 * Note that this interceptor only works with {@link PoolingWorkflow}; results are undefined when used with other workflows. What
 * actually happens is that if the current message (during workflowStart()) increases the count of messages in flight over the
 * maximum pool size then a {@code 503 Server Busy} is immediately returned. The message is also marked with
 * {@link CoreConstants#STOP_PROCESSING_KEY} and {@link CoreConstants#KEY_WORKFLOW_SKIP_PRODUCER} which will cause the underlying
 * workflow to skip processing the message (effectively the message will be discarded); you will see logging to that effect at
 * TRACE/DEBUG level.
 * </p>
 * 
 * @config jetty-no-backlog-interceptor
 * @since 3.7.3
 */
@XStreamAlias("jetty-no-backlog-interceptor")
@AdapterComponent
@ComponentProfile(summary = "Interceptor that automatically returns a 503 if its parent workflow is busy.",
    tag = "interceptor,http,https", since = "3.7.3")
public class JettyNoBacklogInterceptor extends WorkflowInterceptorImpl {
  private transient AtomicInteger messagesInFlight = new AtomicInteger(0);
  private transient Set<String> messageIdsInFlight = new HashSet<>();
  private transient int maxWorkers = 1;

  public JettyNoBacklogInterceptor() {
    super();
  }

  @Override
  public void init() throws CoreException {
    Workflow w = parentWorkflow();
    if (PoolingWorkflow.class.isAssignableFrom(w.getClass())) {
      maxWorkers = ((PoolingWorkflow) w).poolSize();
    }
    log.trace("503 Server Error will be sent when there are {} messages in flight", maxWorkers);

  }

  @Override
  public void start() throws CoreException {
    messageIdsInFlight = new HashSet<>();
    messagesInFlight = new AtomicInteger(0);
  }

  @Override
  public void stop() {
  }

  @Override
  public void close() {
  }

  @Override
  public void workflowStart(AdaptrisMessage inputMsg) {
    if (messagesInFlight.get() >= maxWorkers) {
      log.trace("Already {} msgs in flight; this exceeds {}, sending 503", messagesInFlight.get(), maxWorkers);
      sendErrorResponse(inputMsg);
      addStopProcessingMarker(inputMsg);
      messageComplete(inputMsg);
    }
    else {
      // keep a track of the inputMsg that we're going to be
      // processing... as we will still be fired for workflowEnd
      messagesInFlight.incrementAndGet();
      messageIdsInFlight.add(inputMsg.getUniqueId());
    }
  }

  @Override
  public void workflowEnd(AdaptrisMessage inputMsg, AdaptrisMessage outputMsg) {
    if (messageIdsInFlight.contains(inputMsg.getUniqueId()) || messageIdsInFlight.contains(outputMsg.getUniqueId())) {
      messageIdsInFlight.remove(inputMsg.getUniqueId());
      messageIdsInFlight.remove(outputMsg.getUniqueId());
      messagesInFlight.decrementAndGet();
    }
  }

  private void sendErrorResponse(AdaptrisMessage msg) {
    JettyWrapper wrapper = JettyWrapper.unwrap(msg);
    try {
      wrapper.lock();
      HttpServletResponse response = wrapper.getResponse();
      if (response == null) {
        return;
      }
      response.sendError(503, "Server Busy");
      response.flushBuffer();
      wrapper.setResponse(null);
    }
    catch (Exception e) {
      log.warn("Caught exception : {}", e.getMessage());
      log.trace("Caught exception :", e);
    } finally {
      wrapper.unlock();
    }
  }

  private void addStopProcessingMarker(AdaptrisMessage msg) {
    msg.addMetadata(STOP_PROCESSING_KEY, STOP_PROCESSING_VALUE);
    msg.addMetadata(KEY_WORKFLOW_SKIP_PRODUCER, STOP_PROCESSING_VALUE);
  }

}
