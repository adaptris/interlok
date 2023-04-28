package com.adaptris.core.util;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.DefaultMessageLogger;

public class MessageLoggerTest {

  @Test
  public void testDefaultLogger() {
    AdaptrisMessage msg = createMessage();
    String s = new DefaultMessageLogger().toString(msg);
    System.err.println("testDefaultLogger:: " + s);
    assertNotNull(s);
    assertTrue(s.contains("The quick brown fox jumps over the lazy dog"));
    assertFalse(s.contains("MessageLifecycleEvent"));
    assertFalse(s.contains("hello world"));
  }

  @Test
  public void testFullMessageLogger() {
    AdaptrisMessage msg = createMessage();
    String s = new FullMessageLogger().toString(msg);
    System.err.println("testFullMessageLogger:: " + s);
    assertNotNull(s);
    assertTrue(s.contains("The quick brown fox jumps over the lazy dog"));
    assertTrue(s.contains("MessageLifecycleEvent"));
    assertTrue(s.contains("hello world"));
  }


  @Test
  public void testMinimalLogger() {
    AdaptrisMessage msg = createMessage();
    String s = new MinimalMessageLogger().toString(msg);
    System.err.println("testMinimalLogger:: " + s);
    assertNotNull(s);
    assertFalse(s.contains("The quick brown fox jumps over the lazy dog"));
    assertFalse(s.contains("MessageLifecycleEvent"));
    assertFalse(s.contains("hello world"));
  }


  @Test
  public void testPayloadLoggerIncludeMetadata() {
    AdaptrisMessage msg = createMessage();
    PayloadMessageLogger payloadMessageLogger = new PayloadMessageLogger();
    String s = payloadMessageLogger.toString(msg);
    System.err.println("testPayloadLogger:: " + s);
    assertNotNull(s);
    assertTrue(s.contains("The quick brown fox jumps over the lazy dog"));
    assertFalse(s.contains("MessageLifecycleEvent"));
    assertTrue(s.contains("hello world"));
    assertNull(payloadMessageLogger.getIncludeMetadata());
  }

  @Test
  public void testPayloadLoggerWithoutMetadata() {
    AdaptrisMessage msg = createMessage();
    PayloadMessageLogger payloadMessageLogger = new PayloadMessageLogger();
    payloadMessageLogger.setIncludeMetadata(false);
    String s = payloadMessageLogger.toString(msg);
    System.err.println("testPayloadLogger:: " + s);
    assertNotNull(s);
    assertFalse(s.contains("The quick brown fox jumps over the lazy dog"));
    assertFalse(s.contains("MessageLifecycleEvent"));
    assertTrue(s.contains("hello world"));
    assertFalse(payloadMessageLogger.getIncludeMetadata());
  }

  @Test
  public void testTruncatedLogger() {
    AdaptrisMessage msg = createMessage();
    // all the metadata should be truncated @ 20 characters
    String s = new TruncateMetadata(20).toString(msg);
    System.err.println("testTruncatedLogger:: " + s);
    assertNotNull(s);
    assertFalse(s.contains("The quick brown fox jumps over the lazy dog"));
    assertFalse(s.contains("MessageLifecycleEvent"));
    assertFalse(s.contains("hello world"));
  }


  public static AdaptrisMessage createMessage() {
    AdaptrisMessage message = AdaptrisMessageFactory.getDefaultInstance().newMessage("hello world");
    message.addMetadata("LENGTH_33", "Jived fox nymph grabs quick waltz");
    message.addMetadata("LENGTH_34", "Glib jocks quiz nymph to vex dwarf");
    message.addMetadata("LENGTH_35", "How vexingly quick daft zebras jump");
    message.addMetadata("LENGTH_36", "Sphinx of black quartz, judge my vow");
    message.addMetadata("LENGTH_37", "Jackdaws love my big sphinx of quartz");
    message.addMetadata("LENGTH_39", "Pack my box with five dozen liquor jugs");
    message.addMetadata("LENGTH_43", "The quick brown fox jumps over the lazy dog");
    return message;
  }
}
