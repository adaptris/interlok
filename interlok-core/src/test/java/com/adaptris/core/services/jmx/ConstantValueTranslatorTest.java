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
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import java.util.Date;
import org.junit.Before;
import org.junit.Test;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreException;
import com.adaptris.core.DefaultMessageFactory;

public class ConstantValueTranslatorTest {
  
  private ConstantValueTranslator constantValueTranslator;
  
  private AdaptrisMessage message;
  
  public ConstantValueTranslatorTest() {
    super();
  }
  
  @Before
  public void setUp() throws Exception {
    constantValueTranslator = new ConstantValueTranslator();
    message = DefaultMessageFactory.getDefaultInstance().newMessage();
  }
  

  @Test
  public void testSetValue() throws Exception {
    String newValue = "NewValue";
    String originalValue = "OriginalValue";
    constantValueTranslator.setType("java.lang.String");
    constantValueTranslator.setValue(originalValue);
    constantValueTranslator.setValue(message, newValue);
    assertEquals(originalValue, constantValueTranslator.getValue());
  }

  @Test
  public void testGetValueDefaultType() throws Exception {
    String originalValue = "OriginalValue";
    constantValueTranslator.setValue(originalValue);
    assertEquals(originalValue, constantValueTranslator.getValue(message));
  }

  @Test
  public void testGetValueDateType() throws Exception {
    Date todaysDate = new Date();
    String originalValue = Long.toString(todaysDate.getTime());
    
    constantValueTranslator.setType(Date.class.getName());
    constantValueTranslator.setValue(originalValue);
    
    Object value = constantValueTranslator.getValue(message);
    assertTrue(value instanceof Date);
    assertEquals(originalValue, Long.toString(((Date)value).getTime()));
  }

  @Test
  public void testGetValueIntegerType() throws Exception {
    String originalValue = "1";
    constantValueTranslator.setType(Integer.class.getName());
    constantValueTranslator.setValue(originalValue);
    Object value = constantValueTranslator.getValue(message);
    assertTrue(value instanceof Integer);
    assertEquals(originalValue, value.toString());
  }

  @Test
  public void testGetValueInvalidType() throws Exception {
    String originalValue = "1";
    constantValueTranslator.setType("MyInvalidType");
    constantValueTranslator.setValue(originalValue);
    
    try {
      constantValueTranslator.getValue(message);
      fail("Should fail with a core exception");
    } catch (CoreException ex) {
      // expected
    }
  }

}
