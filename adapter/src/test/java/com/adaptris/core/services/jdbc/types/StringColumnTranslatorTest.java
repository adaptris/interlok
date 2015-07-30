package com.adaptris.core.services.jdbc.types;

import com.adaptris.jdbc.JdbcResultRow;

import junit.framework.TestCase;

public class StringColumnTranslatorTest extends TestCase {
  
  private StringColumnTranslator translator;
  
  public void setUp() throws Exception {
    translator = new StringColumnTranslator();
  }
  
  public void testIntegerTranslator() throws Exception {
    JdbcResultRow row = new JdbcResultRow();
    row.setFieldValue("testField", new Integer(111));
    
    String translated = translator.translate(row, 0);
    assertEquals("111", translated);
  }
  
  public void testIntegerTranslatorColumnName() throws Exception {
    JdbcResultRow row = new JdbcResultRow();
    row.setFieldValue("testField", new Integer(111));
    
    String translated = translator.translate(row, "testField");
    assertEquals("111", translated);
  }
  
  public void testDoubleTranslator() throws Exception {
    JdbcResultRow row = new JdbcResultRow();
    row.setFieldValue("testField", new Double(111));
    
    String translated = translator.translate(row, 0);
    assertEquals("111.0", translated);
  }
  
  public void testFloatTranslator() throws Exception {
    JdbcResultRow row = new JdbcResultRow();
    row.setFieldValue("testField", new Float(111));
    
    String translated = translator.translate(row, 0);
    assertEquals("111.0", translated);
  }
  
  public void testStringTranslator() throws Exception {
    JdbcResultRow row = new JdbcResultRow();
    row.setFieldValue("testField", new String("111"));
    
    String translated = translator.translate(row, 0);
    assertEquals("111", translated);
  }
  
  public void testDoubleFormattedTranslator() throws Exception {
    translator.setFormat("%f");
    JdbcResultRow row = new JdbcResultRow();
    row.setFieldValue("testField", new Double(111));
    
    String translated = translator.translate(row, 0);
    assertEquals("111.000000", translated);
  }

}
