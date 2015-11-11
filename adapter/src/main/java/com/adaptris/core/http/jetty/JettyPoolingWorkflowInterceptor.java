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

package com.adaptris.core.http.jetty;

import java.util.Date;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreException;
import com.adaptris.core.interceptor.WorkflowInterceptorImpl;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * WorkflowInterceptor implementation that allows a Jetty Consumer to be part of a {@link PoolingWorkflow}.
 * 
 * @config jetty-pooling-workflow-interceptor
 * 
 */
@XStreamAlias("jetty-pooling-workflow-interceptor")
public class JettyPoolingWorkflowInterceptor extends WorkflowInterceptorImpl {
  static final String MESSAGE_MONITOR = JettyPoolingWorkflowInterceptor.class
      .getCanonicalName() + ".monitor";

  static final String MESSAGE_COMPLETED_PROCESSING = JettyPoolingWorkflowInterceptor.class
      .getCanonicalName() + ".complete";

  public JettyPoolingWorkflowInterceptor() {
    super();
  }

  @Override
  public void init() throws CoreException {
  }

  @Override
  public void start() throws CoreException {
  }

  @Override
  public void stop() {
  }

  @Override
  public void close() {
  }

  @Override
  public void workflowStart(AdaptrisMessage inputMsg) {
  }

  @Override
  public void workflowEnd(AdaptrisMessage inputMsg, AdaptrisMessage outputMsg) {
    if (inputMsg.getObjectMetadata().containsKey(MESSAGE_MONITOR)) {
      JettyConsumerMonitor o = (JettyConsumerMonitor) inputMsg.getObjectMetadata().get(MESSAGE_MONITOR);
      o.setMessageComplete(true);
      o.setEndTime(new Date().getTime());
      synchronized (o) {
        o.notifyAll();
      }
    }
    if (outputMsg.getObjectMetadata().containsKey(MESSAGE_MONITOR)) {
      JettyConsumerMonitor o = (JettyConsumerMonitor) outputMsg.getObjectMetadata().get(MESSAGE_MONITOR);
      o.setMessageComplete(true);
      o.setEndTime(new Date().getTime());
      synchronized (o) {
        o.notifyAll();
      }
    }
  }

}
