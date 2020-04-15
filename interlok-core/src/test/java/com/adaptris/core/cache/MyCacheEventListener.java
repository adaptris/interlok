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

import java.util.concurrent.atomic.AtomicInteger;

public class MyCacheEventListener implements CacheEventListener {
  private AtomicInteger evictedItems = new AtomicInteger(0);
  private AtomicInteger expiredItems = new AtomicInteger(0);;
  private AtomicInteger putItems = new AtomicInteger(0);;
  private AtomicInteger removedItems = new AtomicInteger(0);;
  private AtomicInteger updatedItems = new AtomicInteger(0);;

  @Override
  public void itemEvicted(String key, Object value) {
    evictedItems.incrementAndGet();
  }

  @Override
  public void itemExpired(String key, Object value) {
    expiredItems.incrementAndGet();
  }

  @Override
  public void itemPut(String key, Object value) {
    putItems.incrementAndGet();
  }

  @Override
  public void itemRemoved(String key, Object value) {
    removedItems.incrementAndGet();
  }

  @Override
  public void itemUpdated(String key, Object value) {
    updatedItems.incrementAndGet();
  }

  public int evictCount() {
    return evictedItems.get();
  }

  public int expiredCount() {
    return expiredItems.get();
  }

  public int putCount() {
    return putItems.get();
  }

  public int removedCount() {
    return removedItems.get();
  }

  public int updatedCount() {
    return updatedItems.get();
  }
}
