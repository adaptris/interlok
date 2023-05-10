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

public class EscapedTimestampParameterTest {
  private String timestampString;
  private java.sql.Timestamp timestamp;

  @BeforeEach
  public void setUp() throws Exception {
    timestamp = new java.sql.Timestamp(System.currentTimeMillis());
    timestampString = timestamp.toString();
  }

  @AfterEach
  public void tearDown() throws Exception {
  }

  @Test
  public void testConvert() throws Exception {
    JdbcEscapedTimestampParameter sp = new JdbcEscapedTimestampParameter();
    assertEquals(timestamp.toString(), sp.convert(timestampString).toString());
  }

  @Test
  public void testUnparseableFormat() throws Exception {
    JdbcEscapedTimestampParameter sp = new JdbcEscapedTimestampParameter();
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
    JdbcEscapedTimestampParameter sp = new JdbcEscapedTimestampParameter();
    sp.setConvertNull(false);
    assertNull(sp.convert(null));
  }

  @Test
  public void testConvertWithConvertNull() throws Exception {
    JdbcEscapedTimestampParameter sp = new JdbcEscapedTimestampParameter();
    sp.setConvertNull(true);
    long convertedTime = sp.convert(null).getTime();
    long now = System.currentTimeMillis();
    assertTrue(now >= convertedTime);
  }

  @Test
  public void testMakeCopy() throws Exception {
    JdbcEscapedTimestampParameter sp = new JdbcEscapedTimestampParameter(timestampString, QueryType.constant, null, null);
    JdbcEscapedTimestampParameter copy = sp.makeCopy();
    BaseCase.assertRoundtripEquality(sp, copy);
  }
}
