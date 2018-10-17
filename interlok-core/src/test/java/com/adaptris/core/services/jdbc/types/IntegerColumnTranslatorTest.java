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

package com.adaptris.core.services.jdbc.types;

import com.adaptris.jdbc.JdbcResultRow;

import junit.framework.TestCase;

public class IntegerColumnTranslatorTest extends TestCase {
  
  private IntegerColumnTranslator translator;
  String expected = "00123";
  
  public void setUp() throws Exception {
    translator = new IntegerColumnTranslator();
  }
  
  public void testFormattedFloat() throws Exception {
    translator.setFormat("%05d");
    Float intVal = new Float("123");
    
    JdbcResultRow row = new JdbcResultRow();
    row.setFieldValue("testField", intVal);
    
    try {
      translator.translate(row, 0);
    } catch (Exception ex) {
      // expected
    }
  }
  
  public void testFormattedDouble() throws Exception {
    translator.setFormat("%05d");
    Double doubleVal = new Double("123");
    
    JdbcResultRow row = new JdbcResultRow();
    row.setFieldValue("testField", doubleVal);
    
    try {
      translator.translate(row, 0);
    } catch (Exception ex) {
      // expected
    }
  }
  
  public void testFormattedInteger() throws Exception {
    translator.setFormat("%05d");
    Integer intVal = new Integer("123");
    
    JdbcResultRow row = new JdbcResultRow();
    row.setFieldValue("testField", intVal);
    
    String translated = translator.translate(row, 0);
    
    assertEquals(expected, translated);
  }
  
  public void testFormattedIntegerColumnName() throws Exception {
    translator.setFormat("%05d");
    Integer intVal = new Integer("123");
    
    JdbcResultRow row = new JdbcResultRow();
    row.setFieldValue("testField", intVal);
    
    String translated = translator.translate(row, "testField");
    
    assertEquals(expected, translated);
  }
  
  public void testFormattedString() throws Exception {
    translator.setFormat("%05d");
    String stringVal = new String("123");
    
    JdbcResultRow row = new JdbcResultRow();
    row.setFieldValue("testField", stringVal);
    
    String translated = translator.translate(row, 0);
    
    assertEquals(expected, translated);
  }
  
  public void testIllegalFormat() throws Exception {
    translator.setFormat("%zZX");
    Integer intVal = new Integer("123");
    
    JdbcResultRow row = new JdbcResultRow();
    row.setFieldValue("testField", intVal);
    
    try {
      translator.translate(row, 0);
    } catch (Exception ex) {
      //expected
    }
  }


}
