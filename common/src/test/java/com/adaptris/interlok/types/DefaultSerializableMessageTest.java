package com.adaptris.interlok.types;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

import com.adaptris.interlok.types.DefaultSerializableMessage;

public class DefaultSerializableMessageTest {

  @Rule
  public TestName testName = new TestName();

  @Before
  public void setUp() throws Exception {

  }

  @After
  public void tearDown() throws Exception {

  }


  @Test
  public void testConvenienceMethods() throws Exception {
    Map<String, String> hdrs = new HashMap<>();
    hdrs.put(testName.getMethodName(), testName.getMethodName());


    DefaultSerializableMessage msg =
        new DefaultSerializableMessage().withUniqueId(testName.getMethodName()).withMessageHeaders(hdrs)
            .withPayload(testName.getMethodName()).withPayloadEncoding(testName.getMethodName());
    assertEquals(testName.getMethodName(), msg.getUniqueId());
    assertEquals(testName.getMethodName(), msg.getContent());
    assertEquals(testName.getMethodName(), msg.getContentEncoding());
    assertEquals(1, msg.getMessageHeaders().size());
    assertEquals(testName.getMethodName(), msg.getMessageHeaders().get(testName.getMethodName()));
  }


  @Test
  public void testAddMessageHeader() {
    DefaultSerializableMessage msg = new DefaultSerializableMessage();
    assertEquals(0, msg.getMessageHeaders().size());
    msg.addMessageHeader(testName.getMethodName(), testName.getMethodName());
    assertEquals(1, msg.getMessageHeaders().size());
    assertEquals(testName.getMethodName(), msg.getMessageHeaders().get(testName.getMethodName()));
  }

  @Test
  public void testRemoveMessageHeader() {
    DefaultSerializableMessage msg = new DefaultSerializableMessage();

    Map<String, String> hdrs = new HashMap<>();
    hdrs.put(testName.getMethodName(), testName.getMethodName());

    msg.setMessageHeaders(hdrs);
    assertEquals(1, msg.getMessageHeaders().size());
    assertEquals(testName.getMethodName(), msg.getMessageHeaders().get(testName.getMethodName()));

    msg.removeMessageHeader(testName.getMethodName());
    assertEquals(0, msg.getMessageHeaders().size());
    assertNull(msg.getMessageHeaders().get(testName.getMethodName()));

  }

  @Test
  public void testMessageHeaders() {

    Map<String, String> hdrs = new HashMap<>();
    hdrs.put(testName.getMethodName(), testName.getMethodName());

    DefaultSerializableMessage msg = new DefaultSerializableMessage();
    msg.setMessageHeaders(hdrs);
    assertEquals(1, msg.getMessageHeaders().size());
    assertEquals(testName.getMethodName(), msg.getMessageHeaders().get(testName.getMethodName()));

    msg.setMessageHeaders((Map<String, String>) null);
    assertEquals(0, msg.getMessageHeaders().size());
  }

  @Test
  public void testMessageHeaders_Properties() {
    DefaultSerializableMessage msg = new DefaultSerializableMessage();

    Properties hdrs = new Properties();
    hdrs.setProperty(testName.getMethodName(), testName.getMethodName());
    msg.withHeadersFromProperties(hdrs);
    assertEquals(1, msg.getMessageHeaders().size());
    assertEquals(testName.getMethodName(), msg.getMessageHeaders().get(testName.getMethodName()));

    assertNotSame(hdrs, msg.getMessageHeaders());

    msg.withHeadersFromProperties((Properties) null);
    assertEquals(0, msg.getMessageHeaders().size());
  }


  @Test
  public void testUniqueId() {
    DefaultSerializableMessage msg = new DefaultSerializableMessage();
    assertNotNull(msg.getUniqueId());
    msg.setUniqueId(testName.getMethodName());
    assertEquals(testName.getMethodName(), msg.getUniqueId());
  }

  @Test
  public void testPayload() {
    DefaultSerializableMessage msg = new DefaultSerializableMessage();
    assertNull(msg.getContent());
    msg.setContent(testName.getMethodName());
    assertEquals(testName.getMethodName(), msg.getContent());
  }

  @Test
  public void testPayloadEncoding() {
    DefaultSerializableMessage msg = new DefaultSerializableMessage();
    assertNull(msg.getContentEncoding());
    msg.setContentEncoding(testName.getMethodName());
    assertEquals(testName.getMethodName(), msg.getContentEncoding());
  }

}
