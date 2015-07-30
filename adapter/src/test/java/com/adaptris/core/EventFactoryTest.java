package com.adaptris.core;

import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.adaptris.core.event.AdapterCloseEvent;
import com.adaptris.core.event.AdapterInitEvent;
import com.adaptris.core.event.AdapterShutdownEvent;
import com.adaptris.core.event.AdapterStartEvent;
import com.adaptris.core.event.AdapterStopEvent;
import com.adaptris.core.event.ChannelRestartEvent;
import com.adaptris.core.event.StandardAdapterStartUpEvent;

@SuppressWarnings("deprecation")
public class EventFactoryTest extends BaseCase {

  private static final String[] ALL_EVENT_CLASSES =
  {
      AdapterCloseEvent.class.getName(), AdapterInitEvent.class.getName(),
      AdapterShutdownEvent.class.getName(), AdapterStartEvent.class.getName(), StandardAdapterStartUpEvent.class.getName(),
 DefaultAdapterStartUpEvent.class.getName(),
      AdapterStopEvent.class.getName(), ChannelRestartEvent.class.getName()
  };

  public EventFactoryTest(java.lang.String testName) {
    super(testName);
  }

  public void testCreateInvalidEvents() {
    String invalid = "com.adaptris.core.PingEventx";
    String nonEvent = "java.lang.String";
    String empty = "";

    try {
      EventFactory.create(invalid);
      fail("no CoreException from invalid class name");
    }
    catch (CoreException e) { /* ok */
    }

    try {
      EventFactory.create(nonEvent);
      fail("no CoreException from non-Event class (String)");
    }
    catch (CoreException e) { /* ok */
    }

    try {
      EventFactory.create(empty);
      fail("no CoreException from \"\"");
    }
    catch (CoreException e) { /* ok */
    }

    try {
      EventFactory.create((String) null);
      fail("no CoreException from null");
    }
    catch (CoreException e) { /* ok */
    }
  }

  public void testCreateEvent() throws Exception {
    for (int i = 0; i < ALL_EVENT_CLASSES.length; i++) {
      Event e = (Event) EventFactory.create(Class.forName(ALL_EVENT_CLASSES[i]));
      assertNotSame(ALL_EVENT_CLASSES[i], e.getNameSpace(), EventNameSpaceConstants.EVENT);
    }
  }

  public void testCreateEventFromString() throws Exception {
    for (int i = 0; i < ALL_EVENT_CLASSES.length; i++) {
      Event e = EventFactory.create(ALL_EVENT_CLASSES[i]);
      assertNotSame(ALL_EVENT_CLASSES[i], e.getNameSpace(), EventNameSpaceConstants.EVENT);
    }
  }

  public void testXmlRoundTrip() throws Exception {
    AdaptrisMarshaller m = DefaultMarshaller.getDefaultMarshaller();
    for (int i = 0; i < ALL_EVENT_CLASSES.length; i++) {
      Event input = EventFactory.create(ALL_EVENT_CLASSES[i]);
      setSetters(input);
      String xml = m.marshal(input);
      log.trace(xml);
      Event output = (Event) m.unmarshal(xml);
      assertRoundtripEquality(input, output);
    }
  }

  public void testCreateNamespace() throws Exception {
    for (int i = 0; i < ALL_EVENT_CLASSES.length; i++) {
      Event e = (Event) EventFactory.create(Class.forName(ALL_EVENT_CLASSES[i]));
      assertNotNull(e.createNameSpace());
    }
  }

  public void testEventConstructor() {
    try {
      new EventStub(null);
      fail("allows namespace=null");

    }
    catch (IllegalArgumentException expected) {

    }
    try {
      new EventStub("");
      fail("allows namespace=''");

    }
    catch (IllegalArgumentException expected) {

    }

  }

  public void testUniqueId() throws Exception {
    for (int i = 0; i < ALL_EVENT_CLASSES.length; i++) {
      Event e = (Event) EventFactory.create(Class.forName(ALL_EVENT_CLASSES[i]));
      try {
        e.setUniqueId("");
        fail("allows unique-id=''");
      }
      catch (IllegalArgumentException expected) {

      }
      try {
        e.setUniqueId(null);
        fail("allows unique-id=null");
      }
      catch (IllegalArgumentException expected) {

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
        Method method = clazz.getMethod(m, new Class[]
        {
          String.class
        });
        method.invoke(event, new Object[]
        {
          text
        });
      }
      catch (NoSuchMethodException e) {
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
}