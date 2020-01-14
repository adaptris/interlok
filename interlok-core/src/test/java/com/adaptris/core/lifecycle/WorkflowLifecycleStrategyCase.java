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

package com.adaptris.core.lifecycle;

import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;
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

  @Override
  public boolean isAnnotatedForJunit4() {
    return true;
  }

  @Before
  public void setUp() throws Exception {
    idGenerator = new PlainIdGenerator();
  }

  @Test
  public void testInitNoStrategy() throws Exception {
    WorkflowList wfl = new WorkflowList();
    wfl.add(createWorkflow(idGenerator.create(wfl)));
    createChannel(wfl).requestInit();
  }

  @Test
  public void testStartNoStrategy() throws Exception {
    WorkflowList wfl = new WorkflowList();
    wfl.add(createWorkflow(idGenerator.create(wfl)));
    createChannel(wfl).requestStart();
  }

  @Test
  public void testStopNoStrategy() throws Exception {
    WorkflowList wfl = new WorkflowList();
    wfl.add(createWorkflow(idGenerator.create(wfl)));
    Channel c = createChannel(wfl);
    c.requestStart();
    c.requestStop();
  }

  @Test
  public void testCloseNoStrategy() throws Exception {
    WorkflowList wfl = new WorkflowList();
    wfl.add(createWorkflow(idGenerator.create(wfl)));
    Channel c = createChannel(wfl);
    c.requestStart();
    c.requestClose();
  }

  @Test
  public void testInit() throws Exception {
    WorkflowList wfl = new WorkflowList();
    wfl.add(createWorkflow(idGenerator.create(wfl)));
    wfl.setLifecycleStrategy(createStrategy());
    createChannel(wfl).requestInit();
  }

  @Test
  public void testStart() throws Exception {
    WorkflowList wfl = new WorkflowList();
    wfl.add(createWorkflow(idGenerator.create(wfl)));
    wfl.setLifecycleStrategy(createStrategy());
    createChannel(wfl).requestStart();

  }

  @Test
  public void testStop() throws Exception {
    WorkflowList wfl = new WorkflowList();
    wfl.add(createWorkflow(idGenerator.create(wfl)));
    wfl.setLifecycleStrategy(createStrategy());
    Channel c = createChannel(wfl);
    c.requestStart();
    c.requestStop();
  }

  @Test
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
