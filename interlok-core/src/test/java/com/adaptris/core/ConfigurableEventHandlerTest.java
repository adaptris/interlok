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

import com.adaptris.core.event.AdapterCloseEvent;
import com.adaptris.core.jms.JmsConnection;
import com.adaptris.core.jms.PtpProducer;
import com.adaptris.core.jms.jndi.StandardJndiImplementation;
import com.adaptris.core.stubs.FailFirstMockMessageProducer;
import com.adaptris.core.stubs.MockMessageProducer;
import com.adaptris.core.util.LifecycleHelper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

public class ConfigurableEventHandlerTest
    extends com.adaptris.interlok.junit.scaffolding.ExampleEventHandlerCase<ConfigurableEventHandler> {


  @Override
  protected ConfigurableEventHandler newEventHandler(String uniqueId) throws CoreException {
    ConfigurableEventHandler result = new ConfigurableEventHandler();
    result.setUniqueId(uniqueId);
    return result;
  }

  @Override
  protected ConfigurableEventHandler applyConfiguration(ConfigurableEventHandler eh) throws CoreException {
    ConfigurableEventHandler eventHandler = eh;
    eventHandler.setUniqueId(UUID.randomUUID().toString());
    eventHandler.setConnection(new NullConnection());
    eventHandler.setProducer(new MockMessageProducer());
    eventHandler.setMarshaller(DefaultMarshaller.getDefaultMarshaller());
    return eventHandler;
  }

  @Test
  public void testSetters() throws Exception {
    ConfigurableEventHandler eventHandler = newEventHandler("testSetters");
    try {
      eventHandler.setConnection(null);
      fail();
    }
    catch (IllegalArgumentException expected) {

    }
    try {
      eventHandler.setProducer(null);
      fail();
    }
    catch (IllegalArgumentException expected) {

    }
  }

  @Test
  public void testSendEventWithException() throws Exception {
    Event e = EventFactory.create(AdapterCloseEvent.class);
    ConfigurableEventHandler eh = applyConfiguration(newEventHandler("testSendEventWithException"));
    eh.setProducer(new FailFirstMockMessageProducer(2));
    try {
      LifecycleHelper.initAndStart(eh);
      eh.send(e);
      eh.setLogAllExceptions(true);
      eh.send(e);
    } finally {
      eh.requestClose();
    }
    doAssertions(eh, 0, e.getClass());
  }

  @Test
  public void testGettersWhenClosed() throws Exception {
    DefaultEventHandler input = applyConfiguration(newEventHandler("testGettersWhenClosed"));
    AdaptrisMessageProducer p1 = input.getProducer();
    AdaptrisConnection con1 = input.getConnection();
    input.requestStart();
    input.requestClose();
    AdaptrisMessageProducer p2 = input.getProducer();
    AdaptrisConnection con2 = input.getConnection();
    assertEquals(p1, p2, "Producers when closed");
    assertEquals(con1, con2, "Connections when closed");
    assertRoundtripEquality(p1, p2);
    assertRoundtripEquality(con1, con2);
  }

  @Test
  public void testSettersWhenClosed() throws Exception {
    DefaultEventHandler input = applyConfiguration(newEventHandler("testSettersWhenClosed"));
    AdaptrisMessageProducer p1 = input.getProducer();
    AdaptrisConnection con1 = input.getConnection();
    input.requestStart();
    input.requestClose();
    input.setConnection(con1);
    input.setProducer(p1);
  }

  @Test
  public void testSettersWhenInitialised() throws Exception {
    DefaultEventHandler input = applyConfiguration(newEventHandler("testSettersWhenInitialised"));
    AdaptrisMessageProducer p1 = input.getProducer();
    AdaptrisConnection con1 = input.getConnection();
    input.requestInit();
    try {
      input.setConnection(con1);
      fail("Should not be able to reset connection once initialised");
    }
    catch (IllegalStateException expected) {
    }
    try {
      input.setProducer(p1);
      fail("Should not be able to reset producer once initialised");
    }
    catch (IllegalStateException expected) {
    }
  }

  @Test
 public void testSettersWhenStarted() throws Exception {
    DefaultEventHandler input = applyConfiguration(newEventHandler("testSettersWhenStarted"));
    AdaptrisMessageProducer p1 = input.getProducer();
    AdaptrisConnection con1 = input.getConnection();
    input.requestStart();
    try {
      input.setConnection(con1);
      fail("Should not be able to reset connection once initialised");
    }
    catch (IllegalStateException expected) {
    }
    try {
      input.setProducer(p1);
      fail("Should not be able to reset producer once initialised");
    }
    catch (IllegalStateException expected) {
    }
  }

  @Test
  public void testSettersWhenStopped() throws Exception {
    DefaultEventHandler input = applyConfiguration(newEventHandler("testSettersWhenStopped"));
    AdaptrisMessageProducer p1 = input.getProducer();
    AdaptrisConnection con1 = input.getConnection();
    input.requestStart();
    input.requestStop();
    try {
      input.setConnection(con1);
      fail("Should not be able to reset connection once initialised");
    }
    catch (IllegalStateException expected) {
    }
    try {
      input.setProducer(p1);
      fail("Should not be able to reset producer once initialised");
    }
    catch (IllegalStateException expected) {
    }
  }

  @Test
  public void testBackReferences() throws Exception {
    DefaultEventHandler evh = new DefaultEventHandler();
    NullConnection conn = new NullConnection();
    evh.setConnection(conn);
    assertEquals(conn, evh.getConnection());
    // Back references don't exist until "init".
    try {
      LifecycleHelper.init(evh);

      assertEquals(1, conn.retrieveExceptionListeners().size());
      assertTrue(evh == conn.retrieveExceptionListeners().toArray()[0]);
    }
    finally {
      LifecycleHelper.close(evh);
    }
  }

  @Test
  public void testEventMatcher() throws Exception {
    Event e = EventFactory.create(AdapterCloseEvent.class);
    assertEquals(e.getClass().getCanonicalName(), EventMatcher.EventMatchType.TYPE.getProperty(e));
    assertEquals(e.getSourceId(), EventMatcher.EventMatchType.SOURCE_ID.getProperty(e));
    assertEquals(e.getDestinationId(), EventMatcher.EventMatchType.DESTINATION_ID.getProperty(e));
    assertEquals(e.getNameSpace(), EventMatcher.EventMatchType.NAMESPACE.getProperty(e));
  }

  @Test
  public void testResolveEventSender() throws Exception {
    Event e = EventFactory.create(AdapterCloseEvent.class);
    ConfigurableEventHandler eh = applyConfiguration(newEventHandler("testResolveEventSender"));
    // with no rules, it should return the default producer
    assertEquals(eh.getProducer(), eh.resolveEventSender(e));

    StandaloneProducer p1 = new StandaloneProducer();

    // add a non matching rule
    eh.getRules().add(
            new ConfigurableEventHandler.Rule(
                    new RegexEventMatcher(
                            "com.adaptris.core.event.AdapterCloseEvent1",
                            Set.of(EventMatcher.EventMatchType.TYPE.name())
                    ),
                    p1
            )
    );
    // should still match default
    assertEquals(eh.getProducer(), eh.resolveEventSender(e));
    StandaloneProducer p2 = new StandaloneProducer();
    eh.getRules().add(
            new ConfigurableEventHandler.Rule(
                    new RegexEventMatcher(
                            "com.adaptris.core.event.AdapterCloseEvent",
                            Set.of(EventMatcher.EventMatchType.TYPE.name())
                    ),
                    p2
            )
    );
    assertEquals(p2, eh.resolveEventSender(e));

    // order of rules matters
    StandaloneProducer p3 = new StandaloneProducer();
    eh.getRules().add(0,
            new ConfigurableEventHandler.Rule(
                    new RegexEventMatcher(
                            "com.adaptris.core.event.*",
                            Set.of(EventMatcher.EventMatchType.TYPE.name())
                    ),
                    p3
            )
    );
    assertEquals(p3, eh.resolveEventSender(e));

    // non matching event
    Event e1 = EventFactory.create(MessageLifecycleEvent.class);
    assertEquals(eh.getProducer(), eh.resolveEventSender(e1));

    // match on namespace with multiple match types configured
    StandaloneProducer p4 = new StandaloneProducer();
    eh.getRules().add(
            new ConfigurableEventHandler.Rule(
                    new RegexEventMatcher(
                            e1.getNameSpace(),
                            Set.of(EventMatcher.EventMatchType.TYPE.name(), EventMatcher.EventMatchType.NAMESPACE.name())
                    ),
                    p4
            )
    );
    assertEquals(p4, eh.resolveEventSender(e1));

  }

  @Test
  public void testComponentStates() throws Exception {
    ConfigurableEventHandler evh = applyConfiguration(newEventHandler("testResolveEventSender"));
    evh.getRules().add(new ConfigurableEventHandler.Rule(
            Mockito.mock(EventMatcher.class),
            Mockito.mock(StandaloneProducer.class)
    ));
    evh.getRules().add(new ConfigurableEventHandler.Rule(
            Mockito.mock(EventMatcher.class),
            Mockito.mock(StandaloneProducer.class)
    ));

    try {
      LifecycleHelper.init(evh);
      LifecycleHelper.prepare(evh);
      LifecycleHelper.start(evh);
      LifecycleHelper.stop(evh);
      evh.getRules().forEach(rule -> {
        try {
          Mockito.verify(rule.getStandaloneProducer()).init();
          Mockito.verify(rule.getStandaloneProducer()).prepare();
          Mockito.verify(rule.getStandaloneProducer()).start();
          Mockito.verify(rule.getStandaloneProducer()).stop();
        } catch (CoreException e) {
          throw new RuntimeException(e);
        }
      });
    }
    finally {
      LifecycleHelper.close(evh);
      evh.getRules().forEach(rule -> {
        Mockito.verify(rule.getStandaloneProducer()).close();
      });
    }
  }

    /**
     * @see ExampleConfigCase#retrieveObjectForSampleConfig()
     */
  @Override
  protected Object retrieveObjectForSampleConfig() {
    Adapter result = null;

    try {
      AdaptrisMessageProducer p = new PtpProducer().withQueue("publishEventsTo");
      DefaultEventHandler eh = new DefaultEventHandler();
      eh.setConnection(new JmsConnection(new StandardJndiImplementation("MyConnectionFactoryName")));
      eh.setProducer(p);
      eh.setMarshaller(DefaultMarshaller.getDefaultMarshaller());
      result = new Adapter();
      result.setChannelList(new ChannelList());
      result.setEventHandler(eh);
      result.setUniqueId(UUID.randomUUID().toString());
    }
    catch (Exception e) {
      throw new RuntimeException(e);
    }
    return result;
  }
}
