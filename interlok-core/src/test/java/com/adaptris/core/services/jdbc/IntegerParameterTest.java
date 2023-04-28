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
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

import com.adaptris.core.services.jdbc.StatementParameterImpl.QueryType;
import com.adaptris.interlok.junit.scaffolding.BaseCase;

public class IntegerParameterTest {

  @Test
  public void testConvert() throws Exception {
    IntegerStatementParameter sp = new IntegerStatementParameter();
    assertEquals(Integer.valueOf(55), sp.convert("55"));
  }

  @Test
  public void testConvertNull() throws Exception {
    IntegerStatementParameter sp = new IntegerStatementParameter();
    sp.setConvertNull(false);
    assertNull(sp.convert(null));
    assertNotNull(sp.convert(""));
  }

  @Test
  public void testConvertWithConvertNull() throws Exception {
    IntegerStatementParameter sp = new IntegerStatementParameter();
    sp.setConvertNull(true);
    assertEquals(Integer.valueOf(0), sp.convert(""));
  }

  @Test
  public void testMakeCopy() throws Exception {
    IntegerStatementParameter sp = new IntegerStatementParameter("0", QueryType.constant, null, null);
    IntegerStatementParameter copy = sp.makeCopy();
    BaseCase.assertRoundtripEquality(sp, copy);
  }

}
