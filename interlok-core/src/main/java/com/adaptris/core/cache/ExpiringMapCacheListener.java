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

import java.util.HashSet;
import java.util.Set;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adaptris.annotation.ComponentProfile;
import com.adaptris.core.util.Args;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

import net.jodah.expiringmap.ExpirationListener;

/**
 * {@code ExpirationListener} implementation that notifies any configured {@link CacheEventListener} that are configured.
 * 
 * @config expiring-map-cache-listener
 * @since 3.8.0
 *
 */
@XStreamAlias("expiring-map-cache-listener")
@ComponentProfile(since = "3.8.0")
public class ExpiringMapCacheListener implements ExpirationListener<String, Object> {

  private transient Logger log = LoggerFactory.getLogger(this.getClass());

  @XStreamImplicit
  @NotNull
  @Valid
  private Set<CacheEventListener> listeners;

  public ExpiringMapCacheListener() {
    setListeners(new HashSet<CacheEventListener>());
  }

  @Override
  public void expired(String key, Object value) {
    for (CacheEventListener listener : getListeners()) {
      listener.itemExpired(key, value);
    }
  }

  public Set<CacheEventListener> getListeners() {
    return listeners;
  }

  public void setListeners(Set<CacheEventListener> listeners) {
    this.listeners = listeners;
  }

  public void addEventListener(CacheEventListener listener) {
    listeners.add(Args.notNull(listener, "event-listener"));
  }

  public boolean removeEventListener(CacheEventListener listener) {
    return listeners.remove(Args.notNull(listener, "event-listener"));
  }
}
