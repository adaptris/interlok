package com.adaptris.core.fs;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.adaptris.core.AdaptrisMarshaller;
import com.adaptris.core.CoreException;

/**
 * Extension to {@link InlineItemCache} that stores the procssed items to disk.
 * <p>
 * The items are stored and to disk upon close() and save() respectively; Items are only read from disk upon init(). If multiple
 * instances of this class point to the same persistentStore then results are undefined.
 * </p>
 * 
 * @license BASIC
 * @author lchan
 * @author $Author: lchan $
 */
public abstract class MarshallingItemCache extends InlineItemCache {
  private static final String DEF_CACHE_DIR = System.getProperty("user.dir") + File.separator;

  private String persistentStore;
  private transient AdaptrisMarshaller marshaller;

  /**
   * Default Constructor
   * <ul>
   * <li>PersistentStore = System.getProperty("user.dir")+ "/uniqueid"</li>
   * </ul>
   *
   * @throws Exception
   */
  public MarshallingItemCache() throws Exception {
    super();
    setPersistentStore(DEF_CACHE_DIR + UUID.randomUUID().toString().replaceAll(":", "").replaceAll("-", ""));
    marshaller = initMarshaller();
  }

  public MarshallingItemCache(String store) throws Exception {
    this();
    setPersistentStore(store);
  }

  protected abstract AdaptrisMarshaller initMarshaller() throws Exception;
  
  @Override
  public void init() throws CoreException {
    readPersistentStore();
    super.init();
  }

  @Override
  public void close() {
    writePersistentStore();
    super.stop();
  }

  @Override
  public void save() {
    super.save();
    writePersistentStore();
  }

  private void writePersistentStore() {
    try {
      File store = new File(getPersistentStore());
      File parent = store.getParentFile();
      if (parent != null) {
        parent.mkdirs();
      }
      marshaller.marshal(convertToList(cache), store);
      logR.trace("Persisted " + cache.size() + " entries to disk");

    }
    catch (CoreException e) {
      logR.warn("Failed to store cache, cache will be empty upon restart");
    }
  }

  private void readPersistentStore() throws CoreException {
    File f = new File(getPersistentStore());
    if (f.exists()) {
      cache = convertToMap((ProcessedItemList) marshaller.unmarshal(new File(getPersistentStore())));
    }
    else {
      logR.warn("[" + f.getAbsolutePath() + "] Non-existent, inaccessible or zero-length");
      cache = new HashMap<String, ProcessedItem>();
    }
  }

  /**
   * @return the filename
   */
  public String getPersistentStore() {
    return persistentStore;
  }

  /**
   * @param filename the file where to store cached items.
   */
  public void setPersistentStore(String filename) {
    persistentStore = filename;
  }

  private static Map<String, ProcessedItem> convertToMap(ProcessedItemList items) {
    Map<String, ProcessedItem> map = new Hashtable<String, ProcessedItem>();
    for (ProcessedItem item : items.getProcessedItems()) {
      map.put(item.getAbsolutePath(), item);
    }
    return map;
  }

  private static ProcessedItemList convertToList(Map<String, ProcessedItem> map) {
    List<ProcessedItem> result = new ArrayList<ProcessedItem>();
    synchronized (map) {
      for (Map.Entry<String, ProcessedItem> e : map.entrySet()) {
        result.add(e.getValue());
      }
    }
    return new ProcessedItemList(result);
  }

}
