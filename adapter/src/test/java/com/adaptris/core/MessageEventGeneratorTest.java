package com.adaptris.core;

public class MessageEventGeneratorTest extends BaseCase {

  public MessageEventGeneratorTest(String name) {
    super(name);
  }

  public void testValidConfirmationId() throws Exception {
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance()
        .newMessage();

    // spoof conf id...
    msg.addObjectMetadata(MessageEventGenerator.CONFIRMATION_ID_KEY, "123");
    // spoof conf id...

    Service s2 = new NullService();

    ServiceList services = new ServiceList();
    services.setOutOfStateHandler(new NullOutOfStateHandler());
    
    services.addService(s2);
    services.doService(msg);

    MessageLifecycleEvent evt = msg.getMessageLifecycleEvent();
    MleMarker mle = evt.getMleMarkers().get(0);

    assertEquals("123", mle.getConfirmationId());
  }

  public void testNullConfirmationId() throws Exception {
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance()
        .newMessage();

    Service s2 = new NullService();

    ServiceList services = new ServiceList();
    services.setOutOfStateHandler(new NullOutOfStateHandler());
    
    services.addService(s2);
    services.doService(msg);

    MessageLifecycleEvent evt = msg.getMessageLifecycleEvent();
    MleMarker mle = evt.getMleMarkers().get(0);

    assertNull(mle.getConfirmationId());
  }

  public void testNullConfirmationIdNoKey() throws Exception {
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance()
        .newMessage();

    Service s2 = new NullService();

    ServiceList services = new ServiceList();
    services.setOutOfStateHandler(new NullOutOfStateHandler());
    
    services.addService(s2);
    services.doService(msg);

    MessageLifecycleEvent evt = msg.getMessageLifecycleEvent();
    MleMarker mle = evt.getMleMarkers().get(0);

    assertNull(mle.getConfirmationId());
  }

  public void testXmlRoundTrip() throws Exception {

    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage();

    Service s2 = new NullService();
    ServiceList services = new ServiceList();
    services.setOutOfStateHandler(new NullOutOfStateHandler());
    
    services.addService(s2);
    services.doService(msg);
    MessageLifecycleEvent input = msg.getMessageLifecycleEvent();
    AdaptrisMarshaller m = DefaultMarshaller.getDefaultMarshaller();
    String xml = m.marshal(input);
    MessageLifecycleEvent output = (MessageLifecycleEvent) m.unmarshal(xml);
    assertRoundtripEquality(input, output);
  }
}
