/*
 * Copyright 2019 Adaptris Ltd.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.adaptris.core.services.conditional.conditions;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.NotBlank;

import com.adaptris.annotation.ComponentProfile;
import com.adaptris.annotation.InputFieldHint;
import com.adaptris.core.AdaptrisConnection;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreException;
import com.adaptris.core.cache.Cache;
import com.adaptris.core.cache.CacheProvider;
import com.adaptris.core.services.cache.CacheConnection;
import com.adaptris.core.services.conditional.Condition;
import com.adaptris.core.util.Args;
import com.adaptris.core.util.LifecycleHelper;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Condition that checks whether a key exists in a {@link Cache}
 * 
 * @config exists-in-cache
 */
@XStreamAlias("exists-in-cache")
@ComponentProfile(summary = "Check the cache for a key", tag = "condition,cache", since = "3.9.1",
    recommended = {CacheConnection.class})
public class ExistsInCache implements Condition {

  @NotNull(message = "connection may not be null")
  @Valid
  private AdaptrisConnection connection;
  @InputFieldHint(expression = true)
  @NotBlank(message = "Key must not be blank")
  private String key;

  @Override
  public void init() throws CoreException {
    LifecycleHelper.prepare(getConnection());
    LifecycleHelper.init(getConnection());
  }

  @Override
  public void start() throws CoreException {
    LifecycleHelper.start(getConnection());
  }

  @Override
  public void stop() {
    LifecycleHelper.stop(getConnection());
  }

  @Override
  public void close() {
    LifecycleHelper.close(getConnection());
  }

  @Override
  public boolean evaluate(AdaptrisMessage msg) throws CoreException {
    Cache cache = getConnection().retrieveConnection(CacheProvider.class).retrieveCache();
    return cache.get(msg.resolve(getKey())) != null;
  }

  public AdaptrisConnection getConnection() {
    return connection;
  }

  /**
   * Set the connection to the {@link Cache} instance.
   * 
   * @param connection the connection.
   */
  public void setConnection(AdaptrisConnection connection) {
    this.connection = Args.notNull(connection, "connection");
  }


  public String getKey() {
    return key;
  }

  /**
   * Specify the key to check for in the cache.
   * 
   * @param key the key.
   */
  public void setKey(String key) {
    this.key = Args.notBlank(key, "key");
  }

  public ExistsInCache withConnection(AdaptrisConnection c) {
    setConnection(c);
    return this;
  }

  public ExistsInCache withKey(String key) {
    setKey(key);
    return this;
  }
}
