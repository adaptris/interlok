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

package com.adaptris.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.Set;

import javax.validation.constraints.NotNull;

import com.adaptris.core.util.MetadataHelper;
import com.adaptris.util.KeyValuePairBag;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * A container class for handling a {@link Collection} of {@link MetadataElement} instance.
 *
 * @config metadata-collection
 * @author amcgrath
 *
 */
@XStreamAlias("metadata-collection")
public class MetadataCollection extends ArrayList<MetadataElement> {

  /**
   * For backward compatible serialisation
   */
  private static final long serialVersionUID = 20120319103110L;

  public MetadataCollection() {
    super();
  }

  public MetadataCollection(@NotNull Set<MetadataElement> elements) {   
    super(Objects.requireNonNull(elements));
  }

  public MetadataCollection(@NotNull MetadataCollection metadataCollection) {
    super(Objects.requireNonNull(metadataCollection));
  }

  public MetadataCollection(@NotNull KeyValuePairBag elements) {
    this(MetadataHelper.convertFromKeyValuePairs(Objects.requireNonNull(elements)));
  }

  public MetadataCollection(@NotNull Map<String, String> elements) {
    this();
    for (Map.Entry<String, String> e : Objects.requireNonNull(elements).entrySet()) {
      add(new MetadataElement(e.getKey(), e.getValue()));
    }
  }

  public MetadataCollection(@NotNull MetadataElement... elements) {
    this(new HashSet<MetadataElement>(Arrays.asList(elements)));
  }
  
  /**
   * Will create an return a new <code>Set</code> from this <code>MetadataCollection</code>
   *
   * @return a set of metadata elements.
   */
  public Set<MetadataElement> toSet() {
    return asSet(this);
  }

  /**
   * Simple utility method that will scan the <code>MetadataElement</code>'s and will check the each key equals the supplied key.
   * Note, this method id case sensitive.
   *
   * @param key the key to search for.
   * @return true if the collection contains this key.
   */
  public boolean containsKey(String key) {
    boolean result = false;
    for (MetadataElement element : this) {
      if (element.getKey().equals(key)) {
        result = true;
        break;
      }
    }
    return result;
  }

  /**
   * Helper to turn a metadata elements into {@link Set}.
   * 
   * @param col the collection
   * @return a {@link Set}
   */
  public static Set<MetadataElement> asSet(Collection<MetadataElement> col) {
    Set<MetadataElement> result = new HashSet<MetadataElement>();
    if (col == null) {
      return result;
    }
    result.addAll(col);
    return result;
  }

  /**
   * Helper to turn a set of metadata elements into {@link Properties}.
   * 
   * @param col the collection
   * @return a {@link Properties}
   */
  public static Properties asProperties(Collection<MetadataElement> col) {
    Properties result = new Properties();
    if (col == null) return result;
    for (MetadataElement e : col) {
      result.setProperty(e.getKey(), e.getValue());
    }
    return result;
  }

  /**
   * Helper to turn a set of metadata elements into {@link Map}.
   *
   * @param col the collection
   * @return a {@link Map}
   */
  public static Map<String, String> asMap(Collection<MetadataElement> col) {
    Map<String, String> result = new HashMap<String, String>();
    if (col == null) {
      return result;
    }
    for (MetadataElement e : col) {
      result.put(e.getKey(), e.getValue());
    }
    return result;
  }
}
