package com.adaptris.core;

import junit.framework.TestCase;

/**
 * <p>
 * <code>SerializableAdaptrisMessageTest</code>
 */
public class SerializableAdaptrisMessageTest extends TestCase {

  public void setUp() throws Exception {
    
  }
  
  public void tearDown() throws Exception {
    
  }
  
  public void testSerializePayload() throws Exception {
    SerializableAdaptrisMessage message = new SerializableAdaptrisMessage();
    message.setPayload("SomePayload");
    
    SerializableAdaptrisMessage unmarshalledMessage = roundTrip(message);
    
    assertTrue(message.getPayload().equals(unmarshalledMessage.getPayload()));
  }
  
  public void testSerializeID() throws Exception {
    SerializableAdaptrisMessage message = new SerializableAdaptrisMessage();
    message.setUniqueId("MyUniqueID");
    
    SerializableAdaptrisMessage unmarshalledMessage = roundTrip(message);
    
    assertTrue(message.getUniqueId().equals(unmarshalledMessage.getUniqueId()));
  }
  
  public void testSerializeMetadata() throws Exception {
    SerializableAdaptrisMessage message = new SerializableAdaptrisMessage();
    message.addMetadata("Key1", "Value1");
    message.addMetadata("Key2", "Value2");
    message.addMetadata("Key3", "Value3");
    
    SerializableAdaptrisMessage unmarshalledMessage = roundTrip(message);
    
    assertTrue(message.getMetadata().equals(unmarshalledMessage.getMetadata()));
  }
  
  public void testSerializeAll() throws Exception {
    SerializableAdaptrisMessage message = new SerializableAdaptrisMessage();
    message.setUniqueId("MyUniqueID");
    message.setPayload("SomePayload");
    message.addMetadata("Key1", "Value1");
    message.addMetadata("Key2", "Value2");
    message.addMetadata("Key3", "Value3");
    message.setPayloadEncoding("SomeEncoding");
    
    SerializableAdaptrisMessage unmarshalledMessage = roundTrip(message);
    
    assertTrue(message.getUniqueId().equals(unmarshalledMessage.getUniqueId()));
    assertTrue(message.getPayload().equals(unmarshalledMessage.getPayload()));
    assertTrue(message.getMetadata().equals(unmarshalledMessage.getMetadata()));
    assertTrue(message.getPayloadEncoding().equals(unmarshalledMessage.getPayloadEncoding()));
  }
  
  private SerializableAdaptrisMessage roundTrip(SerializableAdaptrisMessage message) throws Exception {
    XStreamMarshaller marshaller = new XStreamMarshaller();
    String result = marshaller.marshal(message);
    
    SerializableAdaptrisMessage unmarshalledMessage = (SerializableAdaptrisMessage) marshaller.unmarshal(result);
    
    return unmarshalledMessage;
  }
}
