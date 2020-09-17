/*
 * Copyright 2018 Adaptris Ltd.
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

package com.adaptris.core.services.jdbc;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import com.adaptris.core.services.jdbc.StatementParameterImpl.QueryType;
import com.adaptris.interlok.junit.scaffolding.BaseCase;

public class EscapedDateParameterTest {
  private String dateString;
  private java.sql.Date date;

  @Before
  public void setUp() throws Exception {
    date = new java.sql.Date(System.currentTimeMillis());
    dateString = date.toString();
  }

  @After
  public void tearDown() throws Exception {
  }

  @Test
  public void testConvert() throws Exception {
    JdbcEscapedDateStatementParameter sp = new JdbcEscapedDateStatementParameter();
    assertEquals(date.toString(), sp.convert(dateString).toString());
  }

  @Test
  public void testUnparseableFormat() throws Exception {
    JdbcEscapedDateStatementParameter sp = new JdbcEscapedDateStatementParameter();
    try {
      sp.convert("1999-13-12");
      fail("Expected IllegalArgumentException");
    }
    catch (IllegalArgumentException expected) {
      // expected
    }
  }

  @Test
  public void testConvertNull() throws Exception {
    JdbcEscapedDateStatementParameter sp = new JdbcEscapedDateStatementParameter();
    sp.setConvertNull(false);
    assertNull(sp.convert(null));
  }

  @Test
  public void testConvertWithConvertNull() throws Exception {
    JdbcEscapedDateStatementParameter sp = new JdbcEscapedDateStatementParameter();
    sp.setConvertNull(true);
    long convertedTime = sp.convert(null).getTime();
    long now = System.currentTimeMillis();
    assertTrue("now > convertedTime", now >= convertedTime);
  }


  @Test
  public void testMakeCopy() throws Exception {
    JdbcEscapedDateStatementParameter sp = new JdbcEscapedDateStatementParameter(dateString, QueryType.constant, null, null);
    JdbcEscapedDateStatementParameter copy = sp.makeCopy();
    BaseCase.assertRoundtripEquality(sp, copy);
  }
}
