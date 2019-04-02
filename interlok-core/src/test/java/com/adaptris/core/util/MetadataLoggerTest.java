package com.adaptris.core.util;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.MetadataElement;

public class MetadataLoggerTest {

  @Test
  public void testDefaultMethods() {
    AdaptrisMessage msg = MessageLoggerTest.createMessage();
    MetadataElement e1 = msg.getMetadata("LENGTH_39");
    MetadataElement e2 = msg.getMetadata("LENGTH_43");
    String s = new MetadataKeysOnly().toString(e1, e2);
    System.err.println("testDefaultMethods:: " + s);
    assertNotNull(s);
    assertFalse(s.contains("The quick brown fox jumps over the lazy dog"));
    assertFalse(s.contains("Pack my box with five dozen liquor jugs"));
  }

  @Test
  public void testMetadataKeysOnly() {
    AdaptrisMessage msg = MessageLoggerTest.createMessage();
    String s = new MetadataKeysOnly().toString(msg.getMetadata());
    System.err.println("testMinimalLogger:: " + s);
    assertNotNull(s);
    assertFalse(s.contains("The quick brown fox jumps over the lazy dog"));
    assertFalse(s.contains("Pack my box with five dozen liquor jugs"));
  }

  @Test
  public void testTruncatedLogger() {
    AdaptrisMessage msg = MessageLoggerTest.createMessage();
    // all the metadata should be truncated @ 20 characters
    String s = new TruncateMetadata(20).toString(msg.getMetadata());
    System.err.println("testTruncatedLogger:: " + s);
    assertNotNull(s);
    assertFalse(s.contains("The quick brown fox jumps over the lazy dog"));
    assertTrue(s.contains("The quick brown f..."));
  }
}
