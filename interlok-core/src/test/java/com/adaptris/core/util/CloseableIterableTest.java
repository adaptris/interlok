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

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import org.junit.Test;

@SuppressWarnings("deprecation")
public class CloseableIterableTest implements CloseableIterable<Object> {

  @Test
  public void testEnsureCloseable_List() throws Exception {
    try (CloseableIterable i = CloseableIterable.ensureCloseable(new ArrayList())) {
      assertTrue(i instanceof CloseableIterable);
      assertNotNull(i.iterator());      
    };    
  }

  @Test
  public void testEnsureCloseable_AlreadyCloseable() throws Exception {
    try (CloseableIterable i = CloseableIterable.ensureCloseable(this)) {
      assertTrue(i instanceof CloseableIterable);
      assertSame(this, i);
      assertNull(i.iterator());      
    }; 
  }


  @Override
  public void close() throws IOException {    
  }

  @Override
  public Iterator<Object> iterator() {
    return null;
  }

}
