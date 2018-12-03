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

import com.adaptris.core.CoreException;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Implementation of {@link Cache} that does nothing.
 * <p>
 * This class is included for completeness, all methods are stubs and it is simply designed to provide default behaviour in the
 * event of misconfiguration.
 * </p>
 * 
 * @config null-cache-implementation
 */
@XStreamAlias("null-cache-implementation")
public class NullCacheImplementation implements Cache {

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
  public void close() {
  }

  @Override
  public void put(String key, Serializable value) {
  }

  @Override
  public void put(String key, Object value) {
  }

  @Override
  public Object get(String key) {
    return null;
  }

  @Override
  public void remove(String key) {
  }

  @Override
  public List<String> getKeys() {
    return new ArrayList<String>();
  }

  @Override
  public void clear() {
  }

  @Override
  public int size() {
    return 0;
  }
}
