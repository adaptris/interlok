package com.adaptris.core.services.jmx;

import java.util.Date;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreException;
import com.adaptris.core.DefaultMessageFactory;

import junit.framework.TestCase;

public class MetadataValueTranslatorTest extends TestCase {
  
  private MetadataValueTranslator metadataValueTranslator;
  
  private AdaptrisMessage message;
  
  public MetadataValueTranslatorTest() {
    super();
  }
  
  public void setUp() throws Exception {
    metadataValueTranslator = new MetadataValueTranslator();
    message = DefaultMessageFactory.getDefaultInstance().newMessage();
  }
  
  public void tearDown() throws Exception {
  }
  
  public void testSetValue() throws Exception {
    String newMetadataKey = "NewMetadataKey";
    String newValue = "NewValue";
    
    metadataValueTranslator.setMetadataKey(newMetadataKey);
    metadataValueTranslator.setType("java.lang.String");
    
    metadataValueTranslator.setValue(message, newValue);
    
    assertEquals(newValue, message.getMetadataValue(newMetadataKey));
  }
  
  public void testSetValueNoMetadataKey() throws Exception {
    String newValue = "NewValue";
    
    metadataValueTranslator.setType("java.lang.String");
    // no error, just warning log
    metadataValueTranslator.setValue(message, newValue);
  }
  
  public void testGetValueNoMetadataKey() throws Exception {
    String newValue = "NewValue";
        
    metadataValueTranslator.setValue(message, newValue);
    
    assertNull(newValue, metadataValueTranslator.getValue(message));
  }
  
  public void testSetValueWithIntegerType() throws Exception {
    String newMetadataKey = "NewMetadataKey";
    Integer newValue = new Integer(1);
    
    metadataValueTranslator.setMetadataKey(newMetadataKey);
    metadataValueTranslator.setType("java.lang.Integer");
    
    metadataValueTranslator.setValue(message, newValue);
    
    Object value = metadataValueTranslator.getValue(message);
    
    assertTrue(value instanceof Integer);
    assertEquals(new Integer(1), newValue);
  }
  
  public void testGetValueDefaultType() throws Exception {
    String newMetadataKey = "NewMetadataKey";
    String newValue = "NewValue";
    
    metadataValueTranslator.setMetadataKey(newMetadataKey);
    
    metadataValueTranslator.setValue(message, newValue);
    
    assertEquals(newValue, metadataValueTranslator.getValue(message));
  }
  
  public void testGetValueDateType() throws Exception {
    String newMetadataKey = "NewMetadataKey";
    Date todaysDate = new Date();
    
    metadataValueTranslator.setMetadataKey(newMetadataKey);
    metadataValueTranslator.setType(Date.class.getName());
    
    metadataValueTranslator.setValue(message, todaysDate);
    
    Object value = metadataValueTranslator.getValue(message);
    assertTrue(value instanceof Date);
    assertEquals(todaysDate, ((Date)value));
  }
  
  public void testGetValueInvalidType() throws Exception {
    String newMetadataKey = "NewMetadataKey";
    String newValue = "1";
    
    metadataValueTranslator.setMetadataKey(newMetadataKey);
    metadataValueTranslator.setType("MyInvalidType");
    
    metadataValueTranslator.setValue(message, newValue);
    
    try {
      metadataValueTranslator.getValue(message);
      fail("Should fail with a core exception");
    } catch (CoreException ex) {
      // expected
    }
  }

}
