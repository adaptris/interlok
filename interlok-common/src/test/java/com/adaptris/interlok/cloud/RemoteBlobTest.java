package com.adaptris.interlok.cloud;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

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

  @Test
  public void testErrorSummarySet() {
    long now = System.currentTimeMillis();
    RemoteBlob f = new RemoteBlob.Builder()
        .setBucket("bucket")
        .setName("name")
        .setLastModified(now)
        .setSize(10L)
        .setErrorSummary("something went wrong")
        .build();

    assertEquals("something went wrong", f.getErrorSummary());
  }

  @Test
  public void testErrorSummaryNull() {
    long now = System.currentTimeMillis();
    RemoteBlob f = new RemoteBlob.Builder()
        .setBucket("bucket")
        .setName("name")
        .setLastModified(now)
        .setSize(10L)
        .setErrorSummary(null)
        .build();

    assertNull(f.getErrorSummary());
  }
}
