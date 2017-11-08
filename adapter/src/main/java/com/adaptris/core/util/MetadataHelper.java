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

package com.adaptris.core.util;

import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import com.adaptris.core.MetadataElement;
import com.adaptris.util.KeyValuePair;
import com.adaptris.util.KeyValuePairBag;

public abstract class MetadataHelper {

  public static Properties convertToProperties(Collection<MetadataElement> s) {
    Properties result = new Properties();
    for (MetadataElement e : s) {
      result.setProperty(e.getKey(), e.getValue());
    }
    return result;
  }

  public static Set<MetadataElement> convertFromProperties(Properties p) throws IOException {
    HashSet<MetadataElement> set = new HashSet<>();
    for (String s : p.stringPropertyNames()) {
      set.add(new MetadataElement(s, p.getProperty(s)));
    }
    return set;
  }

  public static Set<MetadataElement> convertFromKeyValuePairs(KeyValuePairBag bag) {
    final HashSet<MetadataElement> set = new HashSet<>();
    for (KeyValuePair e : bag) {
      set.add(new MetadataElement(e));
    }
    return set;
  }
}
