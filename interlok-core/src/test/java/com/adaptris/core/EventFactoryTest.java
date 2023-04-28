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

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.fail;

import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adaptris.core.event.AdapterCloseEvent;
import com.adaptris.core.event.AdapterInitEvent;
import com.adaptris.core.event.AdapterShutdownEvent;
import com.adaptris.core.event.AdapterStartEvent;
import com.adaptris.core.event.AdapterStopEvent;
import com.adaptris.core.event.ChannelRestartEvent;
import com.adaptris.core.event.StandardAdapterStartUpEvent;

@SuppressWarnings("deprecation")
public class EventFactoryTest extends EventFactory {

  private static final String[] ALL_EVENT_CLASSES = { AdapterCloseEvent.class.getName(),
      AdapterInitEvent.class.getName(), AdapterShutdownEvent.class.getName(), AdapterStartEvent.class.getName(),
      StandardAdapterStartUpEvent.class.getName(), DefaultAdapterStartUpEvent.class.getName(),
      AdapterStopEvent.class.getName(), ChannelRestartEvent.class.getName() };
  private transient Logger log = LoggerFactory.getLogger(BaseCase.class);

  @Test
  public void testCreateInvalidEvents() {
    String invalid = "com.adaptris.core.PingEventx";
    String nonEvent = "java.lang.String";
    String empty = "";

    try {
      EventFactory.create(invalid);
      fail("no CoreException from invalid class name");
    } catch (CoreException e) { /* ok */
    }

    try {
      EventFactory.create(nonEvent);
      fail("no CoreException from non-Event class (String)");
    } catch (CoreException e) { /* ok */
    }

    try {
      EventFactory.create(empty);
      fail("no CoreException from \"\"");
    } catch (CoreException e) { /* ok */
    }

    try {
      EventFactory.create((String) null);
      fail("no CoreException from null");
    } catch (CoreException e) { /* ok */
    }
  }

  @Test
  public void testCreateEvent() throws Exception {
    Assertions.assertThrows(CoreException.class, () -> {
      for (int i = 0; i < ALL_EVENT_CLASSES.length; i++) {
        Event e = (Event) EventFactory.create(Class.forName(ALL_EVENT_CLASSES[i]));
        assertNotSame(ALL_EVENT_CLASSES[i], e.getNameSpace(), EventNameSpaceConstants.EVENT);
      }
      EventFactory.create(FailingEvent.class);
    });
  }

  @Test
  public void testCreateEventFromString() throws Exception {
    for (int i = 0; i < ALL_EVENT_CLASSES.length; i++) {
      Event e = EventFactory.create(ALL_EVENT_CLASSES[i]);
      assertNotSame(ALL_EVENT_CLASSES[i], e.getNameSpace(), EventNameSpaceConstants.EVENT);
    }
  }

  @Test
  public void testXmlRoundTrip() throws Exception {
    AdaptrisMarshaller m = DefaultMarshaller.getDefaultMarshaller();
    for (int i = 0; i < ALL_EVENT_CLASSES.length; i++) {
      Event input = EventFactory.create(ALL_EVENT_CLASSES[i]);
      setSetters(input);
      String xml = m.marshal(input);
      log.trace(xml);
      Event output = (Event) m.unmarshal(xml);
      BaseCase.assertRoundtripEquality(input, output);
    }
  }

  @Test
  public void testCreateNamespace() throws Exception {
    for (int i = 0; i < ALL_EVENT_CLASSES.length; i++) {
      Event e = (Event) EventFactory.create(Class.forName(ALL_EVENT_CLASSES[i]));
      assertNotNull(e.createNameSpace());
    }
  }

  @Test
  public void testEventConstructor() {
    try {
      new EventStub(null);
      fail("allows namespace=null");

    } catch (IllegalArgumentException expected) {

    }
    try {
      new EventStub("");
      fail("allows namespace=''");

    } catch (IllegalArgumentException expected) {

    }

  }

  @Test
  public void testUniqueId() throws Exception {
    for (int i = 0; i < ALL_EVENT_CLASSES.length; i++) {
      Event e = (Event) EventFactory.create(Class.forName(ALL_EVENT_CLASSES[i]));
      try {
        e.setUniqueId("");
        fail("allows unique-id=''");
      } catch (IllegalArgumentException expected) {

      }
      try {
        e.setUniqueId(null);
        fail("allows unique-id=null");
      } catch (IllegalArgumentException expected) {

      }
    }
  }

  private void setSetters(Event event) throws Exception {
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    String text = sdf.format(new Date());
    Class clazz = event.getClass();
    List<String> list = getSimpleSetterMethods(clazz);
    for (String m : list) {
      try {
        Method method = clazz.getMethod(m, new Class[] { String.class });
        method.invoke(event, new Object[] { text });
      } catch (NoSuchMethodException e) {
        ;
      }
    }
  }

  private static List<String> getSimpleSetterMethods(Class c) {
    List<String> list = new ArrayList<String>();
    for (Method m : c.getMethods()) {
      if (m.getName().startsWith("set") && m.getParameterTypes().length == 1) {
        list.add(m.getName());
      }
    }
    return list;
  }

  private class EventStub extends Event {

    public EventStub(String s) {
      super(s);
    }
  }

  private class FailingEvent extends Event {

    public FailingEvent(String s) throws Exception {
      super(s);
      throw new Exception();
    }
  }
}
