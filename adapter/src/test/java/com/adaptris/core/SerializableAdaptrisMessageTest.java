package com.adaptris.core;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import junit.framework.TestCase;

import com.adaptris.core.stubs.StubSerializableMessage;
import com.adaptris.interlok.types.SerializableMessage;
import com.adaptris.util.KeyValuePair;
import com.adaptris.util.KeyValuePairSet;

/**
 * <p>
 * <code>SerializableAdaptrisMessageTest</code>
 */
public class SerializableAdaptrisMessageTest extends TestCase {

  private static final String VALUE3 = "Value3";
  private static final String VALUE2 = "Value2";
  private static final String VALUE1 = "Value1";
  private static final String KEY4 = "Key4";
  private static final String KEY3 = "Key3";
  private static final String KEY2 = "Key2";
  private static final String KEY1 = "Key1";

  public void setUp() throws Exception {
    
  }
  
  public void tearDown() throws Exception {
    
  }
  
  public void testConstructors() throws Exception {
    SerializableAdaptrisMessage message = new SerializableAdaptrisMessage(getName());
    assertEquals(getName(), message.getUniqueId());
    assertEquals(0, message.getMetadata().size());
    assertNull(message.getPayload());

    message = new SerializableAdaptrisMessage(getName(), "my payload");
    assertEquals(getName(), message.getUniqueId());
    assertEquals(0, message.getMetadata().size());
    assertEquals("my payload", message.getPayload());
    
    SerializableMessage stub = new StubSerializableMessage();
    stub.setUniqueId(getName());
    stub.setPayload("my payload");
    message = new SerializableAdaptrisMessage(stub);
    assertEquals(getName(), message.getUniqueId());
    assertEquals(0, message.getMetadata().size());
    assertEquals("my payload", message.getPayload());


  }

  public void testSetMetadata() throws Exception {
    SerializableAdaptrisMessage message = new SerializableAdaptrisMessage();
    KeyValuePairSet kvps = new KeyValuePairSet();
    kvps.addKeyValuePair(new KeyValuePair(KEY1, VALUE1));
    kvps.addKeyValuePair(new KeyValuePair(KEY2, VALUE2));
    kvps.addKeyValuePair(new KeyValuePair(KEY3, VALUE3));
    message.setMetadata(kvps);
    assertEquals(3, message.getMetadata().size());
    assertTrue(message.getMessageHeaders().containsKey(KEY1));
    assertTrue(message.getMessageHeaders().containsKey(KEY2));
    assertTrue(message.getMessageHeaders().containsKey(KEY3));
    assertFalse(message.getMessageHeaders().containsKey(KEY4));
    message.setMetadata((KeyValuePairSet) null);
    assertEquals(0, message.getMetadata().size());

  }

  public void testSetMetadata_MetadataElementSet() throws Exception {
    SerializableAdaptrisMessage message = new SerializableAdaptrisMessage();
    HashSet<MetadataElement> set = new HashSet<>();
    set.add(new MetadataElement(KEY1, VALUE1));
    set.add(new MetadataElement(KEY2, VALUE2));
    set.add(new MetadataElement(KEY3, VALUE3));
    message.setMetadata(set);
    assertEquals(3, message.getMetadata().size());
    assertTrue(message.getMessageHeaders().containsKey(KEY1));
    assertTrue(message.getMessageHeaders().containsKey(KEY2));
    assertTrue(message.getMessageHeaders().containsKey(KEY3));
    assertFalse(message.getMessageHeaders().containsKey(KEY4));
    // This should have no effect
    message.setMetadata((HashSet<MetadataElement>) null);
    assertEquals(3, message.getMetadata().size());
    assertTrue(message.getMessageHeaders().containsKey(KEY1));
    assertTrue(message.getMessageHeaders().containsKey(KEY2));
    assertTrue(message.getMessageHeaders().containsKey(KEY3));
    assertFalse(message.getMessageHeaders().containsKey(KEY4));
  }


  public void testAddMetadata() throws Exception {
    SerializableAdaptrisMessage message = new SerializableAdaptrisMessage();
    message.addMetadata(KEY1, VALUE1);
    message.addMetadata(KEY2, VALUE2);
    message.addMetadata(KEY1, VALUE3);
    assertEquals(2, message.getMetadata().size());
    assertTrue(message.getMessageHeaders().containsKey(KEY1));
    assertTrue(message.getMessageHeaders().containsKey(KEY2));
    assertFalse(message.getMessageHeaders().containsKey(KEY4));
  }


  public void testRemoveMetadata() throws Exception {
    SerializableAdaptrisMessage message = new SerializableAdaptrisMessage();
    message.addMetadata(KEY1, VALUE1);
    message.addMetadata(KEY2, VALUE2);
    message.addMetadata(KEY3, VALUE3);
    message.removeMetadata(new MetadataElement(KEY1, VALUE1));
    assertEquals(2, message.getMetadata().size());
    assertFalse(message.getMessageHeaders().containsKey(KEY1));
    assertTrue(message.getMessageHeaders().containsKey(KEY2));
    assertTrue(message.getMessageHeaders().containsKey(KEY3));
  }

  public void testGetMetadataValue() throws Exception {
    SerializableAdaptrisMessage message = new SerializableAdaptrisMessage();
    message.addMetadata(KEY1, VALUE1);
    message.addMetadata(KEY2, VALUE2);
    message.addMetadata(KEY3, VALUE3);
    assertEquals(VALUE1, message.getMetadataValue(KEY1));
    assertNull(message.getMetadataValue(null));
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
    message.addMetadata(KEY1, VALUE1);
    message.addMetadata(KEY2, VALUE2);
    message.addMetadata(KEY3, VALUE3);
    
    SerializableAdaptrisMessage unmarshalledMessage = roundTrip(message);
    
    assertTrue(message.getMetadata().equals(unmarshalledMessage.getMetadata()));
  }
  
  public void testSerializeAll() throws Exception {
    SerializableAdaptrisMessage message = new SerializableAdaptrisMessage();
    message.setUniqueId("MyUniqueID");
    message.setPayload("SomePayload");
    message.addMetadata(KEY1, VALUE1);
    message.addMetadata(KEY2, VALUE2);
    message.addMetadata(KEY3, VALUE3);
    message.setPayloadEncoding("SomeEncoding");
    
    SerializableAdaptrisMessage unmarshalledMessage = roundTrip(message);
    
    assertTrue(message.getUniqueId().equals(unmarshalledMessage.getUniqueId()));
    assertTrue(message.getPayload().equals(unmarshalledMessage.getPayload()));
    assertTrue(message.getMetadata().equals(unmarshalledMessage.getMetadata()));
    assertTrue(message.getPayloadEncoding().equals(unmarshalledMessage.getPayloadEncoding()));
  }
  
  public void testMessageHeaders() throws Exception {
    SerializableAdaptrisMessage message = new SerializableAdaptrisMessage();
    message.addMetadata(KEY1, VALUE1);
    message.addMetadata(KEY2, VALUE2);
    message.addMetadata(KEY3, VALUE3);
    assertTrue(message.getMessageHeaders().containsKey(KEY1));
    assertTrue(message.getMessageHeaders().containsKey(KEY2));
    assertTrue(message.getMessageHeaders().containsKey(KEY3));
    assertFalse(message.getMessageHeaders().containsKey(KEY4));
  }

  public void testAddMessageHeader() throws Exception {
    SerializableAdaptrisMessage message = new SerializableAdaptrisMessage();
    message.addMessageHeader(KEY1, VALUE1);
    message.addMessageHeader(KEY2, VALUE2);
    message.addMessageHeader(KEY3, VALUE3);
    assertTrue(message.containsKey(KEY1));
    assertTrue(message.containsKey(KEY2));
    assertTrue(message.containsKey(KEY3));
    assertFalse(message.containsKey(KEY4));
  }

  public void testRemoveMessageHeader() throws Exception {
    SerializableAdaptrisMessage message = new SerializableAdaptrisMessage();
    message.addMessageHeader(KEY1, VALUE1);
    message.addMessageHeader(KEY2, VALUE2);
    message.addMessageHeader(KEY3, VALUE3);
    message.removeMessageHeader(KEY3);
    message.removeMessageHeader(getName());
    assertEquals(2, message.getMessageHeaders().size());
    assertTrue(message.containsKey(KEY1));
    assertTrue(message.containsKey(KEY2));
    assertFalse(message.containsKey(KEY3));
  }


  public void testSetMessageHeaders() throws Exception {
    SerializableAdaptrisMessage message = new SerializableAdaptrisMessage();
    Map<String, String> p = new HashMap<>();
    p.put(KEY1, VALUE1);
    p.put(KEY2, VALUE2);
    p.put(KEY3, VALUE3);
    message.setMessageHeaders(p);
    assertTrue(message.containsKey(KEY1));
    assertTrue(message.containsKey(KEY2));
    assertTrue(message.containsKey(KEY3));
    assertFalse(message.containsKey(KEY4));
  }

  public void testEquals() throws Exception {
    SerializableAdaptrisMessage message = new SerializableAdaptrisMessage();
    message.setUniqueId("MyUniqueID");
    message.setPayload("SomePayload");
    message.addMetadata(KEY1, VALUE1);
    message.addMetadata(KEY2, VALUE2);
    message.addMetadata(KEY3, VALUE3);
    message.setPayloadEncoding("SomeEncoding");
    assertFalse(message.equals(null));
    assertFalse(message.equals(new Object()));
    assertTrue(message.equals(message));
    SerializableAdaptrisMessage unmarshalledMessage = roundTrip(message);
    assertTrue(message.equals(unmarshalledMessage));

    HashSet<SerializableAdaptrisMessage> set = new HashSet<>();
    set.add(message);
    set.add(unmarshalledMessage);
    assertEquals(1, set.size());
  }

  private SerializableAdaptrisMessage roundTrip(SerializableAdaptrisMessage message) throws Exception {
    XStreamMarshaller marshaller = new XStreamMarshaller();
    String result = marshaller.marshal(message);
    SerializableAdaptrisMessage unmarshalledMessage = (SerializableAdaptrisMessage) marshaller.unmarshal(result);
    return unmarshalledMessage;
  }
}
