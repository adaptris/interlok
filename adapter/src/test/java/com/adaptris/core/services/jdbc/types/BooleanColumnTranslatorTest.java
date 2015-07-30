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
