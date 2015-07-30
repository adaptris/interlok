package com.adaptris.core.services.jdbc.types;

import com.adaptris.jdbc.JdbcResultRow;

import junit.framework.TestCase;

public class DoubleColumnTranslatorTest extends TestCase {
  
  private DoubleColumnTranslator translator;
  String expected = "123.000000";
  
  public void setUp() throws Exception {
    translator = new DoubleColumnTranslator();
  }
  
  public void testFormattedFloat() throws Exception {
    translator.setFormat("%f");
    Float floatVal = new Float("123");
    
    JdbcResultRow row = new JdbcResultRow();
    row.setFieldValue("testField", floatVal);
    
    String translated = translator.translate(row, 0);
    
    assertEquals(expected, translated);
  }
  
  public void testFormattedFloatColumnName() throws Exception {
    translator.setFormat("%f");
    Float floatVal = new Float("123");
    
    JdbcResultRow row = new JdbcResultRow();
    row.setFieldValue("testField", floatVal);
    
    String translated = translator.translate(row, "testField");
    
    assertEquals(expected, translated);
  }
  
  public void testFormattedDouble() throws Exception {
    translator.setFormat("%f");
    Double floatVal = new Double("123");
    
    JdbcResultRow row = new JdbcResultRow();
    row.setFieldValue("testField", floatVal);
    
    String translated = translator.translate(row, 0);
    
    assertEquals(expected, translated);
  }
  
  public void testFormattedInteger() throws Exception {
    translator.setFormat("%f");
    Integer floatVal = new Integer("123");
    
    JdbcResultRow row = new JdbcResultRow();
    row.setFieldValue("testField", floatVal);
    
    String translated = translator.translate(row, 0);
    
    assertEquals(expected, translated);
  }
  
  public void testFormattedString() throws Exception {
    translator.setFormat("%f");
    String floatVal = new String("123");
    
    JdbcResultRow row = new JdbcResultRow();
    row.setFieldValue("testField", floatVal);
    
    String translated = translator.translate(row, 0);
    
    assertEquals(expected, translated);
  }
  
  public void testIllegalFormat() throws Exception {
    translator.setFormat("%zZX");
    String floatVal = new String("123");
    
    JdbcResultRow row = new JdbcResultRow();
    row.setFieldValue("testField", floatVal);
    
    try {
      translator.translate(row, 0);
    } catch (Exception ex) {
      //expected
    }
  }

}
