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

package com.adaptris.core.util;

import java.io.Closeable;
import java.io.IOException;
import java.util.Iterator;

import com.adaptris.annotation.Removal;
import com.adaptris.validation.constraints.ConfigDeprecated;

/**
 * @deprecated since 3.10.2 moved to com.adaptris.interlok.util package.
 */
@Deprecated
@ConfigDeprecated(removalVersion = "4.0.0", groups = Deprecated.class)
public interface CloseableIterable<E> extends com.adaptris.interlok.util.CloseableIterable<E> {

  /**
   * @deprecated since 3.10.2 use {@link com.adaptris.interlok.util.CloseableIterable#ensureCloseable(Iterable)} instead.
   */
  @Deprecated
  @Removal(version="4.0.0")
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
