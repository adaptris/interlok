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

  @Test
  public void testFilterMatches() throws Exception {
    File src = File.createTempFile(this.getClass().getSimpleName(), ".xml");
    write(2048, src);
    CompositeFileFilter df = new CompositeFileFilter(FILTER_SIZE);
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
