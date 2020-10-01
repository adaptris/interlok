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

package com.adaptris.interlok.util;

import java.io.Closeable;
import java.io.IOException;
import java.util.Iterator;

/**
 * This Iterable exists for the purpose of being able to iterate over a list of indeterminate size
 * (possibly too large for memory), while still guaranteeing that whatever resource is being held (like
 * a Stream) will be closed when iteration finishes (or it goes out of scope).
 *
 */
public interface CloseableIterable<E> extends Closeable, Iterable<E> {

  static <E> CloseableIterable<E> ensureCloseable(final Iterable<E> iter) {
    if (iter instanceof CloseableIterable) {
      return (CloseableIterable<E>) iter;
    }
    if (iter instanceof Closeable) {
      return new CloseableIterable<E>() {
        @Override
        public void close() throws IOException {
          ((Closeable) iter).close();
        }

        @Override
        public Iterator<E> iterator() {
          return iter.iterator();
        }
      };
    }
    return new CloseableIterable<E>() {
      @Override
      public void close() throws IOException {
        // No-op
      }

      @Override
      public Iterator<E> iterator() {
        return iter.iterator();
      }
    };
  }
}
