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

package com.adaptris.core.services.splitter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import java.io.Reader;
import java.util.Iterator;
import java.util.List;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Before;
import org.junit.Test;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.DefaultMessageFactory;
import com.adaptris.core.stubs.MockMessageProducer;
import com.adaptris.core.stubs.StubMessageFactory;
import com.adaptris.interlok.util.CloseableIterable;

public class LineCountSplitterTest extends SplitterCase {

  private static Log logR = LogFactory.getLog(LineCountSplitterTest.class);

  private AdaptrisMessage msg;
  private MockMessageProducer producer;
  private BasicMessageSplitterService service;

  @Override
  public boolean isAnnotatedForJunit4() {
    return true;
  }


  @Before
  public void setUp() throws Exception {
    msg = createLineCountMessageInput();
    producer = new MockMessageProducer();
    service = createBasic(new LineCountSplitter());
    service.setProducer(producer);
  }

  @Override
  protected LineCountSplitter createSplitterForTests() {
    return new LineCountSplitter();
  }

  @Override
  public void testSetMessageFactory() throws Exception {
    MessageSplitterImp splitter = createSplitterForTests();
    assertNull(splitter.getMessageFactory());
    assertEquals(DefaultMessageFactory.class, splitter.selectFactory(new DefaultMessageFactory().newMessage()).getClass());

    splitter.setMessageFactory(new StubMessageFactory());
    assertEquals(StubMessageFactory.class, splitter.getMessageFactory().getClass());
    assertEquals(StubMessageFactory.class, splitter.selectFactory(new DefaultMessageFactory().newMessage()).getClass());

    splitter.setMessageFactory(null);
    assertEquals(DefaultMessageFactory.class, splitter.selectFactory(new DefaultMessageFactory().newMessage()).getClass());
    assertEquals(StubMessageFactory.class, splitter.selectFactory(new StubMessageFactory().newMessage()).getClass());
  }

  @Test
  public void testSetBufferSize() {
    LineCountSplitter s = new LineCountSplitter();
    assertNull(s.getBufferSize());
    assertEquals(8192, s.bufferSize());

    s.setBufferSize(1024);
    assertEquals(Integer.valueOf(1024), s.getBufferSize());
    assertEquals(1024, s.bufferSize());

    s.setBufferSize(null);
    assertNull(s.getBufferSize());
    assertEquals(8192, s.bufferSize());
  }

  @Test
  public void testSetIgnoreBlankLines() {
    LineCountSplitter s = new LineCountSplitter();
    assertNull(s.getIgnoreBlankLines());
    assertEquals(false, s.ignoreBlankLines());

    s.setIgnoreBlankLines(Boolean.TRUE);
    assertEquals(Boolean.TRUE, s.getIgnoreBlankLines());
    assertEquals(true, s.ignoreBlankLines());

    s.setIgnoreBlankLines(null);
    assertNull(s.getIgnoreBlankLines());
    assertEquals(false, s.ignoreBlankLines());
  }

  @Test
  public void testDefaultSplit() throws Exception {
    LineCountSplitter s = new LineCountSplitter();
    List<AdaptrisMessage> msgs = toList(s.splitMessage(msg));
    assertEquals("10 split messages", 10, msgs.size());
  }

  @Test
  public void testDefaultSplit_WithCharEncoding() throws Exception {
    LineCountSplitter s = new LineCountSplitter();
    msg.setContentEncoding(System.getProperty("file.encoding"));
    List<AdaptrisMessage> msgs = toList(s.splitMessage(msg));
    assertEquals("10 split messages", 10, msgs.size());
  }

  @Test
  public void testSingleLineSplit() throws Exception {
    LineCountSplitter s = new LineCountSplitter();
    s.setSplitOnLine(1);
    List<AdaptrisMessage> msgs = toList(s.splitMessage(msg));
    assertEquals("100 split messages", 100, msgs.size());
  }

  @Test
  public void testSplitWithIgnore() throws Exception {
    LineCountSplitter s = new LineCountSplitter();
    s.setIgnoreBlankLines(true);
    List<AdaptrisMessage> msgs = toList(s.splitMessage(msg));
    // Tis' data,blankline,data for 100lines, so a 10line split should give
    // 5 actual splits
    assertEquals("5 split messages", 5, msgs.size());
  }

  @Test
  public void testDoServiceWithLineCountSplitter() throws Exception {
    msg.addMetadata("key", "value");
    execute(service, msg);
    assertEquals("Number of messages", 10, producer.getMessages()
        .size());
  }

  @Test
  public void testSplitMessage() throws Exception {
    String obj = "ABCDEFG";
    msg.addObjectHeader(obj, obj);
    LineCountSplitter s = new LineCountSplitter();
    List<AdaptrisMessage> result = toList(s.splitMessage(msg));
    assertEquals("10 split messages", 10, result.size());
    for (AdaptrisMessage m : result) {
      assertFalse("Should not contain object metadata", m.getObjectHeaders().containsKey(obj));
    }
  }

  @Test
  public void testSplitMessageWithObjectMetadata() throws Exception {
    String obj = "ABCDEFG";
    msg.addObjectHeader(obj, obj);
    LineCountSplitter s = new LineCountSplitter();
    s.setCopyObjectMetadata(true);
    List<AdaptrisMessage> result = toList(s.splitMessage(msg));
    assertEquals("10 split messages", 10, result.size());
    for (AdaptrisMessage m : result) {
      assertTrue("Should contain object metadata", m.getObjectHeaders().containsKey(obj));
      assertEquals(obj, m.getObjectHeaders().get(obj));
    }
  }

  @Test
  public void testSplitMessageWithHeader1() throws Exception {
    LineCountSplitter s = new LineCountSplitter();
    s.setKeepHeaderLines(1);
    s.setSplitOnLine(1);
    s.setIgnoreBlankLines(true);

    final String HEADER_TEXT = "HEADER LINE 1";
    List<AdaptrisMessage> result = toList(s.splitMessage(
        createLineCountMessageInputWithHeader(new String[] {HEADER_TEXT})));
    
    assertEquals("50 split messages", 50, result.size());
    
    for(AdaptrisMessage m: result) {
      List<String> lines = IOUtils.readLines(m.getReader());
      assertEquals("2 lines per message", 2, lines.size());
      assertEquals("Must be header line", HEADER_TEXT, lines.get(0));
      assertEquals("Must be regular line", LINE, lines.get(1));
    }
  }

  @Test
  public void testSplitMessageWithHeader2() throws Exception {
    LineCountSplitter s = new LineCountSplitter();
    s.setKeepHeaderLines(2);
    s.setSplitOnLine(10);
    s.setIgnoreBlankLines(true);

    final String HEADER_LINE_1 = "HEADER LINE 1";
    final String HEADER_LINE_2 = "HEADER LINE 2";
    List<AdaptrisMessage> result = toList(s.splitMessage(
        createLineCountMessageInputWithHeader(new String[] {HEADER_LINE_1, HEADER_LINE_2})));
    
    assertEquals("5 split messages", 5, result.size());
    
    for(AdaptrisMessage m: result) {
      try (Reader reader = m.getReader()) {
        List<String> lines = IOUtils.readLines(reader);
        assertEquals("12 lines per message", 12, lines.size());
        assertEquals("Must be header line 1", HEADER_LINE_1, lines.get(0));
        assertEquals("Must be header line 2", HEADER_LINE_2, lines.get(1));
        for(int i=2; i<12; i++) {
          assertEquals("Must be regular line", LINE, lines.get(i));
        }
      }
    }
  }

  @Test
  public void testIterator_DoubleProtection() throws Exception {
    MessageSplitterImp splitter = new LineCountSplitter(1);
    try (CloseableIterable<AdaptrisMessage> iterable = CloseableIterable.ensureCloseable(splitter.splitMessage(msg))) {
      Iterator<AdaptrisMessage> first = iterable.iterator();
      try {
        Iterator<AdaptrisMessage> second = iterable.iterator();
        fail();
      } catch (IllegalStateException expected) {
        
      }
    }
    
  }

  @Test
  public void testIterator_Remove() throws Exception {
    MessageSplitterImp splitter = new LineCountSplitter(1);
    try (CloseableIterable<AdaptrisMessage> iterable = CloseableIterable
        .ensureCloseable(splitter.splitMessage(msg))) {
      Iterator<AdaptrisMessage> first = iterable.iterator();
      try {
        if (first.hasNext()) first.next();
        first.remove();
        fail();
      }
      catch (UnsupportedOperationException expected) {

      }
    }
  }

  @Test
  public void testIterator_HasNext() throws Exception {
    MessageSplitterImp splitter = new LineCountSplitter(1);
    try (CloseableIterable<AdaptrisMessage> iterable = CloseableIterable
        .ensureCloseable(splitter.splitMessage(msg))) {
      Iterator<AdaptrisMessage> first = iterable.iterator();
      assertTrue(first.hasNext());
      assertTrue(first.hasNext());
    }
  }
  
  @Override
  protected String createBaseFileName(Object object) {
    return super.createBaseFileName(object) + "-LineCountSplitter";
  }

  @Override
  protected Object retrieveObjectForSampleConfig() {
    return null; // over-rides retrieveServices below instead
  }

  @Override
  protected List retrieveObjectsForSampleConfig() {
    return createExamples(new LineCountSplitter(100));
  }


}
