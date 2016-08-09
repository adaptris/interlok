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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import com.adaptris.core.stubs.MockChannel;
import com.adaptris.core.stubs.MockMessageProducer;
import com.adaptris.core.util.LifecycleHelper;
import com.adaptris.util.TimeInterval;

public class PollingTriggerTest extends ConsumerCase {

  private PollingTrigger trigger;
  private MockMessageProducer mockProducer;
  private StandardWorkflow workflow;
  private static final String PAYLOAD = "The Quick Brown Fox Jumps Over The Lazy Dog";

  private static final Poller[] POLLERS =
  {
      new FixedIntervalPoller(new TimeInterval(1L, TimeUnit.MINUTES))
      ,new QuartzCronPoller()
      ,new GaussianIntervalPoller(new TimeInterval(5L,TimeUnit.SECONDS),new TimeInterval(6L, TimeUnit.SECONDS))
  };

  private static final List<Poller> POLLER_LIST = Arrays.asList(POLLERS);


  public PollingTriggerTest(java.lang.String testName) {
    super(testName);
  }

  @Override
  protected void setUp() throws Exception {
    trigger = new PollingTrigger();
    FixedIntervalPoller fp = new FixedIntervalPoller();
    fp.setPollInterval(new TimeInterval(1L, TimeUnit.SECONDS));
    trigger.setPoller(fp);
    workflow = new StandardWorkflow();
    mockProducer = new MockMessageProducer();
    workflow.setConsumer(trigger);
    workflow.setProducer(mockProducer);
    Channel channel = new MockChannel();
    channel.getWorkflowList().add(workflow);
    channel.prepare();
  }

  @Override
  protected void tearDown() throws Exception {
  }

  public void testInit() throws Exception {
    LifecycleHelper.init(workflow);
    LifecycleHelper.close(workflow);

  }


  public void testStartWithNonNullMessages() throws Exception {
    trigger.setTemplate(PAYLOAD);
    workflow.init();
    workflow.start();
    waitForMessages(mockProducer, 1);
    workflow.stop();
    workflow.close();
    AdaptrisMessage msg = mockProducer.getMessages().get(0);
    assertEquals("Payloads", PAYLOAD, msg.getContent());
  }

  public void testStartWithEmptyMessages() throws Exception {
    workflow.init();
    workflow.start();
    waitForMessages(mockProducer, 1);
    workflow.stop();
    workflow.close();
    AdaptrisMessage msg = mockProducer.getMessages().get(0);
      assertEquals("Payload Size", 0, msg.getPayload().length);
  }

  @Override
  protected List retrieveObjectsForSampleConfig() {
    ArrayList result = new ArrayList();
    for (Poller t : POLLER_LIST) {
      StandaloneConsumer p = retrieveSampleConfig();
      ((AdaptrisPollingConsumer) p.getConsumer()).setPoller(t);
      result.add(p);
    }
    return result;
  }

  /**
   * @see com.adaptris.core.ExampleConfigCase#retrieveObjectForSampleConfig()
   */
  @Override
  protected Object retrieveObjectForSampleConfig() {
    return null;
  }

  @Override
  protected String createBaseFileName(Object object) {
    AdaptrisPollingConsumer p = (AdaptrisPollingConsumer) ((StandaloneConsumer) object)
        .getConsumer();
    return super.createBaseFileName(object) + "-"
        + p.getPoller().getClass().getSimpleName();
  }

  /**
   *
   * @see com.adaptris.core.ExampleConfigCase#retrieveObjectForSampleConfig()
   */
  protected StandaloneConsumer retrieveSampleConfig() {
    PollingTrigger pt = new PollingTrigger();
    pt.setTemplate("The trigger message");
    StandaloneConsumer result = new StandaloneConsumer();
    result.setConsumer(pt);
    return result;
  }
}
