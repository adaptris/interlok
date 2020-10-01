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

package com.adaptris.interlok.junit.scaffolding;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import java.util.Iterator;
import java.util.Map;
import org.junit.Test;
import com.adaptris.core.Adapter;
import com.adaptris.core.AdapterMarshallerFactory;
import com.adaptris.core.AdapterMarshallerFactory.MarshallingOutput;
import com.adaptris.core.AdapterXStreamMarshallerFactory;
import com.adaptris.core.AdaptrisMarshaller;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageSender;
import com.adaptris.core.CoreException;
import com.adaptris.core.DefaultMarshaller;
import com.adaptris.core.DefaultMessageFactory;
import com.adaptris.core.Event;
import com.adaptris.core.EventFactory;
import com.adaptris.core.EventHandler;
import com.adaptris.core.EventHandlerBase;
import com.adaptris.core.MetadataCollection;
import com.adaptris.core.MetadataElement;
import com.adaptris.core.StandaloneProducer;
import com.adaptris.core.XStreamJsonMarshaller;
import com.adaptris.core.event.AdapterCloseEvent;
import com.adaptris.core.lms.FileBackedMessageFactory;
import com.adaptris.core.stubs.MockMessageProducer;

/**
 * <p>
 * Extension to <code>BaseCase</code> for <code>Service</code>s which provides a
 * method for marshaling sample XML config.
 * </p>
 */
public abstract class ExampleEventHandlerCase<T extends EventHandlerBase>
    extends ExampleConfigGenerator {

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
    if (PROPERTIES.getProperty(BASE_DIR_KEY) != null) {
      setBaseDir(PROPERTIES.getProperty(BASE_DIR_KEY));
    }
    marshallerFactory = AdapterXStreamMarshallerFactory.getInstance();
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

  protected abstract T applyConfiguration(T eh) throws CoreException;

  protected abstract T newEventHandler(String uid) throws CoreException;

  protected MockMessageProducer getProducer(T eh) throws CoreException {
    AdaptrisMessageSender ams = eh.retrieveProducer();
    if (ams instanceof StandaloneProducer) {
      return (MockMessageProducer) ((StandaloneProducer) ams).getProducer();
    }
    return (MockMessageProducer) ams;
  }

  protected void doAssertions(T eh, int msgCount, Class expectedEventClass) throws Exception {
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

  @Test
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

  @Test
  public void testSetMarshaller() throws Exception {
    EventHandlerBase eventHandler = applyConfiguration(newEventHandler(getName()));

    XStreamJsonMarshaller marshaller = (XStreamJsonMarshaller) marshallerFactory.createMarshaller(MarshallingOutput.JSON);
    eventHandler.setMarshaller(marshaller);
    assertEquals(marshaller, eventHandler.getMarshaller());
    assertEquals(marshaller, eventHandler.currentMarshaller());
    eventHandler.setMarshaller(null);
    assertEquals(DefaultMarshaller.getDefaultMarshaller(), eventHandler.currentMarshaller());
  }

  @Test
  public void testSendEvent() throws Exception {
    Event e = EventFactory.create(AdapterCloseEvent.class);
    T eh = applyConfiguration(newEventHandler(getName()));
    try {
      eh.requestStart();
      eh.send(e);
      doAssertions(eh, 1, e.getClass());
    }
    finally {
      eh.requestClose();
    }
  }

  @Test
  public void testSendEvent_WithProperties() throws Exception {
    Event e = EventFactory.create(AdapterCloseEvent.class);
    T eh = applyConfiguration(newEventHandler(getName()));
    Map<String, String> properties = MetadataCollection.asMap(new MetadataCollection(new MetadataElement("hello", "world")));
    try {
      eh.requestStart();
      eh.send(e, properties);
      doAssertions(eh, 1, e.getClass());
      MockMessageProducer p = getProducer(eh);
      AdaptrisMessage msg = p.getMessages().get(0);
      assertTrue(msg.headersContainsKey("hello"));
      assertEquals("world", msg.getMetadataValue("hello"));
    }
    finally {
      eh.requestClose();
    }
  }


  @Test
  public void testSendMultipleEvent() throws Exception {
    int count = 10;
    Event e = EventFactory.create(AdapterCloseEvent.class);
    T eh = applyConfiguration(newEventHandler(getName()));
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

  @Test
  public void testSetMessageFactory() throws Exception {
    T eh = newEventHandler(getName());
    assertNull(eh.getMessageFactory());
    assertEquals(DefaultMessageFactory.class, eh.currentMessageFactory().getClass());
    eh.setMessageFactory(new FileBackedMessageFactory());
    assertNotNull(eh.getMessageFactory());
    assertEquals(FileBackedMessageFactory.class, eh.getMessageFactory().getClass());
    assertEquals(FileBackedMessageFactory.class, eh.currentMessageFactory().getClass());
  }

  @Test
  public void testLogAllException() throws Exception {
    T eh = newEventHandler(getName());
    assertNull(eh.getLogAllExceptions());
    eh.setLogAllExceptions(Boolean.FALSE);
    assertNotNull(eh.getLogAllExceptions());
    assertEquals(Boolean.FALSE, eh.getLogAllExceptions());
  }

  @Test
  public void testShutdownWait() throws Exception {
    T eh = newEventHandler(getName());
    assertNull(eh.getShutdownWaitSeconds());
    assertEquals(60, eh.shutdownWaitSeconds());
    eh.setShutdownWaitSeconds(90);
    assertNotNull(eh.getShutdownWaitSeconds());
    assertEquals(90, eh.shutdownWaitSeconds());

  }
}
