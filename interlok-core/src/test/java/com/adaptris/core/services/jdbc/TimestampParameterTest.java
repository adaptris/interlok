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

package com.adaptris.core.services.jdbc;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import java.text.SimpleDateFormat;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import com.adaptris.core.services.jdbc.StatementParameterImpl.QueryType;

public class TimestampParameterTest extends com.adaptris.interlok.junit.scaffolding.BaseCase {

  private static final String DEFAULT_TIMESTAMP_FORMAT = "yyyy-MM-dd'T'HH:mm:ssZ";
  private String timestampString;
  private java.sql.Timestamp timestamp;

  @Before
  public void setUp() throws Exception {
    SimpleDateFormat sdf = new SimpleDateFormat(DEFAULT_TIMESTAMP_FORMAT);
    timestampString = sdf.format(new java.util.Date());
    timestamp = new java.sql.Timestamp(sdf.parse(timestampString).getTime());
  }

  @After
  public void tearDown() throws Exception {
  }

  @Test
  public void testConvert() throws Exception {
    TimestampStatementParameter sp = create();
    assertEquals(timestamp, sp.convert(timestampString));
  }

  @Test
  public void testUnparseableFormat() throws Exception {
    TimestampStatementParameter sp = create();
    sp.setDateFormat("yyyy-MM-ddHH:mm:ss");
    sp.setConvertNull(false);
    try {
      sp.convert(timestampString);
      fail("Expected IllegalArgumentException");
    }
    catch (IllegalArgumentException expected) {
      // expected
    }
  }

  @Test
  public void testConvertNull() throws Exception {
    TimestampStatementParameter sp = create();
    sp.setConvertNull(false);
    assertNull(sp.convert(null));
  }

  @Test
  public void testConvertWithConvertNull() throws Exception {
    TimestampStatementParameter sp = create();
    sp.setConvertNull(true);
    long convertedTime = ((java.sql.Timestamp) sp.convert(null)).getTime();
    long now = System.currentTimeMillis();
    assertTrue("now > convertedTime", now >= convertedTime);
  }

  @Test
  public void testMakeCopy() throws Exception {
    TimestampStatementParameter sp = new TimestampStatementParameter(timestampString, QueryType.constant, null, null,
        new SimpleDateFormat(DEFAULT_TIMESTAMP_FORMAT));
    TimestampStatementParameter copy = sp.makeCopy();
    assertRoundtripEquality(sp, copy);
  }

  private TimestampStatementParameter create() throws Exception {
    TimestampStatementParameter sp = new TimestampStatementParameter();
    sp.setDateFormat(DEFAULT_TIMESTAMP_FORMAT);
    return sp;
  }
}
