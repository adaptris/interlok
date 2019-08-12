/*
    Copyright Adaptris

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
*/

package com.adaptris.core.services.conditional.conditions;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.DefaultMessageFactory;
import com.adaptris.core.cache.Cache;
import com.adaptris.core.cache.ExpiringMapCache;
import com.adaptris.core.services.cache.CacheConnection;
import com.adaptris.core.util.LifecycleHelper;

public class CacheConditionTest {
  
  @Test
  public void testExists() throws Exception {
    CacheConnection conn = new CacheConnection().withCacheInstance(new ExpiringMapCache());
    ExistsInCache condition = new ExistsInCache().withConnection(conn).withKey("%message{key}");
    try {
      AdaptrisMessage message = DefaultMessageFactory.getDefaultInstance().newMessage();
      LifecycleHelper.initAndStart(condition);
      Cache cache = conn.retrieveCache();
      cache.put("key", "VALUE");
      message.addMessageHeader("key", "key");
      assertTrue(condition.evaluate(message));
    } finally {
      LifecycleHelper.stopAndClose(condition);
    }
  }

  @Test
  public void testDoesNotExist() throws Exception {
    CacheConnection conn = new CacheConnection().withCacheInstance(new ExpiringMapCache());
    ExistsInCache condition = new ExistsInCache().withConnection(conn).withKey("does-not-exist");
    try {
      AdaptrisMessage message = DefaultMessageFactory.getDefaultInstance().newMessage();
      LifecycleHelper.initAndStart(condition);
      Cache cache = conn.retrieveCache();
      cache.put("key", "VALUE");
      assertFalse(condition.evaluate(message));
    } finally {
      LifecycleHelper.stopAndClose(condition);
    }
  }
}
