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

import java.util.ArrayList;
import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import com.adaptris.annotation.AutoPopulated;
import com.adaptris.annotation.Removal;
import com.adaptris.core.AdaptrisConnection;
import com.adaptris.core.AdaptrisConnectionImp;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.ConnectedService;
import com.adaptris.core.CoreException;
import com.adaptris.core.ServiceException;
import com.adaptris.core.ServiceImp;
import com.adaptris.core.cache.Cache;
import com.adaptris.core.cache.CacheProvider;
import com.adaptris.core.util.Args;
import com.adaptris.core.util.ExceptionHelper;
import com.adaptris.core.util.LifecycleHelper;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

/**
 * Base class that provides common functions used by all cache services
 * 
 */
public abstract class CacheServiceBase extends ServiceImp implements ConnectedService {

  @Deprecated
  @Valid
  @Removal(version = "3.9.0")
  private Cache cache;
  @Valid
  private AdaptrisConnection connection;

  // All this for backwards compatibility. dammit.
  private transient AdaptrisConnection cacheConnection;

  @Valid
  @XStreamImplicit
  @NotNull
  @AutoPopulated
  private List<CacheEntryEvaluator> cacheEntryEvaluators;

  public CacheServiceBase() {
    setCacheEntryEvaluators(new ArrayList<CacheEntryEvaluator>());
  }


  @Override
  public void prepare() throws CoreException {
    try {
      if (cache != null) {
        log.warn("'cache' is deprecated; use a connection instead");
        cacheConnection = new CacheWrapper(cache);
      }
      else {
        cacheConnection = Args.notNull(connection, "connection");
      }
      LifecycleHelper.prepare(cacheConnection);
    }
    catch (IllegalArgumentException e) {
      throw ExceptionHelper.wrapCoreException(e);
    }
  }

  @Override
  public void closeService() {
    LifecycleHelper.close(cacheConnection);
  }

  @Override
  public void initService() throws CoreException {
    LifecycleHelper.init(cacheConnection);

  }

  @Override
  public void start() throws CoreException {
    super.start();
    LifecycleHelper.start(cacheConnection);
  }

  @Override
  public void stop() {
    super.stop();
    LifecycleHelper.stop(cacheConnection);
  }

  /**
   * The cache object to be used by this service
   * 
   * @param cache
   * @deprecated since 3.6.4 - use {@link CacheConnection} with {@link #setConnection(AdaptrisConnection)} instead.
   */
  @Deprecated
  @Removal(version = "3.9.0", message = "use #setConnection(AdaptrisConnection) with a CacheConnection")
  public void setCache(Cache cache) {
    this.cache = cache;
  }

  /**
   * The cache object to be used by this service
   * 
   * @deprecated since 3.6.4 - use {@link CacheConnection} with {@link #setConnection(AdaptrisConnection)} instead.
   */
  @Removal(version = "3.9.0", message = "use #setConnection(AdaptrisConnection) with a CacheConnection")
  public Cache getCache() {
    return cache;
  }

  protected Cache retrieveCache() {
    return cacheConnection.retrieveConnection(CacheProvider.class).retrieveCache();
  }

  public List<CacheEntryEvaluator> getCacheEntryEvaluators() {
    return cacheEntryEvaluators;
  }

  /**
   * Set the list of evaluators that will be used to for generate keys for accessing the cache.
   *
   */
  public void setCacheEntryEvaluators(List<CacheEntryEvaluator> list) {
    cacheEntryEvaluators = Args.notNull(list, "cacheEntryEvaluators");
  }

  public void addCacheEntryEvaluator(CacheEntryEvaluator generator) {
    cacheEntryEvaluators.add(generator);
  }

  public AdaptrisConnection getConnection() {
    return connection;
  }

  public void setConnection(AdaptrisConnection cacheConnection) {
    this.connection = cacheConnection;
  }

  /**
   * Retrieves the value from the cache and then stores it against the message using the supplied value translator
   *
   */
  protected void addCacheValueToMessage(AdaptrisMessage msg, String key, CacheValueTranslator cvt, boolean quietly)
      throws CoreException {
    Object value = retrieveCache().get(key);
    if (value == null) {
      if (quietly) {
        log.warn("Unable to find value in cache for key [{}], nothing to do", key);
        return;
      } else {
        throw new ServiceException("No value in cache for key " + key);
      }
    }
    cvt.addValueToMessage(msg, value);
  }

  private class CacheWrapper extends AdaptrisConnectionImp implements CacheProvider {
    private Cache myCache;
    public CacheWrapper(Cache c) {
      myCache = c;
    }

    @Override
    protected void prepareConnection() throws CoreException {
      LifecycleHelper.prepare(myCache);
    }

    @Override
    protected void initConnection() throws CoreException {
      LifecycleHelper.init(myCache);
    }

    @Override
    protected void startConnection() throws CoreException {
      LifecycleHelper.start(myCache);
    }

    @Override
    protected void stopConnection() {
      LifecycleHelper.stop(myCache);
    }

    @Override
    protected void closeConnection() {
      LifecycleHelper.close(myCache);
    }

    @Override
    public Cache retrieveCache() {
      return myCache;
    }

  }

}
