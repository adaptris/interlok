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

import junit.framework.TestCase;

import com.adaptris.jdbc.JdbcResultRow;

public class BooleanColumnTranslatorTest extends TestCase {
  
  private BooleanColumnTranslator translator;
  
  public void setUp() throws Exception {
    translator = new BooleanColumnTranslator();
  }
  
  public void testTrueString() throws Exception {
    JdbcResultRow row = new JdbcResultRow();
    row.setFieldValue("testField", "true");
    
    String translated = translator.translate(row, 0);
    
    assertEquals("true", translated);
  }
  
  public void testTrueStringColumnName() throws Exception {
    JdbcResultRow row = new JdbcResultRow();
    row.setFieldValue("testField", "true");
    
    String translated = translator.translate(row, "testField");
    
    assertEquals("true", translated);
  }
  
  public void testFalseString() throws Exception {
    JdbcResultRow row = new JdbcResultRow();
    row.setFieldValue("testField", "false");
    
    String translated = translator.translate(row, 0);
    
    assertEquals("false", translated);
  }
  
  public void testFalseBoolean() throws Exception {
    JdbcResultRow row = new JdbcResultRow();
    row.setFieldValue("testField", new Boolean("false"));
    
    String translated = translator.translate(row, 0);
    
    assertEquals("false", translated);
  }
  
  public void testTrueBoolean() throws Exception {
    JdbcResultRow row = new JdbcResultRow();
    row.setFieldValue("testField", new Boolean("true"));
    
    String translated = translator.translate(row, 0);
    
    assertEquals("true", translated);
  }
  
  public void testTrueBool() throws Exception {
    JdbcResultRow row = new JdbcResultRow();
    boolean val = true;
    row.setFieldValue("testField", val);
    
    String translated = translator.translate(row, 0);
    
    assertEquals("true", translated);
  }
  
  public void testFalseBool() throws Exception {
    JdbcResultRow row = new JdbcResultRow();
    boolean val = false;
    row.setFieldValue("testField", val);
    
    String translated = translator.translate(row, 0);
    
    assertEquals("false", translated);
  }

}
