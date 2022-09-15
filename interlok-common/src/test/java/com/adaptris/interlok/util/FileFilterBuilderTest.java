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
package com.adaptris.interlok.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.FileFilter;
import java.util.Map;
import java.util.function.Function;

import org.apache.commons.io.filefilter.DelegateFileFilter;
import org.apache.commons.io.filefilter.FileFileFilter;
import org.apache.commons.io.filefilter.SizeFileFilter;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.junit.Test;

public class FileFilterBuilderTest extends FileFilterBuilder {

  @Test
  public void testEmptyExpression() throws Exception {
    FileFilter f = build("", FileFilterBuilder.DEFAULT_FILE_FILTER_IMP);
    assertNotNull(f);
    assertTrue(f.accept(null));
  }

  @Test
  public void testLongConstructor() throws Exception {
    FileFilter f = build("12", SizeFileFilter.class.getCanonicalName());
    assertEquals(SizeFileFilter.class, f.getClass());
  }

  @Test
  public void testStringConstructor() throws Exception {
    FileFilter f = build("*test*.java~*~", WildcardFileFilter.class.getCanonicalName());
    assertEquals(WildcardFileFilter.class, f.getClass());
  }

  @Test(expected = RuntimeException.class)
  public void testNonPrimitiveConstructor() throws Exception {
    build("*test*.java~*~", DelegateFileFilter.class.getCanonicalName());
  }

  @Test
  public void testConverters() throws Exception {
    for (Map.Entry<Class, Function<String, Object>> converter : SUPPORTED_CNST_ARGS.entrySet()) {
      assertNotNull(converter.getKey());
      assertNotNull(converter.getValue());
      try {
        // This is just for coverage!
        converter.getValue().apply("12");
      } catch (Exception ignored) {

      }
    }
  }

  @Test
  public void testNoArgConstructor() throws Exception {
    FileFilter f = build("", AcceptAny.class.getCanonicalName());
    assertEquals(AcceptAny.class, f.getClass());
  }

  @Test(expected = RuntimeException.class)
  public void testInvalidNoArgsConstructor() throws Exception {
    // Since FileFileFilter doesn't have a public noargs constructor.
    build("", FileFileFilter.class.getCanonicalName());
  }

}
