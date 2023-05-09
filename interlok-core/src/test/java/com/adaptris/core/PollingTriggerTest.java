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
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;

import com.adaptris.core.DynamicPollingTemplate.TemplateProvider;
import com.adaptris.core.stubs.MockChannel;
import com.adaptris.core.stubs.MockMessageProducer;
import com.adaptris.interlok.junit.scaffolding.BaseCase;
import com.adaptris.interlok.junit.scaffolding.ExampleConsumerCase;
import com.adaptris.util.TimeInterval;
public class PollingTriggerTest extends ExampleConsumerCase {

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
      new DynamicPollingTemplate(new NullMessageProvider())
  };

  private static final List<Poller> POLLER_LIST = Arrays.asList(POLLERS);

  @Test
  public void testTriggerWithStaticTemplate() throws Exception {
    Trigger trigger = new Trigger();
    MockMessageProducer mockProducer = new MockMessageProducer();
    Channel c = createChannel(new PollingTrigger(trigger, new StaticPollingTemplate(PAYLOAD)), mockProducer);
    // c.getWorkflowList().getWorkflows().get(0).getConsumer()
    // .setDestination(new ConfiguredConsumeDestination(getName(), null, getName()));
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

  @Test
  public void testTriggerWithDynamicTemplate() throws Exception {
    Trigger trigger = new Trigger();
    MockMessageProducer mockProducer = new MockMessageProducer();
    Channel c = createChannel(new PollingTrigger(trigger,
        new DynamicPollingTemplate(new NullMessageProvider())), mockProducer);
    try {
      BaseCase.start(c);
      trigger.fire();
      AdaptrisMessage msg = mockProducer.getMessages().get(0);
      assertEquals("PayloadContent", msg.getContent());
    }
    finally {
      BaseCase.stop(c);
    }
  }

  @Test
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

  @Test
  public void testTrigger_TemplateFails() throws Exception {
    Trigger trigger = new Trigger();
    MockMessageProducer mockProducer = new MockMessageProducer();
    Channel c = createChannel(new PollingTrigger(trigger,
        new DynamicPollingTemplate(new ExceptionMessageProvider())), mockProducer);
    try {
      BaseCase.start(c);
      trigger.fire();
      assertEquals(0, mockProducer.getMessages().size());
    }
    finally {
      BaseCase.stop(c);
    }
  }

  @Test
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
  
  static class NullMessageProvider extends NullService implements TemplateProvider {
    @Override
    public void doService(AdaptrisMessage msg) throws ServiceException {
      msg.setContent("PayloadContent", msg.getContentEncoding());
    }
  }
  
  static class ExceptionMessageProvider extends NullService implements TemplateProvider {
    @Override
    public void doService(AdaptrisMessage msg) throws ServiceException {
      throw new ServiceException("Expected");
    }
  }

  private class Trigger extends PollerImp {

    public void fire() {
      processMessages();
    }
  }
}
