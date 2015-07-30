package com.adaptris.core.services.jdbc.types;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;

import junit.framework.TestCase;

import com.adaptris.jdbc.JdbcResultRow;

public class DateColumnTranslatorTest extends TestCase {
  
  private DateColumnTranslator translator;
  private GregorianCalendar gDate;
  private Date date;
  
  public void setUp() throws Exception {
    translator = new DateColumnTranslator();
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    date = sdf.parse("2013-08-22");
    
    gDate = new GregorianCalendar();
    gDate.setTime(date);
  }
  
  public void testDateNoFormat() throws Exception {
    JdbcResultRow row = new JdbcResultRow();
    row.setFieldValue("testField", date);
    
    String translated = translator.translate(row, 0);
    
    assertEquals("2013-08-22", translated);
  }
  
  public void testDateNoFormatColumnName() throws Exception {
    JdbcResultRow row = new JdbcResultRow();
    row.setFieldValue("testField", date);
    
    String translated = translator.translate(row, "testField");
    
    assertEquals("2013-08-22", translated);
  }
  
  public void testDateFormatted() throws Exception {
    translator.setDateFormat("yyyy");
    JdbcResultRow row = new JdbcResultRow();
    row.setFieldValue("testField", date);
    
    String translated = translator.translate(row, 0);
    
    assertEquals("2013", translated);
  }
  
  public void testGregorianNoFormat() throws Exception {
    JdbcResultRow row = new JdbcResultRow();
    row.setFieldValue("testField", gDate);
    
    String translated = translator.translate(row, 0);
    
    assertEquals("2013-08-22", translated);
  }
  
  public void testGregorianFormatted() throws Exception {
    translator.setDateFormat("yyyy");
    JdbcResultRow row = new JdbcResultRow();
    row.setFieldValue("testField", gDate);
    
    String translated = translator.translate(row, 0);
    
    assertEquals("2013", translated);
  }
  
  public void testString() throws Exception {
    JdbcResultRow row = new JdbcResultRow();
    row.setFieldValue("testField", "2013-08-22");
    
    try {
      translator.translate(row, 0);
    } catch (Exception ex) {
      // pass, expected
    }
    
  }

}
