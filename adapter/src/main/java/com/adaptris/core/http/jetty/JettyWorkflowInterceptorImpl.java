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


public abstract class JettyWorkflowInterceptorImpl extends WorkflowInterceptorImpl {

  @Override
  public void init() throws CoreException {
    // Override as required.
  }

  @Override
  public void start() throws CoreException {
    // Override as required.
  }

  @Override
  public void stop() {
    // Override as required.
  }

  @Override
  public void close() {
    // Override as required.
  }

  protected static void endWorkflow(AdaptrisMessage inputMsg, AdaptrisMessage outputMsg) {
    messageComplete(inputMsg);
    if (inputMsg != outputMsg) {
      messageComplete(outputMsg);
    }
  }

  protected static void messageComplete(AdaptrisMessage msg) {
    JettyWrapper wrapper = JettyWrapper.unwrap(msg);
    try {
      wrapper.lock();
      JettyConsumerMonitor o = wrapper.getMonitor();
      o.setMessageComplete(true);
      o.setEndTime(new Date().getTime());
      synchronized (o) {
        o.notifyAll();
      }
    } catch (InterruptedException e) {
    } finally {
      wrapper.unlock();
    }
  }

}
