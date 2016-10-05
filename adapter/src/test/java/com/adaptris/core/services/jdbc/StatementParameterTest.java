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
import com.adaptris.core.ServiceException;
import com.adaptris.core.services.jdbc.StatementParameterImpl.QueryType;

public class StatementParameterTest extends BaseCase {

  private static final String STRING_VALUE = "ABCDEFG";

  public StatementParameterTest(String n) {
    super(n);
  }

  @Before
  public void setUp() throws Exception {

  }

  @After
  public void tearDown() throws Exception {

  }

  @Test
  public void testConvertString() throws Exception {
    StatementParameter sp = new StatementParameter();
    sp.setQueryClass(String.class.getCanonicalName());
    assertEquals(STRING_VALUE, sp.convertToQueryClass(STRING_VALUE));
  }

  @Test
  public void testConvertNoClass() throws Exception {
    StatementParameter sp = new StatementParameter();
    try {
      sp.convertToQueryClass(STRING_VALUE);
      fail("Expected ServiceException");
    }
    catch (ServiceException expected) {
      // expected
    }
  }

  @Test
  public void testConvertToNonString() throws Exception {
    StatementParameter sp = new StatementParameter();
    sp.setQueryClass(SimpleStringWrapper.class.getCanonicalName());
    SimpleStringWrapper wrapper = new SimpleStringWrapper(STRING_VALUE);
    assertEquals(wrapper, sp.convertToQueryClass(STRING_VALUE));
  }

  @Test
  public void testConvertNull() throws Exception {
    StatementParameter sp = new StatementParameter();
    sp.setQueryClass(String.class.getCanonicalName());
    sp.setConvertNull(false);
    assertNull(sp.convertToQueryClass(null));
  }

  @Test
  public void testConvertWithConvertNull() throws Exception {
    StatementParameter sp = new StatementParameter();
    sp.setQueryClass(String.class.getCanonicalName());
    sp.setConvertNull(true);
    assertEquals("", sp.convertToQueryClass(null));
  }

  @Test
  public void testMakeCopy() throws Exception {
    StatementParameter sp = new StatementParameter("hello", String.class.getName(), QueryType.constant, null, null);
    StatementParameter copy = sp.makeCopy();
    assertRoundtripEquality(sp, copy);
  }

}
