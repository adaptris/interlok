/*******************************************************************************
 * Copyright 2019 Adaptris Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package com.adaptris.core.services.dynamic;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import java.io.InputStream;
import org.junit.Test;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.DefaultMarshaller;
import com.adaptris.core.ServiceList;
import com.adaptris.core.cache.ExpiringMapCache;
import com.adaptris.core.services.LogMessageService;
import com.adaptris.core.services.cache.CacheConnection;
import com.adaptris.core.util.LifecycleHelper;

public class ServiceFromCacheTest {


  @Test
  public void testGetInputStream() throws Exception {
    CacheConnection conn = createCache("actualCacheKey");

    ServiceFromCache extractor =
        new ServiceFromCache().withKey("%message{myKey}").withConnection(conn);
    
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage();
    msg.addMetadata("myKey", "actualCacheKey");
    try {
      LifecycleHelper.initAndStart(extractor);
      try (InputStream in = extractor.getInputStream(msg)) {
        assertNotNull(in);
        assertEquals(ServiceList.class,
            DefaultMarshaller.getDefaultMarshaller().unmarshal(in).getClass());
      }
    } finally {
      LifecycleHelper.stopAndClose(extractor);
    }
  }

  @Test(expected = NullPointerException.class)
  public void testNotFound() throws Exception {
    CacheConnection conn = createCache("actualCacheKey");

    ServiceFromCache extractor =
        new ServiceFromCache().withKey("%message{myKey}").withConnection(conn);

    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage();
    msg.addMetadata("myKey", "nonExistentKey");
    try {
      LifecycleHelper.initAndStart(extractor);
      // NPE will be thrown by IOUtils.toInputStream() since it's not in the cache.
      InputStream in = extractor.getInputStream(msg);
    } finally {
      LifecycleHelper.stopAndClose(extractor);
    }
  }

  private CacheConnection createCache(String key) throws Exception {
    ExpiringMapCache cacheInstance = new ExpiringMapCache();
    CacheConnection conn = new CacheConnection();
    conn.setCacheInstance(cacheInstance);
    LifecycleHelper.initAndStart(conn);
    conn.retrieveCache().put(key,
        DynamicServiceExecutorTest.createMessage(new ServiceList(new LogMessageService()))
            .getContent());
    return conn;
  }
}
