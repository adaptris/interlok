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
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.adaptris.core.services.jdbc.StatementParameterImpl.QueryType;
import com.adaptris.interlok.junit.scaffolding.BaseCase;

public class EscapedTimeParameterTest {

  private String timeString;
  private java.sql.Time time;

  @BeforeEach
  public void setUp() throws Exception {
    time = new java.sql.Time(System.currentTimeMillis());
    timeString = time.toString();
  }

  @AfterEach
  public void tearDown() throws Exception {
  }

  @Test
  public void testConvert() throws Exception {
    JdbcEscapedTimeParameter sp = new JdbcEscapedTimeParameter();
    assertEquals(time.toString(), sp.convert(timeString).toString());
  }

  @Test
  public void testUnparseableFormat() throws Exception {
    JdbcEscapedTimeParameter sp = new JdbcEscapedTimeParameter();
    try {
      sp.convert("1999-12-31'T'12:01:12");
      fail("Expected IllegalArgumentException");
    }
    catch (IllegalArgumentException expected) {
      // expected
    }
  }

  @Test
  public void testConvertNull() throws Exception {
    JdbcEscapedTimeParameter sp = new JdbcEscapedTimeParameter();
    sp.setConvertNull(false);
    assertNull(sp.convert(null));
  }

  @Test
  public void testConvertWithConvertNull() throws Exception {
    JdbcEscapedTimeParameter sp = new JdbcEscapedTimeParameter();
    sp.setConvertNull(true);
    long convertedTime = sp.convert(null).getTime();
    long now = System.currentTimeMillis();
    assertTrue(now >= convertedTime);
  }

  @Test
  public void testMakeCopy() throws Exception {
    JdbcEscapedTimeParameter sp = new JdbcEscapedTimeParameter(timeString, QueryType.constant, null, null);
    JdbcEscapedTimeParameter copy = sp.makeCopy();
    BaseCase.assertRoundtripEquality(sp, copy);

  }
}
