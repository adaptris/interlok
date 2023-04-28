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

package com.adaptris.core.services.splitter;

import static com.adaptris.core.services.splitter.MessageSplitterServiceImp.KEY_CURRENT_SPLIT_MESSAGE_COUNT;
import static com.adaptris.core.services.splitter.SplitterCase.createAdvanced;
import static com.adaptris.interlok.junit.scaffolding.services.ExampleServiceCase.execute;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.Iterator;

import org.junit.jupiter.api.Test;

import com.adaptris.core.AdaptrisMarshaller;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreException;
import com.adaptris.core.DefaultEventHandler;
import com.adaptris.core.DefaultMarshaller;
import com.adaptris.core.EventHandler;
import com.adaptris.core.EventHandlerAware;
import com.adaptris.core.MessageLifecycleEvent;
import com.adaptris.core.Service;
import com.adaptris.core.ServiceException;
import com.adaptris.core.ServiceImp;
import com.adaptris.core.ServiceList;
import com.adaptris.core.StandaloneProducer;
import com.adaptris.core.services.exception.ConfiguredException;
import com.adaptris.core.services.exception.ThrowExceptionService;
import com.adaptris.core.stubs.MockMessageProducer;
import com.adaptris.core.util.LifecycleHelper;
import com.adaptris.interlok.junit.scaffolding.services.ExampleServiceCase;

public class AdvancedMessageSplitterServiceTest extends BasicMessageSplitterServiceTest {


  @Override
  public void testServiceSetters() {
    AdvancedMessageSplitterService service = createForTests();
    try {
      service.setService(null);
      fail("Expected IllegalArgumentException");
    }
    catch (IllegalArgumentException e) {
      ;
    }
    try {
      service.setSplitter(null);
      fail("Expected IllegalArgumentException");
    }
    catch (IllegalArgumentException e) {
      ;
    }
  }

  @Override
  public void testInit() throws Exception {
    AdvancedMessageSplitterService service = createForTests();
    try {
      service.init();
      fail();
    }
    catch (CoreException expected) {

    }
    service.setSplitter(new LineCountSplitter());
    service.setService(new ServiceList());
    service.init();
    assertNotNull(service.wrappedServices());
    assertEquals(1, service.wrappedServices().length);
  }

  @Test
  public void testRedmineIssue1556() throws Exception {
    testEventHandlerPassedToServiceCollection();
  }

  @Test
  public void testEventHandlerPassedToServiceCollection() throws Exception {
    AdvancedMessageSplitterService service = createForTests();
    EventHandlerAwareService ehService = new EventHandlerAwareService();
    service.setService(ehService);
    service.setSplitter(new SimpleRegexpMessageSplitter("\\|"));
    DefaultEventHandler eh = new DefaultEventHandler();
    service.registerEventHandler(eh);
    LifecycleHelper.init(service);
    LifecycleHelper.start(service);
    assertEquals(eh, ehService.eventHandler());
  }

  @Test
  public void testSetSendEvents() throws Exception {
    AdvancedMessageSplitterService service = createForTests();
    assertNull(service.getSendEvents());
    assertFalse(service.sendEvents());
    service.setSendEvents(Boolean.TRUE);
    assertNotNull(service.getSendEvents());
    assertEquals(Boolean.TRUE, service.getSendEvents());
    assertTrue(service.sendEvents());
    service.setSendEvents(null);
    assertNull(service.getSendEvents());
    assertFalse(service.sendEvents());

  }

  @Test
  public void testDoServiceWithFailures_NullEventHandler() throws Exception {
    MockMessageProducer producer = new MockMessageProducer();
    AdaptrisMessage msg = createMessage(REGEXP_DATA);
    AdvancedMessageSplitterService service = createAdvanced(new SimpleRegexpMessageSplitter("\\|"), new Service[]
    {
        new ThrowExceptionService(new ConfiguredException("Fail")), new StandaloneProducer(producer)
    });
    service.registerEventHandler(null);
    try {
      ExampleServiceCase.execute(service, msg);
      fail("Expecting failure from AlwaysFailService");
    } catch (ServiceException expected) {
      ;
    }
  }

  @Test
  public void testDoServiceWithFailures_NullEventHandler_SendEvents() throws Exception {
    MockMessageProducer producer = new MockMessageProducer();
    AdaptrisMessage msg = createMessage(REGEXP_DATA);
    AdvancedMessageSplitterService service = createAdvanced(new SimpleRegexpMessageSplitter("\\|"), new Service[]
    {
        new ThrowExceptionService(new ConfiguredException("Fail")), new StandaloneProducer(producer)
    });
    service.registerEventHandler(null);
    service.setSendEvents(true);
    try {
      ExampleServiceCase.execute(service, msg);
      fail("Expecting failure from AlwaysFailService");
    } catch (ServiceException expected) {
      ;
    }
  }

  @Test
  public void testDoServiceWithFailures_SendEventsTrue() throws Exception {
    MockMessageProducer producer = new MockMessageProducer();
    AdaptrisMessage msg = createMessage(REGEXP_DATA);
    AdvancedMessageSplitterService service = createAdvanced(new SimpleRegexpMessageSplitter("\\|"), new Service[]
    {
        new ThrowExceptionService(new ConfiguredException("Fail")), new StandaloneProducer(producer)
    });
    DefaultEventHandler eh = new DefaultEventHandler();
    MockMessageProducer ehp = new MockMessageProducer();
    eh.setProducer(ehp);
    LifecycleHelper.initAndStart(eh);
    service.registerEventHandler(eh);
    service.setSendEvents(true);
    try {
      ExampleServiceCase.execute(service, msg);
      fail("Expecting failure from AlwaysFailService");
    } catch (ServiceException expected) {
      ;
    }
    LifecycleHelper.stopAndClose(eh);
    assertEvents(ehp, 1, MessageLifecycleEvent.class);
  }

  @Test
  public void testDoServiceWithFailures_SendEventsFalse() throws Exception {
    MockMessageProducer producer = new MockMessageProducer();
    AdaptrisMessage msg = createMessage(REGEXP_DATA);
    AdvancedMessageSplitterService service = createAdvanced(new SimpleRegexpMessageSplitter("\\|"), new Service[]
    {
        new ThrowExceptionService(new ConfiguredException("Fail")), new StandaloneProducer(producer)
    });
    DefaultEventHandler eh = new DefaultEventHandler();
    MockMessageProducer ehp = new MockMessageProducer();
    eh.setProducer(ehp);
    LifecycleHelper.initAndStart(eh);
    service.registerEventHandler(eh);
    service.setSendEvents(false);
    try {
      ExampleServiceCase.execute(service, msg);
      fail("Expecting failure from AlwaysFailService");
    } catch (ServiceException expected) {
      ;
    }
    LifecycleHelper.stopAndClose(eh);
    assertEvents(ehp, 0, MessageLifecycleEvent.class);
  }

  @Test
  public void testDoServiceWithEventHandler_Null() throws Exception {
    MockMessageProducer producer = new MockMessageProducer();
    AdvancedMessageSplitterService service = createServiceImpl(new SimpleRegexpMessageSplitter("\\|"), producer);
    AdaptrisMessage msg = createMessage(REGEXP_DATA);
    service.registerEventHandler(null);
    ExampleServiceCase.execute(service, msg);
    assertEquals(4, producer.getMessages().size());
    assertEquals(4, Integer.parseInt(msg.getMetadataValue(MessageSplitterServiceImp.KEY_SPLIT_MESSAGE_COUNT)));
    int count = 0;
    for (AdaptrisMessage m : producer.getMessages()) {
      count ++;
      assertEquals(count, Integer.parseInt(m.getMetadataValue(KEY_CURRENT_SPLIT_MESSAGE_COUNT)));
    }
  }

  @Test
  public void testDoServiceWithNullEventHandler_SendEventsTrue() throws Exception {
    MockMessageProducer producer = new MockMessageProducer();
    AdvancedMessageSplitterService service = createServiceImpl(new SimpleRegexpMessageSplitter("\\|"), producer);
    AdaptrisMessage msg = createMessage(REGEXP_DATA);
    service.registerEventHandler(null);
    service.setSendEvents(true);
    ExampleServiceCase.execute(service, msg);
    assertEquals(4, producer.getMessages().size());
    assertEquals(4,
        Integer.parseInt(msg.getMetadataValue(MessageSplitterServiceImp.KEY_SPLIT_MESSAGE_COUNT)));
    int count = 0;
    for (AdaptrisMessage m : producer.getMessages()) {
      count++;
      assertEquals(count, Integer.parseInt(m.getMetadataValue(KEY_CURRENT_SPLIT_MESSAGE_COUNT)));
    }
  }

  @Test
  public void testDoServiceWithEventHandler_SendEventsDefault() throws Exception {
    MockMessageProducer producer = new MockMessageProducer();
    AdvancedMessageSplitterService service = createServiceImpl(new SimpleRegexpMessageSplitter("\\|"), producer);
    AdaptrisMessage msg = createMessage(REGEXP_DATA);
    DefaultEventHandler eh = new DefaultEventHandler();
    MockMessageProducer ehp = new MockMessageProducer();
    eh.setProducer(ehp);
    eh.requestStart();
    service.registerEventHandler(eh);
    ExampleServiceCase.execute(service, msg);
    eh.requestClose();
    assertEquals(4, producer.getMessages().size());
    assertEquals(4,
        Integer.parseInt(msg.getMetadataValue(MessageSplitterServiceImp.KEY_SPLIT_MESSAGE_COUNT)));
    assertEvents(ehp, 0, MessageLifecycleEvent.class);

    int count = 0;
    for (AdaptrisMessage m : producer.getMessages()) {
      count++;
      assertEquals(count, Integer.parseInt(m.getMetadataValue(KEY_CURRENT_SPLIT_MESSAGE_COUNT)));
    }
  }

  @Test
  public void testDoServiceWithEventHandler_SendEventsTrue() throws Exception {
    MockMessageProducer producer = new MockMessageProducer();
    AdvancedMessageSplitterService service = createServiceImpl(new SimpleRegexpMessageSplitter("\\|"), producer);
    AdaptrisMessage msg = createMessage(REGEXP_DATA);
    DefaultEventHandler eh = new DefaultEventHandler();
    MockMessageProducer ehp = new MockMessageProducer();
    eh.setProducer(ehp);
    eh.requestStart();
    service.registerEventHandler(eh);
    service.setSendEvents(true);
    ExampleServiceCase.execute(service, msg);
    eh.requestClose();
    assertEquals(4, producer.getMessages().size());
    assertEquals(4, Integer.parseInt(msg.getMetadataValue(MessageSplitterServiceImp.KEY_SPLIT_MESSAGE_COUNT)));
    assertEvents(ehp, 4, MessageLifecycleEvent.class);
    int count = 0;
    for (AdaptrisMessage m : producer.getMessages()) {
      count ++;
      assertEquals(count, Integer.parseInt(m.getMetadataValue(KEY_CURRENT_SPLIT_MESSAGE_COUNT)));
    }
  }

  @Test
  public void testDoServiceWithEventHandlerSendEventsFalse() throws Exception {
    MockMessageProducer producer = new MockMessageProducer();
    AdvancedMessageSplitterService service = createServiceImpl(new SimpleRegexpMessageSplitter("\\|"), producer);
    AdaptrisMessage msg = createMessage(REGEXP_DATA);
    DefaultEventHandler eh = new DefaultEventHandler();
    MockMessageProducer ehp = new MockMessageProducer();
    eh.setProducer(ehp);
    eh.requestStart();
    service.registerEventHandler(eh);
    service.setSendEvents(false);
    ExampleServiceCase.execute(service, msg);
    eh.requestClose();
    assertEquals(4, producer.getMessages().size());
    assertEquals(4, Integer.parseInt(msg.getMetadataValue(MessageSplitterServiceImp.KEY_SPLIT_MESSAGE_COUNT)));
    assertEvents(ehp, 0, MessageLifecycleEvent.class);
    int count = 0;
    for (AdaptrisMessage m : producer.getMessages()) {
      count ++;
      assertEquals(count, Integer.parseInt(m.getMetadataValue(KEY_CURRENT_SPLIT_MESSAGE_COUNT)));
    }
  }

  @Test
  public void testDoServiceWithFailures() throws Exception {
    MockMessageProducer producer = new MockMessageProducer();
    AdaptrisMessage msg = createMessage(REGEXP_DATA);
    AdvancedMessageSplitterService service = createAdvanced(new SimpleRegexpMessageSplitter("\\|"), new Service[]
    {
        new ThrowExceptionService(new ConfiguredException("Fail")), new StandaloneProducer(producer)
    });
    try {
      ExampleServiceCase.execute(service, msg);
      fail("Expecting failure from AlwaysFailService");
    }
    catch (ServiceException expected) {
      ;
    }
  }

  @Test
  public void testDoServiceWithFailuresIgnored() throws Exception {
    MockMessageProducer producer = new MockMessageProducer();
    AdaptrisMessage msg = createMessage(REGEXP_DATA);
    AdvancedMessageSplitterService service = createAdvanced(new SimpleRegexpMessageSplitter("\\|"), new Service[]
    {
        new ThrowExceptionService(new ConfiguredException("Fail")), new StandaloneProducer(producer)
    });
    service.setIgnoreSplitMessageFailures(true);
    execute(service, msg);
    assertEquals(0, producer.getMessages().size());
    // We should still get 4 messages as the splitCount. They may not have been successfully
    // processed, but they were still results from the split!
    assertEquals(4, Integer.parseInt(msg.getMetadataValue(MessageSplitterServiceImp.KEY_SPLIT_MESSAGE_COUNT)));
    int count = 0;
    for (AdaptrisMessage m : producer.getMessages()) {
      count ++;
      assertEquals(count, Integer.parseInt(m.getMetadataValue(KEY_CURRENT_SPLIT_MESSAGE_COUNT)));
    }
  }

  @Override
  protected AdvancedMessageSplitterService createServiceImpl(MessageSplitter splitter, MockMessageProducer producer) {
    AdvancedMessageSplitterService service = new AdvancedMessageSplitterService();
    service.setSplitter(splitter);
    service.setService(new ServiceList(new Service[]
    {
      new StandaloneProducer(producer)
    }));
    return service;
  }

  protected AdvancedMessageSplitterService createForTests() {
    return new AdvancedMessageSplitterService();
  }

  private void assertEvents(MockMessageProducer eh, int msgCount, Class expectedEventClass) throws Exception {
    AdaptrisMarshaller cm = DefaultMarshaller.getDefaultMarshaller();
    assertEquals(msgCount, eh.getMessages().size());
    for (Iterator i = eh.getMessages().iterator(); i.hasNext();) {
      AdaptrisMessage m = (AdaptrisMessage) i.next();
      Object o = cm.unmarshal(m.getContent());
      assertEquals(expectedEventClass, o.getClass());
    }
  }

  private class EventHandlerAwareService extends ServiceImp implements EventHandlerAware {

    EventHandler evtHandler;

    public EventHandler eventHandler() {
      return evtHandler;
    }

    @Override
    public void registerEventHandler(EventHandler eh) {
      evtHandler = eh;
    }

    @Override
    public void doService(AdaptrisMessage msg) throws ServiceException {
    }

    @Override
    protected void initService() throws CoreException {

    }

    @Override
    protected void closeService() {
    }

    @Override
    public void prepare() throws CoreException {}
  }
}
