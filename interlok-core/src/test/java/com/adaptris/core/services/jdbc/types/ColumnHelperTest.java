package com.adaptris.core.services.jdbc.types;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.sql.Types;

import org.junit.jupiter.api.Test;

import com.adaptris.jdbc.JdbcResultRow;

public class ColumnHelperTest extends ColumnHelper {

  @Test
  public void testTranslate() throws Exception {
    JdbcResultRow row = new JdbcResultRow();
    row.setFieldValue("testField", Integer.valueOf(1), Types.INTEGER);
    {
      String translated = translate(row, 0);
      assertEquals("1", translated);
    }
  }

  @Test
  public void testToString() throws Exception {
    {
      JdbcResultRow row = new JdbcResultRow();
      row.setFieldValue("testField", "1", Types.VARCHAR);
      String translated = toString(row, 0);
      assertEquals("1", translated);
    }
    {
      JdbcResultRow row = new JdbcResultRow();
      row.setFieldValue("testField", "1".getBytes(), Types.BINARY);
      String translated = toString(row, 0);
      assertEquals("1", translated);
    }
  }

}
