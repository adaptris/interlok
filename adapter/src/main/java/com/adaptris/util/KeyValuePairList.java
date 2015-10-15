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

package com.adaptris.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Properties;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

/**
 * <p>
 * An {@link List} of {@linkplain KeyValuePair} instances.
 * </p>
 * 
 * @config key-value-pair-list
 */
@XStreamAlias("key-value-pair-list")
public class KeyValuePairList extends KeyValuePairBag implements List<KeyValuePair> {

  @XStreamImplicit(itemFieldName = "key-value-pair")
  private List<KeyValuePair> list;

  public KeyValuePairList() {
    super();
    list = new ArrayList<KeyValuePair>();
  }

  public KeyValuePairList(Collection<KeyValuePair> pairs) {
    this();
    addAll(pairs);
  }

  public KeyValuePairList(Properties properties) {
    this();
    addAll(properties);
  }

  public KeyValuePairList(Map<String, String> map) {
    this();
    addAll(map);
  }



  @Override
  public List<KeyValuePair> getKeyValuePairs() {
    return list;
  }

  @Override
  public void addKeyValuePair(KeyValuePair pair) {
    if (pair == null) {
      throw new IllegalArgumentException();
    }
    add(pair);
  }

  /**
   * {@inheritDoc}
   */
  public void add(int index, KeyValuePair element) {
    getKeyValuePairs().add(index, element);
  }

  /**
   * {@inheritDoc}
   */
  public boolean addAll(int index, Collection<? extends KeyValuePair> c) {
    return getKeyValuePairs().addAll(index, c);
  }

  /**
   * {@inheritDoc}
   */
  public KeyValuePair get(int index) {
    return getKeyValuePairs().get(index);
  }

  /**
   * {@inheritDoc}
   */
  public int indexOf(Object o) {
    return getKeyValuePairs().indexOf(o);
  }

  /**
   * {@inheritDoc}
   */
  public int lastIndexOf(Object o) {
    return getKeyValuePairs().lastIndexOf(o);
  }

  /**
   * {@inheritDoc}
   */
  public ListIterator<KeyValuePair> listIterator() {
    return getKeyValuePairs().listIterator();
  }

  /**
   * {@inheritDoc}
   */
  public ListIterator<KeyValuePair> listIterator(int index) {
    return getKeyValuePairs().listIterator(index);
  }

  /**
   * {@inheritDoc}
   */
  public KeyValuePair remove(int index) {
    return getKeyValuePairs().remove(index);
  }

  /**
   * {@inheritDoc}
   */
  public KeyValuePair set(int index, KeyValuePair element) {
    return getKeyValuePairs().set(index, element);
  }

  /**
   * {@inheritDoc}
   */
  public List<KeyValuePair> subList(int fromIndex, int toIndex) {
    return getKeyValuePairs().subList(fromIndex, toIndex);
  }
}
