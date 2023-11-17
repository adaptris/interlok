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
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.sql.Types;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.adaptris.jdbc.JdbcResultRow;

public class BooleanColumnTranslatorTest {

  private BooleanColumnTranslator translator;

  @BeforeEach
  public void setUp() throws Exception {
    translator = new BooleanColumnTranslator();
  }

  @Test
  public void testTrueString() throws Exception {
    JdbcResultRow row = new JdbcResultRow();
    row.setFieldValue("testField", "true", Types.BOOLEAN);

    String translated = translator.translate(row, 0);

    assertEquals("true", translated);
  }

  @Test
  public void testTrueStringColumnName() throws Exception {
    JdbcResultRow row = new JdbcResultRow();
    row.setFieldValue("testField", "true", Types.BOOLEAN);

    String translated = translator.translate(row, "testField");

    assertEquals("true", translated);
  }

  @Test
  public void testFalseString() throws Exception {
    JdbcResultRow row = new JdbcResultRow();
    row.setFieldValue("testField", "false", Types.BOOLEAN);

    String translated = translator.translate(row, 0);

    assertEquals("false", translated);
  }

  @Test
  public void testFalseBoolean() throws Exception {
    JdbcResultRow row = new JdbcResultRow();
    row.setFieldValue("testField", Boolean.valueOf("false"), Types.BOOLEAN);

    String translated = translator.translate(row, 0);

    assertEquals("false", translated);
  }

  @Test
  public void testTrueBoolean() throws Exception {
    JdbcResultRow row = new JdbcResultRow();
    row.setFieldValue("testField", Boolean.valueOf("true"), Types.BOOLEAN);

    String translated = translator.translate(row, 0);

    assertEquals("true", translated);
  }

  @Test
  public void testTrueBool() throws Exception {
    JdbcResultRow row = new JdbcResultRow();
    boolean val = true;
    row.setFieldValue("testField", val, Types.BOOLEAN);

    String translated = translator.translate(row, 0);

    assertEquals("true", translated);
  }

  @Test
  public void testFalseBool() throws Exception {
    JdbcResultRow row = new JdbcResultRow();
    boolean val = false;
    row.setFieldValue("testField", val, Types.BOOLEAN);

    String translated = translator.translate(row, 0);

    assertEquals("false", translated);
  }

}
