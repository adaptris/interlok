package com.adaptris.core.services.jdbc.types;

import java.sql.Clob;

import javax.sql.rowset.serial.SerialClob;

import com.adaptris.jdbc.JdbcResultRow;

import junit.framework.TestCase;

public class ClobColumnTranslatorTest extends TestCase {
  
  private ClobColumnTranslator translator;
  
  public void setUp() throws Exception {
    translator = new ClobColumnTranslator();
  }

  public void testClobToString() throws Exception {
    Clob clob = new SerialClob("SomeData".toCharArray());
    JdbcResultRow row = new JdbcResultRow();
    row.setFieldValue("testField", clob);
    
    String translated = translator.translate(row, 0);
    
    assertEquals("SomeData", translated);
  }
  
  public void testClobToStringColumnName() throws Exception {
    Clob clob = new SerialClob("SomeData".toCharArray());
    JdbcResultRow row = new JdbcResultRow();
    row.setFieldValue("testField", clob);
    
    String translated = translator.translate(row, "testField");
    
    assertEquals("SomeData", translated);
  }
  
  public void testClobIncorrectType() throws Exception {
    JdbcResultRow row = new JdbcResultRow();
    row.setFieldValue("testField", new Integer(999));
    
    try {
      translator.translate(row, 0);
    } catch (Exception ex) {
      // pass, expected
    }
  }
  
  public void testClobIncorrectTypeColumnName() throws Exception {
    JdbcResultRow row = new JdbcResultRow();
    row.setFieldValue("testField", new Integer(999));
    
    try {
      translator.translate(row, "testField");
    } catch (Exception ex) {
      // pass, expected
    }
  }
  
}
