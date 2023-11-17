/*
 * Copyright 2017 Adaptris Ltd.
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
package com.adaptris.naming.adapter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class AdapterNamingExceptionTest {

  @BeforeEach
  public void setUp() throws Exception {
  }

  @AfterEach
  public void tearDown() throws Exception {
  }

  @Test
  public void testAdapterNamingException() {
    AdapterNamingException e = new AdapterNamingException();
    assertNotNull(e);
    assertNull(e.getCause());
    assertNull(e.getMessage());
  }

  @Test
  public void testAdapterNamingExceptionThrowable() {
    Exception cause = new Exception();
    AdapterNamingException e = new AdapterNamingException(cause);
    assertEquals(cause, e.getCause());
    assertEquals(Exception.class.getName(), e.getMessage());

  }

  @Test
  public void testAdapterNamingExceptionString() {
    AdapterNamingException e = new AdapterNamingException("hello");
    assertNull(e.getCause());
    assertEquals("hello", e.getMessage());
  }

  @Test
  public void testAdapterNamingExceptionStringThrowable() {
    Exception cause = new Exception();
    AdapterNamingException e = new AdapterNamingException("hello", cause);
    assertEquals(cause, e.getCause());
    assertEquals("hello", e.getMessage());
  }

}
