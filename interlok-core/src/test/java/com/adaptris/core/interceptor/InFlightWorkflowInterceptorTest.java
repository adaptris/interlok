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

package com.adaptris.core.interceptor;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.CoreException;
import com.adaptris.core.PoolingWorkflow;
import com.adaptris.core.WorkflowInterceptor;
import com.adaptris.core.services.WaitService;
import com.adaptris.core.stubs.MockChannel;
import com.adaptris.core.stubs.MockMessageProducer;
import com.adaptris.core.util.LifecycleHelper;
import com.adaptris.interlok.junit.scaffolding.services.ExampleServiceCase;
import com.adaptris.util.TimeInterval;

public class InFlightWorkflowInterceptorTest {

  
  
  @BeforeEach
  public void setUp() {

  }

  @AfterEach
  public void tearDown() {

  }

  @Test
  public void testInterceptor() throws Exception {
    InFlightWorkflowInterceptor interceptor = new InFlightWorkflowInterceptor("testInterceptor");
    final PoolingWorkflow wf = createPoolingWorkflow("workflow", interceptor);
    wf.setPoolSize(1);
    wf.setShutdownWaitTime(new TimeInterval(10L, TimeUnit.SECONDS));
    MockMessageProducer prod = new MockMessageProducer();
    wf.setProducer(prod);
    wf.getServiceCollection().add(new WaitService(new TimeInterval(2L, TimeUnit.SECONDS)));
    MockChannel c = new MockChannel();
    c.getWorkflowList().add(wf);
    try {
      LifecycleHelper.initAndStart(c);
      wf.onAdaptrisMessage(AdaptrisMessageFactory.getDefaultInstance().newMessage());
      assertEquals(1, interceptor.messagesInFlightCount());
      assertEquals(0, interceptor.messagesPendingCount());
      new Thread(new Runnable() {
        @Override
        public void run() {
          wf.onAdaptrisMessage(AdaptrisMessageFactory.getDefaultInstance().newMessage());
        }
      }).start();
      LifecycleHelper.waitQuietly(100);
      assertEquals(1, interceptor.messagesInFlightCount());
      assertEquals(1, interceptor.messagesPendingCount());
      ExampleServiceCase.waitForMessages(prod, 2, 10000);
    } finally {
      LifecycleHelper.stopAndClose(c);
    }
  }

  protected static PoolingWorkflow createPoolingWorkflow(String uid, WorkflowInterceptor... interceptors) throws CoreException {
    PoolingWorkflow wf = new PoolingWorkflow();
    wf.setUniqueId(uid);
    for (WorkflowInterceptor wi : interceptors) {
      wf.addInterceptor(wi);
    }
    return wf;
  }
}
