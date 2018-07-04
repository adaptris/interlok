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
import static com.adaptris.core.util.LifecycleHelper.waitQuietly;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import java.util.HashSet;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.adaptris.core.CoreException;
import com.adaptris.core.util.LifecycleHelper;
import com.adaptris.util.TimeInterval;

public class RetryingCacheProxyTest {

  @Before
  public void setUp() throws Exception {
  }

  @After
  public void tearDown() {

  }

  @Test
  public void testInit_NoCache() throws Exception {
    RetryingCacheProxy cache = new RetryingCacheProxy();
    try {
      LifecycleHelper.init(cache);
      fail();
    }
    catch (CoreException expected) {

    }
    cache.setProxiedCache(new NullCacheImplementation());
    LifecycleHelper.init(cache);
  }

  @Test
  public void testSetMaxAttempts() throws Exception {
    RetryingCacheProxy cache = new RetryingCacheProxy();
    assertNull(cache.getMaxAttempts());
    assertEquals(2, cache.maxAttempts());
    cache.setMaxAttempts(10);
    assertEquals(Integer.valueOf(10), cache.getMaxAttempts());
    assertEquals(10, cache.maxAttempts());

    cache.setMaxAttempts(null);
    assertNull(cache.getMaxAttempts());
    assertEquals(2, cache.maxAttempts());

  }

  @Test
  public void testSetRetryInterval() throws Exception {
    TimeInterval newTi = new TimeInterval(10L, TimeUnit.MINUTES);
    RetryingCacheProxy cache = new RetryingCacheProxy();

    assertNull(cache.getRetryInterval());
    assertEquals(2000, cache.retryInterval());

    cache.setRetryInterval(newTi);
    assertEquals(newTi, cache.getRetryInterval());
    assertEquals(newTi.toMilliseconds(), cache.retryInterval());

    cache.setRetryInterval(null);

    assertNull(cache.getRetryInterval());
    assertEquals(2000, cache.retryInterval());

  }

  @Test
  public void testPut() throws Exception {
    RetryingCacheProxy cache = new RetryingCacheProxy(new ExpiringMapCache());
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
    RetryingCacheProxy cache = new RetryingCacheProxy(new ExpiringMapCache());
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
  public void testGet_EventuallyReturnsNull() throws Exception {
    RetryingCacheProxy cache = new RetryingCacheProxy().withProxiedCache(new ExpiringMapCache()).withMaxAttempts(2)
        .withRetryInterval(new TimeInterval(10L, TimeUnit.MILLISECONDS));
    initAndStart(cache);
    try {
      // This will wait for the requisite timeout!
      assertNull(cache.get("XXX"));
    }
    finally {
      stopAndClose(cache);
    }
  }

  @Test
  public void testGet_EventuallyReturnsValue() throws Exception {
    final ExpiringMapCache proxiedCache = new ExpiringMapCache();
    RetryingCacheProxy cache = new RetryingCacheProxy(proxiedCache);
    cache.setRetryInterval(new TimeInterval(500L, TimeUnit.MILLISECONDS));
    initAndStart(cache);
    try {
      new Thread() {
        public void run() {
          waitQuietly(500);
          putQuietly(proxiedCache, "XXX", "value");
        }
      }.start();
      assertEquals("value", cache.get("XXX"));
    }
    finally {
      cache.clear();
      stopAndClose(cache);
    }
  }

  @Test
  public void testRemove() throws Exception {
    RetryingCacheProxy cache = new RetryingCacheProxy(new ExpiringMapCache());
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
    RetryingCacheProxy cache = new RetryingCacheProxy(new ExpiringMapCache());
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

  private void putQuietly(Cache cache, String key, Object value) {
    try {
      cache.put(key, value);
    }
    catch (CoreException e) {
      throw new RuntimeException(e);
    }
  }

}
