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
package com.adaptris.core.services.splitter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import com.adaptris.core.ServiceException;

public class ServiceExceptionHandlerTest {

  @Before
  public void setUp() throws Exception {
  }

  @After
  public void tearDown() throws Exception {
  }

  @Test
  public void testUncaughtException() throws Exception {
    ServiceExceptionHandler handler = new ServiceExceptionHandler();
    handler.uncaughtException(Thread.currentThread(), new ServiceException("first"));
    handler.uncaughtException(Thread.currentThread(), new ServiceException("second"));
    assertNotNull(handler.getFirstThrowableException());
    assertEquals("first", handler.getFirstThrowableException().getMessage());
    handler.clearExceptions();
    assertNull(handler.getFirstThrowableException());
  }

  @Test
  @SuppressWarnings("deprecation")
  public void testThrowFirst_NoExceptions() throws Exception {
    ServiceExceptionHandler handler = new ServiceExceptionHandler();
    handler.throwFirstException();
    assertNull(handler.getFirstThrowableException());
  }

  @Test
  @SuppressWarnings("deprecation")
  public void testThrowFirst() throws Exception {
    ServiceExceptionHandler handler = new ServiceExceptionHandler();
    handler.uncaughtException(Thread.currentThread(), new ServiceException("first"));
    handler.uncaughtException(Thread.currentThread(), new ServiceException("second"));
    try {
      handler.throwFirstException();
      fail();
    } catch (ServiceException expected) {
      assertEquals("first", expected.getMessage());
      assertEquals(1, expected.getSuppressed().length);
    }
    assertNull(handler.getFirstThrowableException());
  }

}
