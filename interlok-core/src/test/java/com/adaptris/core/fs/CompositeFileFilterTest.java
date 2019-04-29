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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;

/**
 * @author lchan
 * @author $Author: lchan $
 */
public class CompositeFileFilterTest {
  protected transient Log logR = LogFactory.getLog(this.getClass());
  private static final String FILTER_SIZE = "SizeGT=1024__@@__Perl=.*\\.xml";
  private static final String FILTER_CUSTOM = "SizeGT=1024__@@__Perl=.*\\.xml__@@__com.adaptris.core.fs.YesOrNo=false";
  private static final String FILTER_N0CLASSDEF = "SizeGT=1024__@@__Perl=.*\\.xml__@@__com.adaptris.core.fs.Blah=false";

  private static final String[] FILTER_STRINGS = {
      "NewerThan=-PT1H",
      "OlderThan=-PT1H",
      "SizeGT=1024",
      "SizeGTE=1024",
      "SizeLT=1024",
      "SizeLTE=1024",
      "com.adaptris.core.fs.YesOrNo=false",
      "IWon'tWork"
  };

  @Test
  public void testCreate() throws Exception {
    for (String filter : FILTER_STRINGS) {
      CompositeFileFilter df = new CompositeFileFilter(filter, true);
    }
  }

  @Test
  public void testFilterMatches() throws Exception {
    File src = File.createTempFile(this.getClass().getSimpleName(), ".xml");
    write(2048, src);
    CompositeFileFilter df = new CompositeFileFilter(FILTER_SIZE, true);
    boolean accepted = df.accept(src);
    String text = src + ", size=" + src.length() + " should match";
    src.delete();
    assertTrue(text, accepted);
  }

  @Test
  public void testFilterNoMatch() throws Exception {
    File src = File.createTempFile(this.getClass().getSimpleName(), ".xml");
    write(999, src);
    CompositeFileFilter df = new CompositeFileFilter(FILTER_SIZE);
    boolean accepted = df.accept(src);
    String text = src + ", size=" + src.length() + " should not match";
    src.delete();
    assertFalse(text, accepted);
  }

  @Test
  public void testCustomFilter() throws Exception {
    File src = File.createTempFile(this.getClass().getSimpleName(), ".xml");
    write(2048, src);
    CompositeFileFilter df = new CompositeFileFilter(FILTER_CUSTOM);
    boolean accepted = df.accept(src);
    String text = src + ", size=" + src.length() + " should not match";
    src.delete();
    assertFalse(text, accepted);
  }

  @Test
  public void testCustomFilterNoClassDef() throws Exception {
    File src = File.createTempFile(this.getClass().getSimpleName(), ".xml");
    write(2048, src);
    CompositeFileFilter df = new CompositeFileFilter(FILTER_N0CLASSDEF);
    boolean accepted = df.accept(src);
    String text = src + ", size=" + src.length() + " should match, custom filefilter ignored.";
    src.delete();
    assertTrue(text, accepted);
  }

  private void write(long size, File f) throws IOException {
    RandomAccessFile rf = new RandomAccessFile(f, "rw");
    rf.setLength(size);
    rf.close();
  }
}
