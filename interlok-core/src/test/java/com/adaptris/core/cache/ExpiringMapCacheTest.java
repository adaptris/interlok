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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.HashSet;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.adaptris.core.util.LifecycleHelper;
import com.adaptris.util.TimeInterval;

import net.jodah.expiringmap.ExpirationPolicy;

public class ExpiringMapCacheTest {

  @Before
  public void setUp() throws Exception {
  }

  @After
  public void tearDown() {

  }

  @Test
  public void testMaxEntries() throws Exception {
    ExpiringMapCache cache = new ExpiringMapCache();
    assertEquals(1024, cache.maxEntries());
    cache.withMaxEntries(10);
    assertEquals(10, cache.maxEntries());
  }

  @Test
  public void testExpirationPolicy() throws Exception {
    ExpiringMapCache cache = new ExpiringMapCache();
    assertEquals(ExpirationPolicy.ACCESSED, cache.expirationPolicy());
    cache.withExpirationPolicy(ExpirationPolicy.CREATED);
    assertEquals(ExpirationPolicy.CREATED, cache.expirationPolicy());
  }


  @Test
  public void testExpiration() throws Exception {
    ExpiringMapCache cache = new ExpiringMapCache();
    assertEquals(60000, cache.expiration());
    cache.withExpiration(new TimeInterval(10L, TimeUnit.SECONDS));
    assertEquals(10000, cache.expiration());
  }

  @Test
  public void testEventListener() throws Exception {
    ExpiringMapCache cache = new ExpiringMapCache();
    assertNotNull(cache.getEventListener());
    ExpiringMapCacheListener myListener = new ExpiringMapCacheListener();
    cache.withEventListener(myListener);
    assertEquals(myListener, cache.getEventListener());
  }

  @Test
  public void testPut() throws Exception {
    ExpiringMapCache cache = new ExpiringMapCache();
    initAndStart(cache);
    try {
      cache.put("one", "1");
      assertEquals(1, cache.size());
      // Just to put something non-serializable in there.
      cache.put("three", new Object());
      assertEquals(2, cache.size());
      cache.put("six", "6");
      assertEquals(3, cache.size());
    }
    finally {
      stopAndClose(cache);
    }
  }

  @Test
  public void testClear() throws Exception {
    ExpiringMapCache cache = new ExpiringMapCache();
    initAndStart(cache);
    try {
      cache.put("one", "1");
      assertEquals(1, cache.size());
      cache.clear();
      assertEquals(0, cache.size());
    }
    finally {
      stopAndClose(cache);
    }
  }

  @Test
  public void testGet() throws Exception {
    ExpiringMapCache cache = new ExpiringMapCache();
    initAndStart(cache);
    try {
      cache.put("XXX", "value");
      assertEquals("value", cache.get("XXX"));
    }
    finally {
      cache.clear();
      stopAndClose(cache);
    }
  }

  @Test
  public void testRemove() throws Exception {
    ExpiringMapCache cache = new ExpiringMapCache();
    initAndStart(cache);
    try {
      cache.put("one", "1");
      cache.put("two", "1");
      cache.put("three", "1");
      assertEquals(3, cache.size());

      cache.remove("one");
      assertEquals(2, cache.size());
      cache.remove("two");
      assertEquals(1, cache.size());
      cache.remove("three");
      assertEquals(0, cache.size());
      cache.remove("something");
      assertEquals(0, cache.size());
    }
    finally {
      stopAndClose(cache);
    }
  }

  @Test
  public void testGetKeys() throws Exception {
    ExpiringMapCache cache = new ExpiringMapCache();
    initAndStart(cache);
    try {
      cache.put("one", "1");
      cache.put("two", "1");
      cache.put("three", "1");
      assertEquals(3, cache.getKeys().size());
      List<String> x = cache.getKeys();
      List<String> y = cache.getKeys();
      // Turn the lists into a standard hashset so that our expected equality
      // will always compare the values.
      assertEquals(new HashSet(x), new HashSet(y));
    }
    finally {
      stopAndClose(cache);
    }
  }

  @Test
  public void testListeners() throws Exception {
    ExpiringMapCache cache = new ExpiringMapCache().withExpiration(new TimeInterval(100L, TimeUnit.MILLISECONDS))
        .withExpirationPolicy(ExpirationPolicy.CREATED);

    try {
      MyCacheEventListener listener = new MyCacheEventListener();
      MyCacheEventListener removedListener = new MyCacheEventListener();
      cache.getEventListener().addEventListener(listener);
      cache.getEventListener().addEventListener(removedListener);
      cache.getEventListener().removeEventListener(removedListener);
      initAndStart(cache);
      cache.put("one", "1");
      cache.put("two", "2");
      cache.put("three", "3");
      cache.put("four", "4");
      cache.put("five", "5");
      LifecycleHelper.waitQuietly(1000);
      assertTrue(listener.expiredItems > 1);
      assertEquals(0, removedListener.expiredItems);

    }
    finally {
      stopAndClose(cache);
    }
  }

}
