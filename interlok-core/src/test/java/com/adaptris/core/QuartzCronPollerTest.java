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
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import java.util.Iterator;
import org.junit.Test;
import org.quartz.Scheduler;
import com.adaptris.core.stubs.MockChannel;
import com.adaptris.core.stubs.MockMessageProducer;

public class QuartzCronPollerTest extends BaseCase {

  private static final String PAYLOAD = "The Quick Brown Fox Jumps Over The Lazy Dog";

  @Override
  public boolean isAnnotatedForJunit4() {
    return true;
  }


  @Test
  public void testSchedulerGroup() throws Exception {
    QuartzCronPoller p1 = new QuartzCronPoller();
    p1.setCronExpression("*/5 * * * * ?");
    p1.setSchedulerGroup("MYGROUP");
    assertEquals("MYGROUP", p1.getSchedulerGroup());
    p1.setSchedulerGroup(null);
    assertNull(p1.getSchedulerGroup());
    p1.setSchedulerGroup("");
    assertEquals("", p1.getSchedulerGroup());
  }

  @Test
  public void testGeneratedSchedulerGroup_hasId() throws Exception {
    QuartzCronPoller p1 = new QuartzCronPoller();
    p1.setCronExpression("*/5 * * * * ?");
    p1.setSchedulerGroup("MYGROUP");
    StandardWorkflow wf1 = createWorkflow(null, p1, new MockMessageProducer());
    Channel channel = new MockChannel();
    channel.getWorkflowList().add(wf1);
    channel.prepare();
    try {
      channel.requestInit();
      assertEquals("MYGROUP", p1.generateSchedulerGroup());
    }
    finally {
      channel.requestClose();
    }
  }

  @Test
  public void testGeneratedSchedulerGroup_NoGroup() throws Exception {
    QuartzCronPoller p1 = new QuartzCronPoller();
    p1.setCronExpression("*/5 * * * * ?");
    StandardWorkflow wf1 = createWorkflow(null, p1, new MockMessageProducer());
    Channel channel = new MockChannel();
    channel.getWorkflowList().add(wf1);
    channel.prepare();
    try {
      channel.requestInit();
      assertEquals(Scheduler.DEFAULT_GROUP, p1.generateSchedulerGroup());
    }
    finally {
      channel.requestClose();
    }
  }

  @Test
  public void testQuartzId() throws Exception {
    QuartzCronPoller p1 = new QuartzCronPoller();
    p1.setCronExpression("*/5 * * * * ?");
    p1.setQuartzId("quartzId");
    assertEquals("quartzId", p1.getQuartzId());
    p1.setQuartzId(null);
    assertNull(p1.getQuartzId());
    p1.setQuartzId("");
    assertEquals("", p1.getQuartzId());
  }

  @Test
  public void testGeneratedQuartzId_hasQuartzId() throws Exception {
    QuartzCronPoller p1 = new QuartzCronPoller();
    p1.setCronExpression("*/5 * * * * ?");
    p1.setQuartzId("quartzId");
    StandardWorkflow wf1 = createWorkflow(null, p1, new MockMessageProducer());
    Channel channel = new MockChannel();
    channel.getWorkflowList().add(wf1);
    channel.prepare();
    try {
      channel.requestInit();
      assertEquals("quartzId", p1.getName());
    }
    finally {
      channel.requestClose();
    }
  }

  @Test
  public void testGeneratedQuartzId_NoDestination() throws Exception {
    QuartzCronPoller p1 = new QuartzCronPoller();
    p1.setCronExpression("*/5 * * * * ?");
    StandardWorkflow wf1 = createWorkflow(null, p1, new MockMessageProducer());
    Channel channel = new MockChannel();
    channel.getWorkflowList().add(wf1);
    channel.prepare();
    try {
      channel.requestInit();
      assertTrue("No Destination Configured doesn't cause NPE", p1.getName().startsWith("NoDestination"));
    }
    finally {
      channel.requestClose();
    }
  }

  @Test
  public void testGeneratedQuartzId_HasDestination() throws Exception {
    QuartzCronPoller p1 = new QuartzCronPoller();
    p1.setCronExpression("*/5 * * * * ?");
    StandardWorkflow wf1 = createWorkflow(new ConfiguredConsumeDestination("MyDestination"), p1, new MockMessageProducer());
    Channel channel = new MockChannel();
    channel.getWorkflowList().add(wf1);
    channel.prepare();
    try {
      channel.requestInit();
      assertTrue("No Destination Configured doesn't cause NPE", p1.getName().startsWith("MyDestination"));
    }
    finally {
      channel.requestClose();
    }
  }

  @Test
  public void testBug917() throws Exception {
    QuartzCronPoller p1 = new QuartzCronPoller();
    p1.setCronExpression("*/5 * * * * ?");
    StandardWorkflow wf1 = createWorkflow(null, p1, new MockMessageProducer());
    Channel channel = new MockChannel();
    channel.getWorkflowList().add(wf1);
    channel.prepare();
    try {
      channel.requestInit();
      assertTrue("No Destination Configured doesn't cause NPE", p1.getName().startsWith("NoDestination"));
    }
    finally {
      channel.requestClose();
    }
  }

  @Test
  public void testWithMessages() throws Exception {

    MockMessageProducer mock1 = new MockMessageProducer();
    MockMessageProducer mock2 = new MockMessageProducer();
    StandardWorkflow wf1 = createWorkflow(new ConfiguredConsumeDestination("2Secs"), new QuartzCronPoller("*/2 * * * * ?"), mock1);
    StandardWorkflow wf2 = createWorkflow(new ConfiguredConsumeDestination("VeryRare"), new QuartzCronPoller("0 00 00 31 * ?"),
        mock2);
    Channel channel = new MockChannel();
    channel.getWorkflowList().add(wf1);
    channel.getWorkflowList().add(wf2);
    channel.prepare();
    try {
      channel.requestStart();
      waitForMessages(mock1, 1);
    }
    finally {
      channel.requestClose();
    }
    assertTrue("No. Produced Messages >=1", mock1.getMessages().size() >= 1);
    for (Iterator i = mock1.getMessages().iterator(); i.hasNext();) {
      AdaptrisMessage msg = (AdaptrisMessage) i.next();
      assertEquals("Payloads", PAYLOAD, msg.getContent());
    }
    assertEquals("No. Produced messages = 0", 0, mock2.getMessages().size());
  }

  @Test
  public void testWithMessages_StandardThreadPool() throws Exception {

    MockMessageProducer mock1 = new MockMessageProducer();
    MockMessageProducer mock2 = new MockMessageProducer();
    StandardWorkflow wf1 = createWorkflow(new ConfiguredConsumeDestination("2Secs"), new QuartzCronPoller("*/2 * * * * ?", false),
        mock1);
    StandardWorkflow wf2 = createWorkflow(new ConfiguredConsumeDestination("VeryRare"), new QuartzCronPoller("0 00 00 31 * ?", false),
        mock2);
    Channel channel = new MockChannel();
    channel.getWorkflowList().add(wf1);
    channel.getWorkflowList().add(wf2);
    channel.prepare();
    try {
      channel.requestStart();
      waitForMessages(mock1, 1);
    }
    finally {
      channel.requestClose();
    }
    assertTrue("No. Produced Messages >=1", mock1.getMessages().size() >= 1);
    for (Iterator i = mock1.getMessages().iterator(); i.hasNext();) {
      AdaptrisMessage msg = (AdaptrisMessage) i.next();
      assertEquals("Payloads", PAYLOAD, msg.getContent());
    }
    assertEquals("No. Produced messages = 0", 0, mock2.getMessages().size());
  }

  private StandardWorkflow createWorkflow(ConsumeDestination d, QuartzCronPoller poller, AdaptrisMessageProducer prod) {
    PollingTrigger trigger = new PollingTrigger(poller, new StaticPollingTemplate(PAYLOAD));
    if (d != null) {
      trigger.setDestination(d);
    }
    StandardWorkflow result = new StandardWorkflow();
    result.setConsumer(trigger);
    result.setProducer(prod);
    return result;

  }
}
