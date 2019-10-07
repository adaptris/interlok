package com.adaptris.interlok.cloud;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

public class RemoteFileTest {

  @Test
  public void testIsDirectory() {
    RemoteFile f = new RemoteFile.Builder().setPath("hello").build();
    assertFalse(f.isDirectory());
    assertTrue(new RemoteFile.Builder().setPath("hello").setIsDirectory(true).build().isDirectory());
  }

  @Test
  public void testIsFile() {
    RemoteFile f = new RemoteFile.Builder().setPath("hello").build();
    assertFalse(f.isFile());
    assertTrue(new RemoteFile.Builder().setPath("hello").setIsFile(true).build().isFile());
  }

  @Test
  public void testLastModified() {
    RemoteFile f = new RemoteFile.Builder().setPath("hello").build();
    assertEquals(-1, f.lastModified());
    long now = System.currentTimeMillis();
    assertEquals(now, new RemoteFile.Builder().setPath("hello").setLastModified(now).build().lastModified());
  }

  @Test
  public void testLength() {
    RemoteFile f = new RemoteFile.Builder().setPath("hello").build();
    assertEquals(-1, f.length());
    long size = 10L;
    assertEquals(size, new RemoteFile.Builder().setPath("hello").setLength(size).build().length());
  }

}
