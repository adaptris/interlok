/*
 * $RCSfile: LineCountSplitterTest.java,v $
 * $Revision: 1.3 $
 * $Date: 2009/01/28 10:01:53 $
 * $Author: lchan $
 */
package com.adaptris.core.services.splitter;

import java.io.Reader;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.DefaultMessageFactory;
import com.adaptris.core.stubs.MockMessageProducer;
import com.adaptris.core.stubs.StubMessageFactory;

public class LineCountSplitterTest extends SplitterCase {

  private static Log logR = LogFactory.getLog(LineCountSplitterTest.class);

  private AdaptrisMessage msg;
  private MockMessageProducer producer;
  private BasicMessageSplitterService service;

  public LineCountSplitterTest(java.lang.String testName) {
    super(testName);
  }

  @Override
  protected void setUp() throws Exception {
    msg = createLineCountMessageInput();
    producer = new MockMessageProducer();
    service = createBasic(new LineCountSplitter());
    service.setProducer(producer);
  }

  @Override
  protected void tearDown() throws Exception {
  }

  @Override
  protected LineCountSplitter createSplitterForTests() {
    return new LineCountSplitter();
  }

  @Override
  public void testSetMessageFactory() throws Exception {
    MessageSplitterImp splitter = createSplitterForTests();
    assertNotNull(splitter.getMessageFactory());
    assertEquals(DefaultMessageFactory.class, splitter.getMessageFactory().getClass());
    assertEquals(DefaultMessageFactory.class, splitter.selectFactory(new DefaultMessageFactory().newMessage()).getClass());

    splitter.setMessageFactory(new StubMessageFactory());
    assertEquals(StubMessageFactory.class, splitter.getMessageFactory().getClass());
    assertEquals(StubMessageFactory.class, splitter.selectFactory(new DefaultMessageFactory().newMessage()).getClass());

    splitter.setMessageFactory(null);
    assertEquals(DefaultMessageFactory.class, splitter.selectFactory(new DefaultMessageFactory().newMessage()).getClass());
    assertEquals(StubMessageFactory.class, splitter.selectFactory(new StubMessageFactory().newMessage()).getClass());
  }

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

  public void testDefaultSplit() throws Exception {
    LineCountSplitter s = new LineCountSplitter();
    List<AdaptrisMessage> msgs = toList(s.splitMessage(msg));
    assertEquals("10 split messages", 10, msgs.size());
  }

  public void testDefaultSplit_WithCharEncoding() throws Exception {
    LineCountSplitter s = new LineCountSplitter();
    msg.setCharEncoding(System.getProperty("file.encoding"));
    List<AdaptrisMessage> msgs = toList(s.splitMessage(msg));
    assertEquals("10 split messages", 10, msgs.size());
  }

  public void testSingleLineSplit() throws Exception {
    LineCountSplitter s = new LineCountSplitter();
    s.setSplitOnLine(1);
    List<AdaptrisMessage> msgs = toList(s.splitMessage(msg));
    assertEquals("100 split messages", 100, msgs.size());
  }

  public void testSplitWithIgnore() throws Exception {
    LineCountSplitter s = new LineCountSplitter();
    s.setIgnoreBlankLines(true);
    List<AdaptrisMessage> msgs = toList(s.splitMessage(msg));
    // Tis' data,blankline,data for 100lines, so a 10line split should give
    // 5 actual splits
    assertEquals("5 split messages", 5, msgs.size());
  }

  public void testDoServiceWithLineCountSplitter() throws Exception {
    msg.addMetadata("key", "value");
    execute(service, msg);
    assertEquals("Number of messages", 10, producer.getMessages()
        .size());
  }

  public void testSplitMessage() throws Exception {
    Object obj = "ABCDEFG";
    msg.getObjectMetadata().put(obj, obj);
    LineCountSplitter s = new LineCountSplitter();
    List<AdaptrisMessage> result = toList(s.splitMessage(msg));
    assertEquals("10 split messages", 10, result.size());
    for (AdaptrisMessage m : result) {
      assertFalse("Should not contain object metadata", m.getObjectMetadata().containsKey(obj));
    }
  }

  public void testSplitMessageWithObjectMetadata() throws Exception {
    Object obj = "ABCDEFG";
    msg.getObjectMetadata().put(obj, obj);
    LineCountSplitter s = new LineCountSplitter();
    s.setCopyObjectMetadata(true);
    List<AdaptrisMessage> result = toList(s.splitMessage(msg));
    assertEquals("10 split messages", 10, result.size());
    for (AdaptrisMessage m : result) {
      assertTrue("Should contain object metadata", m.getObjectMetadata().containsKey(obj));
      assertEquals(obj, m.getObjectMetadata().get(obj));
    }
  }
  
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
    return createExamples(new LineCountSplitter());
  }


}