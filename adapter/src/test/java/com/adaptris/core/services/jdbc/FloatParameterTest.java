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

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.adaptris.core.BaseCase;
import com.adaptris.core.services.jdbc.StatementParameterImpl.QueryType;

public class FloatParameterTest extends BaseCase {

  public FloatParameterTest(String n) {
    super(n);
  }

  @Before
  public void setUp() throws Exception {
  }

  @After
  public void tearDown() throws Exception {
  }

  @Test
  public void testConvert() throws Exception {
    FloatStatementParameter sp = new FloatStatementParameter();
    assertEquals(Float.valueOf(55.0f), sp.toFloat("55.0"));
  }

  @Test
  public void testConvertNull() throws Exception {
    FloatStatementParameter sp = new FloatStatementParameter();
    sp.setConvertNull(false);
    try {
      sp.toFloat(null);
      fail("Expected Exception");
    }
    catch (RuntimeException expected) {
      // expected
    }
    try {
      sp.toFloat("");
      fail("Expected Exception");
    }
    catch (RuntimeException expected) {
      // expected
    }
  }

  @Test
  public void testConvertWithConvertNull() throws Exception {
    FloatStatementParameter sp = new FloatStatementParameter();
    sp.setConvertNull(true);
    assertEquals(Float.valueOf(0), sp.toFloat(""));
  }

  @Test
  public void testMakeCopy() throws Exception {
    FloatStatementParameter sp = new FloatStatementParameter("0.0", QueryType.constant, null, null);
    FloatStatementParameter copy = sp.makeCopy();
    assertRoundtripEquality(sp, copy);

  }
}
