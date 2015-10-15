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

package com.adaptris.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class PseudoRandomIdGeneratorTest {

  @Test
  public void testCreateId() throws Exception {
    IdGenerator guid = new PseudoRandomIdGenerator();
    assertNotNull(guid.create(guid));
  }

  @Test
  public void testCreateIdWithNull() throws Exception {
    IdGenerator guid = new PseudoRandomIdGenerator();
    assertNotNull(guid.create(null));
  }

  @Test
  public void testPrefix() throws Exception {
    PseudoRandomIdGenerator guid = new PseudoRandomIdGenerator("prefix");
    assertEquals("prefix", guid.getPrefix());
    assertNotNull(guid.create(guid));
    assertTrue(guid.create(guid).startsWith("prefix"));
    assertEquals(18, guid.create(guid).length());
  }
}
