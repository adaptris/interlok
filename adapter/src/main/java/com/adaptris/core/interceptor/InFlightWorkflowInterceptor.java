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

package com.adaptris.core.interceptor;

import static org.apache.commons.lang.StringUtils.isEmpty;

import java.util.concurrent.atomic.AtomicInteger;

import javax.management.MalformedObjectNameException;

import com.adaptris.annotation.AdapterComponent;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.core.AdaptrisComponent;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreException;
import com.adaptris.core.PoolingWorkflow;
import com.adaptris.core.Workflow;
import com.adaptris.core.runtime.ParentRuntimeInfoComponent;
import com.adaptris.core.runtime.RuntimeInfoComponent;
import com.adaptris.core.runtime.RuntimeInfoComponentFactory;
import com.adaptris.core.runtime.WorkflowManager;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * WorkflowInterceptor implementation that exposes acts as the source for {@link MessageInFlightMBean}.
 * 
 * 
 * @config in-flight-workflow-interceptor
 * 
 */
@XStreamAlias("in-flight-workflow-interceptor")
@AdapterComponent
@ComponentProfile(summary = "Interceptor that reports on whether there are in flight messages on this workflow",
    tag = "interceptor")
public class InFlightWorkflowInterceptor extends WorkflowInterceptorImpl {
  
  public static final String UID_SUFFIX = "-InFlight";

  static {
    RuntimeInfoComponentFactory.registerComponentFactory(new JmxFactory());
  }

  private transient AtomicInteger messagesInFlight = new AtomicInteger(0);
  private transient int maxWorkers = 1;

  public InFlightWorkflowInterceptor() {
    super();
  }

  public InFlightWorkflowInterceptor(String uid) {
    this();
    setUniqueId(uid);
  }

  @Override
  public synchronized void workflowStart(AdaptrisMessage inputMsg) {
    messagesInFlight.incrementAndGet();
  }

  @Override
  public synchronized void workflowEnd(AdaptrisMessage inputMsg, AdaptrisMessage outputMsg) {
    messagesInFlight.decrementAndGet();
  }

  int messagesInFlightCount() {
    return Math.min(maxWorkers, messagesInFlight.get());
  }

  int messagesPendingCount() {
    // Pending is the message in flight - the number of workers.
    int inFlight = messagesInFlight.get();
    return inFlight - messagesInFlightCount();
  }

  @Override
  public void init() throws CoreException {
    Workflow w = parentWorkflow();
    if (PoolingWorkflow.class.isAssignableFrom(w.getClass())) {
      maxWorkers = ((PoolingWorkflow) w).poolSize();
    }
  }

  @Override
  public void start() throws CoreException {
    messagesInFlight = new AtomicInteger(0);
  }

  @Override
  public void stop() {
  }

  @Override
  public void close() {}

  private static class JmxFactory extends RuntimeInfoComponentFactory {

    @Override
    protected boolean isSupported(AdaptrisComponent e) {
      if (e != null && e instanceof InFlightWorkflowInterceptor) {
        return !isEmpty(((InFlightWorkflowInterceptor) e).getUniqueId());
      }
      return false;
    }

    @Override
    protected RuntimeInfoComponent createComponent(ParentRuntimeInfoComponent parent, AdaptrisComponent e)
        throws MalformedObjectNameException {
      return new MessageInFlight((WorkflowManager) parent, (InFlightWorkflowInterceptor) e);
    }

  }

}
