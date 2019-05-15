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
import com.adaptris.core.services.EmbeddedScriptingService;
import com.adaptris.core.stubs.MockChannel;
import com.adaptris.core.stubs.MockMessageProducer;
import com.adaptris.util.TimeInterval;

public class PollingTriggerTest extends ConsumerCase {

  private static final String PAYLOAD = "The Quick Brown Fox Jumps Over The Lazy Dog";

  private static final Poller[] POLLERS =
  {
      new FixedIntervalPoller(new TimeInterval(1L, TimeUnit.MINUTES)),
      new RandomIntervalPoller(new TimeInterval(1L, TimeUnit.MINUTES)), new QuartzCronPoller(),
      new GaussianIntervalPoller(new TimeInterval(5L, TimeUnit.SECONDS), new TimeInterval(6L, TimeUnit.SECONDS))
  };

  private static final PollingTrigger.MessageProvider[] TEMPLATES =
  {
      new StaticPollingTemplate("Hello World"),
      new DynamicPollingTemplate(
          new EmbeddedScriptingService().withScript("javascript", "message.setContent('Hello World', 'UTF-8')"))
  };

  private static final List<Poller> POLLER_LIST = Arrays.asList(POLLERS);


  public PollingTriggerTest(java.lang.String testName) {
    super(testName);
  }

  @Override
  protected void tearDown() throws Exception {
  }

  public void testTriggerWithStaticTemplate() throws Exception {
    Trigger trigger = new Trigger();
    MockMessageProducer mockProducer = new MockMessageProducer();
    Channel c = createChannel(new PollingTrigger(trigger, new StaticPollingTemplate(PAYLOAD)), mockProducer);
    c.getWorkflowList().getWorkflows().get(0).getConsumer()
        .setDestination(new ConfiguredConsumeDestination(getName(), null, getName()));
    try {
      BaseCase.start(c);
      trigger.fire();
      AdaptrisMessage msg = mockProducer.getMessages().get(0);
      assertEquals(PAYLOAD, msg.getContent());
    }
    finally {
      BaseCase.stop(c);
    }
  }

  public void testTriggerWithDynamicTemplate() throws Exception {
    Trigger trigger = new Trigger();
    String script = "message.setContent('" + PAYLOAD + "', 'UTF-8')";
    MockMessageProducer mockProducer = new MockMessageProducer();
    Channel c = createChannel(new PollingTrigger(trigger,
        new DynamicPollingTemplate(new EmbeddedScriptingService().withScript("nashorn", script))), mockProducer);
    try {
      BaseCase.start(c);
      trigger.fire();
      AdaptrisMessage msg = mockProducer.getMessages().get(0);
      assertEquals(PAYLOAD, msg.getContent());
    }
    finally {
      BaseCase.stop(c);
    }
  }

  public void testTriggerWithDynamicTemplate_NoConfig() throws Exception {
    Trigger trigger = new Trigger();
    MockMessageProducer mockProducer = new MockMessageProducer();
    Channel c = createChannel(new PollingTrigger(trigger, new DynamicPollingTemplate()), mockProducer);
    try {
      BaseCase.start(c);
      trigger.fire();
      AdaptrisMessage msg = mockProducer.getMessages().get(0);
      assertEquals(0, msg.getSize());
    }
    finally {
      BaseCase.stop(c);
    }
  }

  public void testTrigger_TemplateFails() throws Exception {
    String script = "message.setContent('" + PAYLOAD + "')"; // setContent must have a encoding...
    Trigger trigger = new Trigger();
    MockMessageProducer mockProducer = new MockMessageProducer();
    Channel c = createChannel(new PollingTrigger(trigger,
        new DynamicPollingTemplate(new EmbeddedScriptingService().withScript("nashorn", script))), mockProducer);
    try {
      BaseCase.start(c);
      trigger.fire();
      assertEquals(0, mockProducer.getMessages().size());
    }
    finally {
      BaseCase.stop(c);
    }
  }

  public void testTriggerWithEmptyMessages() throws Exception {
    Trigger trigger = new Trigger();
    MockMessageProducer mockProducer = new MockMessageProducer();
    Channel c = createChannel(new PollingTrigger(trigger), mockProducer);
    try {
      BaseCase.start(c);
      trigger.fire();
      AdaptrisMessage msg = mockProducer.getMessages().get(0);
      assertEquals(0, msg.getSize());
    }
    finally {
      BaseCase.stop(c);
    }
  }

  @Override
  protected List retrieveObjectsForSampleConfig() {
    ArrayList result = new ArrayList();
    for (Poller p : POLLER_LIST) {
      for (PollingTrigger.MessageProvider t : TEMPLATES) {
        result.add(new StandaloneConsumer(new PollingTrigger(p, t)));
      }
    }
    return result;
  }

  @Override
  protected Object retrieveObjectForSampleConfig() {
    return null;
  }

  @Override
  protected String createBaseFileName(Object object) {
    PollingTrigger p = (PollingTrigger) ((StandaloneConsumer) object).getConsumer();
    return super.createBaseFileName(object) + "-" + p.getMessageProvider().getClass().getSimpleName() + "-"
        + p.getPoller().getClass().getSimpleName();
  }

  private Channel createChannel(PollingTrigger trigger, MockMessageProducer mock) throws Exception {
    return new MockChannel().withWorkflow(new StandardWorkflow(trigger, mock));
  }

  private class Trigger extends PollerImp {

    public void fire() {
      processMessages();
    }
  }
}
