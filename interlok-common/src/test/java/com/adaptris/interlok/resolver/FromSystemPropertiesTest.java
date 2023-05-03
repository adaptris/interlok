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
package com.adaptris.interlok.resolver;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class FromSystemPropertiesTest {

  @BeforeEach
  public void setUp() throws Exception {
  }

  @AfterEach
  public void tearDown() throws Exception {
  }

  @Test
  public void testResolveString() {
    FromSystemProperties resolver = new FromSystemProperties();
    assertNotEquals("%sysprop{java.version}", resolver.resolve("%sysprop{java.version}"));
    assertNotEquals("%sysprop{java.version}%sysprop{java.version}",
        resolver.resolve("%sysprop{java.version}%sysprop{java.version}"));
    assertEquals("hello", resolver.resolve("hello"));
    assertEquals("not.a.system.property", resolver.resolve("%sysprop{not.a.system.property}"));
    assertNull(resolver.resolve(null));
  }

  @Test
  public void testCanHandle() {
    FromSystemProperties resolver = new FromSystemProperties();
    assertFalse(resolver.canHandle("hello"));
    assertTrue(resolver.canHandle("%sysprop{java.version}"));
    assertFalse(resolver.canHandle("%env{PATH}"));
  }

  @Test
  public void testAsMap() {
    assertEquals(System.getProperties().size(), ResolverImp.asMap(System.getProperties()).size());
    assertEquals(0, ResolverImp.asMap(null).size());
  }
}
