package com.adaptris.core.fs;

import java.io.File;

import org.apache.commons.io.FileUtils;

import com.adaptris.core.AdaptrisMarshaller;
import com.adaptris.core.CoreException;

public abstract class MarshallingCacheCase extends ExpiringCacheCase {

  public MarshallingCacheCase(String name) {
    super(name);
  }

  protected File persistentStore;

  protected abstract MarshallingItemCache createCache() throws Exception;
  
  protected abstract AdaptrisMarshaller createMarshaller() throws Exception;
  
  @Override
  public void setUp() throws Exception {
    persistentStore = File.createTempFile(this.getClass().getSimpleName(), "");
    persistentStore.delete();
    super.setUp();
  }

  @Override
  public void tearDown() throws Exception {
    FileUtils.deleteQuietly(persistentStore);
    super.tearDown();
  }

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
