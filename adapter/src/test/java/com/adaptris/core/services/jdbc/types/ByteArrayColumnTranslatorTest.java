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

import java.io.StringWriter;

import com.adaptris.jdbc.JdbcResultRow;

import junit.framework.TestCase;

public class ByteArrayColumnTranslatorTest extends TestCase {
  
  private ByteArrayColumnTranslator translator;
  
  public void setUp() throws Exception {
    translator = new ByteArrayColumnTranslator();
  }
  
  public void testByteToString() throws Exception {
    JdbcResultRow row = new JdbcResultRow();
    row.setFieldValue("testField", "SomeData".getBytes());
    
    String translated = translator.translate(row, 0);
    
    assertEquals("SomeData", translated);
  }
  
  public void testByteToStringEncoded() throws Exception {
    JdbcResultRow row = new JdbcResultRow();
    row.setFieldValue("testField", "SomeData".getBytes("UTF-8"));
    
    String translated = translator.translate(row, 0);
    
    assertEquals("SomeData", translated);
  }

  public void testByteToStringColName() throws Exception {
    JdbcResultRow row = new JdbcResultRow();
    row.setFieldValue("testField", "SomeData".getBytes());
    
    String translated = translator.translate(row, "testField");
    
    assertEquals("SomeData", translated);
  }
  
  public void testByteIncorrectObject() throws Exception {
    JdbcResultRow row = new JdbcResultRow();
    row.setFieldValue("testField", new Integer(10));
    
    try {
      translator.translate(row, "testField");
      fail();
    } catch(Exception ex) {
      //pass, expected
    }
  }
  
  public void testBlobWrite() throws Exception {
    BlobColumnTranslator translator = new BlobColumnTranslator();
    TestBlob blob = new TestBlob();
    String myData = new String("SomeData");
    blob.setBytes(0, myData.getBytes());

    JdbcResultRow row = new JdbcResultRow();
    row.setFieldValue("testField", blob);

    StringWriter writer = new StringWriter();
    translator.write(row, 0, writer);
    String translated = writer.toString();

    assertEquals("SomeData", translated);
  }
}
