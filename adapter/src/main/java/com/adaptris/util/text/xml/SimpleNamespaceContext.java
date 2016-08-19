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

package com.adaptris.util.text.xml;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.util.KeyValuePair;
import com.adaptris.util.KeyValuePairSet;

/**
 * Implementation of NamespaceContext that is based on a number of key value pairs.
 * 
 * @author lchan
 * 
 */
public final class SimpleNamespaceContext {

  /**
   * Create a NamespaceContext based on the supplied key value pairs.
   * <p>
   * For our purposes; each element of the KeyValuePairSet.
   * <ul>
   * <li>The key is the namespace prefix</li>
   * <li>The value is the namespace uri</li>
   * </ul>
   * </p>
   *
   * @param set the key values that will form the namespace context.
   * @return a NamespaceContext implementation or null if the set was empty / null.
   */
  public static NamespaceContext create(KeyValuePairSet set) {
    if (set == null || set.size() == 0) {
      return null;
    }
    return new NamespaceContextMap(set);
  }

  /**
   * Create a NamespaceContext based on the supplied key value pairs or object metadata.
   * <p>
   * In this instance, the {@link NamespaceContext} that may be present in the {@link com.adaptris.core.AdaptrisMessage} will only be used if no
   * context was created from the supplied {@link KeyValuePairSet}.
   * </p>
   * 
   * @param set the values that will form the namespace context
   * @param msg the {@link com.adaptris.core.AdaptrisMessage} which might contain the object metadata.
   * @return a {@link NamespaceContext} implementation or null if none was available.
   * @see #create(KeyValuePairSet)
   */
  public static NamespaceContext create(KeyValuePairSet set, AdaptrisMessage msg) {
    NamespaceContext result = create(set);
    String metadataKey = SimpleNamespaceContext.class.getCanonicalName();
    if (result == null && msg.getObjectHeaders().containsKey(metadataKey)) {      
      result = (NamespaceContext) msg.getObjectHeaders().get(metadataKey);
    }
    return result;
  }

  private static class NamespaceContextMap implements NamespaceContext {

    private final Map<String, String> prefixMap;
    private final Map<String, Set<String>> nsMap;

    private NamespaceContextMap(KeyValuePairSet set) {
      Map<String, String> map = new HashMap<String, String>();
      for (KeyValuePair pair : set) {
        map.put(pair.getKey(), pair.getValue());
      }
      prefixMap = createPrefixMap(map);
      nsMap = createNamespaceMap(prefixMap);
    }

    private Map<String, String> createPrefixMap(Map<String, String> source) {
      Map<String, String> result = new HashMap<String, String>(source);
      addDefault(result, XMLConstants.XML_NS_PREFIX, XMLConstants.XML_NS_URI);
      addDefault(result, XMLConstants.XMLNS_ATTRIBUTE, XMLConstants.XMLNS_ATTRIBUTE_NS_URI);
      return Collections.unmodifiableMap(result);
    }

    private void addDefault(Map<String, String> prefixMap, String prefix, String nsURI) {
      String previous = prefixMap.put(prefix, nsURI);
      if (previous != null && !previous.equals(nsURI)) {
        throw new IllegalArgumentException("Couldn't add default prefixes");
      }
    }

    private Map<String, Set<String>> createNamespaceMap(Map<String, String> prefixMap) {
      Map<String, Set<String>> result = new HashMap<String, Set<String>>();
      for (Map.Entry<String, String> entry : prefixMap.entrySet()) {
        String nsURI = entry.getValue();
        Set<String> existingPrefixes = result.get(nsURI);
        if (isNull(existingPrefixes)) {
          existingPrefixes = new HashSet<String>();
          result.put(nsURI, existingPrefixes);
        }
        existingPrefixes.add(entry.getKey());
      }
      for (Map.Entry<String, Set<String>> entry : result.entrySet()) {
        Set<String> readOnly = Collections.unmodifiableSet(entry.getValue());
        entry.setValue(readOnly);
      }
      return result;
    }

    @Override
    public String getNamespaceURI(String prefix) {
      if (isNull(prefix)) {
        return XMLConstants.NULL_NS_URI;
      }
      String nsURI = prefixMap.get(prefix);
      return isNull(nsURI) ? XMLConstants.NULL_NS_URI : nsURI;
    }

    @Override
    public String getPrefix(String namespaceURI) {
      if (isNull(namespaceURI)) {
        return null;
      }
      Set<String> prefixes = nsMap.get(namespaceURI);
      return isNull(prefixes) ? null : prefixes.iterator().next();
    }

    @Override
    public Iterator<String> getPrefixes(String namespaceURI) {
      if (isNull(namespaceURI)) {
        return null;
      }
      Set<String> prefixes = nsMap.get(namespaceURI);
      return isNull(prefixes) ? null : prefixes.iterator();
    }

    private boolean isNull(Object value) {
      return null == value;
    }
  }
}
