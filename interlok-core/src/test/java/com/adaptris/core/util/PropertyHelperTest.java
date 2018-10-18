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
import static org.junit.Assert.assertNull;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Map;
import java.util.Properties;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.adaptris.core.BaseCase;
import com.adaptris.core.stubs.TempFileUtils;

public class PropertyHelperTest extends PropertyHelper {

  @Before
  public void setUp() throws Exception {
  }

  @After
  public void tearDown() throws Exception {
  }

  @Test
  public void testGetPropertySubsetPropertiesString() {
    Properties p = getPropertySubset(createTestSample(), "a");
    assertEquals(2, p.size());
  }

  @Test
  public void testGetPropertySubsetPropertiesStringBoolean() {
    Properties p = getPropertySubset(createTestSample(), "A", true);
    assertEquals(2, p.size());
  }

  @Test
  public void testGetPropertyIgnoringCasePropertiesStringString() {
    assertEquals("a.value", getPropertyIgnoringCase(createTestSample(), "A.KEY", "defaultValue"));
    assertEquals("defaultValue", getPropertyIgnoringCase(createTestSample(), "BLAH", "defaultValue"));
  }

  @Test
  public void testAsMap() {
    Map<String, String> map = asMap(createTestSample());
    assertEquals(4, map.size());
    assertEquals("a.value", map.get("a.key"));
    map = asMap(null);
    assertNotNull(map);
    assertEquals(0, map.size());
  }

  @Test
  public void testGetPropertyIgnoringCasePropertiesString() {
    assertEquals("a.value", getPropertyIgnoringCase(createTestSample(), "A.KEY"));
    assertNull(getPropertyIgnoringCase(createTestSample(), "BLAH"));
  }

  @Test
  public void testLoadQuietly_File() throws Exception {
    File file = TempFileUtils.createTrackedFile(this);
    Properties sample = createTestSample();
    try (FileOutputStream out = new FileOutputStream(file)) {
      sample.store(out, "");
    }
    Properties loaded = loadQuietly(file);
    assertEquals(sample.size(), loaded.size());
    Properties doesNotExist = loadQuietly((File) null);
    assertEquals(0, doesNotExist.size());
  }

  @Test
  public void testLoadQuietly_Resource() throws Exception {
    Properties loaded = loadQuietly(BaseCase.PROPERTIES_RESOURCE);
    assertEquals(BaseCase.PROPERTIES.size(), loaded.size());
    Properties doesNotExist = loadQuietly((String) null);
    assertEquals(0, doesNotExist.size());
  }

  @Test
  public void testLoadQuietly_URL() throws Exception {
    File file = TempFileUtils.createTrackedFile(this);
    Properties sample = createTestSample();
    try (FileOutputStream out = new FileOutputStream(file)) {
      sample.store(out, "");
    }
    Properties loaded = loadQuietly(file.toURI().toURL());
    assertEquals(sample.size(), loaded.size());
    Properties doesNotExist = loadQuietly((URL) null);
    assertEquals(0, doesNotExist.size());
  }

  @Test
  public void testLoadQuietly_InputStream() throws Exception {
    Properties sample = createTestSample();
    byte[] bytes = asByteArray(sample);
    try (ByteArrayInputStream in = new ByteArrayInputStream(bytes)) {
      Properties loaded = loadQuietly(in);
      assertEquals(sample.size(), loaded.size());
    }
    Properties doesNotExist = loadQuietly((InputStream) null);
    assertEquals(0, doesNotExist.size());
  }

  @Test(expected = IOException.class)
  public void testLoad_PropertyInputStream() throws Exception {
    Properties sample = createTestSample();
    byte[] bytes = asByteArray(sample);
    Properties loaded = load(() -> {
      return new ByteArrayInputStream(bytes);
    });
    assertEquals(sample.size(), loaded.size());
    Properties doesNotExist = load(() -> {
      throw new IOException();
    });
  }

  private Properties createTestSample() {
    Properties result = new Properties();
    result.setProperty("a.key", "a.value");
    result.setProperty("a.anotherKey", "a.anotherKey");
    result.setProperty("b.key", "b.value");
    result.setProperty("b.anotherKey", "b.anotherKey");
    return result;
  }

  private byte[] asByteArray(Properties p) throws Exception {
    try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
      p.store(out, "");
      return out.toByteArray();
    }
  }
}
