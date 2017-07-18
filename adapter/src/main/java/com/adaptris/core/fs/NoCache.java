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

import com.adaptris.core.AdaptrisComponent;
import com.adaptris.core.CoreException;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * {@link ProcessedItemCache} implementation that doesn't cache.
 * 
 * @config fs-no-processed-item-cache
 * 
 * @author lchan
 * @author $Author: lchan $
 */
@XStreamAlias("fs-no-processed-item-cache")
public class NoCache implements ProcessedItemCache {

  @Deprecated
  private String uniqueId;

  public NoCache() {
  }

  @Override
  public boolean contains(String key) {
    return false;
  }

  @Override
  public ProcessedItem get(String key) {
    return null;
  }

  @Override
  public void update(ProcessedItem i) {
    return;
  }


  @Override
  public void clear() {
    return;
  }

  @Override
  public int size() {
    return 0;
  }

  /**
   * @see com.adaptris.core.AdaptrisComponent#close()
   */
  @Override
  public void close() {
  }

  /**
   * @see com.adaptris.core.AdaptrisComponent#init()
   */
  @Override
  public void init() throws CoreException {
  }

  /**
   * @see com.adaptris.core.AdaptrisComponent#start()
   */
  @Override
  public void start() throws CoreException {
  }

  /**
   * @see com.adaptris.core.AdaptrisComponent#stop()
   */
  @Override
  public void stop() {
  }

  @Override
  public void prepare() throws CoreException {}

  @Override
  public void save() {
  }

  @Override
  public void update(ProcessedItemList list) {
  }

  @Override
  public void evict() {
  }

  /**
   * Not required as this component doesn't need to extend {@link AdaptrisComponent}
   * 
   * @deprecated since 3.6.3
   */
  @Deprecated
  public String getUniqueId() {
    return uniqueId;
  }

  /**
   * Not required as this component doesn't need to extend {@link AdaptrisComponent}
   * 
   * @deprecated since 3.6.3
   */
  @Deprecated
  public void setUniqueId(String uniqueId) {
    this.uniqueId = uniqueId;
  }
}
