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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import org.junit.Test;
import com.adaptris.core.BaseCase;
import com.adaptris.core.services.jdbc.StatementParameterImpl.QueryType;

public class DoubleParameterTest {

  @Test
  public void testConvert() throws Exception {
    DoubleStatementParameter sp = new DoubleStatementParameter();
    assertEquals(Double.valueOf(55.0), sp.convert("55.0"));
  }

  @Test
  public void testConvertNull() throws Exception {
    DoubleStatementParameter sp = new DoubleStatementParameter();
    sp.setConvertNull(false);
    assertNull(sp.convert(null));
    // This won't be of type Double...
    assertNotNull(sp.convert(""));
  }

  @Test
  public void testConvertWithConvertNull() throws Exception {
    DoubleStatementParameter sp = new DoubleStatementParameter();
    sp.setConvertNull(true);
    assertEquals(Double.valueOf(0), sp.convert(""));
  }

  @Test
  public void testMakeCopy() throws Exception {
    DoubleStatementParameter sp = new DoubleStatementParameter("0.0", QueryType.constant, null, null);
    DoubleStatementParameter copy = sp.makeCopy();
    BaseCase.assertRoundtripEquality(sp, copy);

  }
}
