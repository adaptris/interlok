/*
 * Copyright 2017 Adaptris Ltd.
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.Map;

import javax.xml.namespace.NamespaceContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.adaptris.util.KeyValuePair;
import com.adaptris.util.KeyValuePairSet;
import com.adaptris.util.text.xml.Resolver;
import com.adaptris.util.text.xml.SimpleNamespaceContext;

public class DocumentBuilderFactoryBuilderTest {

  private static final String EXTERN_ENTITIES = "http://xml.org/sax/features/external-general-entities"; // false
  private static final String DISALLOW_DOCTYPE = "http://apache.org/xml/features/disallow-doctype-decl"; // true

  private static final String[] FEATURES = {
      EXTERN_ENTITIES, DISALLOW_DOCTYPE
  };

  private static final Boolean[] FEATURE_VALUES = {
      Boolean.FALSE, Boolean.TRUE
  };

  @Before
  public void setUp() throws Exception {}

  @After
  public void tearDown() throws Exception {}

  @Test
  public void testNewInstance() {
    DocumentBuilderFactoryBuilder b = DocumentBuilderFactoryBuilder.newInstance();
    assertNotNull(b.getFeatures());
    assertEquals(0, b.getFeatures().size());
    assertEquals(true, b.getNamespaceAware());
    
    assertEquals(b, DocumentBuilderFactoryBuilder.newInstance(b));
    assertNotSame(b, DocumentBuilderFactoryBuilder.newInstance(null));
  }

  @Test
  public void testConfigureDocumentBuilderFactory() throws Exception {
    DocumentBuilderFactory f = DocumentBuilderFactory.newInstance();
    DocumentBuilderFactoryBuilder builder = DocumentBuilderFactoryBuilder.newInstance().withCoalescing(true)
        .withEntityResolver(new Resolver()).withExpandEntityReferences(true).withIgnoreComments(true)
        .withIgnoreWhitespace(true).withNamespaceAware(true)
        .withValidating(true).withXIncludeAware(true);
    assertEquals(f, builder.configure(f));
    assertEquals(true, f.isCoalescing());
    assertEquals(true, f.isExpandEntityReferences());
    assertEquals(true, f.isIgnoringComments());
    assertEquals(true, f.isIgnoringElementContentWhitespace());
    assertEquals(true, f.isValidating());
    assertEquals(true, f.isNamespaceAware());
    assertEquals(true, f.isXIncludeAware());
  }

  @Test
  public void testConfigureDocumentBuilder() throws Exception {
    DocumentBuilder dbf = DocumentBuilderFactory.newInstance().newDocumentBuilder();
    DocumentBuilderFactoryBuilder builder = DocumentBuilderFactoryBuilder.newInstance().withCoalescing(true)
        .withEntityResolver(new Resolver()).withExpandEntityReferences(true).withIgnoreComments(true).withIgnoreWhitespace(true)
        .withNamespaceAware(true).withXIncludeAware(true).withValidating(true);
    assertEquals(dbf, builder.configure(dbf));
  }

  @Test
  public void testNewDocumentBuilder() throws Exception {
    DocumentBuilderFactory f = DocumentBuilderFactory.newInstance();
    DocumentBuilderFactoryBuilder builder = DocumentBuilderFactoryBuilder.newInstance().withCoalescing(true)
        .withEntityResolver(new Resolver()).withExpandEntityReferences(true).withIgnoreComments(true).withIgnoreWhitespace(true)
        .withNamespaceAware(true).withValidating(true).withXIncludeAware(true);

    assertNotNull(builder.newDocumentBuilder(f));
  }

  @Test
  public void testBuild() throws Exception {
    DocumentBuilderFactoryBuilder b = DocumentBuilderFactoryBuilder.newInstance();
    assertNotNull(b.build());
    assertTrue(DocumentBuilderFactory.class.isAssignableFrom(b.build().getClass()));
    DocumentBuilderFactory f = b.build();
    assertEquals(false, f.isCoalescing());
    assertEquals(true, f.isExpandEntityReferences());
    assertEquals(false, f.isIgnoringComments());
    assertEquals(false, f.isIgnoringElementContentWhitespace());
    assertEquals(false, f.isValidating());
    assertEquals(true, f.isNamespaceAware());
    assertEquals(false, f.isXIncludeAware());
  }

  @Test
  public void testWithFeaturesMapOfStringBoolean() throws Exception {
    DocumentBuilderFactoryBuilder b = DocumentBuilderFactoryBuilder.newInstance().withFeatures(featuresAsMap());
    DocumentBuilderFactory f = b.build();
    assertEquals(false, f.getFeature(EXTERN_ENTITIES));
    assertEquals(true, f.getFeature(DISALLOW_DOCTYPE));
  }

  @Test
  public void testWithFeaturesKeyValuePairSet() throws Exception {
    DocumentBuilderFactoryBuilder b = DocumentBuilderFactoryBuilder.newInstance().withFeatures(features());
    DocumentBuilderFactory f = b.build();
    assertEquals(false, f.getFeature(EXTERN_ENTITIES));
    assertEquals(true, f.getFeature(DISALLOW_DOCTYPE));
  }

  @Test
  public void testWithNamespaceAware() throws Exception {
    DocumentBuilderFactoryBuilder b = DocumentBuilderFactoryBuilder.newInstance();
    assertEquals(true, b.getNamespaceAware());
    assertEquals(true, b.build().isNamespaceAware());
    assertEquals(b, b.withNamespaceAware(createNamespaceContext()));
    assertEquals(true, b.getNamespaceAware());
    assertEquals(true, b.build().isNamespaceAware());
    assertEquals(b, b.withNamespaceAware((NamespaceContext) null));
    assertEquals(false, b.getNamespaceAware());
    assertEquals(false, b.build().isNamespaceAware());
    assertEquals(b, b.withNamespaceAware((Boolean) null));
    assertEquals(false, b.build().isNamespaceAware());
  }

  private KeyValuePairSet features() {
    KeyValuePairSet result = new KeyValuePairSet();
    for (int i = 0; i < FEATURES.length; i++) {
      result.add(new KeyValuePair(FEATURES[i], FEATURE_VALUES[i].toString()));
    }
    return result;

  }

  private Map<String, Boolean> featuresAsMap() {
    Map<String, Boolean> result = new HashMap<>();
    for (int i = 0; i < FEATURES.length; i++) {
      result.put(FEATURES[i], FEATURE_VALUES[i]);
    }
    return result;
  }

  public static KeyValuePairSet createNamespaceEntries() {
    KeyValuePairSet result = new KeyValuePairSet();
    result.add(new KeyValuePair("xsd", "http://www.w3.org/2001/XMLSchema"));
    result.add(new KeyValuePair("xs", "http://www.w3.org/2001/XMLSchema"));
    return result;
  }

  public static NamespaceContext createNamespaceContext() {
    return SimpleNamespaceContext.create(createNamespaceEntries());
  }
}
