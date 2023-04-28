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

package com.adaptris.util.text;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.fail;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class NullConverterTest {

  @BeforeEach
  public void setUp() throws Exception {

  }

  @AfterEach
  public void tearDown() throws Exception {

  }

  @Test
  public void testNullToEmptyString() throws Exception {
    NullConverter c = new NullToEmptyStringConverter();
    assertNotNull(c.convert((String) null));
    assertEquals("", c.convert((String) null));
    assertEquals("ABC", c.convert("ABC"));
  }

  @Test
  public void testNullsNotSupported() throws Exception {
    NullConverter c = new NullsNotSupportedConverter();
    String x = null;
    try {
      c.convert(x);
      fail("Converted a null object when you should have");
    }
    catch (UnsupportedOperationException expected) {

    }
    assertEquals("ABC", c.convert("ABC"));
  }

  @Test
  public void testNullsPassThrough() throws Exception {
    NullConverter c = new NullPassThroughConverter();
    String x = null;
    assertNull(c.convert(x));
  }

}
