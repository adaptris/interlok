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
package com.adaptris.core.cache;

import static com.adaptris.core.util.LifecycleHelper.initAndStart;
import static com.adaptris.core.util.LifecycleHelper.stopAndClose;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import com.adaptris.util.TimeInterval;

public class NullCacheImplementationTest {


  @Before
  public void setUp() throws Exception {
  }

  @After
  public void tearDown() {

  }

  @Test
  public void testPut() throws Exception {
    NullCacheImplementation cache = new NullCacheImplementation();
    initAndStart(cache);
    try {
      cache.put("one", "1");
      // Just to put something non-serializable in there.
      cache.put("three", new Object());
      assertEquals(0, cache.size());

      cache.put("six", "6");
      assertEquals(0, cache.size());
    }
    finally {
      stopAndClose(cache);
    }
  }

  @Test
  public void testClear() throws Exception {
    NullCacheImplementation cache = new NullCacheImplementation();
    initAndStart(cache);
    try {
      cache.put("one", "1");
      assertEquals(0, cache.size());
      cache.clear();
      assertEquals(0, cache.size());
    }
    finally {
      stopAndClose(cache);
    }
  }

  @Test
  public void testGet() throws Exception {
    NullCacheImplementation cache = new NullCacheImplementation();
    initAndStart(cache);
    try {
      assertNull(cache.get("XXX"));
    }
    finally {
      stopAndClose(cache);
    }
  }

  @Test
  public void testRemove() throws Exception {
    NullCacheImplementation cache = new NullCacheImplementation();
    initAndStart(cache);
    try {
      cache.remove("one");
      cache.remove("two");
      cache.remove("three");
      cache.remove("something");
    }
    finally {
      stopAndClose(cache);
    }
  }

  @Test
  public void testGetKeys() throws Exception {
    NullCacheImplementation cache = new NullCacheImplementation();
    initAndStart(cache);
    try {
      assertEquals(0, cache.getKeys().size());
      List<String> x = cache.getKeys();
      List<String> y = cache.getKeys();
      // New object each time.
      assertFalse(x == y);
    }
    finally {
      stopAndClose(cache);
    }
  }


  @Test
  public void testPut_WithExpiration() throws Exception {
    NullCacheImplementation cache = new NullCacheImplementation();
    TimeInterval expiry = new TimeInterval(250L, TimeUnit.MILLISECONDS);
    initAndStart(cache);
    try {
      cache.put("one", "1", expiry);
      cache.put("object", new Object(), expiry);
      assertEquals(0, cache.size());
      assertEquals(0, cache.getKeys().size());
    } finally {
      stopAndClose(cache);
    }
  }
}
