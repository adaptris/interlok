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

package com.adaptris.core.stubs;

import com.adaptris.core.Channel;
import com.adaptris.core.CoreException;
import com.adaptris.core.DefaultEventHandler;
import com.adaptris.core.EventHandler;
import com.adaptris.core.NullProcessingExceptionHandler;
import com.adaptris.core.Workflow;
import com.adaptris.util.PlainIdGenerator;

public class MockChannel extends Channel {

  private EventHandler eh;
  private int startCount = 0, initCount = 0, stopCount = 0, closeCount = 0;

  public MockChannel() throws Exception {
    super();
    setUniqueId("channel_" + new PlainIdGenerator().create(this));
    setMessageErrorHandler(new NullProcessingExceptionHandler());
  }

  public MockChannel(Workflow... workflows) throws Exception {
    this();
    for (Workflow w : workflows) {
      getWorkflowList().add(w);
    }
  }

  @Override
  public void prepare() throws CoreException {
    if (eh == null) {
      eh = new DefaultEventHandler();
    }
    eh.requestStart();
    registerEventHandler(eh);
    super.prepare();
    registerActiveMsgErrorHandler(getMessageErrorHandler());
    for (Workflow workflow : getWorkflowList()) {
      if (workflow.getMessageErrorHandler() != null) {
        workflow.registerActiveMsgErrorHandler(workflow.getMessageErrorHandler());
      }
      else {
        workflow.registerActiveMsgErrorHandler(getMessageErrorHandler());
      }
    }
  }

  public EventHandler obtainEventHandler() {
    return eh;
  }

  public void setEventHandler(EventHandler evtHandler) {
    this.eh = evtHandler;
  }

  @Override
  public void init() throws CoreException {
    prepare();
    super.init();
    initCount++;
  }

  @Override
  public void start() throws CoreException {
    super.start();
    startCount++;
  }

  @Override
  public void stop() {
    super.stop();
    stopCount++;
  }

  @Override
  public void close() {
    super.close();
    closeCount++;
  }

  public int getStartCount() {
    return startCount;
  }

  public int getInitCount() {
    return initCount;
  }

  public int getStopCount() {
    return stopCount;
  }

  public int getCloseCount() {
    return closeCount;
  }

  public Channel withWorkflow(Workflow w) {
    getWorkflowList().add(w);
    return this;
  }
}
