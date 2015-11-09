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

import com.adaptris.core.fs.FsProducer;
import com.adaptris.core.services.exception.ConfiguredException;
import com.adaptris.core.services.exception.ThrowExceptionService;
import com.adaptris.core.services.metadata.AddMetadataService;
import com.adaptris.core.stubs.EventHandlerAwareService;
import com.adaptris.core.stubs.MockMessageConsumer;
import com.adaptris.core.stubs.MockMessageProducer;
import com.adaptris.core.util.LifecycleHelper;

public class StandardProcessingExceptionHandlerTest extends ExampleErrorHandlerCase {

  public StandardProcessingExceptionHandlerTest(java.lang.String testName) {
    super(testName);
  }

  public void testNoServices() throws Exception {
    StandardProcessingExceptionHandler meh = new StandardProcessingExceptionHandler();
    try {
      AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage("XXXX");
      start(meh);
      meh.handleProcessingException(msg);
    }
    finally {
      stop(meh);
    }
  }

  public void testSingleProducer() throws Exception {
    StandardProcessingExceptionHandler meh = new StandardProcessingExceptionHandler();
    MockMessageProducer producer = new MockMessageProducer();
    try {
      AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage("XXXX");
      meh.setProcessingExceptionService(new ServiceList(new ArrayList<Service>(Arrays.asList(new Service[]
      {
        new StandaloneProducer(producer)
      }))));
      start(meh);
      meh.handleProcessingException(msg);
      assertTrue("Make sure all produced", producer.getMessages().size() == 1);
    }
    finally {
      stop(meh);
    }
  }

  public void testServiceList() throws Exception {
    StandardProcessingExceptionHandler meh = new StandardProcessingExceptionHandler();
    MockMessageProducer producer = new MockMessageProducer();
    try {
      AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage("XXXX");
      meh.setProcessingExceptionService(new ServiceList(Arrays.asList(new Service[]
      {
          new StandaloneProducer(producer), new StandaloneProducer(producer)
      })));
      start(meh);
      meh.handleProcessingException(msg);
      log.debug("Number of messages " + producer.getMessages().size());
      assertTrue("Make sure all produced", producer.getMessages().size() == 2);
    }
    finally {
      stop(meh);
    }
  }

  public void testAdapter_HasEventHandler() throws Exception {
    EventHandlerAwareService srv = new EventHandlerAwareService();
    StandardProcessingExceptionHandler meh = new StandardProcessingExceptionHandler(srv);
    DefaultEventHandler eventHandler = new DefaultEventHandler();
    Adapter a = AdapterTest.createAdapter(getName(), eventHandler);
    a.setMessageErrorHandler(meh);
    try {
      LifecycleHelper.init(a);
      assertEquals(eventHandler, srv.retrieveEventHandler());
    }
    finally {
      LifecycleHelper.close(a);
    }
  }

  public void testChannel_HasEventHandler() throws Exception {
    EventHandlerAwareService srv = new EventHandlerAwareService();
    StandardProcessingExceptionHandler meh = new StandardProcessingExceptionHandler(srv);
    DefaultEventHandler eventHandler = new DefaultEventHandler();
    Adapter adapter = AdapterTest.createAdapter(getName(), eventHandler);
    Channel c = new Channel();
    c.setMessageErrorHandler(meh);
    adapter.getChannelList().add(c);
    try {
      LifecycleHelper.init(adapter);
      assertEquals(eventHandler, srv.retrieveEventHandler());
    }
    finally {
      LifecycleHelper.close(adapter);
    }
  }

  public void testWorkflow_HasEventHandler() throws Exception {
    EventHandlerAwareService srv = new EventHandlerAwareService();
    StandardProcessingExceptionHandler meh = new StandardProcessingExceptionHandler(srv);
    DefaultEventHandler eventHandler = new DefaultEventHandler();
    Adapter a = AdapterTest.createAdapter(getName(), eventHandler);
    Channel c = new Channel();
    StandardWorkflow wf = new StandardWorkflow();
    wf.setMessageErrorHandler(meh);
    c.getWorkflowList().add(wf);
    a.getChannelList().add(c);
    try {
      LifecycleHelper.init(a);
      assertEquals(eventHandler, srv.retrieveEventHandler());
    }
    finally {
      LifecycleHelper.close(a);
    }
  }

  public void testMultipleErrorHandlers_AlwaysHandle_True() throws Exception {
    MockMessageProducer aep = new MockMessageProducer();
    MockMessageProducer wep = new MockMessageProducer();
    MockMessageProducer cep = new MockMessageProducer();
    StandardProcessingExceptionHandler aeh = new StandardProcessingExceptionHandler(new StandaloneProducer(aep));
    aeh.setAlwaysHandleException(true);
    StandardProcessingExceptionHandler ceh = new StandardProcessingExceptionHandler(new StandaloneProducer(cep));
    ceh.setAlwaysHandleException(true);
    StandardProcessingExceptionHandler weh = new StandardProcessingExceptionHandler(new StandaloneProducer(wep));
    Adapter adapter = AdapterTest.createAdapter(getName());
    adapter.setMessageErrorHandler(aeh);
    MockMessageConsumer consumer = new MockMessageConsumer();
    adapter.getChannelList().clear();
    adapter.getChannelList().add(createChannel(consumer, ceh, weh, new ThrowExceptionService(new ConfiguredException(getName()))));
    try {
      start(adapter);
      consumer.submitMessage(AdaptrisMessageFactory.getDefaultInstance().newMessage());
      assertEquals(1, aep.messageCount());
      assertEquals(1, cep.messageCount());
      assertEquals(1, wep.messageCount());
    }
    finally {
      stop(adapter);
    }
  }

  public void testMultipleErrorHandlers_AlwaysHandle_False() throws Exception {
    MockMessageProducer aep = new MockMessageProducer();
    MockMessageProducer wep = new MockMessageProducer();
    MockMessageProducer cep = new MockMessageProducer();
    StandardProcessingExceptionHandler aeh = new StandardProcessingExceptionHandler(new StandaloneProducer(aep));
    aeh.setAlwaysHandleException(false);
    StandardProcessingExceptionHandler weh = new StandardProcessingExceptionHandler(new StandaloneProducer(wep));
    StandardProcessingExceptionHandler ceh = new StandardProcessingExceptionHandler(new StandaloneProducer(cep));
    Adapter adapter = AdapterTest.createAdapter(getName());
    adapter.setMessageErrorHandler(aeh);
    MockMessageConsumer consumer = new MockMessageConsumer();
    adapter.getChannelList().clear();
    adapter.getChannelList().add(createChannel(consumer, ceh, weh, new ThrowExceptionService(new ConfiguredException(getName()))));
    try {
      start(adapter);
      consumer.submitMessage(AdaptrisMessageFactory.getDefaultInstance().newMessage());
      assertEquals(0, aep.messageCount());
      assertEquals(0, cep.messageCount());
      assertEquals(1, wep.messageCount());
    }
    finally {
      stop(adapter);
    }
  }

  public void testServiceListWithErrorWithServiceId() throws Exception {
    StandardProcessingExceptionHandler meh = new StandardProcessingExceptionHandler();
    MockMessageProducer producer = new MockMessageProducer();
    try {
      AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage("XXXX");
      ServiceList services = new ServiceList(Arrays.asList(new Service[]
      {
          new ThrowExceptionService(new ConfiguredException("Always Fail")), new StandaloneProducer(producer)
      }));
      services.setUniqueId("FailingServiceList");
      meh.setProcessingExceptionService(services);
      start(meh);
      meh.handleProcessingException(msg);
      assertTrue("Make sure none produced", producer.getMessages().size() == 0);
    }
    finally {
      stop(meh);
    }
  }

  public void testServiceListWithErrorWithBlankServiceId() throws Exception {
    StandardProcessingExceptionHandler meh = new StandardProcessingExceptionHandler();
    MockMessageProducer producer = new MockMessageProducer();
    try {
      AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage("XXXX");
      ServiceList services = new ServiceList(Arrays.asList(new Service[]
      {
          new ThrowExceptionService(new ConfiguredException("Always Fail")), new StandaloneProducer(producer)
      }));
      services.setUniqueId("");
      meh.setProcessingExceptionService(services);
      start(meh);
      meh.handleProcessingException(msg);
      assertTrue("Make sure none produced", producer.getMessages().size() == 0);
    }
    finally {
      stop(meh);
    }
  }

  public void testServiceListWithAddMetadata() throws Exception {
    StandardProcessingExceptionHandler meh = new StandardProcessingExceptionHandler();
    MockMessageProducer producer = new MockMessageProducer();
    try {
      AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage("XXXX");
      meh.setProcessingExceptionService(new ServiceList(Arrays.asList(new Service[]
      {
          new StandaloneProducer(producer), new AddMetadataService(Arrays.asList(new MetadataElement[]
          {
            new MetadataElement("myKey", "myValue")
          })), new StandaloneProducer(producer)
      })));
      start(meh);
      meh.handleProcessingException(msg);
      log.debug("Number of messages " + producer.getMessages().size());
      assertTrue("Make sure all produced", producer.getMessages().size() == 2);
      assertTrue(producer.getMessages().get(1).containsKey("myKey"));
      assertEquals("myValue", producer.getMessages().get(1).getMetadataValue("myKey"));
    }
    finally {
      stop(meh);
    }

  }

  @Override
  protected StandardProcessingExceptionHandler createForExamples() {
    StandardProcessingExceptionHandler meh = new StandardProcessingExceptionHandler();
    FsProducer producer = new FsProducer(new ConfiguredProduceDestination("/path/to/bad-directory"));
    producer.setEncoder(new MimeEncoder(false, null, null));
    meh.setProcessingExceptionService(new ServiceList(Arrays.asList(new Service[]
    {
      new StandaloneProducer(producer)
    })));
    return meh;
  }

  @Override
  protected String createBaseFileName(Object object) {
    return StandardProcessingExceptionHandler.class.getCanonicalName();
  }


}
