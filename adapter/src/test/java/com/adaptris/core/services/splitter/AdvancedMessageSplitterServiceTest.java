package com.adaptris.core.services.splitter;

import static com.adaptris.core.ServiceCase.execute;
import static com.adaptris.core.services.splitter.MessageSplitterServiceImp.KEY_CURRENT_SPLIT_MESSAGE_COUNT;
import static com.adaptris.core.services.splitter.SplitterCase.createAdvanced;

import java.util.Iterator;

import com.adaptris.core.AdaptrisMarshaller;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreException;
import com.adaptris.core.DefaultEventHandler;
import com.adaptris.core.DefaultMarshaller;
import com.adaptris.core.EventHandler;
import com.adaptris.core.EventHandlerAware;
import com.adaptris.core.MessageLifecycleEvent;
import com.adaptris.core.Service;
import com.adaptris.core.ServiceCase;
import com.adaptris.core.ServiceException;
import com.adaptris.core.ServiceImp;
import com.adaptris.core.ServiceList;
import com.adaptris.core.StandaloneProducer;
import com.adaptris.core.services.exception.ConfiguredException;
import com.adaptris.core.services.exception.ThrowExceptionService;
import com.adaptris.core.stubs.LicenseStub;
import com.adaptris.core.stubs.MockMessageProducer;
import com.adaptris.core.util.LifecycleHelper;
import com.adaptris.util.license.License;

public class AdvancedMessageSplitterServiceTest extends BasicMessageSplitterServiceTest {

  public AdvancedMessageSplitterServiceTest(String name) {
    super(name);
  }

  @Override
  public void testServiceSetters() {
    AdvancedMessageSplitterService service = new AdvancedMessageSplitterService();
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
    AdvancedMessageSplitterService service = new AdvancedMessageSplitterService();
    try {
      service.init();
      fail();
    }
    catch (CoreException expected) {

    }
    service.setSplitter(new LineCountSplitter());
    service.setService(new ServiceList());
    service.init();
  }

  public void testRedmineIssue1556() throws Exception {
    testEventHandlerPassedToServiceCollection();
  }

  public void testEventHandlerPassedToServiceCollection() throws Exception {
    AdvancedMessageSplitterService service = new AdvancedMessageSplitterService();
    EventHandlerAwareService ehService = new EventHandlerAwareService();
    service.setService(ehService);
    service.setSplitter(new SimpleRegexpMessageSplitter("\\|"));
    DefaultEventHandler eh = new DefaultEventHandler();
    service.registerEventHandler(eh);
    LifecycleHelper.init(service);
    LifecycleHelper.start(service);
    assertEquals(eh, ehService.eventHandler());
  }

  public void testSetSendEvents() throws Exception {
    AdvancedMessageSplitterService service = new AdvancedMessageSplitterService();
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

  public void testDoServiceWithEventHandler_SendEventsDefault() throws Exception {
    MockMessageProducer producer = new MockMessageProducer();
    AdvancedMessageSplitterService service = createServiceImpl(new SimpleRegexpMessageSplitter("\\|"), producer);
    AdaptrisMessage msg = createMessage(REGEXP_DATA);
    DefaultEventHandler eh = new DefaultEventHandler();
    MockMessageProducer ehp = new MockMessageProducer();
    eh.setProducer(ehp);
    eh.requestStart();
    service.registerEventHandler(eh);
    ServiceCase.execute(service, msg);
    eh.requestClose();
    assertEquals("Number of messages", 4, producer.getMessages().size());
    assertEquals("splitCount metadata", 4, Integer.parseInt(msg.getMetadataValue(MessageSplitterServiceImp.KEY_SPLIT_MESSAGE_COUNT)));
    assertEvents(ehp, 0, MessageLifecycleEvent.class);
    
    int count = 0;
    for (AdaptrisMessage m : producer.getMessages()) {
      count ++;
      assertEquals(count, Integer.parseInt(m.getMetadataValue(KEY_CURRENT_SPLIT_MESSAGE_COUNT)));
    }
  }

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
    ServiceCase.execute(service, msg);
    eh.requestClose();
    assertEquals("Number of messages", 4, producer.getMessages().size());
    assertEquals("splitCount metadata", 4, Integer.parseInt(msg.getMetadataValue(MessageSplitterServiceImp.KEY_SPLIT_MESSAGE_COUNT)));
    assertEvents(ehp, 4, MessageLifecycleEvent.class);
    int count = 0;
    for (AdaptrisMessage m : producer.getMessages()) {
      count ++;
      assertEquals(count, Integer.parseInt(m.getMetadataValue(KEY_CURRENT_SPLIT_MESSAGE_COUNT)));
    }
  }

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
    ServiceCase.execute(service, msg);
    eh.requestClose();
    assertEquals("Number of messages", 4, producer.getMessages().size());
    assertEquals("splitCount metadata", 4, Integer.parseInt(msg.getMetadataValue(MessageSplitterServiceImp.KEY_SPLIT_MESSAGE_COUNT)));
    assertEvents(ehp, 0, MessageLifecycleEvent.class);
    int count = 0;
    for (AdaptrisMessage m : producer.getMessages()) {
      count ++;
      assertEquals(count, Integer.parseInt(m.getMetadataValue(KEY_CURRENT_SPLIT_MESSAGE_COUNT)));
    }
  }

  public void testDoServiceWithFailures() throws Exception {
    MockMessageProducer producer = new MockMessageProducer();
    AdaptrisMessage msg = createMessage(REGEXP_DATA);
    AdvancedMessageSplitterService service = createAdvanced(new SimpleRegexpMessageSplitter("\\|"), new Service[]
    {
        new ThrowExceptionService(new ConfiguredException("Fail")), new StandaloneProducer(producer)
    });
    try {
      ServiceCase.execute(service, msg);
      fail("Expecting failure from AlwaysFailService");
    }
    catch (ServiceException expected) {
      ;
    }
  }

  public void testDoServiceWithFailuresIgnored() throws Exception {
    MockMessageProducer producer = new MockMessageProducer();
    AdaptrisMessage msg = createMessage(REGEXP_DATA);
    AdvancedMessageSplitterService service = createAdvanced(new SimpleRegexpMessageSplitter("\\|"), new Service[]
    {
        new ThrowExceptionService(new ConfiguredException("Fail")), new StandaloneProducer(producer)
    });
    service.setIgnoreSplitMessageFailures(true);
    execute(service, msg);
    assertEquals("Number of messages", 0, producer.getMessages().size());
    // We should still get 4 messages as the splitCount. They may not have been successfully 
    // processed, but they were still results from the split!
    assertEquals("splitCount metadata", 4, Integer.parseInt(msg.getMetadataValue(MessageSplitterServiceImp.KEY_SPLIT_MESSAGE_COUNT)));
    int count = 0;
    for (AdaptrisMessage m : producer.getMessages()) {
      count ++;
      assertEquals(count, Integer.parseInt(m.getMetadataValue(KEY_CURRENT_SPLIT_MESSAGE_COUNT)));
    }
  }

  public void testIsEnabledUsesEmbeddedService() throws Exception {
    AdvancedMessageSplitterService service = new AdvancedMessageSplitterService();
    assertTrue(service.isEnabled(new LicenseStub()));
    service.setService(new ServiceList() {
      @Override
      public boolean isEnabled(License license) throws CoreException {
        return false;
      }
    });
    assertFalse(service.isEnabled(new LicenseStub()));
  }

  public void testRedmine1004() throws Exception {
    testIsEnabledUsesEmbeddedService();
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

  private void assertEvents(MockMessageProducer eh, int msgCount, Class expectedEventClass) throws Exception {
    AdaptrisMarshaller cm = DefaultMarshaller.getDefaultMarshaller();
    assertEquals("Should have " + msgCount + " produced message", msgCount, eh.getMessages().size());
    for (Iterator i = eh.getMessages().iterator(); i.hasNext();) {
      AdaptrisMessage m = (AdaptrisMessage) i.next();
      Object o = cm.unmarshal(m.getStringPayload());
      assertEquals("Classname", expectedEventClass, o.getClass());
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
    public void init() throws CoreException {
    }

    @Override
    public void close() {
    }

    @Override
    public boolean isEnabled(License license) throws CoreException {
      return true;
    }
  }
}
