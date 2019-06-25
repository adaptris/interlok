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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.adaptris.annotation.AdvancedConfig;
import com.adaptris.core.CoreException;
import com.adaptris.util.TimeInterval;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * In memory cache of items that have been processed.
 * 
 * @config fs-inline-processed-item-cache
 * 
 * @author lchan
 * @author $Author: lchan $
 */
@XStreamAlias("fs-inline-processed-item-cache")
public class InlineItemCache implements ProcessedItemCache {
  private static final TimeInterval DEFAULT_AGE_BEFORE_EVICTION = new TimeInterval(12L, TimeUnit.HOURS);

  protected transient Map<String, ProcessedItem> cache = new HashMap<String, ProcessedItem>();
  protected transient Logger logR = LoggerFactory.getLogger(this.getClass());

  @AdvancedConfig
  private TimeInterval ageBeforeEviction;
  
  public InlineItemCache() {
  }

  @Override
  public boolean contains(String key) {
    return cache.containsKey(key);
  }

  @Override
  public ProcessedItem get(String key) {
    return cache.get(key);
  }

  @Override
  public void update(ProcessedItem i) {
    cache.put(i.getAbsolutePath(), i);
  }

  @Override
  public void clear() {
    cache.clear();
  }

  @Override
  public int size() {
    return cache.size();
  }

  @Override
  public void close() {
  }


  @Override
  public void init() throws CoreException {
  }


  @Override
  public void start() throws CoreException {
  }


  @Override
  public void stop() {
  }

  @Override
  public void save() {

  }

  @Override
  public void evict() {
    long now = System.currentTimeMillis();
    Set<Map.Entry<String, ProcessedItem>> entries = cache.entrySet();
    for (Iterator<Map.Entry<String, ProcessedItem>> i = entries.iterator(); i.hasNext(); ) {
      Map.Entry<String, ProcessedItem> entry = i.next();
      long age = entry.getValue().getLastProcessed();
      if (now - age > ageBeforeEvictionMs()) {
        i.remove();
      }
    }
  }

  @Override
  public void update(ProcessedItemList list) {
    for (ProcessedItem item : list.getProcessedItems()) {
      update(item);
    }
  }

  long ageBeforeEvictionMs() {
    return TimeInterval.toMillisecondsDefaultIfNull(getAgeBeforeEviction(),
        DEFAULT_AGE_BEFORE_EVICTION);
  }

  public TimeInterval getAgeBeforeEviction() {
    return ageBeforeEviction;
  }

  /**
   * Specify the age of an entry in the cache before it is evicted.
   *
   * @param interval the time before eviction to set, default 12 hours
   */
  public void setAgeBeforeEviction(TimeInterval interval) {
    ageBeforeEviction = interval;
  }

}
