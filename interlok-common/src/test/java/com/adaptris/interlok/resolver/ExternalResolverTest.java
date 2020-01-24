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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class ExternalResolverTest {

  @Before
  public void setUp() throws Exception {
  }

  @After
  public void tearDown() throws Exception {
  }

  @Test
  public void testResolve() {
    assertNull(ExternalResolver.resolve(null));
    assertNull(ExternalResolver.resolve(null, null));
    assertNotEquals("%env{PATH}", ExternalResolver.resolve("%env{PATH}"));
    assertNotEquals("%sysprop{java.version}", ExternalResolver.resolve("%sysprop{java.version}"));
    assertNotEquals("%sysprop{java.version}", ExternalResolver.resolve("%sysprop{java.version}", null));
    assertEquals("hello", ExternalResolver.resolve("hello"));
    assertEquals("NOT_A_ENVVAR", ExternalResolver.resolve("%env{NOT_A_ENVVAR}"));
    assertEquals("%payload{NOT_FOUND}", ExternalResolver.resolve("%payload{NOT_FOUND}"));
  }

  @Test
  public void testUnresolvableException() {
    Exception e = new UnresolvableException();
    assertTrue(e instanceof UnresolvableException);
    e = new UnresolvableException("Some message");
    assertEquals("Some message", e.getMessage());
    e = new UnresolvableException(new Exception());
    assertNotNull(e.getCause());
  }
}
