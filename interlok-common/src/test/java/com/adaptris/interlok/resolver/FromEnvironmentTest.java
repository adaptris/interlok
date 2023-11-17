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

public class FromEnvironmentTest {

  @BeforeEach
  public void setUp() throws Exception {
  }

  @AfterEach
  public void tearDown() throws Exception {
  }

  @Test
  public void testResolveString() {
    FromEnvironment resolver = new FromEnvironment();
    assertNotEquals("%env{PATH}", resolver.resolve("%env{PATH}"));
    assertNotEquals("%env{PATH}%env{PATH}", resolver.resolve("%env{PATH}%env{PATH}"));
    assertEquals("hello", resolver.resolve("hello"));
    assertEquals("NOT_A_ENVVAR", resolver.resolve("%env{NOT_A_ENVVAR}"));
    assertNull(resolver.resolve(null));
  }

  @Test
  public void testCanHandle() {
    FromEnvironment resolver = new FromEnvironment();
    assertFalse(resolver.canHandle("hello"));
    assertFalse(resolver.canHandle("%sysprop{java.version}"));
    assertTrue(resolver.canHandle("%env{PATH}"));
  }


}
