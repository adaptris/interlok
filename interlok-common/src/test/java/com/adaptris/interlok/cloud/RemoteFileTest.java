package com.adaptris.interlok.cloud;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

public class RemoteFileTest {

  @Test
  public void testIsDirectory() {
    RemoteFile f = new RemoteFile("hello");
    assertFalse(f.isDirectory());
    assertTrue(f.withIsDirectory(true).isDirectory());
  }

  @Test
  public void testIsFile() {
    RemoteFile f = new RemoteFile("hello");
    assertFalse(f.isFile());
    assertTrue(f.withIsFile(true).isFile());
  }

  @Test
  public void testLastModified() {
    RemoteFile f = new RemoteFile("hello");
    assertEquals(-1, f.lastModified());
    long now = System.currentTimeMillis();
    assertEquals(now, f.withLastModified(now).lastModified());
  }

  @Test
  public void testLength() {
    RemoteFile f = new RemoteFile("hello");
    assertEquals(-1, f.length());
    long size = 10L;
    assertEquals(size, f.withLength(size).length());
  }

}
