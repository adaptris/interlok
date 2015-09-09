package com.adaptris.core.services.splitter;

import static com.adaptris.core.services.mime.MimeJunitHelper.create;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.adaptris.core.AdaptrisMessage;

public class MimePartSplitterTest extends SplitterCase {

  private static Log logR = LogFactory.getLog(MimePartSplitterTest.class);

  public MimePartSplitterTest(java.lang.String testName) {
    super(testName);
  }

  @Override
  protected void setUp() throws Exception {
  }

  @Override
  protected void tearDown() throws Exception {
  }

  @Override
  protected MimePartSplitter createSplitterForTests() {
    return new MimePartSplitter();
  }

  public void testSetters() throws Exception {
    MimePartSplitter m = new MimePartSplitter();
    assertFalse(m.preserveHeaders());
    assertNull(m.getPreserveHeaders());
    m.setPreserveHeaders(Boolean.TRUE);
    assertEquals(true, m.preserveHeaders());
    assertEquals(Boolean.TRUE, m.getPreserveHeaders());

    assertNull(m.getHeaderPrefix());
    m.setHeaderPrefix("fred");
    assertEquals("fred", m.getHeaderPrefix());
  }

  public void testSplitMessage() throws Exception {
    Object obj = "ABCDEFG";
    AdaptrisMessage msg = create();
    msg.getObjectMetadata().put(obj, obj);
    MimePartSplitter m = new MimePartSplitter();
    List<AdaptrisMessage> result = m.splitMessage(msg);
    assertEquals(3, result.size());
    for (AdaptrisMessage smsg : result) {
      assertFalse("Should not contain object metadata", smsg.getObjectMetadata().containsKey(obj));
    }
  }

  public void testSplitMessageWithObjectMetadata() throws Exception {
    Object obj = "ABCDEFG";
    AdaptrisMessage msg = create();
    msg.getObjectMetadata().put(obj, obj);
    MimePartSplitter m = new MimePartSplitter();
    m.setCopyObjectMetadata(true);
    List<AdaptrisMessage> result = m.splitMessage(msg);
    assertEquals(3, result.size());
    for (AdaptrisMessage smsg : result) {
      assertTrue("Should contain object metadata", smsg.getObjectMetadata().containsKey(obj));
      assertEquals(obj, smsg.getObjectMetadata().get(obj));
    }
  }

  public void testSplitMessage_WithPreserveHeaders() throws Exception {
    AdaptrisMessage msg = create();
    MimePartSplitter m = new MimePartSplitter();
    m.setPreserveHeaders(Boolean.TRUE);
    List<AdaptrisMessage> result = m.splitMessage(msg);
    assertEquals(3, result.size());
    for (AdaptrisMessage smsg : result) {
      assertTrue(smsg.containsKey("Content-Id"));
      assertTrue(smsg.containsKey("Content-Transfer-Encoding"));
    }
  }

  @Override
  protected String createBaseFileName(Object object) {
    return super.createBaseFileName(object) + "-MimePartSplitter";
  }

  @Override
  protected Object retrieveObjectForSampleConfig() {
    return null; // over-rides retrieveServices below instead
  }

  @Override
  protected List retrieveObjectsForSampleConfig() {
    return createExamples(new MimePartSplitter());
  }



}