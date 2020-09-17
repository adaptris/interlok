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
package com.adaptris.interlok.util;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import org.junit.Test;

public class CloseableIterableTest implements CloseableIterable<Object> {

  @Test
  public void testEnsureCloseable_List() throws Exception {
    try (CloseableIterable i = CloseableIterable.ensureCloseable(new ArrayList())) {
      assertTrue(i instanceof CloseableIterable);
      assertNotNull(i.iterator());
    };
  }

  @Test
  public void testEnsureCloseable_CloseableIterable() throws Exception {
    try (CloseableIterable i = CloseableIterable.ensureCloseable(this)) {
      assertTrue(i instanceof CloseableIterable);
      assertSame(this, i);
      assertNull(i.iterator());
    };
  }

  @Test
  public void testEnsureCloseable_AlreadyCloseable() throws Exception {
    DummyCloser dc = new DummyCloser();
    try (CloseableIterable i = CloseableIterable.ensureCloseable(dc)) {
      assertTrue(i instanceof CloseableIterable);
      assertNull(i.iterator());
      assertFalse(i.getClass().equals(DummyCloser.class));
    }
  }


  @Override
  public void close() throws IOException {
  }

  @Override
  public Iterator<Object> iterator() {
    return null;
  }

  private class DummyCloser implements Closeable, Iterable<Object> {
    @Override
    public void close() throws IOException {}

    @Override
    public Iterator<Object> iterator() {
      return null;
    }
  }
}
