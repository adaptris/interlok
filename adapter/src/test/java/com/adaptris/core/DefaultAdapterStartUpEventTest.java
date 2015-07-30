package com.adaptris.core;


@SuppressWarnings("deprecation")
public class DefaultAdapterStartUpEventTest extends BaseCase {

  public DefaultAdapterStartUpEventTest(java.lang.String testName) {
    super(testName);
  }

  public void testSetAdapter() throws Exception {
    DefaultAdapterStartUpEvent evt = new DefaultAdapterStartUpEvent();
    Adapter adapter = new Adapter();
    evt.setAdapter(adapter);
  }

  public void testMarshalledHasNoAdapter() throws Exception {
    DefaultAdapterStartUpEvent event = new DefaultAdapterStartUpEvent();
    Adapter adapter = new Adapter();
    event.setAdapter(adapter);
    AdaptrisMarshaller m = DefaultMarshaller.getDefaultMarshaller();
    String s = m.marshal(event);
    System.out.println("Event marshalled - " + s);
    DefaultAdapterStartUpEvent mEvent = (DefaultAdapterStartUpEvent) m.unmarshal(s);
    System.out.println("mEvent unmarshalled - " + mEvent.toString());
  }
}
