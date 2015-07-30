package com.adaptris.core.lifecycle;

import org.apache.log4j.Logger;

import com.adaptris.core.BaseCase;
import com.adaptris.core.Channel;
import com.adaptris.core.ConfiguredConsumeDestination;
import com.adaptris.core.StandardWorkflow;
import com.adaptris.core.Workflow;
import com.adaptris.core.WorkflowImp;
import com.adaptris.core.WorkflowLifecycleStrategy;
import com.adaptris.core.WorkflowList;
import com.adaptris.core.stubs.MockChannel;
import com.adaptris.core.stubs.MockMessageConsumer;
import com.adaptris.util.IdGenerator;
import com.adaptris.util.PlainIdGenerator;

public abstract class WorkflowLifecycleStrategyCase extends BaseCase {

  protected IdGenerator idGenerator;
  protected Logger log = Logger.getLogger(this.getClass());

  public WorkflowLifecycleStrategyCase(java.lang.String testName) {
    super(testName);
  }

  @Override
  public void setUp() throws Exception {
    idGenerator = new PlainIdGenerator();
  }

  public void testInitNoStrategy() throws Exception {
    WorkflowList wfl = new WorkflowList();
    wfl.add(createWorkflow(idGenerator.create(wfl)));
    createChannel(wfl).requestInit();
  }

  public void testStartNoStrategy() throws Exception {
    WorkflowList wfl = new WorkflowList();
    wfl.add(createWorkflow(idGenerator.create(wfl)));
    createChannel(wfl).requestStart();
  }

  public void testStopNoStrategy() throws Exception {
    WorkflowList wfl = new WorkflowList();
    wfl.add(createWorkflow(idGenerator.create(wfl)));
    Channel c = createChannel(wfl);
    c.requestStart();
    c.requestStop();
  }

  public void testCloseNoStrategy() throws Exception {
    WorkflowList wfl = new WorkflowList();
    wfl.add(createWorkflow(idGenerator.create(wfl)));
    Channel c = createChannel(wfl);
    c.requestStart();
    c.requestClose();
  }

  public void testInit() throws Exception {
    WorkflowList wfl = new WorkflowList();
    wfl.add(createWorkflow(idGenerator.create(wfl)));
    wfl.setLifecycleStrategy(createStrategy());
    createChannel(wfl).requestInit();
  }

  public void testStart() throws Exception {
    WorkflowList wfl = new WorkflowList();
    wfl.add(createWorkflow(idGenerator.create(wfl)));
    wfl.setLifecycleStrategy(createStrategy());
    createChannel(wfl).requestStart();

  }

  public void testStop() throws Exception {
    WorkflowList wfl = new WorkflowList();
    wfl.add(createWorkflow(idGenerator.create(wfl)));
    wfl.setLifecycleStrategy(createStrategy());
    Channel c = createChannel(wfl);
    c.requestStart();
    c.requestStop();
  }

  public void testClose() throws Exception {
    WorkflowList wfl = new WorkflowList();
    wfl.add(createWorkflow(idGenerator.create(wfl)));
    wfl.setLifecycleStrategy(createStrategy());
    Channel c = createChannel(wfl);
    c.requestStart();
    c.requestClose();
  }

  protected abstract WorkflowLifecycleStrategy createStrategy();

  protected Workflow createWorkflow(String uid) {
    return configure(new StandardWorkflow(), uid);
  }

  protected Workflow configure(WorkflowImp wf, String uid) {
    wf.setUniqueId(uid);
    wf.setConsumer(new MockMessageConsumer(new ConfiguredConsumeDestination(uid)));
    return wf;
  }

  protected Channel createChannel(WorkflowList wfl) throws Exception {
    Channel channel = new MockChannel();
    channel.setWorkflowList(wfl);
    channel.prepare();
    return channel;
  }

}