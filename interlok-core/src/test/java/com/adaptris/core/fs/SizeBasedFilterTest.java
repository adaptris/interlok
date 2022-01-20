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

package com.adaptris.core.fs;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import java.io.File;
import org.junit.Before;
import org.junit.Test;

public class SizeBasedFilterTest extends com.adaptris.interlok.junit.scaffolding.BaseCase {

  private static final int DIFF = 2;
  private File file;


  @Before
  public void setUp() throws Exception {
    file = new File(PROPERTIES.getProperty("fs.SizeBasedFilter"));
  }

  @Test
  public void testSizeLessThanFileIsSmaller() throws Exception {
    SizeBasedFileFilter filter = new SizeLessThan(String.valueOf(file.length() + DIFF));
    assertTrue("filter.accept() should be true", filter.accept(file));
    filter = new SizeLessThan(file.length() + DIFF);
    assertTrue("filter.accept() should be true", filter.accept(file));
  }

  @Test
  public void testSizeLessThanFileIsLarger() throws Exception {
    SizeBasedFileFilter filter = new SizeLessThan(String.valueOf(file.length() - DIFF));
    assertFalse("filter.accept() should be false", filter.accept(file));
    filter = new SizeLessThan(file.length() - DIFF);
    assertFalse("filter.accept() should be false", filter.accept(file));
  }

  @Test
  public void testSizeLessThanFileIsExact() throws Exception {
    SizeBasedFileFilter filter = new SizeLessThan(String.valueOf(file.length()));
    assertFalse("filter.accept() should be false", filter.accept(file));
    filter = new SizeLessThan(file.length());
    assertFalse("filter.accept() should be false", filter.accept(file));
  }

  @Test
  public void testSizeLessThanOrEqualFileIsSmaller() throws Exception {
    SizeBasedFileFilter filter = new SizeLessThanOrEqual(String.valueOf(file.length() + DIFF));
    assertTrue("filter.accept() should be true", filter.accept(file));
    filter = new SizeLessThanOrEqual(file.length() + DIFF);
    assertTrue("filter.accept() should be true", filter.accept(file));
  }

  @Test
  public void testSizeLessThanOrEqualFileIsLarger() throws Exception {
    SizeBasedFileFilter filter = new SizeLessThanOrEqual(String.valueOf(file.length() - DIFF));
    assertFalse("filter.accept() should be false", filter.accept(file));
    filter = new SizeLessThanOrEqual(file.length() - DIFF);
    assertFalse("filter.accept() should be false", filter.accept(file));
  }

  @Test
  public void testSizeLessThanOrEqualFileIsExact() throws Exception {
    SizeBasedFileFilter filter = new SizeLessThanOrEqual(String.valueOf(file.length()));
    assertTrue("filter.accept() should be true", filter.accept(file));
    filter = new SizeLessThanOrEqual(file.length());
    assertTrue("filter.accept() should be true", filter.accept(file));
  }

  @Test
  public void testSizeGreaterThanFileIsSmaller() throws Exception {
    SizeBasedFileFilter filter = new SizeGreaterThan(String.valueOf(file.length() + DIFF));
    assertFalse("filter.accept() should be false", filter.accept(file));
    filter = new SizeGreaterThan(file.length() + DIFF);
    assertFalse("filter.accept() should be false", filter.accept(file));
  }

  @Test
  public void testSizeGreaterThanFileIsLarger() throws Exception {
    SizeBasedFileFilter filter = new SizeGreaterThan(String.valueOf(file.length() - DIFF));
    assertTrue("filter.accept() should be true", filter.accept(file));
    filter = new SizeGreaterThan(file.length() - DIFF);
    assertTrue("filter.accept() should be true", filter.accept(file));
  }

  @Test
  public void testSizeGreaterThanFileIsExact() throws Exception {
    SizeBasedFileFilter filter = new SizeGreaterThan(String.valueOf(file.length()));
    assertFalse("filter.accept() should be false", filter.accept(file));
    filter = new SizeGreaterThan(file.length());
    assertFalse("filter.accept() should be false", filter.accept(file));
  }

  @Test
  public void testSizeGreaterThanOrEqualFileIsSmaller() throws Exception {
    SizeBasedFileFilter filter = new SizeGreaterThanOrEqual(String.valueOf(file.length() + DIFF));
    assertFalse("filter.accept() should be false", filter.accept(file));
    filter = new SizeGreaterThanOrEqual(file.length() + DIFF);
    assertFalse("filter.accept() should be false", filter.accept(file));
  }

  @Test
  public void testSizeGreaterThanOrEqualFileIsLarger() throws Exception {
    SizeBasedFileFilter filter = new SizeGreaterThanOrEqual(String.valueOf(file.length() - DIFF));
    assertTrue("filter.accept() should be true", filter.accept(file));
    filter = new SizeGreaterThanOrEqual(file.length() - DIFF);
    assertTrue("filter.accept() should be true", filter.accept(file));
  }

  @Test
  public void testSizeGreaterThanOrEqualFileIsExact() throws Exception {
    SizeBasedFileFilter filter = new SizeGreaterThanOrEqual(String.valueOf(file.length()));
    assertTrue("filter.accept() should be true", filter.accept(file));
    filter = new SizeGreaterThanOrEqual(file.length());
    assertTrue("filter.accept() should be true", filter.accept(file));
  }

  @Test
  public void testSizeBasedHumanReadableConstructor() throws Exception {
    final long kilobyte = 1024;
    final long megabyte = 1024*kilobyte;
    final long gigabyte = 1024*megabyte;
    final long ten_gigabytes = 10 * gigabyte;
    assertEquals(1024, new SizeGreaterThan("1024").getFilesize());
    assertEquals(1024, new SizeGreaterThan("1024J").getFilesize());
    assertEquals(1024, new SizeGreaterThan("1024b").getFilesize());
    assertEquals(kilobyte, new SizeGreaterThanOrEqual("1k").getFilesize());
    assertEquals(megabyte, new SizeGreaterThan("1M").getFilesize());
    assertEquals(megabyte, new SizeGreaterThan("1MB").getFilesize());
    assertEquals(gigabyte, new SizeLessThan("1G").getFilesize());
    assertEquals(ten_gigabytes, new SizeLessThanOrEqual("10Gb").getFilesize());
  }
}
