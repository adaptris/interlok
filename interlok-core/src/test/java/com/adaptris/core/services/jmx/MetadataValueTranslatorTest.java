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

package com.adaptris.core.services.jmx;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import java.util.Date;
import org.junit.Before;
import org.junit.Test;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreException;
import com.adaptris.core.DefaultMessageFactory;

public class MetadataValueTranslatorTest {
  
  private MetadataValueTranslator metadataValueTranslator;
  
  private AdaptrisMessage message;
  
  @Before
  public void setUp() throws Exception {
    metadataValueTranslator = new MetadataValueTranslator();
    message = DefaultMessageFactory.getDefaultInstance().newMessage();
  }
  

  @Test
  public void testSetValue() throws Exception {
    String newMetadataKey = "NewMetadataKey";
    String newValue = "NewValue";
    
    metadataValueTranslator.setMetadataKey(newMetadataKey);
    metadataValueTranslator.setType("java.lang.String");
    
    metadataValueTranslator.setValue(message, newValue);
    
    assertEquals(newValue, message.getMetadataValue(newMetadataKey));
  }

  @Test
  public void testSetValueNoMetadataKey() throws Exception {
    String newValue = "NewValue";
    
    metadataValueTranslator.setType("java.lang.String");
    // no error, just warning log
    metadataValueTranslator.setValue(message, newValue);
  }

  @Test
  public void testGetValueNoMetadataKey() throws Exception {
    String newValue = "NewValue";
        
    metadataValueTranslator.setValue(message, newValue);
    
    assertNull(newValue, metadataValueTranslator.getValue(message));
  }

  @Test
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

  @Test
  public void testGetValueDefaultType() throws Exception {
    String newMetadataKey = "NewMetadataKey";
    String newValue = "NewValue";
    
    metadataValueTranslator.setMetadataKey(newMetadataKey);
    
    metadataValueTranslator.setValue(message, newValue);
    
    assertEquals(newValue, metadataValueTranslator.getValue(message));
  }

  @Test
  public void testGetValueDateType() throws Exception {
    String newMetadataKey = "NewMetadataKey";
    Date todaysDate = new Date();
    
    metadataValueTranslator.setMetadataKey(newMetadataKey);
    metadataValueTranslator.setType(Date.class.getName());
    
    metadataValueTranslator.setValue(message, todaysDate);
    
    Object value = metadataValueTranslator.getValue(message);
    assertTrue(value instanceof Date);
    assertEquals(todaysDate, value);
  }

  @Test
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
