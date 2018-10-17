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

import java.util.Iterator;

import com.adaptris.core.AdapterMarshallerFactory.MarshallingOutput;
import com.adaptris.core.event.AdapterCloseEvent;
import com.adaptris.core.stubs.MockMessageProducer;

/**
 * <p>
 * Extension to <code>BaseCase</code> for <code>Service</code>s which provides a
 * method for marshaling sample XML config.
 * </p>
 */
public abstract class ExampleEventHandlerCase extends ExampleConfigCase {

  // private static final String CONFIG_REQUEST_EVENT = "<?xml version=\"1.0\"?>" + "<config-request-event>"
  // + "<unique-id>xxx-yyy-zzz</unique-id>" + "<destination-id>partnera</destination-id>" + "<source-id>sample_client</source-id>"
  // + "</config-request-event>";
  //
  // private static final String PING_EVENT = "<?xml version=\"1.0\"?>\n" + "<ping-event>\n" +
  // "<unique-id>xxx-yyy-zzz</unique-id>\n"
  // + "<destination-id>partnera</destination-id>\n" + "<source-id>sample_client</source-id>\n"
  // + "<direct-reply-to xsi:type=\"java:com.adaptris.core.ConfiguredProduceDestination\">\n"
  // + "<destination>destination</destination>\n" + "</direct-reply-to>\n" + "</ping-event>\n";

  /**
   * Key in unit-test.properties that defines where example goes unless overriden {@link #setBaseDir(String)}.
   * 
   */
  public static final String BASE_DIR_KEY = "EventHandlerCase.baseDir";

  protected AdapterMarshallerFactory marshallerFactory;

  public ExampleEventHandlerCase() {
    super();

    if (PROPERTIES.getProperty(BASE_DIR_KEY) != null) {
      setBaseDir(PROPERTIES.getProperty(BASE_DIR_KEY));
    }
    marshallerFactory = AdapterXStreamMarshallerFactory.getInstance();
  }

  public ExampleEventHandlerCase(String name) {
    this();
    setName(name);
  }
  

  @Override
  protected String createExampleXml(Object object) throws Exception {
    String result = getExampleCommentHeader(object);

    Adapter w = (Adapter) object;

    result = result + configMarshaller.marshal(w);
    return result;
  }

  @Override
  protected String createBaseFileName(Object object) {
    return ((Adapter) object).getEventHandler().getClass().getName();
  }

  protected abstract EventHandler applyConfiguration(EventHandler eh) throws CoreException;

  protected abstract EventHandler newEventHandler(String uid) throws CoreException;

  protected MockMessageProducer getProducer(EventHandler eh) throws CoreException {
    AdaptrisMessageSender ams = ((EventHandlerBase) eh).retrieveProducer();
    if (ams instanceof StandaloneProducer) {
      return (MockMessageProducer) ((StandaloneProducer) ams).getProducer();
    }
    return (MockMessageProducer) ams;
  }

  protected void doAssertions(EventHandler eh, int msgCount, Class expectedEventClass) throws Exception {
    AdaptrisMarshaller cm = DefaultMarshaller.getDefaultMarshaller();
    MockMessageProducer producer = getProducer(eh);
    waitForMessages(producer, msgCount);
    assertEquals("Should have " + msgCount + " produced message", msgCount, producer.getMessages().size());
    for (Iterator i = producer.getMessages().iterator(); i.hasNext();) {
      AdaptrisMessage m = (AdaptrisMessage) i.next();
      Object o = cm.unmarshal(m.getContent());
      assertEquals("Classname", expectedEventClass, o.getClass());
    }
  }

  public void testLifecycle() throws Exception {
    EventHandler eventHandler = applyConfiguration(newEventHandler("testLifecycle"));
    eventHandler.requestInit();
    eventHandler.requestInit();
    eventHandler.requestStart();
    eventHandler.requestStart();
    eventHandler.requestStop();
    eventHandler.requestStop();
    eventHandler.requestClose();
    eventHandler.requestClose();
  }

  public void testSetMarshaller() throws Exception {
    EventHandlerBase eventHandler = (EventHandlerBase) applyConfiguration(newEventHandler(getName()));
    
    XStreamJsonMarshaller marshaller = (XStreamJsonMarshaller) marshallerFactory.createMarshaller(MarshallingOutput.JSON);
    eventHandler.setMarshaller(marshaller);
    assertEquals(marshaller, eventHandler.getMarshaller());
    assertEquals(marshaller, eventHandler.currentMarshaller());
    eventHandler.setMarshaller(null);
    assertEquals(DefaultMarshaller.getDefaultMarshaller(), eventHandler.currentMarshaller());
  }

  public void testCreateEvent() throws Exception {
    EventHandlerBase eventHandler = (EventHandlerBase) applyConfiguration(newEventHandler(getName()));
    eventHandler.setMarshaller(DefaultMarshaller.getDefaultMarshaller());
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage();
    try {
      eventHandler.createEvent(msg);
      fail("Should have caused exception");
    }
    catch (CoreException e) {
      // pass
    }
    msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(createEvent());
    eventHandler.createEvent(msg);
  }



  public void testSendEvent() throws Exception {
    Event e = EventFactory.create(AdapterCloseEvent.class);
    EventHandler eh = applyConfiguration(newEventHandler(getName()));
    try {
      eh.requestStart();
      eh.send(e);
      doAssertions(eh, 1, e.getClass());
    }
    finally {
      eh.requestClose();
    }
  }

  public void testSendEventWithDestination() throws Exception {
    Event e = EventFactory.create(AdapterCloseEvent.class);
    EventHandler eh = applyConfiguration(newEventHandler(getName()));
    try {
      eh.requestStart();
      eh.send(e, new ConfiguredProduceDestination("destination"));
      doAssertions(eh, 1, e.getClass());
    }
    finally {
      eh.requestClose();

    }
  }

  public void testSendMultipleEvent() throws Exception {
    int count = 10;
    Event e = EventFactory.create(AdapterCloseEvent.class);
    EventHandler eh = applyConfiguration(newEventHandler(getName()));
    try {
      eh.requestStart();
      for (int i = 0; i < count; i++) {
        eh.send(e);
      }
      doAssertions(eh, count, e.getClass());
    }
    finally {
      eh.requestClose();
    }
  }

  private AdaptrisMessage createMessage(Event evt) throws CoreException {
    AdaptrisMessage result = AdaptrisMessageFactory.getDefaultInstance().newMessage(defaultMarshaller.marshal(evt));
    result.addMetadata(CoreConstants.EVENT_NAME_SPACE_KEY, evt.getNameSpace());
    result.addMetadata(CoreConstants.EVENT_CLASS, evt.getClass().getName());
    return result;
  }

  public String createEvent() throws CoreException {
    AdaptrisMarshaller m = DefaultMarshaller.getDefaultMarshaller();
    AdapterCloseEvent e = EventFactory.create(AdapterCloseEvent.class);
    e.setUniqueId("xxx-yyy-zzz");
    e.setDestinationId("partnera");
    e.setSourceId("sample_client");
    return m.marshal(e);
  }

}
