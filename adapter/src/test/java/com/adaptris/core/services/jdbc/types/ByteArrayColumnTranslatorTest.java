package com.adaptris.core.services.jdbc.types;

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
    } catch(Exception ex) {
      //pass, expected
    }
  }
  
}
