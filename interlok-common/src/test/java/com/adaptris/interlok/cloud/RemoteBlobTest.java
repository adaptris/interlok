package com.adaptris.interlok.cloud;

import static org.junit.Assert.assertEquals;
import org.junit.Test;

public class RemoteBlobTest {

  @Test
  public void testBuilder() {
    long now = System.currentTimeMillis();
    RemoteBlob f = new RemoteBlob.Builder().setBucket("bucket").setName("name").setLastModified(now).setSize(10L).build();
    assertEquals("bucket", f.getBucket());
    assertEquals("name", f.getName());
    assertEquals(now, f.getLastModified());
    assertEquals(10L, f.getSize());
  }

  @Test
  public void testToRemoteFile() {
    long now = System.currentTimeMillis();
    RemoteBlob f = new RemoteBlob.Builder().setBucket("bucket").setName("name").setLastModified(now).setSize(10L).build();
    RemoteFile file = f.toFile();
    assertEquals("name", file.getName());
    assertEquals(now, file.lastModified());
    assertEquals(10L, file.length());
  }

}
