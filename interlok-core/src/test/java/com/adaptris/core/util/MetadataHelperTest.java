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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.adaptris.core.CoreException;
import com.adaptris.core.MetadataCollection;
import com.adaptris.core.MetadataElement;
import com.adaptris.util.KeyValuePair;
import com.adaptris.util.KeyValuePairCollection;

public class MetadataHelperTest extends MetadataHelper {

  @BeforeEach
  public void setUp() {
  }

  @Test
  public void testConvertToProperties() {
    MetadataCollection c = new MetadataCollection();
    c.add(new MetadataElement("test", "value"));
    assertNotNull(convertToProperties(c));
    assertTrue(convertToProperties(c).containsKey("test"));
  }

  @Test
  public void testConvertFromProperties() throws Exception {
    Properties c = new Properties();
    c.setProperty("test", "value");
    assertNotNull(convertFromProperties(c));
    assertTrue(convertFromProperties(c).contains(new MetadataElement("test", "")));
  }

  @Test
  public void testConvertFromKeyValuePairs() {
    KeyValuePairCollection elements = new KeyValuePairCollection();
    elements.add(new KeyValuePair("test", "value"));
    assertNotNull(convertFromKeyValuePairs(elements));
    assertTrue(convertFromKeyValuePairs(elements).contains(new MetadataElement("test", "")));
  }

  @Test
  public void testToSet() {
    Set<MetadataElement> elements = new HashSet<>();
    elements.add(new MetadataElement("test", "value"));
    MetadataCollection c = new MetadataCollection(elements);
    assertEquals(elements, c.toSet());
  }

  @Test
  public void testMetadataFromMatchGroups() throws Exception {
    List<String> keys = Arrays.asList(new String[] { "key1", "key2" });
    Matcher matcher = Pattern.compile("/path/(.*)/(.*)").matcher("/path/value1/value2");
    matcher.matches();
    Set<MetadataElement> metadata = metadataFromMatchGroups(matcher, keys);
    assertEquals(2, metadata.size());
  }

  @Test
  public void testMetadataFromMatchGroups_Mismatch() throws Exception {
    Assertions.assertThrows(CoreException.class, () -> {
      Matcher matcher = Pattern.compile("^/path/(.*)/(.*)$").matcher("/path/value1/value2");
      matcher.matches();
      Set<MetadataElement> metadata = metadataFromMatchGroups(matcher, Collections.emptyList());
    });
  }
}
