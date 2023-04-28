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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;

import com.adaptris.core.services.exception.ConfiguredException;
import com.adaptris.core.services.exception.ThrowExceptionService;
import com.adaptris.core.services.metadata.PayloadFromTemplateService;
import com.adaptris.core.stubs.MockChannel;
import com.adaptris.core.stubs.MockMessageProducer;
import com.adaptris.core.stubs.MockSkipProducerService;
import com.adaptris.core.stubs.MockWorkflowInterceptor;
import com.adaptris.core.util.Args;
import com.adaptris.util.TimeInterval;

public class MultiProducerWorkflowTest
    extends com.adaptris.interlok.junit.scaffolding.ExampleWorkflowCase {

  private static final String ORIGINAL_PAYLOAD = PAYLOAD_1;
  private static final String MODIFIED_PAYLOAD = PAYLOAD_2;


  @Test
  public void testToString() throws Exception {
    MockMessageProducer mock1 = new MockMessageProducer();
    MockMessageProducer mock2 = new MockMessageProducer();
    MockChannel channel = createChannel(Arrays.asList(new AdaptrisMessageProducer[]
    {
        mock1, mock2
    }), Arrays.asList(new Service[]
    {
        new PayloadFromTemplateService().withTemplate(MODIFIED_PAYLOAD)
    }));
    MultiProducerWorkflow workflow = (MultiProducerWorkflow) channel.getWorkflowList().get(0);
    assertNotNull(workflow.toString());
  }

  @Test
  public void testUseProcessedMessage() throws Exception {
    MockMessageProducer mock1 = new MockMessageProducer();
    MockMessageProducer mock2 = new MockMessageProducer();
    MockChannel channel = createChannel(Arrays.asList(new AdaptrisMessageProducer[]
    {
        mock1, mock2
    }), Arrays.asList(new Service[]
    {
        new PayloadFromTemplateService().withTemplate(MODIFIED_PAYLOAD)
    }));
    try {
      MultiProducerWorkflow workflow = (MultiProducerWorkflow) channel.getWorkflowList().get(0);
      workflow.setUseProcessedMessage(true);
      channel.prepare();
      channel.requestStart();
      AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(ORIGINAL_PAYLOAD);
      workflow.onAdaptrisMessage(msg);
      assertEquals(1, mock1.getMessages().size());
      assertEquals(MODIFIED_PAYLOAD, mock1.getMessages().get(0).getContent());
      assertEquals(1, mock2.getMessages().size());
      assertEquals(MODIFIED_PAYLOAD, mock2.getMessages().get(0).getContent());
    }
    finally {
      channel.requestClose();
    }
  }

  @Test
  public void testHandleChannelUnavailable_bug2343() throws Exception {
    MockMessageProducer mock1 = new MockMessageProducer();
    MockMessageProducer mock2 = new MockMessageProducer();
    final MockChannel channel = createChannel(Arrays.asList(new AdaptrisMessageProducer[]
    {
        mock1, mock2
    }), Arrays.asList(new Service[]
    {
        new PayloadFromTemplateService().withTemplate(MODIFIED_PAYLOAD)
    }));
    try {
      MultiProducerWorkflow workflow = (MultiProducerWorkflow) channel.getWorkflowList().get(0);
      workflow.setUseProcessedMessage(true);
      workflow.setChannelUnavailableWaitInterval(new TimeInterval(1200L, TimeUnit.MILLISECONDS));
      channel.prepare();
      channel.requestStart();
      AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(ORIGINAL_PAYLOAD);
      channel.toggleAvailability(false);
      Timer t = new Timer();
      t.schedule(new TimerTask() {
        @Override
        public void run() {
          channel.toggleAvailability(true);
        }

      }, 500);
      workflow.onAdaptrisMessage(msg);
      log.debug("After workflow.onMessage()");
      assertEquals(1, mock1.getMessages().size());
      assertEquals(MODIFIED_PAYLOAD, mock1.getMessages().get(0).getContent());
      assertEquals(1, mock2.getMessages().size());
      assertEquals(MODIFIED_PAYLOAD, mock2.getMessages().get(0).getContent());
      t.cancel();
    }
    finally {
      channel.requestClose();
    }
  }

  @Test
  public void testUseOriginalMessage() throws Exception {
    MockMessageProducer mock1 = new MockMessageProducer();
    MockMessageProducer mock2 = new MockMessageProducer();
    MockChannel channel = createChannel(Arrays.asList(new AdaptrisMessageProducer[]
    {
        mock1, mock2
    }), Arrays.asList(new Service[]
    {
        new PayloadFromTemplateService().withTemplate(MODIFIED_PAYLOAD)
    }));
    try {
      MultiProducerWorkflow workflow = (MultiProducerWorkflow) channel.getWorkflowList().get(0);
      workflow.setUseProcessedMessage(false);
      channel.prepare();
      channel.requestStart();
      AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(ORIGINAL_PAYLOAD);
      workflow.onAdaptrisMessage(msg);
      assertEquals(1, mock1.getMessages().size());
      assertEquals(ORIGINAL_PAYLOAD, mock1.getMessages().get(0).getContent());
      assertEquals(1, mock2.getMessages().size());
      assertEquals(ORIGINAL_PAYLOAD, mock2.getMessages().get(0).getContent());
    }
    finally {
      channel.requestClose();
    }
  }

  @Test
  public void testOnMessage_SkipProducer() throws Exception {
    MockMessageProducer mock1 = new MockMessageProducer();
    MockMessageProducer mock2 = new MockMessageProducer();
    MockMessageProducer workflowProducer = new MockMessageProducer();
    MockMessageProducer serviceProducer = new MockMessageProducer();
    MockChannel channel = createChannel(Arrays.asList(new AdaptrisMessageProducer[]
    {
        mock1, mock2
    }), Arrays.asList(new Service[]
    {
        new StandaloneProducer(serviceProducer), new MockSkipProducerService()
    }));
    try {
      MultiProducerWorkflow workflow = (MultiProducerWorkflow) channel.getWorkflowList().get(0);
      workflow.setProducer(workflowProducer);
      workflow.setUseProcessedMessage(false);
      channel.prepare();
      channel.requestStart();
      AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(ORIGINAL_PAYLOAD);
      workflow.onAdaptrisMessage(msg);
      assertEquals(1, mock1.messageCount());
      assertEquals(1, mock2.messageCount());
      assertEquals(1, serviceProducer.messageCount());
      assertEquals(0, workflowProducer.messageCount());
    }
    finally {
      channel.requestClose();
    }
  }

  @Test
  public void testWorkflowWithInterceptor() throws Exception {
    MockMessageProducer mock1 = new MockMessageProducer();
    MockMessageProducer mock2 = new MockMessageProducer();
    MockWorkflowInterceptor interceptor = new MockWorkflowInterceptor();

    MockChannel channel = createChannel(Arrays.asList(new AdaptrisMessageProducer[]
    {
        mock1, mock2
    }), Arrays.asList(new Service[]
    {
        new PayloadFromTemplateService().withTemplate(MODIFIED_PAYLOAD)
    }));
    try {
      MultiProducerWorkflow workflow = (MultiProducerWorkflow) channel.getWorkflowList().get(0);
      workflow.setUseProcessedMessage(false);
      workflow.addInterceptor(interceptor);
      channel.prepare();
      channel.requestStart();
      AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(ORIGINAL_PAYLOAD);
      workflow.onAdaptrisMessage(msg);
      assertEquals(1, mock1.getMessages().size());
      assertEquals(ORIGINAL_PAYLOAD, mock1.getMessages().get(0).getContent());
      assertEquals(1, mock2.getMessages().size());
      assertEquals(ORIGINAL_PAYLOAD, mock2.getMessages().get(0).getContent());
      assertEquals(1, interceptor.messageCount());
    }
    finally {
      channel.requestClose();
    }
  }

  @Test
  public void testHandleServiceException() throws Exception {
    System.out.println("*** HandleServiceException START");
    MockMessageProducer mock1 = new MockMessageProducer();
    MockMessageProducer mock2 = new MockMessageProducer();
    MockMessageProducer meh = new MockMessageProducer();
    MockChannel channel = createChannel(Arrays.asList(new AdaptrisMessageProducer[]
    {
        mock1, mock2
    }), Arrays.asList(new Service[]
    {
      new ThrowExceptionService(new ConfiguredException("Fail"))
    }));
    channel.setMessageErrorHandler(new StandardProcessingExceptionHandler(
    		new ServiceList(new ArrayList(Arrays.asList(new Service[]
    	      {
    	        new StandaloneProducer(meh)
    	      })))));
    try {
      MultiProducerWorkflow workflow = (MultiProducerWorkflow) channel.getWorkflowList().get(0);
      workflow.setUseProcessedMessage(false);
      channel.prepare();
      channel.requestStart();
      AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(ORIGINAL_PAYLOAD);
      workflow.onAdaptrisMessage(msg);
      assertEquals(0, mock1.getMessages().size());
      assertEquals(0, mock2.getMessages().size());
      assertEquals(1, meh.getMessages().size());
      assertEquals(ORIGINAL_PAYLOAD, meh.getMessages().get(0).getContent());
    }
    finally {
      channel.requestClose();
    }
    System.out.println("*** HandleServiceException END");
  }

  @Test
  public void testHandleProduceException() throws Exception {
    MockMessageProducer mock1 = new MockMessageProducer();
    MockMessageProducer mock2 = new MockMessageProducer();
    MockMessageProducer meh = new MockMessageProducer();
    MockChannel channel = createChannel(Arrays.asList(new AdaptrisMessageProducer[]
    {
        mock1, mock2
    }), Arrays.asList(new Service[]
    {
        new PayloadFromTemplateService().withTemplate(MODIFIED_PAYLOAD)
    }));
    channel.setMessageErrorHandler(new StandardProcessingExceptionHandler(
    		new ServiceList(new ArrayList(Arrays.asList(new Service[]
    	    	      {
    	    	        new StandaloneProducer(meh)
    	    	      })))));
    try {
      MultiProducerWorkflow workflow = (MultiProducerWorkflow) channel.getWorkflowList().get(0);
      workflow.setProducer(new MockMessageProducer() {
        @Override
        protected void doProduce(AdaptrisMessage msg, String endpoint) throws ProduceException {
          throw new ProduceException();
        }
      });
      workflow.setUseProcessedMessage(false);
      channel.prepare();
      channel.requestStart();
      AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(ORIGINAL_PAYLOAD);
      workflow.onAdaptrisMessage(msg);
      assertEquals(0, mock1.getMessages().size());
      assertEquals(0, mock2.getMessages().size());
      assertEquals(1, meh.getMessages().size());
      assertEquals(ORIGINAL_PAYLOAD, meh.getMessages().get(0).getContent());
    }
    finally {
      channel.requestClose();
    }
  }

  @Test
  public void testHandleAdditionalProducerProduceException() throws Exception {
    MockMessageProducer mock1 = new MockMessageProducer() {
      @Override
      protected void doProduce(AdaptrisMessage msg, String endpoint) throws ProduceException {
        throw new ProduceException();
      }
    };
    MockMessageProducer mock2 = new MockMessageProducer();
    MockChannel channel = createChannel(Arrays.asList(new AdaptrisMessageProducer[]
    {
        mock1, mock2
    }), Arrays.asList(new Service[]
    {
        new PayloadFromTemplateService().withTemplate(MODIFIED_PAYLOAD)
    }));
    try {
      MultiProducerWorkflow workflow = (MultiProducerWorkflow) channel.getWorkflowList().get(0);
      workflow.setUseProcessedMessage(false);
      channel.prepare();
      channel.requestStart();
      AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(ORIGINAL_PAYLOAD);
      workflow.onAdaptrisMessage(msg);
      assertEquals(0, mock1.getMessages().size());
      assertEquals(1, mock2.getMessages().size());
      assertEquals(ORIGINAL_PAYLOAD, mock2.getMessages().get(0).getContent());
    }
    finally {
      channel.requestClose();
    }
  }

  @Test
  public void testHandleRuntimeException() throws Exception {
    MockMessageProducer mock1 = new MockMessageProducer();
    MockMessageProducer mock2 = new MockMessageProducer();
    MockMessageProducer meh = new MockMessageProducer();
    MockChannel channel = createChannel(Arrays.asList(new AdaptrisMessageProducer[]
    {
        mock1, mock2
    }), Arrays.asList(new Service[]
    {
        new PayloadFromTemplateService().withTemplate(MODIFIED_PAYLOAD)

    }));
    channel.setMessageErrorHandler(new StandardProcessingExceptionHandler(
    		new ServiceList(new ArrayList(Arrays.asList(new Service[]
  	      {
	        new StandaloneProducer(meh)
	      })))));
    try {
      MultiProducerWorkflow workflow = (MultiProducerWorkflow) channel.getWorkflowList().get(0);
      workflow.setUseProcessedMessage(false);
      workflow.setProducer(new MockMessageProducer() {
        @Override
        protected void doProduce(AdaptrisMessage msg, String endpoint) throws ProduceException {
          throw new RuntimeException();
        }
      });
      channel.prepare();
      channel.requestStart();
      AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(ORIGINAL_PAYLOAD);
      workflow.onAdaptrisMessage(msg);
      assertEquals(0, mock1.getMessages().size());
      assertEquals(0, mock2.getMessages().size());
      assertEquals(1, meh.getMessages().size());
      assertEquals(ORIGINAL_PAYLOAD, meh.getMessages().get(0).getContent());
    }
    finally {
      channel.requestClose();
    }
  }

  @Test
  public void testOnMessage_withConsumeLocation() throws Exception {
    MockMessageProducer producer = new MockMessageProducer();
    MockChannel channel = createChannel(Arrays.asList( new AdaptrisMessageProducer[] {
        producer}), Arrays.asList(new Service[] {new NullService()}));
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(PAYLOAD_1);
    msg.addMessageHeader(getName(), "hello world");
    MultiProducerWorkflow workflow = (MultiProducerWorkflow) channel.getWorkflowList().get(0);
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
    MockChannel channel = createChannel(Arrays.asList(new AdaptrisMessageProducer[] {producer}),
        Arrays.asList(new Service[] {new NullService()}));
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(PAYLOAD_1);
    MultiProducerWorkflow workflow = (MultiProducerWorkflow) channel.getWorkflowList().get(0);
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

  private MockChannel createChannel(List<AdaptrisMessageProducer> list, List<Service> services) throws Exception {
    MockChannel channel = new MockChannel();
    MultiProducerWorkflow workflow = new MultiProducerWorkflow();
    String uuid = UUID.randomUUID().toString();
    // workflow.getConsumer().setDestination(new ConfiguredConsumeDestination(uuid));
    for (AdaptrisMessageProducer p : list) {
      workflow.addStandaloneProducer(new StandaloneProducer(p));
    }
    workflow.getServiceCollection().addAll(services);
    channel.getWorkflowList().add(workflow);
    return channel;
  }

  @Override
  protected Object retrieveObjectForSampleConfig() {
    Channel c = new Channel();
    c.setUniqueId(UUID.randomUUID().toString());
    MultiProducerWorkflow workflow = new MultiProducerWorkflow();
    workflow.setUniqueId(UUID.randomUUID().toString());
    workflow.addStandaloneProducer(
        new StandaloneProducer(new NullMessageProducer().withUniqueID("Producer2")));
    workflow.addStandaloneProducer(
        new StandaloneProducer(new NullMessageProducer().withUniqueID("Producer3")));
    workflow.setProducer(new NullMessageProducer().withUniqueID("Producer1"));
    c.getWorkflowList().add(workflow);
    return c;
  }

  @Override
  protected String createBaseFileName(Object object) {
    return MultiProducerWorkflow.class.getName();
  }

  @Override
  protected MultiProducerWorkflow createWorkflowForGenericTests() {
    return new MultiProducerWorkflow();
  }

  @Test
  public void testAddStandaloneProducer() {
    MultiProducerWorkflow wf = new MultiProducerWorkflow();
    wf.addStandaloneProducer(new StandaloneProducer());
    assertEquals(1, wf.getStandaloneProducers().size());
    try {
      wf.addStandaloneProducer(null);
      fail();
    }
    catch (IllegalArgumentException e) {
    }
    assertEquals(1, wf.getStandaloneProducers().size());
  }

  @Test
  public void testSetStandaloneProducers() {
    MultiProducerWorkflow wf = new MultiProducerWorkflow();
    wf.setStandaloneProducers(Arrays.asList(new StandaloneProducer[]
    {
      new StandaloneProducer()
    }));
    assertEquals(1, wf.getStandaloneProducers().size());
    try {
      wf.setStandaloneProducers(null);
      fail();
    }
    catch (IllegalArgumentException e) {
    }
    assertEquals(1, wf.getStandaloneProducers().size());
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
