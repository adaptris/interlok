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

public class MyCacheEventListener implements CacheEventListener {
  public int evictedItems = 0;
  public int expiredItems = 0;
  public int putItems = 0;
  public int removedItems = 0;
  public int updatedItems = 0;

  @Override
  public void itemEvicted(String key, Object value) {
    evictedItems++;
  }

  @Override
  public void itemExpired(String key, Object value) {
    expiredItems++;
  }

  @Override
  public void itemPut(String key, Object value) {
    putItems++;
  }

  @Override
  public void itemRemoved(String key, Object value) {
    removedItems++;
  }

  @Override
  public void itemUpdated(String key, Object value) {
    updatedItems++;
  }
}
