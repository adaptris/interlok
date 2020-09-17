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

import java.io.Serializable;
import java.util.AbstractSet;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import com.adaptris.core.util.Args;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

/**
 * <p>
 * A {@linkplain Set} of <code>KeyValuePair</code> instances.
 * </p>
 * 
 * @config key-value-pair-set
 */
@XStreamAlias("key-value-pair-set")
public class KeyValuePairSet extends KeyValuePairBag implements Set<KeyValuePair>, Serializable {

  /**
   *
   */
  private static final long serialVersionUID = 2013111201L;

  @XStreamImplicit(itemFieldName = "key-value-pair")
  private LinkedHashSet<KeyValuePair> set;

  /**
   * <p>
   * Creates a new instance.
   * </p>
   */
  public KeyValuePairSet() {
    super();
    set = new LinkedHashSet<KeyValuePair>();
  }

  public KeyValuePairSet(Collection<KeyValuePair> pairs) {
    this();
    addAll(pairs);
  }

  public KeyValuePairSet(Properties properties) {
    this();
    addAll(properties);
  }

  public KeyValuePairSet(Map<String, String> map) {
    this();
    addAll(map);
  }

  @Override
  public Set<KeyValuePair> getKeyValuePairs() {
    return set;
  }

  @Override
  public void addKeyValuePair(KeyValuePair pair) {
    Args.notNull(pair, "keyValuePair");
    if (getKeyValuePairs().contains(pair)) {
      getKeyValuePairs().remove(pair);
    }
    add(pair);
  }

  @Override
  public boolean equals(Object o) {
    return super.equals(o) && o instanceof Set;
  }

  /**
   * {@inheritDoc}
   *
   * <p>
   * The behaviour of this method is the same as
   * {@link AbstractSet#removeAll(Collection)}
   * </p>
   *
   * @see AbstractSet#removeAll(Collection)
   */
  @Override
  public boolean removeAll(Collection<?> c) {
    boolean modified = false;

    if (size() > c.size()) {
      for (Iterator<?> i = c.iterator(); i.hasNext();) {
        modified |= remove(i.next());
      }
    }
    else {
      for (Iterator<?> i = iterator(); i.hasNext();) {
        if (c.contains(i.next())) {
          i.remove();
          modified = true;
        }
      }
    }
    return modified;
  }
}
