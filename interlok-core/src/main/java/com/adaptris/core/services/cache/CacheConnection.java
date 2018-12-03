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
package com.adaptris.core.services.cache;

import javax.validation.Valid;

import com.adaptris.core.AdaptrisConnectionImp;
import com.adaptris.core.CoreException;
import com.adaptris.core.cache.Cache;
import com.adaptris.core.cache.CacheProvider;
import com.adaptris.core.cache.NullCacheImplementation;
import com.adaptris.core.util.LifecycleHelper;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * A Connection to a cache instance.
 * 
 * @config cache-connection
 */
@XStreamAlias("cache-connection")
public class CacheConnection extends AdaptrisConnectionImp implements CacheProvider {

  private static final Cache DEFAULT_CACHE_IMPL = new NullCacheImplementation();
  
  @Valid
  private Cache cacheInstance;

  public CacheConnection() {
    super();
  }

  public CacheConnection(Cache cache) {
    this();
    setCacheInstance(cache);
  }

  @Override
  protected void prepareConnection() throws CoreException {
    LifecycleHelper.prepare(getCacheInstance());
  }

  @Override
  protected void initConnection() throws CoreException {
    LifecycleHelper.init(getCacheInstance());
  }

  @Override
  protected void startConnection() throws CoreException {
    LifecycleHelper.start(getCacheInstance());

  }

  @Override
  protected void stopConnection() {
    LifecycleHelper.stop(getCacheInstance());
  }

  @Override
  protected void closeConnection() {
    LifecycleHelper.close(getCacheInstance());
  }

  public Cache getCacheInstance() {
    return cacheInstance;
  }

  public void setCacheInstance(Cache cache) {
    this.cacheInstance = cache;
  }

  public Cache retrieveCache() {
    return getCacheInstance() != null ? getCacheInstance() : DEFAULT_CACHE_IMPL;
  }

}
