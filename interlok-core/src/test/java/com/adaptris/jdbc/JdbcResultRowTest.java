package com.adaptris.jdbc;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import java.sql.Types;
import org.junit.Test;

public class JdbcResultRowTest {

  @Test
  @SuppressWarnings("deprecation")
  public void testSetFieldValue_Deprecated() {
    JdbcResultRow row = new JdbcResultRow();
    row.setFieldValue("testField", Integer.valueOf(1));
    assertEquals(1, row.getFieldCount());
    assertEquals(1, row.getFieldNames().size());
    assertEquals(1, row.getFieldTypes().size());
    assertEquals(1, row.getFieldValue(0));
    assertNull(row.getFieldType(0));
  }

  @Test
  public void testSetFieldValue() {
    JdbcResultRow row = new JdbcResultRow();
    row.setFieldValue("testField0", Integer.valueOf(1), Types.INTEGER);
    row.setFieldValue("testField1", Integer.valueOf(1), ParameterValueType.INTEGER);
    assertEquals(2, row.getFieldCount());
    assertEquals(2, row.getFieldNames().size());
    assertEquals(2, row.getFieldTypes().size());
    assertEquals(1, row.getFieldValue(0));
    assertEquals(ParameterValueType.INTEGER, row.getFieldType(0));
    assertEquals(ParameterValueType.INTEGER, row.getFieldType(1));
  }

  @Test(expected = IndexOutOfBoundsException.class)
  public void testGetFieldValue_Index() {
    JdbcResultRow row = new JdbcResultRow();
    row.setFieldValue("testField", Integer.valueOf(1), Types.INTEGER);
    assertEquals(1, row.getFieldCount());
    assertEquals(1, row.getFieldNames().size());
    assertEquals(1, row.getFieldTypes().size());
    assertEquals(1, row.getFieldValue(0));
    row.getFieldValue(15);
  }

  @Test
  public void testGetFieldValue_FieldName() {
    JdbcResultRow row = new JdbcResultRow();
    row.setFieldValue("testField", Integer.valueOf(1), Types.INTEGER);
    assertEquals(1, row.getFieldCount());
    assertEquals(1, row.getFieldNames().size());
    assertEquals(1, row.getFieldTypes().size());
    assertEquals(1, row.getFieldValue("testField"));
    assertNull(row.getFieldValue("testField2"));
  }

  @Test(expected = IndexOutOfBoundsException.class)
  public void testGetFieldType_Index() {
    JdbcResultRow row = new JdbcResultRow();
    row.setFieldValue("testField", Integer.valueOf(1), Types.INTEGER);
    assertEquals(1, row.getFieldCount());
    assertEquals(1, row.getFieldNames().size());
    assertEquals(1, row.getFieldTypes().size());
    assertEquals(ParameterValueType.INTEGER, row.getFieldType(0));
    row.getFieldType(15);
  }

  @Test
  public void testGetFieldType_FieldName() {
    JdbcResultRow row = new JdbcResultRow();
    row.setFieldValue("testField", Integer.valueOf(1), Types.INTEGER);
    assertEquals(1, row.getFieldCount());
    assertEquals(1, row.getFieldNames().size());
    assertEquals(1, row.getFieldTypes().size());
    assertEquals(ParameterValueType.INTEGER, row.getFieldType("testField"));
    assertNull(row.getFieldType("testField2"));
  }

}
