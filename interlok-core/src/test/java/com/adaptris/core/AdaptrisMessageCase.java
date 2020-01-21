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

package com.adaptris.core;

import com.adaptris.core.stubs.MockEncoder;
import com.adaptris.util.GuidGenerator;
import com.adaptris.util.stream.StreamUtil;
import org.apache.commons.io.IOUtils;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

@SuppressWarnings("deprecation")
public abstract class AdaptrisMessageCase {

  protected static final String PAYLOAD = "Glib jocks quiz nymph to vex dwarf";
  protected static final String PAYLOAD2 = "Pack my box with five dozen liquor jugs";
  protected static final String PAYLOAD3 = "<a><b>B</b><c><d>D</d></c></a>";
  protected static final String PAYLOAD4 = "{ \"store\": { \"book\": [ { \"category\": \"reference\", \"author\": \"Nigel Rees\", \"title\": \"Sayings of the Century\", \"price\": 8.95 }, { \"category\": \"fiction\", \"author\": \"Evelyn Waugh\", \"title\": \"Sword of Honour\", \"price\": 12.99 }, { \"category\": \"fiction\", \"author\": \"Herman Melville\", \"title\": \"Moby Dick\", \"isbn\": \"0-553-21311-3\", \"price\": 8.99 }, { \"category\": \"fiction\", \"author\": \"J. R. R. Tolkien\", \"title\": \"The Lord of the Rings\", \"isbn\": \"0-395-19395-8\", \"price\": 22.99 } ], \"bicycle\": { \"color\": \"red\", \"price\": 19.95 } }, \"expensive\": 10 }";

  protected static final String VAL2 = "val2";
  protected static final String KEY2 = "key2";
  protected static final String VAL1 = "val1";
  protected static final String KEY1 = "key1";

  private AdaptrisMessage createMessage() throws UnsupportedEncodingException {
    return createMessage(null);
  }

  private AdaptrisMessage createMessage(String charEncoding) throws UnsupportedEncodingException {
    return getMessageFactory().newMessage(PAYLOAD, charEncoding, createMetadata());
  }

  private Set<MetadataElement> createMetadata() {
    Set<MetadataElement> metadata = new HashSet(Arrays.asList(new MetadataElement[] {
        new MetadataElement(KEY1, VAL1), new MetadataElement(KEY2, VAL2)
    }));
    return metadata;

  }
  protected abstract AdaptrisMessageFactory getMessageFactory();

  @Test
  public void testGetMessageFactory() throws Exception {
    AdaptrisMessageFactory mf = getMessageFactory();
    AdaptrisMessage msg = mf.newMessage();
    assertEquals(mf, msg.getFactory());
  }

  @Test
  public void testRemoveHeader() throws Exception {
    AdaptrisMessageFactory mf = getMessageFactory();
    AdaptrisMessage msg = mf.newMessage();
    msg.addMessageHeader("hello", "world");
    msg.removeMessageHeader("hello");
    assertFalse(msg.containsKey("hello"));
  }

  @Test
  public void testSetNextServiceId() throws Exception {
    String nextServiceId = "NEXT";
    AdaptrisMessage msg1 = createMessage();
    msg1.setNextServiceId(nextServiceId);
    assertEquals(nextServiceId, msg1.getNextServiceId());
    try {
      msg1.setNextServiceId(null);
      fail();
    }
    catch (IllegalArgumentException e) {

    }
    assertEquals(nextServiceId, msg1.getNextServiceId());
  }

  @Test
  public void testToString() throws Exception {
    AdaptrisMessage msg1 = createMessage();
    assertNotNull(msg1.toString());
    assertNotNull(msg1.toString(true));
    assertNotNull(msg1.toString(false));
    assertNotNull(msg1.toString(true, true));
    assertNotNull(msg1.toString(true, false));
    assertNotNull(msg1.toString(false, true));
    assertNotNull(msg1.toString(false, false));
  }

  @Test
  public void testGetPayload() throws Exception {
    AdaptrisMessage msg1 = createMessage();

    assertEquals(PAYLOAD, new String(msg1.getPayload()));
  }

  @Test
  public void testSetPayload() throws Exception {
    AdaptrisMessage msg1 = createMessage();

    msg1.setPayload(PAYLOAD2.getBytes());
    assertTrue(Arrays.equals(PAYLOAD2.getBytes(), msg1.getPayload()));
  }

  @Test
  public void testGetStringPayload() throws Exception {
    AdaptrisMessage msg1 = createMessage();

    assertEquals(PAYLOAD, msg1.getStringPayload());
  }

  @Test
  public void testSetStringPayload() throws Exception {
    AdaptrisMessage msg1 = createMessage();
    msg1.setStringPayload(PAYLOAD2, msg1.getContentEncoding());
    assertEquals(PAYLOAD2, msg1.getContent());
    // with spec. char enc.
    String enc = "ISO-8859-1";
    String payload2 = new String(PAYLOAD2.getBytes(), enc);

    msg1.setStringPayload(payload2, enc);
    assertEquals(payload2, msg1.getContent());
  }

  @Test
  public void testSetContent() throws Exception {
    AdaptrisMessage msg1 = createMessage();
    msg1.setContent(PAYLOAD2, msg1.getContentEncoding());
    assertEquals(PAYLOAD2, msg1.getContent());
    // with spec. char enc.
    String enc = "ISO-8859-1";
    String payload2 = new String(PAYLOAD2.getBytes(), enc);

    msg1.setContent(payload2, enc);
    assertEquals(payload2, msg1.getContent());
  }

  @Test
  public void testSetStringPayload_RemovesEncoding() throws Exception {
    AdaptrisMessage msg1 = createMessage();
    msg1.setContentEncoding("ISO-8859-1");
    assertEquals("ISO-8859-1", msg1.getContentEncoding());
    msg1.setStringPayload(PAYLOAD2);
    assertEquals(PAYLOAD2, msg1.getStringPayload());
    assertNull(msg1.getContentEncoding());
  }

  @Test
  public void testGetMetadataValue() throws Exception {
    AdaptrisMessage msg1 = createMessage();

    assertTrue(msg1.getMetadataValue("key1").equals("val1"));
    assertTrue(msg1.getMetadataValue("key3") == null);
    assertNull(msg1.getMetadataValue(null));
  }
  
  @Test
  public void testGetReferencedMetadata() throws Exception {
    AdaptrisMessage msg1 = createMessage();
    
    msg1.addMessageHeader("RefKey", "key1");

    assertTrue(msg1.getMetadataValue("RefKey").equals("key1"));
    assertTrue(msg1.getMetadataValue("$$RefKey").equals("val1"));
  }

  @Test
  public void testGetMetadata() throws Exception {
    AdaptrisMessage msg1 = createMessage();

    assertEquals(createMetadata(), msg1.getMetadata());
  }

  @Test
  public void testEncoder() throws Exception {
    AdaptrisMessage msg1 = createMessage();

    MockEncoder m = new MockEncoder();
    byte[] bytes = msg1.encode(m);
    assertTrue(Arrays.equals(PAYLOAD.getBytes(), bytes));
    byte[] b2 = msg1.encode(null);
    assertTrue(Arrays.equals(PAYLOAD.getBytes(), b2));
  }

  @Test
  public void testContainsKey() throws Exception {
    AdaptrisMessage msg1 = createMessage();

    assertTrue(msg1.headersContainsKey("key1"));
    assertTrue(!msg1.headersContainsKey("key3"));
  }
  
  @Test
  public void testContainsReferencedKey() throws Exception {
    AdaptrisMessage msg1 = createMessage();
    
    msg1.addMessageHeader("RefKey1", "key1");

    assertTrue(msg1.headersContainsKey("RefKey1"));
    assertTrue(msg1.headersContainsKey("$$RefKey1")); // tests for "key1"
  }

  @Test
  public void testAddMetadata() throws Exception {
    AdaptrisMessage msg1 = createMessage();

    msg1.addMetadata("key4", "val4");
    assertTrue(msg1.getMetadataValue("key4").equals("val4"));

    msg1.addMetadata(new MetadataElement("key5", "val5"));
    assertTrue(msg1.getMetadataValue("key5").equals("val5"));
  }
  
  @Test
  public void testAddReferencedMetadata() throws Exception {
    AdaptrisMessage msg1 = createMessage();
    
    msg1.addMessageHeader("RefKey", "key999");

    msg1.addMetadata("$$RefKey", "val999");
    assertTrue(msg1.getMetadataValue("key999").equals("val999"));
    assertTrue(msg1.getMetadataValue("$$RefKey").equals("val999"));
  }

  @Test
  public void testGetMetadataElement() throws Exception {
    AdaptrisMessage msg1 = createMessage();

    MetadataElement me = new MetadataElement(KEY1, VAL1);
    assertEquals(me, msg1.getMetadata(KEY1));
    assertNull(msg1.getMetadata(null));
    assertNull(msg1.getMetadata("something"));
  }

  @Test
  public void testSetMetadata() throws Exception {
    AdaptrisMessage msg1 = createMessage();

    MetadataElement mez = new MetadataElement("key6", "val6");
    Set newMetadata = new HashSet();
    newMetadata.add(mez);

    msg1.clearMetadata();
    msg1.setMetadata(newMetadata);

    assertTrue(newMetadata.equals(msg1.getMetadata()));
  }

  @Test
  public void testClearMetadata() throws Exception {
    AdaptrisMessage msg1 = createMessage();

    msg1.clearMetadata();
    assertEquals(new HashSet(), msg1.getMetadata());
  }

  @Test
  public void testGetSize() throws Exception {
    AdaptrisMessage msg1 = createMessage();

    assertEquals(PAYLOAD.length(), msg1.getSize());
  }

  @Test
  public void testSetCharEncoding() throws Exception {
    AdaptrisMessage msg1 = createMessage();

    msg1.setContentEncoding(StandardCharsets.ISO_8859_1.name());

    assertEquals(Charset.forName("iso-8859-1").name(), msg1.getCharEncoding());
    msg1.setCharEncoding("iso-8859-2");
    assertEquals(Charset.forName("iso-8859-2").name(), msg1.getContentEncoding());
    try {
      msg1.setContentEncoding("well, this, should be invalid");
      fail();
    } catch (Exception expected) {

    }

  }

  @Test
  public void testGetUniqueId() throws Exception {
    AdaptrisMessage msg1 = createMessage();

    assertTrue(msg1.getUniqueId() != null && !msg1.getUniqueId().equals(""));
  }

  @Test
  public void testSetUniqueId() throws Exception {
    AdaptrisMessage msg1 = createMessage();
    msg1.setUniqueId("uuid");

    assertTrue(msg1.getUniqueId().equals("uuid"));
  }

  @Test
  public void testAddMessageEvent() throws Exception {
    AdaptrisMessage msg1 = createMessage();

    msg1.addEvent(new StandaloneProducer(), true);
    msg1.addEvent(new StandaloneProducer(), true);
    msg1.addEvent(new StandaloneProducer(), true);
    assertEquals(3, msg1.getMessageLifecycleEvent().getMleMarkers().size());
    msg1.addMetadata(CoreConstants.MLE_SEQUENCE_KEY, "FRED");
    msg1.addEvent(new StandaloneProducer(), true);
    assertEquals(4, msg1.getMessageLifecycleEvent().getMleMarkers().size());
    // Should have been reset by the "non-int" mle sequence number
    assertEquals("1", msg1.getMetadataValue(CoreConstants.MLE_SEQUENCE_KEY));
  }

  @Test
  public void testObjectMetadata() throws Exception {
    AdaptrisMessage msg1 = createMessage();

    Object metadata2 = new Object();
    msg1.addObjectMetadata("key", metadata2);

    java.util.Map<?, ?> objectMetadata = msg1.getObjectMetadata();

    assertTrue(objectMetadata.keySet().size() == 1);
    assertTrue(metadata2.equals(objectMetadata.get("key")));
  }

  @Test
  public void testObjectHeaders() throws Exception {
    AdaptrisMessage msg1 = createMessage();

    Object metadata2 = new Object();
    msg1.addObjectHeader("key", metadata2);

    java.util.Map<?, ?> objectMetadata = msg1.getObjectHeaders();

    assertTrue(objectMetadata.keySet().size() == 1);
    assertTrue(metadata2.equals(objectMetadata.get("key")));
  }


  @Test
  public void testCloneAdaptrisMessage() throws Exception {
    AdaptrisMessage msg1 = createMessage();

    msg1.addEvent(new StandaloneProducer(), true);
    AdaptrisMessage msg2 = (AdaptrisMessage) msg1.clone();

    assertTrue(msg2.getPayload() != msg1.getPayload());
    assertTrue(msg2.getMetadata() != msg1.getMetadata());
    assertTrue(msg2.getMessageLifecycleEvent() != msg1.getMessageLifecycleEvent());
    assertTrue(msg2.getContent().equals(msg1.getContent()));
    assertTrue(msg2.getMetadata().equals(msg1.getMetadata()));
    MessageLifecycleEvent event1 = msg1.getMessageLifecycleEvent();
    MessageLifecycleEvent event2 = msg2.getMessageLifecycleEvent();
    assertEquals(event1.getCreationTime(), event2.getCreationTime());
    assertEquals(event1.getMessageUniqueId(), event2.getMessageUniqueId());
    assertEquals(event1.getMleMarkers().size(), event2.getMleMarkers().size());
  }

  @Test
  // This is Interlok-2129
  public void testCloneMessage_CheckMetadata() throws Exception {
    AdaptrisMessage msg1 = createMessage();

    msg1.addEvent(new StandaloneProducer(), true);
    AdaptrisMessage msg2 = (AdaptrisMessage) msg1.clone();
    Set<MetadataElement> msg2Metadata = msg2.getMetadata();
    Set<MetadataElement> msg1Metadata = msg1.getMetadata();

    for (MetadataElement e : msg2Metadata) {
      e.setValue("hello");
    }
    for (MetadataElement e : msg1Metadata) {
      assertNotSame("hello", e.getValue());
    }
  }

  @Test
  public void testEquivalentForTracking() throws Exception {
    AdaptrisMessage msg1 = createMessage();

    AdaptrisMessage msg2 = (AdaptrisMessage) msg1.clone();
    assertTrue(msg2.equivalentForTracking(msg1));
    msg2.setContentEncoding(null);
    msg1.setContentEncoding(null);
    assertTrue(msg2.equivalentForTracking(msg1));
    msg2.setContentEncoding(null);
    msg1.setContentEncoding("UTF-8");
    assertFalse(msg2.equivalentForTracking(msg1));

    msg2.setContentEncoding("ISO-8859-1");
    assertFalse(msg2.equivalentForTracking(msg1));
    msg1.setContentEncoding(null);
    assertFalse(msg2.equivalentForTracking(msg1));

    msg2.setContentEncoding("UTF-8");
    msg1.setContentEncoding("UTF-8");
    assertTrue(msg2.equivalentForTracking(msg1));

    msg2.addMetadata("hello", "world");
    assertFalse(msg2.equivalentForTracking(msg1));

    msg2.setUniqueId(new GuidGenerator().getUUID());
    assertFalse(msg2.equivalentForTracking(msg1));

  }

  @Test
  public void testGetMetadataValueIgnoreKeyCaseExactMatch() throws Exception {
    AdaptrisMessage msg1 = createMessage();

    msg1.addMetadata("AAA", "1");
    msg1.addMetadata("aaa", "2");
    msg1.addMetadata("Aaa", "3");
    msg1.addMetadata("aAa", "4");
    msg1.addMetadata("aaA", "5");

    String result1 = msg1.getMetadataValueIgnoreKeyCase("AAA");
    assertTrue("1".equals(result1));

    String result2 = msg1.getMetadataValueIgnoreKeyCase("aaa");
    assertTrue("2".equals(result2));

    String result3 = msg1.getMetadataValueIgnoreKeyCase("Aaa");
    assertTrue("3".equals(result3));

    String result4 = msg1.getMetadataValueIgnoreKeyCase("aAa");
    assertTrue("4".equals(result4));

    String result5 = msg1.getMetadataValueIgnoreKeyCase("aaA");
    assertTrue("5".equals(result5));

    String result6 = msg1.getMetadataValueIgnoreKeyCase("aaaa");
    assertTrue(result6 == null);
  }

  @Test
  public void testGetMetadataValueIgnoreKeyCase() throws Exception {
    AdaptrisMessage msg1 = createMessage();

    msg1.addMetadata("AAA", "1");

    String result1 = msg1.getMetadataValueIgnoreKeyCase("aaa");
    assertTrue("1".equals(result1));

    String result2 = msg1.getMetadataValueIgnoreKeyCase("Aaa");
    assertTrue("1".equals(result2));

    String result3 = msg1.getMetadataValueIgnoreKeyCase("aAa");
    assertTrue("1".equals(result3));

    String result4 = msg1.getMetadataValueIgnoreKeyCase("aaA");
    assertTrue("1".equals(result4));

    String result5 = msg1.getMetadataValueIgnoreKeyCase("aaaa");
    assertTrue(result5 == null);
  }

  @Test
  public void testInputStream() throws Exception {
    AdaptrisMessage msg1 = createMessage();

    InputStream in = msg1.getInputStream();
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    StreamUtil.copyStream(in, out);
    assertEquals(PAYLOAD, out.toString());
  }

  @Test
  public void testOutputStream() throws Exception {
    AdaptrisMessage msg1 = createMessage();

    PrintStream out = new PrintStream(msg1.getOutputStream());
    out.print(PAYLOAD2);
    // w/o closing the output stream, it's not going to be equal
    assertNotSame(PAYLOAD2, msg1.getContent());
    out.close();
    assertEquals(PAYLOAD2, msg1.getContent());
  }

  @Test
  public void testReader() throws Exception {
    AdaptrisMessage msg1 = createMessage();

    Reader in = msg1.getReader();
    StringWriter out = new StringWriter();
    IOUtils.copy(in, out);
    out.flush();
    assertEquals(PAYLOAD, out.toString());
  }

  @Test
  public void testReaderWithCharEncoding() throws Exception {
    AdaptrisMessage msg1 = createMessage("ISO-8859-1");
    Reader in = msg1.getReader();
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    IOUtils.copy(in, out, "ISO-8859-1");
    out.flush();
    assertTrue(Arrays.equals(msg1.getPayload(), out.toByteArray()));
  }

  @Test
  public void testWriter() throws Exception {
    AdaptrisMessage msg1 = createMessage();

    PrintWriter out = new PrintWriter(msg1.getWriter());
    out.print(PAYLOAD2);
    // w/o closing the output stream, it's not going to be equal
    assertNotSame(PAYLOAD2, msg1.getContent());
    out.close();
    assertEquals(PAYLOAD2, msg1.getContent());
  }

  @Test
  public void testWriter_UnchangedCharEncoding() throws Exception {
    AdaptrisMessage msg1 = createMessage("ISO-8859-1");

    PrintWriter out = new PrintWriter(msg1.getWriter());
    out.print(PAYLOAD2);
    // w/o closing the output stream, it's not going to be equal
    assertNotSame(PAYLOAD2, msg1.getContent());
    out.close();
    assertTrue(Arrays.equals(PAYLOAD2.getBytes("ISO-8859-1"), msg1.getPayload()));
  }

  @Test
  public void testWriter_ChangeCharEncoding() throws Exception {
    StringBuilder sb = new StringBuilder(PAYLOAD2);
    Charset iso8859 = Charset.forName("ISO-8859-1");

    ByteBuffer inputBuffer = ByteBuffer.wrap(new byte[]
    {
      (byte) 0xFC // a u with an umlaut in ISO-8859-1
    });

    CharBuffer d1 = iso8859.decode(inputBuffer);
    sb.append(d1.toString());
    String payload = sb.toString();
    AdaptrisMessage msg1 = createMessage("ISO-8859-1");

    PrintWriter out = new PrintWriter(msg1.getWriter("UTF-8"));
    out.print(payload);
    out.close();
    assertEquals("UTF-8", msg1.getCharEncoding());
    assertFalse(Arrays.equals(payload.getBytes("ISO-8859-1"), msg1.getPayload()));
  }

  @Test
  public void testWriter_ChangedCharEncodingNull() throws Exception {
    AdaptrisMessage msg1 = createMessage("ISO-8859-1");

    PrintWriter out = new PrintWriter(msg1.getWriter(null));
    out.print(PAYLOAD2);
    out.close();
    assertEquals("ISO-8859-1", msg1.getCharEncoding());
    assertTrue(Arrays.equals(PAYLOAD2.getBytes("ISO-8859-1"), msg1.getPayload()));
  }


  @Test
  public void testResolve() throws Exception {
    AdaptrisMessage msg = createMessage();
    msg.addMessageHeader("key*3", "val3");
    msg.addMessageHeader("nestedKey", "%message{key1}");
    assertNull(msg.resolve(null));
    assertEquals("", msg.resolve(""));
    assertEquals("Hello World", msg.resolve("Hello World"));
    assertEquals(VAL1, msg.resolve("%message{key1}"));
    assertEquals(VAL2, msg.resolve("%message{key2}"));
    assertEquals("val3", msg.resolve("%message{key*3}"));
    assertEquals(VAL1, msg.resolve("%message{nestedKey}"));
    assertEquals(PAYLOAD.length(), Integer.parseInt(msg.resolve("%message{%size}")));
    assertEquals(msg.getUniqueId(), msg.resolve("%message{%uniqueId}"));

    msg.setPayload(PAYLOAD3.getBytes());
    assertEquals(String.format("%d,B", msg.getPayload().length), msg.resolve("%message{%size},%payload{xpath:/a/b/text()}"));
    try {
      msg.resolve("%payload{xpath:/a/c/node()}");
      fail();
    } catch (Exception e) {
      // expected
    }
    try {
      msg.resolve("%payload{xpath:/invalid/cnode(-)}");
      fail();
    } catch (Exception e) {
      // expected
    }

    msg.setPayload(PAYLOAD4.getBytes());
    assertEquals(String.format("The Lord of the Rings", msg.getPayload().length), msg.resolve("%payload{jsonpath:$.store.book[3].title}"));
    try {
      msg.resolve("%payload{jsonpath:$['store']['book'][2]}");
      fail();
    } catch (Exception e) {
      // expected
    }
    try {
      msg.resolve("%payload{jsonpath:$['store'invalid][-]}");
      fail();
    } catch (Exception e) {
      // expected
    }

    assertEquals(String.format("%s_%s_%s", VAL1, VAL2, "val3"), msg.resolve("%message{key1}_%message{key2}_%message{key*3}"));
    assertEquals(String.format("%s_%s_%s", VAL1, VAL1, "val3"), msg.resolve("%message{key1}_%message{key1}_%message{key*3}"));
    assertEquals(String.format("SELECT * FROM TABLE where key1=%s and key2=%s", VAL1, VAL2),
        msg.resolve("SELECT * FROM TABLE where key1=%message{key1} and key2=%message{key2}"));
    try {
      msg.resolve("%message{does_not_exist}");
      fail();
    }
    catch (UnresolvedMetadataException expected) {
      assertTrue(expected.getMessage().contains("does_not_exist"));
    }
    try {
      msg.resolve("%payload{invalid:type}");
      fail();
    } catch (Exception e) {
      // expected
    }
  }

  // INTERLOK-1949 - resolve() should work with MetadataResolver...
  @Test
  public void testResolve_WithIndirection() throws Exception {
    AdaptrisMessage msg = createMessage();
    msg.addMessageHeader("key3", "key1");
    // $$key3 --> really use key1 as the key --> VAL1
    assertNotSame("%message{$$key3}", msg.resolve("%message{$$key3}"));
    assertEquals(VAL1, msg.resolve("%message{$$key3}"));
  }

  @Test
  public void testSetMessageHeaders() throws Exception {
    AdaptrisMessage msg = createMessage();
    msg.clearMetadata();
    msg.addMessageHeader("key1", "val1");
    assertEquals(1, msg.getMessageHeaders().size());
    Map<String, String> hdrs = new HashMap<>();
    hdrs.put("key2", "val2");
    hdrs.put("key3", "val3");
    msg.setMessageHeaders(hdrs);
    assertEquals(3, msg.getMessageHeaders().size());
  }
}
