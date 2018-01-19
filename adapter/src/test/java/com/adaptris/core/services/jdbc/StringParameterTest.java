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

public class StringParameterTest extends BaseCase {

  public StringParameterTest(String n) {
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
    StringStatementParameter sp = new StringStatementParameter();
    assertEquals("55", sp.convert(Integer.valueOf("55")));
  }


  @Test
  public void testConvertNull() throws Exception {
    StringStatementParameter sp = new StringStatementParameter();
    sp.setConvertNull(false);
    assertNull(sp.convert(null));
    assertEquals("", sp.convert(""));
  }

  @Test
  public void testConvertWithConvertNull() throws Exception {
    StringStatementParameter sp = new StringStatementParameter();
    sp.setConvertNull(true);
    assertEquals("", sp.convert(null));
    assertEquals("", sp.convert(""));
  }

  @Test
  public void testMakeCopy() throws Exception {
    StringStatementParameter sp = new StringStatementParameter("hello", QueryType.constant, null, null);
    StringStatementParameter copy = sp.makeCopy();
    assertRoundtripEquality(sp, copy);
  }

}
