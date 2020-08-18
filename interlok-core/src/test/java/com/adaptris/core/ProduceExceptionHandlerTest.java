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

import static org.junit.Assert.assertEquals;
import org.junit.Test;
import com.adaptris.core.stubs.MockChannel;
import com.adaptris.core.stubs.MockMessageProducer;


@SuppressWarnings("deprecation")
public class ProduceExceptionHandlerTest extends com.adaptris.interlok.junit.scaffolding.BaseCase {

  @Test
  public void testNullProduceExceptionHandler() throws Exception {
    MockChannel channel = new MockChannel();
    MyStandardWorkflow wf = new MyStandardWorkflow();
    wf.setProducer(new FailingProducer());
    wf.setProduceExceptionHandler(new NullProduceExceptionHandler());
    channel.getWorkflowList().add(wf);
    start(channel);
    wf.onAdaptrisMessage(AdaptrisMessageFactory.getDefaultInstance().newMessage());
    Thread.sleep(1000);
    assertEquals(1, channel.getStartCount());
  }

  @Test
  public void testRestartProduceExceptionHandler() throws Exception {
    MockChannel channel = new MockChannel();
    MyStandardWorkflow wf = new MyStandardWorkflow();
    wf.setProducer(new FailingProducer());
    wf.setProduceExceptionHandler(new RestartProduceExceptionHandler());
    channel.getWorkflowList().add(wf);
    start(channel);
    wf.onAdaptrisMessage(AdaptrisMessageFactory.getDefaultInstance().newMessage());
    Thread.sleep(1000);
    assertEquals(1, channel.getStartCount());
    assertEquals(2, wf.getStartCount());
  }

  @Test
  public void testChannelRestartProduceExceptionHandler() throws Exception {
    MockChannel channel = new MockChannel();
    MyStandardWorkflow wf = new MyStandardWorkflow();
    wf.setProducer(new FailingProducer());
    wf.setProduceExceptionHandler(new ChannelRestartProduceExceptionHandler());
    channel.getWorkflowList().add(wf);
    start(channel);
    wf.onAdaptrisMessage(AdaptrisMessageFactory.getDefaultInstance().newMessage());
    Thread.sleep(1000);
    assertEquals(2, channel.getStartCount());
    assertEquals(2, wf.getStartCount());
  }

  private class FailingProducer extends MockMessageProducer {
    @Override
    protected void doProduce(AdaptrisMessage msg, String endpoint) throws ProduceException {
      throw new ProduceException();
    }

  }

  private class MyStandardWorkflow extends StandardWorkflow {
    private int startCount = 0, initCount = 0, stopCount = 0, closeCount = 0;

    @Override
    protected void initialiseWorkflow() throws CoreException {
      super.initialiseWorkflow();
      initCount++;
    }

    /**
     * @see com.adaptris.core.WorkflowImp#startWorkflow()
     */
    @Override
    protected void startWorkflow() throws CoreException {
      super.startWorkflow();
      startCount++;
    }

    /**
     * @see com.adaptris.core.WorkflowImp#stopWorkflow()
     */
    @Override
    protected void stopWorkflow() {
      super.stopWorkflow();
      stopCount++;
    }

    /**
     * @see com.adaptris.core.WorkflowImp#closeWorkflow()
     */
    @Override
    protected void closeWorkflow() {
      super.closeWorkflow();
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
  }
}
