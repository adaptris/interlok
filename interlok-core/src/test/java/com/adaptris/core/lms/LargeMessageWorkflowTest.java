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

package com.adaptris.core.lms;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.UUID;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.Channel;
import com.adaptris.core.ConfiguredConsumeDestination;
import com.adaptris.core.ConfiguredProduceDestination;
import com.adaptris.core.CoreConstants;
import com.adaptris.core.MetadataElement;
import com.adaptris.core.ProduceDestination;
import com.adaptris.core.ProduceException;
import com.adaptris.core.Service;
import com.adaptris.core.ServiceList;
import com.adaptris.core.StandaloneProducer;
import com.adaptris.core.StandardProcessingExceptionHandler;
import com.adaptris.core.StandardWorkflowTest;
import com.adaptris.core.services.exception.ConfiguredException;
import com.adaptris.core.services.exception.ThrowExceptionService;
import com.adaptris.core.services.metadata.AddMetadataService;
import com.adaptris.core.services.metadata.PayloadFromMetadataService;
import com.adaptris.core.stubs.MockChannel;
import com.adaptris.core.stubs.MockMessageProducer;

@SuppressWarnings("deprecation")
public class LargeMessageWorkflowTest extends StandardWorkflowTest {

  public LargeMessageWorkflowTest(java.lang.String testName) {
    super(testName);
  }

  @Override
  protected void setUp() {

  }

  public void testCleanup() {
    FilePurge.getInstance().purge();
  }

  @Override
  public void testServiceException() throws Exception {

    MockMessageProducer producer = new MockMessageProducer();
    MockMessageProducer meh = new MockMessageProducer();
    MockChannel channel = createChannel(producer, Arrays.asList(new Service[]
    {
        new AddMetadataService(Arrays.asList(new MetadataElement[]
        {
          new MetadataElement(METADATA_KEY, METADATA_VALUE)
        })), new PayloadFromMetadataService(PAYLOAD_2), new ThrowExceptionService(new ConfiguredException("Fail"))
    }));
    try {
      LargeMessageWorkflow workflow = (LargeMessageWorkflow) channel.getWorkflowList().get(0);
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
      for (AdaptrisMessage m : meh.getMessages()) {
        assertEquals(PAYLOAD_2, m.getContent());
        assertTrue("Contains correct metadata key", m.containsKey(METADATA_KEY));
        assertEquals(METADATA_VALUE, m.getMetadataValue(METADATA_KEY));
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

  @Override
  public void testProduceException() throws Exception {

    MockMessageProducer producer = new MockMessageProducer() {
      @Override
      public void produce(AdaptrisMessage msg) throws ProduceException {
        throw new ProduceException();
      }

      @Override
      public void produce(AdaptrisMessage msg, ProduceDestination destination) throws ProduceException {
        throw new ProduceException();
      }
    };
    ;
    MockMessageProducer meh = new MockMessageProducer();
    MockChannel channel = createChannel(producer, Arrays.asList(new Service[]
    {
        new AddMetadataService(Arrays.asList(new MetadataElement[]
        {
          new MetadataElement(METADATA_KEY, METADATA_VALUE)
        })), new PayloadFromMetadataService(PAYLOAD_2)
    }));
    try {
      LargeMessageWorkflow workflow = (LargeMessageWorkflow) channel.getWorkflowList().get(0);
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
      for (AdaptrisMessage m : meh.getMessages()) {
        assertEquals(PAYLOAD_2, m.getContent());
        assertTrue("Contains correct metadata key", m.containsKey(METADATA_KEY));
        assertEquals(METADATA_VALUE, m.getMetadataValue(METADATA_KEY));
      }
    }
    finally {
      stop(channel);
    }
  }

  @Override
  public void testRuntimeException() throws Exception {

    MockMessageProducer producer = new MockMessageProducer() {
      @Override
      public void produce(AdaptrisMessage msg) throws ProduceException {
        throw new RuntimeException();
      }

      @Override
      public void produce(AdaptrisMessage msg, ProduceDestination destination) throws ProduceException {
        throw new RuntimeException();
      }
    };
    ;
    MockMessageProducer meh = new MockMessageProducer();
    MockChannel channel = createChannel(producer, Arrays.asList(new Service[]
    {
        new AddMetadataService(Arrays.asList(new MetadataElement[]
        {
          new MetadataElement(METADATA_KEY, METADATA_VALUE)
        })), new PayloadFromMetadataService(PAYLOAD_2)
    }));
    try {
      LargeMessageWorkflow workflow = (LargeMessageWorkflow) channel.getWorkflowList().get(0);
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
      for (AdaptrisMessage m : meh.getMessages()) {
        assertEquals(PAYLOAD_2, m.getContent());
        assertTrue("Contains correct metadata key", m.containsKey(METADATA_KEY));
        assertEquals(METADATA_VALUE, m.getMetadataValue(METADATA_KEY));
      }
    }
    finally {
      stop(channel);
    }
  }

  @Override
  protected Object retrieveObjectForSampleConfig() {
    Channel c = new Channel();
    LargeMessageWorkflow workflow = new LargeMessageWorkflow();
    workflow.setConsumer(new LargeFsConsumer(new ConfiguredConsumeDestination("file:////path/to/consume/directory")));
    workflow.setProducer(new LargeFsProducer(new ConfiguredProduceDestination("file:////path/to/produce/directory")));
    c.getWorkflowList().add(workflow);
    c.setUniqueId(UUID.randomUUID().toString());
    workflow.setUniqueId(UUID.randomUUID().toString());
    return c;
  }

  @Override
  protected String createBaseFileName(Object object) {
    return LargeMessageWorkflow.class.getName();
  }

  @Override
  protected LargeMessageWorkflow createWorkflowForGenericTests() {
    return new LargeMessageWorkflow();
  }
}
