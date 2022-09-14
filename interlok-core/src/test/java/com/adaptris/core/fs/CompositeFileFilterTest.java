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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

import org.junit.Test;

import com.adaptris.core.stubs.TempFileUtils;

/**
 * @author lchan
 * @author $Author: lchan $
 */
public class CompositeFileFilterTest {
  private static final String FILTER_SIZE = "SizeGT=1024__@@__Regex=.*\\.xml";
  private static final String FILTER_CUSTOM = "SizeGT=1024__@@__Regex=.*\\.xml__@@__com.adaptris.core.fs.YesOrNo=false";
  private static final String FILTER_NO_CLASSDEF = "SizeGT=1024__@@__Regex=.*\\.xml__@@__com.adaptris.core.fs.Blah=false";
  private static final String FILTER_EMPTY_EXPR = "com.adaptris.core.fs.IsFileFilter=__@@__Regex=.*\\.xml";

  private static final String[] FILTER_STRINGS = {
      "NewerThan=-PT1H",
      "OlderThan=-PT1H",
      "SizeGT=1024",
      "SizeGTE=1024",
      "SizeLT=1024",
      "SizeLTE=1024",
      "com.adaptris.core.fs.YesOrNo=false",
  };

  @Test
  public void testCreate() throws Exception {
    for (String filter : FILTER_STRINGS) {
      new CompositeFileFilter(filter, true);
    }
  }

  @Test(expected = RuntimeException.class)
  public void testCreate_Invalid() throws Exception {
    new CompositeFileFilter("IWon'tWork", true);
  }

  @Test
  public void testFilterMatches() throws Exception {
    CompositeFileFilter df = new CompositeFileFilter(FILTER_SIZE, false);
    File src = TempFileUtils.createTrackedFile(this.getClass().getSimpleName(), ".xml", df);
    write(2048, src);
    assertTrue(df.accept(src));
  }

  @Test
  public void testFilterNoMatch() throws Exception {
    CompositeFileFilter df = new CompositeFileFilter(FILTER_SIZE);
    File src = TempFileUtils.createTrackedFile(this.getClass().getSimpleName(), ".xml", df);
    write(999, src);
    assertFalse(df.accept(src));
  }

  @Test
  public void testCustomFilter() throws Exception {
    CompositeFileFilter df = new CompositeFileFilter(FILTER_CUSTOM);
    File src = TempFileUtils.createTrackedFile(this.getClass().getSimpleName(), ".xml", df);
    write(2048, src);
    assertFalse(df.accept(src));
  }

  @Test(expected = RuntimeException.class)
  public void testCustomFilterNoClassDef() throws Exception {
    new CompositeFileFilter(FILTER_NO_CLASSDEF);
  }

  @Test
  public void testCustomFilter_NoExpr() throws Exception {
    CompositeFileFilter df = new CompositeFileFilter(FILTER_EMPTY_EXPR);
    File src = TempFileUtils.createTrackedFile(this.getClass().getSimpleName(), ".xml", null, df,
        () -> "<xml/>");
    assertTrue(df.accept(src));
  }


  private void write(long size, File f) throws IOException {
    RandomAccessFile rf = new RandomAccessFile(f, "rw");
    rf.setLength(size);
    rf.close();
  }
}
