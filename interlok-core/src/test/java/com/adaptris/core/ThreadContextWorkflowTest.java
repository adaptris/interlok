/*
 * Copyright 2021 Adaptris Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package com.adaptris.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import org.awaitility.Awaitility;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.adaptris.core.services.exception.ConfiguredException;
import com.adaptris.core.services.exception.ThrowExceptionService;
import com.adaptris.core.services.metadata.AddMetadataService;
import com.adaptris.core.services.metadata.PayloadFromTemplateService;
import com.adaptris.core.stubs.FailFirstMockMessageProducer;
import com.adaptris.core.stubs.MockChannel;
import com.adaptris.core.stubs.MockMessageProducer;
import com.adaptris.core.stubs.MockSkipProducerService;
import com.adaptris.core.stubs.MockWorkflowInterceptor;
import com.adaptris.core.stubs.StaticMockMessageProducer;
import com.adaptris.core.util.Args;
import com.adaptris.core.util.MinimalMessageLogger;
import com.adaptris.util.TimeInterval;

public class ThreadContextWorkflowTest
    extends com.adaptris.interlok.junit.scaffolding.ExampleWorkflowCase {

  private static final Logger log = LoggerFactory.getLogger(ThreadContextWorkflowTest.class);

  protected static final String METADATA_KEY = "key1";
  protected static final String METADATA_VALUE = "value";

  @Test
  public void testOnMessageWithSendEvents() throws Exception {
    MockMessageProducer producer = new MockMessageProducer();
    MockMessageProducer eventProd = new MockMessageProducer();
    DefaultEventHandler evtHandler = new DefaultEventHandler(eventProd);
    MockChannel channel = createChannel(producer, Arrays.asList(new Service[]
    {
        new AddMetadataService(Arrays.asList(new MetadataElement[]
        {
          new MetadataElement(METADATA_KEY, METADATA_VALUE)
        })), new PayloadFromTemplateService().withTemplate(PAYLOAD_2)
    }));
    try {
      channel.setEventHandler(evtHandler);
      ThreadContextWorkflow workflow = (ThreadContextWorkflow) channel.getWorkflowList().get(0);
      workflow.setSendEvents(true);
      channel.prepare();
      AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(PAYLOAD_1);
      start(channel);
      workflow.onAdaptrisMessage(msg);
      assertEquals("Make sure all produced", 1, producer.getMessages().size());
      for (Iterator i = producer.getMessages().iterator(); i.hasNext();) {
        AdaptrisMessage m = (AdaptrisMessage) i.next();
        assertEquals(PAYLOAD_2, m.getContent());
        assertTrue("Contains correct metadata key", m.headersContainsKey(METADATA_KEY));
        assertEquals(METADATA_VALUE, m.getMetadataValue(METADATA_KEY));
      }
      waitForMessages(eventProd, 1);
      assertEquals(1, eventProd.messageCount());
    }
    finally {
      stop(channel);
      stop(evtHandler);
    }
  }

  @Test
  public void testOnMessageWithoutEvents() throws Exception {
    MockMessageProducer producer = new MockMessageProducer();
    MockMessageProducer eventProd = new MockMessageProducer();
    DefaultEventHandler evtHandler = new DefaultEventHandler(eventProd);
    MockChannel channel = createChannel(producer, Arrays.asList(new Service[]
    {
        new AddMetadataService(Arrays.asList(new MetadataElement[]
        {
          new MetadataElement(METADATA_KEY, METADATA_VALUE)
        })), new PayloadFromTemplateService().withTemplate(PAYLOAD_2)
    }));
    try {
      channel.setEventHandler(evtHandler);
      ThreadContextWorkflow workflow = (ThreadContextWorkflow) channel.getWorkflowList().get(0);
      workflow.setSendEvents(false);
      channel.prepare();
      AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(PAYLOAD_1);
      start(channel);
      workflow.onAdaptrisMessage(msg);
      assertEquals("Make sure all produced", 1, producer.getMessages().size());
      for (Iterator i = producer.getMessages().iterator(); i.hasNext();) {
        AdaptrisMessage m = (AdaptrisMessage) i.next();
        assertEquals(PAYLOAD_2, m.getContent());
        assertTrue("Contains correct metadata key", m.headersContainsKey(METADATA_KEY));
        assertEquals(METADATA_VALUE, m.getMetadataValue(METADATA_KEY));
      }
      waitForMessages(eventProd, 0);
      assertEquals(0, eventProd.messageCount());
    }
    finally {
      stop(channel);
      stop(evtHandler);
    }
  }

  @Test
  public void testOnMessageWithInterceptors() throws Exception {
    MockWorkflowInterceptor interceptor = new MockWorkflowInterceptor();
    MockMessageProducer producer = new MockMessageProducer();
    MockChannel channel = createChannel(producer, Arrays.asList(new Service[]
    {
        new AddMetadataService(Arrays.asList(new MetadataElement[]
        {
          new MetadataElement(METADATA_KEY, METADATA_VALUE)
        })), new PayloadFromTemplateService().withTemplate(PAYLOAD_2)
    }));
    int count = 10;
    try {
      ThreadContextWorkflow workflow = (ThreadContextWorkflow) channel.getWorkflowList().get(0);
      workflow.setAdditionalDebug(Boolean.TRUE);
      workflow.addInterceptor(interceptor);
      channel.prepare();
      start(channel);
      for (int i = 0; i < count; i++) {
        AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(PAYLOAD_1);
        workflow.onAdaptrisMessage(msg);
      }
      assertEquals("Make sure all produced", count, producer.messageCount());
      assertEquals("Make sure all intercepted", count, interceptor.messageCount());
      for (AdaptrisMessage m : producer.getMessages()) {
        assertEquals(PAYLOAD_2, m.getContent());
        assertTrue("Contains correct metadata key", m.headersContainsKey(METADATA_KEY));
        assertEquals(METADATA_VALUE, m.getMetadataValue(METADATA_KEY));
      }
    }
    finally {
      stop(channel);
    }

  }

  @Test
  public void testHandleChannelUnavailable() throws Exception {
    MockMessageProducer producer = new MockMessageProducer();
    final MockChannel channel = createChannel(producer, Arrays.asList(new Service[]
    {
        new AddMetadataService(Arrays.asList(new MetadataElement[]
        {
          new MetadataElement(METADATA_KEY, METADATA_VALUE)
        })), new PayloadFromTemplateService().withTemplate(PAYLOAD_2)
    }));
    try {
      ThreadContextWorkflow workflow = (ThreadContextWorkflow) channel.getWorkflowList().get(0);
      workflow.setChannelUnavailableWaitInterval(new TimeInterval(1200L, TimeUnit.MILLISECONDS));
      AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(PAYLOAD_1);
      channel.prepare();
      start(channel);
      channel.toggleAvailability(false);
      Timer t = new Timer(true);
      t.schedule(new TimerTask() {
        @Override
        public void run() {
          channel.toggleAvailability(true);
        }

      }, 500);
      workflow.onAdaptrisMessage(msg);

      assertEquals("Make sure all produced", 1, producer.getMessages().size());
      for (Iterator i = producer.getMessages().iterator(); i.hasNext();) {
        AdaptrisMessage m = (AdaptrisMessage) i.next();
        assertEquals(PAYLOAD_2, m.getContent());
        assertTrue("Contains correct metadata key", m.headersContainsKey(METADATA_KEY));
        assertEquals(METADATA_VALUE, m.getMetadataValue(METADATA_KEY));
      }
    }
    finally {
      stop(channel);
    }
  }

  @Test
  public void testServiceException() throws Exception {
    MockMessageProducer producer = new MockMessageProducer();
    MockMessageProducer meh = new MockMessageProducer();
    MockChannel channel = createChannel(producer, Arrays.asList(new Service[]
    {
      new ThrowExceptionService(new ConfiguredException("Fail"))
    }));
    try {
      ThreadContextWorkflow workflow = (ThreadContextWorkflow) channel.getWorkflowList().get(0);
      channel.setMessageErrorHandler(new StandardProcessingExceptionHandler(
    		  new ServiceList(new ArrayList<Service>(Arrays.asList(new Service[]
    	    	      {
    	    	        new StandaloneProducer(meh)
    	    	      })))));
      channel.prepare();

      AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(PAYLOAD_1);
      start(channel);
      workflow.onAdaptrisMessage(msg);

      assertEquals("Make none produced", 0, producer.getMessages().size());
      assertEquals(1, meh.getMessages().size());
      for (Iterator i = meh.getMessages().iterator(); i.hasNext();) {
        AdaptrisMessage m = (AdaptrisMessage) i.next();
        assertEquals(PAYLOAD_1, m.getContent());
        assertFalse("Does not contains correct metadata key", m.headersContainsKey(METADATA_KEY));
        assertNotNull(m.getObjectHeaders().get(CoreConstants.OBJ_METADATA_EXCEPTION));
        assertNotNull(m.getObjectHeaders().get(CoreConstants.OBJ_METADATA_EXCEPTION_CAUSE));
        assertEquals(ThrowExceptionService.class.getSimpleName(),
            m.getObjectHeaders().get(CoreConstants.OBJ_METADATA_EXCEPTION_CAUSE));
      }
    }
    finally {
      stop(channel);
    }
  }

  @Test
  public void testBrokenPool() throws Exception {
    MockMessageProducer producer = new MockMessageProducer();
    MockMessageProducer eventProd = new MockMessageProducer();
    DefaultEventHandler evtHandler = new DefaultEventHandler(eventProd);
    MockChannel channel = createChannel(producer, Arrays.asList(new Service[]
    {
        new AddMetadataService(Arrays.asList(new MetadataElement[]
        {
          new MetadataElement(METADATA_KEY, METADATA_VALUE)
        })), new PayloadFromTemplateService().withTemplate(PAYLOAD_2)
    }));
    MockMessageProducer meh = new MockMessageProducer();
    try {
      ThreadContextWorkflow workflow = (ThreadContextWorkflow) channel.getWorkflowList().get(0);
      channel.setMessageErrorHandler(new StandardProcessingExceptionHandler(
          new ServiceList(
              new ArrayList<Service>(Arrays.asList(new Service[] {new StandaloneProducer(meh)})))));
      channel.prepare();

      AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(PAYLOAD_1);
      start(channel);
      // close the pool which should cause an IllegalStateException
      workflow.getObjectPool().close();
      workflow.onAdaptrisMessage(msg);

      assertEquals("Make none produced", 0, producer.getMessages().size());
      assertEquals(1, meh.getMessages().size());
      for (Iterator i = meh.getMessages().iterator(); i.hasNext();) {
        AdaptrisMessage m = (AdaptrisMessage) i.next();
        assertEquals(PAYLOAD_1, m.getContent());
        assertFalse("Does not contains correct metadata key", m.headersContainsKey(METADATA_KEY));
      }
    } finally {
      stop(channel);
    }
  }

  @Test
  public void testProduceException() throws Exception {
    MockMessageProducer producer = new MockMessageProducer() {
      @Override
      protected void doProduce(AdaptrisMessage msg, String endpoint) throws ProduceException {
        throw new ProduceException();
      }
    };
    MockMessageProducer meh = new MockMessageProducer();
    MockChannel channel = createChannel(producer,
        Arrays.asList(new Service[] {new ThrowExceptionService(new ConfiguredException("Fail"))}));
    try {
      ThreadContextWorkflow workflow = (ThreadContextWorkflow) channel.getWorkflowList().get(0);
      channel.setMessageErrorHandler(new StandardProcessingExceptionHandler(
    		  new ServiceList(new ArrayList<Service>(Arrays.asList(new Service[]
    	    	      {
    	    	        new StandaloneProducer(meh)
    	    	      })))));
      channel.prepare();

      AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(PAYLOAD_1);
      start(channel);
      workflow.onAdaptrisMessage(msg);

      assertEquals("Make none produced", 0, producer.getMessages().size());
      assertEquals(1, meh.getMessages().size());
      for (Iterator i = meh.getMessages().iterator(); i.hasNext();) {
        AdaptrisMessage m = (AdaptrisMessage) i.next();
        assertEquals(PAYLOAD_1, m.getContent());
        assertFalse("Does not contains correct metadata key", m.headersContainsKey(METADATA_KEY));
      }
    }
    finally {
      stop(channel);
    }
  }

  @Test
  public void testRuntimeException() throws Exception {
    MockMessageProducer producer = new MockMessageProducer() {
      @Override
      protected void doProduce(AdaptrisMessage msg, String endpoint) throws ProduceException {
        throw new RuntimeException();
      }
    };
    MockMessageProducer meh = new MockMessageProducer();
    MockChannel channel = createChannel(producer, Arrays.asList(new Service[]
    {
        new AddMetadataService(Arrays.asList(new MetadataElement[]
        {
          new MetadataElement(METADATA_KEY, METADATA_VALUE)
        })), new PayloadFromTemplateService().withTemplate(PAYLOAD_2)
    }));
    try {
      ThreadContextWorkflow workflow = (ThreadContextWorkflow) channel.getWorkflowList().get(0);
      channel.setMessageErrorHandler(new StandardProcessingExceptionHandler(
    		  new ServiceList(new ArrayList<Service>(Arrays.asList(new Service[]
    	    	      {
    	    	        new StandaloneProducer(meh)
    	    	      })))));
      channel.prepare();

      AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(PAYLOAD_1);
      start(channel);
      workflow.onAdaptrisMessage(msg);

      assertEquals("Make none produced", 0, producer.getMessages().size());
      assertEquals(1, meh.getMessages().size());
      for (Iterator i = meh.getMessages().iterator(); i.hasNext();) {
        AdaptrisMessage m = (AdaptrisMessage) i.next();
        assertEquals(PAYLOAD_1, m.getContent());
        assertFalse("Does not contains correct metadata key", m.headersContainsKey(METADATA_KEY));
      }
    }
    finally {
      stop(channel);
    }
  }

  @Test
  public void testOnMessage_SkipProducer() throws Exception {
    MockMessageProducer producer = new MockMessageProducer();
    StaticMockMessageProducer serviceProducer = new StaticMockMessageProducer();
    serviceProducer.getMessages().clear();
    MockChannel channel = createChannel(producer, Arrays.asList(
        new Service[] {new StandaloneProducer(serviceProducer), new MockSkipProducerService()}));
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(PAYLOAD_1);
    ThreadContextWorkflow workflow = (ThreadContextWorkflow) channel.getWorkflowList().get(0);
    try {
      start(channel);
      workflow.onAdaptrisMessage(msg);
      assertEquals(1, serviceProducer.messageCount());
      assertEquals(0, producer.messageCount());
    } finally {
      stop(channel);
    }
  }


  @Test
  public void testOnMessage_MessageLogger() throws Exception {
    MockMessageProducer producer = new MockMessageProducer();
    MockChannel channel = createChannel(producer, Arrays.asList(new Service[] {new NullService()}));
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(PAYLOAD_1);
    ThreadContextWorkflow workflow = (ThreadContextWorkflow) channel.getWorkflowList().get(0);
    workflow.setMessageLogger(new MinimalMessageLogger());
    try {
      start(channel);
      workflow.onAdaptrisMessage(msg);
      assertEquals(1, producer.messageCount());
    } finally {
      stop(channel);
    }
  }

  @Test
  public void testOnMessage_withConsumeLocation() throws Exception {
    MockMessageProducer producer = new MockMessageProducer();
    MockChannel channel = createChannel(producer, Arrays.asList(new Service[] {new NullService()}));
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(PAYLOAD_1);
    msg.addMessageHeader(getName(), "hello world");
    ThreadContextWorkflow workflow = (ThreadContextWorkflow) channel.getWorkflowList().get(0);
    workflow.setConsumer(new ConsumerWithLocation(getName()));
    try {
      start(channel);
      workflow.onAdaptrisMessage(msg);
      AdaptrisMessage consumed = producer.getMessages().get(0);
      assertTrue(consumed.headersContainsKey(CoreConstants.MESSAGE_CONSUME_LOCATION));
      assertEquals("hello world",
          consumed.getMetadataValue(CoreConstants.MESSAGE_CONSUME_LOCATION));
    } finally {
      stop(channel);
    }
  }

  @Test
  public void testOnMessage_withConsumeLocation_NoMatch() throws Exception {
    MockMessageProducer producer = new MockMessageProducer();
    MockChannel channel = createChannel(producer, Arrays.asList(new Service[] {new NullService()}));
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(PAYLOAD_1);
    ThreadContextWorkflow workflow = (ThreadContextWorkflow) channel.getWorkflowList().get(0);
    workflow.setConsumer(new ConsumerWithLocation(getName()));
    try {
      start(channel);
      workflow.onAdaptrisMessage(msg);
      AdaptrisMessage consumed = producer.getMessages().get(0);
      assertFalse(consumed.headersContainsKey(CoreConstants.MESSAGE_CONSUME_LOCATION));
    } finally {
      stop(channel);
    }
  }


  @Test
  public void testOnMessage_SuccessCallback() throws Exception {
    AtomicBoolean onSuccess = new AtomicBoolean(false);
    MockChannel channel = createChannel(new MockMessageProducer(), Arrays.asList(new Service[] {new NullService()}));
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(PAYLOAD_1);
    ThreadContextWorkflow workflow = (ThreadContextWorkflow) channel.getWorkflowList().get(0);
    try {
      start(channel);
      workflow.onAdaptrisMessage(msg, (m) -> {
        onSuccess.set(true);
      });
      assertTrue(onSuccess.get());
    } finally {
      stop(channel);
    }

  }

  @Test
  public void testOnMessage_ServiceFailsFailureCallback() throws Exception {
    AtomicBoolean onSuccess = new AtomicBoolean(false);
    MockChannel channel = createChannel(new MockMessageProducer(), Arrays.asList(new Service[] {new ThrowExceptionService(new ConfiguredException("expected"))}));
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(PAYLOAD_1);
    ThreadContextWorkflow workflow = (ThreadContextWorkflow) channel.getWorkflowList().get(0);
    try {
      start(channel);
      workflow.onAdaptrisMessage(msg,
          (m) -> {
            onSuccess.set(true);
          },
          (m) -> {
            onSuccess.set(false);
          });

      assertFalse(onSuccess.get());
    } finally {
      stop(channel);
    }
  }

  @Test
  public void testOnMessage_ServiceFailsWithRetryHandlerFailureCallback() throws Exception {
    AtomicBoolean onSuccess = new AtomicBoolean(false);
    MockChannel channel = createChannel(new MockMessageProducer(), Arrays.asList(new Service[] {new ThrowExceptionService(new ConfiguredException("expected"))}));

    channel.setMessageErrorHandler(new RetryMessageErrorHandler(3, new TimeInterval(100l, TimeUnit.MILLISECONDS), new NullService()));

    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(PAYLOAD_1);
    ThreadContextWorkflow workflow = (ThreadContextWorkflow) channel.getWorkflowList().get(0);
    try {
      start(channel);
      workflow.onAdaptrisMessage(msg,
          (m) -> {
            onSuccess.set(true);
          },
          (m) -> {
            onSuccess.set(false);
          });

      assertFalse(onSuccess.get());
    } finally {
      stop(channel);
    }
  }

  @Test
  public void testOnMessage_ProducerFailsFailureCallback() throws Exception {
    AtomicBoolean onSuccess = new AtomicBoolean(false);
    MockChannel channel = createChannel(new FailFirstMockMessageProducer(1), Arrays.asList(new Service[] {new NullService()}));
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(PAYLOAD_1);
    ThreadContextWorkflow workflow = (ThreadContextWorkflow) channel.getWorkflowList().get(0);
    try {
      start(channel);
      workflow.onAdaptrisMessage(msg,
          (m) -> {
            onSuccess.set(true);
          },
          (m) -> {
            onSuccess.set(false);
          });

      assertFalse(onSuccess.get());
    } finally {
      stop(channel);
    }
  }

  @Test
  public void testOnMessage_ProducerFailsWithRetryHandlerSuccessCallback() throws Exception {
    AtomicBoolean onSuccess = new AtomicBoolean(false);
    MockChannel channel = createChannel(new FailFirstMockMessageProducer(1), Arrays.asList(new Service[] {new NullService()}));

    channel.setMessageErrorHandler(new RetryMessageErrorHandler(2, new TimeInterval(100l, TimeUnit.MILLISECONDS), new NullService()));

    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(PAYLOAD_1);
    ThreadContextWorkflow workflow = (ThreadContextWorkflow) channel.getWorkflowList().get(0);
    try {
      start(channel);
      workflow.onAdaptrisMessage(msg,
          (m) -> {
            onSuccess.set(true);
          },
          (m) -> {
            onSuccess.set(false);
          });

      Awaitility.await()
        .atMost(Duration.ofSeconds(2))
      .with()
        .pollInterval(Duration.ofMillis(100))
        .until(onSuccess::get);

      assertTrue(onSuccess.get());
    } finally {
      stop(channel);
    }
  }

  @Test
  public void testOnMessage_ProducerFailsWithRetryHandlerFailureCallback() throws Exception {
    AtomicBoolean onSuccess = new AtomicBoolean(false);
    MockChannel channel = createChannel(new FailFirstMockMessageProducer(5), Arrays.asList(new Service[] {new NullService()}));

    channel.setMessageErrorHandler(new RetryMessageErrorHandler(4, new TimeInterval(100l, TimeUnit.MILLISECONDS), new NullService()));

    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(PAYLOAD_1);
    ThreadContextWorkflow workflow = (ThreadContextWorkflow) channel.getWorkflowList().get(0);
    try {
      start(channel);
      workflow.onAdaptrisMessage(msg,
          (m) -> {
            onSuccess.set(true);
          },
          (m) -> {
            onSuccess.set(false);
          });

      assertFalse(onSuccess.get());
    } finally {
      stop(channel);
    }
  }

  @Override
  protected Object retrieveObjectForSampleConfig() {
    Channel c = new Channel();
    c.setUniqueId(UUID.randomUUID().toString());
    ThreadContextWorkflow wf1 = createWorkflowForExampleConfig();
    wf1.setUniqueId("Unthrottled-Workflow");
    c.getWorkflowList().add(wf1);
    return c;
  }


  protected MockChannel createChannel(AdaptrisMessageProducer producer, List<Service> services) throws Exception {
    MockChannel channel = new MockChannel();
    ThreadContextWorkflow workflow = createWorkflowForGenericTests();
    workflow.setProducer(producer);
    workflow.getServiceCollection().addAll(services);
    channel.getWorkflowList().add(workflow);
    return channel;
  }

  @Override
  protected String createBaseFileName(Object object) {
    return ThreadContextWorkflow.class.getName();
  }

  @Override
  protected ThreadContextWorkflow createWorkflowForGenericTests() {
    return new ThreadContextWorkflow();
  }

  private ThreadContextWorkflow createWorkflowForExampleConfig() {
    ThreadContextWorkflow wf = new ThreadContextWorkflow();
    NullMessageConsumer consumer = new NullMessageConsumer();
    wf.setConsumer(consumer);
    wf.setProducer(new NullMessageProducer());
    return wf;
  }

  private class ConsumerWithLocation extends NullMessageConsumer {
    private String metadataKey;

    public ConsumerWithLocation(String key) {
      metadataKey = Args.notBlank(key, "metadataKey");
    }

    @Override
    public String consumeLocationKey() {
      return metadataKey;
    }
  }
}
