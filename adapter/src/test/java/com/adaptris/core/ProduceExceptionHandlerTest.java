package com.adaptris.core;

import com.adaptris.core.stubs.MockChannel;
import com.adaptris.core.stubs.MockMessageProducer;


public class ProduceExceptionHandlerTest extends BaseCase {

  public ProduceExceptionHandlerTest(String name) {
    super(name);
  }

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
    public void produce(AdaptrisMessage msg) throws ProduceException {
      throw new ProduceException();
    }

    @Override
    public void produce(AdaptrisMessage msg, ProduceDestination destination) throws ProduceException {
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
