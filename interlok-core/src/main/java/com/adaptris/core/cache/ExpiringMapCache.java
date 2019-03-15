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
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import com.adaptris.annotation.AdvancedConfig;
import com.adaptris.annotation.AutoPopulated;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.annotation.DisplayOrder;
import com.adaptris.annotation.InputFieldDefault;
import com.adaptris.core.CoreException;
import com.adaptris.core.util.Args;
import com.adaptris.util.NumberUtils;
import com.adaptris.util.TimeInterval;
import com.thoughtworks.xstream.annotations.XStreamAlias;

import net.jodah.expiringmap.ExpirationPolicy;
import net.jodah.expiringmap.ExpiringMap;

/**
 * Cache implementation backed by {@code net.jodah:expiringmap} hosted on
 * <a href="https://github.com/jhalterman/expiringmap">github</a>.
 * 
 * @config expiring-map-cache
 */
@XStreamAlias("expiring-map-cache")
@ComponentProfile(since = "3.8.0")
@DisplayOrder(order ={"maxEntries", "expiration", "expirationPolicy", "eventListener"})
public class ExpiringMapCache implements Cache {

  private static final int DEFAULT_MAX_VALUES = 1024;
  private static final TimeInterval DEFAULT_EXPIRATION = new TimeInterval(60L, TimeUnit.SECONDS);

  @InputFieldDefault(value = "1024")
  private Integer maxEntries;
  @InputFieldDefault(value = "60 seconds")
  private TimeInterval expiration;
  @InputFieldDefault(value = "ACCESSED")
  private ExpirationPolicy expirationPolicy;
  @AdvancedConfig
  @Valid
  @AutoPopulated
  @NotNull
  private ExpiringMapCacheListener eventListener;

  private transient ExpiringMap<String, Object> cache;

  public ExpiringMapCache() {
    setEventListener(new ExpiringMapCacheListener());
  }

  @Override
  public void init() throws CoreException {
    cache = ExpiringMap.builder().maxSize(maxEntries()).asyncExpirationListener(getEventListener())
        .expirationPolicy(expirationPolicy()).expiration(expiration(), TimeUnit.MILLISECONDS).build();
  }

  @Override
  public void start() throws CoreException {
  }

  @Override
  public void stop() {
  }

  @Override
  public void close() {
  }

  @Override
  public void put(String key, Serializable value) throws CoreException {
    cache.put(key, value);
  }

  @Override
  public void put(String key, Object value) throws CoreException {
    cache.put(key, value);
  }

  @Override
  public Object get(String key) throws CoreException {
    return cache.get(key);
  }

  @Override
  public void remove(String key) throws CoreException {
    cache.remove(key);
  }

  @Override
  public List<String> getKeys() throws CoreException {
    return new ArrayList<String>(cache.keySet());
  }

  @Override
  public void clear() throws CoreException {
    cache.clear();
  }

  @Override
  public int size() throws CoreException {
    return cache.size();
  }

  public Integer getMaxEntries() {
    return maxEntries;
  }

  public void setMaxEntries(Integer maxEntries) {
    this.maxEntries = maxEntries;
  }

  public int maxEntries() {
    return NumberUtils.toIntDefaultIfNull(getMaxEntries(), DEFAULT_MAX_VALUES);
  }

  public TimeInterval getExpiration() {
    return expiration;
  }

  public void setExpiration(TimeInterval expiration) {
    this.expiration = expiration;
  }

  public long expiration() {
    return TimeInterval.toMillisecondsDefaultIfNull(getExpiration(), DEFAULT_EXPIRATION);
  }

  public ExpiringMapCacheListener getEventListener() {
    return eventListener;
  }

  public void setEventListener(ExpiringMapCacheListener cacheListener) {
    this.eventListener = Args.notNull(cacheListener, "cacheListener");
  }

  public ExpirationPolicy getExpirationPolicy() {
    return expirationPolicy;
  }

  public void setExpirationPolicy(ExpirationPolicy expirationPolicy) {
    this.expirationPolicy = Args.notNull(expirationPolicy, "expirationPolicy");
  }

  public ExpirationPolicy expirationPolicy() {
    return getExpirationPolicy() != null ? getExpirationPolicy() : ExpirationPolicy.ACCESSED;
  }

  public ExpiringMapCache withMaxEntries(Integer i) {
    setMaxEntries(i);
    return this;
  }

  public ExpiringMapCache withExpiration(TimeInterval t) {
    setExpiration(t);
    return this;
  }

  public ExpiringMapCache withExpirationPolicy(ExpirationPolicy p) {
    setExpirationPolicy(p);
    return this;
  }

  public ExpiringMapCache withEventListener(ExpiringMapCacheListener p) {
    setEventListener(p);
    return this;
  }
}
