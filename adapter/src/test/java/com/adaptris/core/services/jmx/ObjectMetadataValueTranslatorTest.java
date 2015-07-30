package com.adaptris.core.services.jmx;

import java.util.Date;

import junit.framework.TestCase;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.DefaultMessageFactory;

public class ObjectMetadataValueTranslatorTest extends TestCase {
  
  private ObjectMetadataValueTranslator objectMetadataValueTranslator;
  
  private AdaptrisMessage message;
  
  public ObjectMetadataValueTranslatorTest() {
    super();
  }
  
  public void setUp() throws Exception {
    objectMetadataValueTranslator = new ObjectMetadataValueTranslator();
    message = DefaultMessageFactory.getDefaultInstance().newMessage();
  }
  
  public void tearDown() throws Exception {
  }
  
  public void testSetValue() throws Exception {
    String newMetadataKey = "NewMetadataKey";
    String expectedValue = "NewValue";
    
    objectMetadataValueTranslator.setMetadataKey(newMetadataKey);
    objectMetadataValueTranslator.setType("java.lang.String");
    
    objectMetadataValueTranslator.setValue(message, expectedValue);
    
    String newValue = (String) objectMetadataValueTranslator.getValue(message);
    
    assertEquals(expectedValue, newValue);
  }
  
  public void testSetValueNoMetadataKey() throws Exception {
    String newValue = "NewValue";
    
    objectMetadataValueTranslator.setType("java.lang.String");
    // no error, just warning log
    objectMetadataValueTranslator.setValue(message, newValue);
  }
  
  public void testGetValueNoMetadataKey() throws Exception {
    String newValue = "NewValue";
        
    objectMetadataValueTranslator.setValue(message, newValue);
    
    assertNull(newValue, objectMetadataValueTranslator.getValue(message));
  }
  
  public void testSetValueWithIntegerType() throws Exception {
    String newMetadataKey = "NewMetadataKey";
    Integer newValue = new Integer(1);
    
    objectMetadataValueTranslator.setMetadataKey(newMetadataKey);
    objectMetadataValueTranslator.setType("java.lang.Integer");
    
    objectMetadataValueTranslator.setValue(message, newValue);
    
    Object value = objectMetadataValueTranslator.getValue(message);
    
    assertTrue(value instanceof Integer);
    assertEquals(new Integer(1), newValue);
  }
  
  public void testGetValueDefaultType() throws Exception {
    String newMetadataKey = "NewMetadataKey";
    String newValue = "NewValue";
    
    objectMetadataValueTranslator.setMetadataKey(newMetadataKey);
    
    objectMetadataValueTranslator.setValue(message, newValue);
    
    assertEquals(newValue, objectMetadataValueTranslator.getValue(message));
  }
  
  public void testGetValueDateType() throws Exception {
    String newMetadataKey = "NewMetadataKey";
    Date todaysDate = new Date();
    
    objectMetadataValueTranslator.setMetadataKey(newMetadataKey);
    objectMetadataValueTranslator.setType(Date.class.getName());
    
    objectMetadataValueTranslator.setValue(message, todaysDate);
    
    Object value = objectMetadataValueTranslator.getValue(message);
    assertTrue(value instanceof Date);
    assertEquals(todaysDate, ((Date)value));
  }

}
