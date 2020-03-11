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

package com.adaptris.core.fs;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import java.io.File;
import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import com.adaptris.core.AdaptrisMarshaller;
import com.adaptris.core.CoreException;

public abstract class MarshallingCacheCase extends ExpiringCacheCase {

  protected File persistentStore;

  @Override
  protected abstract MarshallingItemCache createCache() throws Exception;
  
  protected abstract AdaptrisMarshaller createMarshaller() throws Exception;
  
  @Before
  public void setUp() throws Exception {
    persistentStore = File.createTempFile(this.getClass().getSimpleName(), "");
    persistentStore.delete();
  }

  @After
  public void tearDown() throws Exception {
    FileUtils.deleteQuietly(persistentStore);
  }

  @Test
  public void testPersistence() throws Exception {
    String oldName = Thread.currentThread().getName();
    MarshallingItemCache cache = createCache();
    try {
      Thread.currentThread().setName("testPersistence");
      start(cache);
      int count = 100;
      cache.update(createCacheEntries(count));
      stop(cache);
      start(cache);
      assertEquals(100, cache.size());
    }
    finally {
      Thread.currentThread().setName(oldName);
      stop(cache);
    }

  }

  @Test
  public void testPersistenceWithBadPersistentStore() throws Exception {
    String oldName = Thread.currentThread().getName();
    MarshallingItemCache cache = createCache();
    File badStore = File.createTempFile(this.getClass().getSimpleName(), "");
    badStore.delete();
    badStore.mkdirs();
    cache.setPersistentStore(badStore.getCanonicalPath());

    try {
      Thread.currentThread().setName("testPersistenceWithBadPersistentStore");
      cache.init();
      cache.close();
      fail();
    }
    catch (CoreException expected) {
      log.debug(expected.getMessage(), expected);
    }
    finally {
      Thread.currentThread().setName(oldName);
      FileUtils.deleteQuietly(badStore);
    }

  }

  @Test
  public void testPersistenceWithZeroLengthPersistentStore() throws Exception {
    String oldName = Thread.currentThread().getName();
    MarshallingItemCache cache = createCache();
    File badStore = File.createTempFile(this.getClass().getSimpleName(), "");
    cache.setPersistentStore(badStore.getCanonicalPath());
    try {
      Thread.currentThread().setName("testPersistenceWithZeroLengthPersistentStore");
      cache.init();
      cache.close();
      fail();
    }
    catch (CoreException expected) {
      log.debug(expected.getMessage(), expected);

    }
    finally {
      Thread.currentThread().setName(oldName);
      FileUtils.deleteQuietly(badStore);
    }

  }

  @Test
  public void testRoundTripAfterFlush() throws Exception {
    String oldName = Thread.currentThread().getName();
    MarshallingItemCache cache = createCache();
    try {
      Thread.currentThread().setName("testRoundTripAfterFlush");
      start(cache);
      int count = 100;
      cache.update(createCacheEntries(count));
      cache.evict();
      cache.save();
      AdaptrisMarshaller marshaller = createMarshaller();
      ProcessedItemList list = (ProcessedItemList) marshaller
          .unmarshal(persistentStore);
      assertEquals(100, list.getProcessedItems().size());
      for (ProcessedItem item : list.getProcessedItems()){
        ProcessedItem cachedItem = cache.get(item.getAbsolutePath());
        assertNotNull("Cache item " + item.getAbsolutePath()
            + "should not be null", cachedItem);
        assertEquals(cachedItem.getAbsolutePath(), item.getAbsolutePath());
        assertEquals(cachedItem.getFilesize(), item.getFilesize());
        assertEquals(cachedItem.getLastModified(), item.getLastModified());
        assertEquals(cachedItem.getLastProcessed(), item.getLastProcessed());
      }
    }
    finally {
      Thread.currentThread().setName(oldName);
      stop(cache);
    }
  }

}
