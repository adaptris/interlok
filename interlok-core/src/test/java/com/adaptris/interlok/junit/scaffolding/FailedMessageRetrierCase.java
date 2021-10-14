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

package com.adaptris.interlok.junit.scaffolding;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Test;
import com.adaptris.core.AdaptrisMessageConsumer;
import com.adaptris.core.AdaptrisMessageProducer;
import com.adaptris.core.Channel;
import com.adaptris.core.FailedMessageRetrier;
import com.adaptris.core.StandardWorkflow;
import com.adaptris.core.stubs.MockChannel;
import com.adaptris.core.stubs.MockMessageConsumer;
import com.adaptris.core.stubs.MockMessageProducer;
import com.adaptris.util.GuidGenerator;

public abstract class FailedMessageRetrierCase extends ExampleFailedMessageRetrierCase {

  private static final GuidGenerator guid = new GuidGenerator();


  protected StandardWorkflow createWorkflow() throws Exception {
    return createWorkflow(new GuidGenerator().getUUID());
  }

  protected StandardWorkflow createWorkflow(String uniqueId) throws Exception {
    AdaptrisMessageConsumer consumer = new MockMessageConsumer();
    AdaptrisMessageProducer producer = new MockMessageProducer();

    StandardWorkflow workflow = new StandardWorkflow();
    workflow.setUniqueId(uniqueId);
    workflow.setConsumer(consumer);
    workflow.setProducer(producer);
    Channel channel = new MockChannel();
    channel.setUniqueId(null);
    channel.getWorkflowList().add(workflow);
    channel.prepare();
    return workflow;
  }

  @Test
  public void testRegisteredWorkflowIds() throws Exception {
    FailedMessageRetrier retrier = create();
    StandardWorkflow wf1 = createWorkflow();
    StandardWorkflow wf2 = createWorkflow();
    retrier.addWorkflow(wf1);
    retrier.addWorkflow(wf2);
    assertEquals(2, retrier.registeredWorkflowIds().size());
    assertTrue(retrier.registeredWorkflowIds().contains(wf1.obtainWorkflowId()));
    assertTrue(retrier.registeredWorkflowIds().contains(wf2.obtainWorkflowId()));
  }

  @Test
  public void testClearWorkflows() throws Exception {
    FailedMessageRetrier retrier = create();
    retrier.addWorkflow(createWorkflow());
    retrier.addWorkflow(createWorkflow());
    retrier.clearWorkflows();
    assertEquals(0, retrier.registeredWorkflowIds().size());
  }


  protected abstract FailedMessageRetrier create();

  protected abstract FailedMessageRetrier createForExamples();
}
