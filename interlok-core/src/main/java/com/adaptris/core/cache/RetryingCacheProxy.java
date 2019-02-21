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

import java.io.Serializable;
import java.util.List;
import java.util.concurrent.TimeUnit;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.adaptris.annotation.InputFieldDefault;
import com.adaptris.core.CoreException;
import com.adaptris.core.util.Args;
import com.adaptris.core.util.ExceptionHelper;
import com.adaptris.core.util.LifecycleHelper;
import com.adaptris.util.NumberUtils;
import com.adaptris.util.TimeInterval;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Implementation of {@link Cache} that proxies another {@link Cache} instance.
 * <p>
 * The usecase for this cache proxy is to periodically "retry" a {@link #get(String)} operation in the event that a timing issue
 * causes the cache not to have been populated when an attempt to get a value is made. If not explicitly configured, then there are
 * 2 retries spaced 2 seconds apart. Other than this behaviour, everything else is delegated to the proxied cache implementation.
 * </p>
 * 
 * @config retrying-cache-proxy
 */
@XStreamAlias("retrying-cache-proxy")
public class RetryingCacheProxy implements Cache {

  private transient static final TimeInterval DEFAULT_RETRY_INTERVAL = new TimeInterval(2L, TimeUnit.SECONDS);
  private transient static final int DEFAULT_MAX_ATTEMPTS = 2;

  private transient Logger log = LoggerFactory.getLogger(this.getClass());
  @NotNull
  @Valid
  private Cache proxiedCache;
  @InputFieldDefault(value = "2")
  private Integer maxAttempts;
  @Valid
  private TimeInterval retryInterval;

  public RetryingCacheProxy() {
  }

  public RetryingCacheProxy(Cache cache) {
    this();
    setProxiedCache(cache);
  }

  @Override
  public void init() throws CoreException {
    try {
      Args.notNull(getProxiedCache(), "proxied-cache");
      LifecycleHelper.init(getProxiedCache());
    }
    catch (IllegalArgumentException e) {
      throw ExceptionHelper.wrapCoreException(e);
    }
  }

  @Override
  public void close() {
    LifecycleHelper.close(getProxiedCache());
  }

  @Override
  public void start() throws CoreException {
    LifecycleHelper.start(getProxiedCache());
  }

  @Override
  public void stop() {
    LifecycleHelper.stop(getProxiedCache());
  }

  @Override
  public void put(String key, Serializable value) throws CoreException {
    getProxiedCache().put(key, value);
  }

  @Override
  public void put(String key, Object value) throws CoreException {
    getProxiedCache().put(key, value);
  }

  @Override
  public Object get(String key) throws CoreException {
    int max = maxAttempts();
    int i = 0;
    Object result = null;
    while (i <= max) {
      try {
        result = attemptGet(key);
        break;
      }
      catch (NotFoundException e) {
        LifecycleHelper.waitQuietly(retryInterval());
      }
      i++;
    }
    return result;
  }

  private Object attemptGet(String key) throws CoreException, NotFoundException {
    Object result = null;
    result = getProxiedCache().get(key);
    if (result == null) {
      throw new NotFoundException();
    }
    return result;
  }

  @Override
  public void remove(String key) throws CoreException {
    getProxiedCache().remove(key);
  }

  @Override
  public List<String> getKeys() throws CoreException {
    return getProxiedCache().getKeys();
  }

  @Override
  public void clear() throws CoreException {
    getProxiedCache().clear();
  }

  @Override
  public int size() throws CoreException {
    return getProxiedCache().size();
  }

  public Cache getProxiedCache() {
    return proxiedCache;
  }

  public void setProxiedCache(Cache c) {
    this.proxiedCache = Args.notNull(c, "proxied-cache");
  }

  public Integer getMaxAttempts() {
    return maxAttempts;
  }

  int maxAttempts() {
    return NumberUtils.toIntDefaultIfNull(getMaxAttempts(), DEFAULT_MAX_ATTEMPTS);
  }

  /**
   * Set the maximum number of attempts to get a cache.
   * 
   * @param max maximum number of attempts, defaults to 2.
   */
  public void setMaxAttempts(Integer max) {
    this.maxAttempts = max;
  }

  public TimeInterval getRetryInterval() {
    return retryInterval;
  }

  /**
   * Set the interval between each retry attempt.
   * 
   * @param t the interval between each attempt, defaults to 2 seconds.
   */
  public void setRetryInterval(TimeInterval t) {
    this.retryInterval = t;
  }

  long retryInterval() {
    return TimeInterval.toMillisecondsDefaultIfNull(getRetryInterval(), DEFAULT_RETRY_INTERVAL);
  }

  public RetryingCacheProxy withMaxAttempts(Integer i) {
    setMaxAttempts(i);
    return this;
  }

  public RetryingCacheProxy withRetryInterval(TimeInterval i) {
    setRetryInterval(i);
    return this;
  }

  public RetryingCacheProxy withProxiedCache(Cache c) {
    setProxiedCache(c);
    return this;
  }

  private class NotFoundException extends Exception {

    private static final long serialVersionUID = 2014060601L;

    public NotFoundException() {
      super();
    }
  }
}
