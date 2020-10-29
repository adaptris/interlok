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
package com.adaptris.core.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import com.adaptris.core.CoreException;
import com.adaptris.core.ProduceException;
import com.adaptris.core.ServiceException;
import com.adaptris.interlok.InterlokException;

public class ExceptionHelperTest extends ExceptionHelper {

  @Before
  public void setUp() throws Exception {}

  @After
  public void tearDown() throws Exception {}

  @Test
  public void testRethrowCoreExceptionThrowable() {
    Exception c1 = new Exception();
    try {
      rethrowCoreException(c1);
      fail();
    } catch (CoreException e) {
      assertEquals(c1, e.getCause());
    }
    CoreException c2 = new CoreException();
    try {
      rethrowCoreException(c2);
      fail();
    } catch (CoreException e) {
      assertEquals(c2, e);
    }
  }

  @Test
  public void testWrapCoreExceptionThrowable() {
    CoreException cause = new CoreException();
    try {
      throw wrapCoreException(cause);
    } catch (CoreException e) {
      assertEquals(cause, e);
    }
    try {
      throw wrapCoreException(new Exception());
    } catch (CoreException e) {
      assertNotNull(e.getCause());
    }
  }

  @Test
  public void testRethrowServiceExceptionThrowable() {
    Exception c1 = new Exception();
    try {
      rethrowServiceException(c1);
      fail();
    } catch (ServiceException e) {
      assertEquals(c1, e.getCause());
    }
    ServiceException c2 = new ServiceException();
    try {
      rethrowServiceException(c2);
      fail();
    } catch (ServiceException e) {
      assertEquals(c2, e);
    }
  }

  @Test
  public void testWrapServiceExceptionThrowable() {
    ServiceException cause = new ServiceException();
    try {
      throw wrapServiceException(cause);
    } catch (ServiceException e) {
      assertEquals(cause, e);
    }
    try {
      throw wrapServiceException(new Exception());
    } catch (ServiceException e) {
      assertNotNull(e.getCause());
    }
  }

  @Test
  public void testRethrowProduceExceptionThrowable() {
    Exception c1 = new Exception();
    try {
      rethrowProduceException(c1);
      fail();
    } catch (ProduceException e) {
      assertEquals(c1, e.getCause());
    }
    ProduceException c2 = new ProduceException();
    try {
      rethrowProduceException(c2);
      fail();
    } catch (ProduceException e) {
      assertEquals(c2, e);
    }
  }

  @Test
  public void testWrapProduceExceptionThrowable() {
    ProduceException cause = new ProduceException();
    try {
      throw wrapProduceException(cause);
    } catch (ProduceException e) {
      assertEquals(cause, e);
    }
    try {
      throw wrapProduceException(new Exception());
    } catch (ProduceException e) {
      assertNotNull(e.getCause());
    }
  }

  @Test
  public void testWrapInterlokExceptionThrowable() {
    InterlokException cause = new InterlokException();
    try {
      throw wrapInterlokException(cause);
    } catch (InterlokException e) {
      assertEquals(cause, e);
    }
    try {
      throw wrapInterlokException(new Exception());
    } catch (InterlokException e) {
      assertNotNull(e.getCause());
    }
  }

}
