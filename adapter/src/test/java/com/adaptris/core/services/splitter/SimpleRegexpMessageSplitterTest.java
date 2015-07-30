/*
 * $RCSfile: SimpleRegexpMessageSplitterTest.java,v $
 * $Revision: 1.5 $
 * $Date: 2009/05/01 16:28:48 $
 * $Author: lchan $
 */
package com.adaptris.core.services.splitter;

import java.util.List;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.CoreException;

public class SimpleRegexpMessageSplitterTest extends SplitterCase {

  private static final String THIRD_FIELD_OF_CSV = "^[^,]+,[^,]+,([^,]+)";
  private static final String SIMPLE_MSG = "****\n****\n****\n****\n";
  private static final String GROUPED_CSV = "A1,A2,Group1\n" + "B1,B2,Group1\n" + "C1,C2,Group1\n" + "D1,D2,Group2\n"
      + "E1,E2,Group3\n" + "F1,F2,Group3\n";

  private static final String MULTILINE_INPUT = "ABC\nDEF\nGHI\nEND\nABC\nDEF\nGHI\nEND\n";

  public SimpleRegexpMessageSplitterTest(String arg0) {
    super(arg0);
  }

  @Override
  protected void setUp() throws Exception {
    super.setUp();

  }

  @Override
  protected SimpleRegexpMessageSplitter createSplitterForTests() {
    return new SimpleRegexpMessageSplitter();
  }
  public void testSplit() throws Exception {
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(SIMPLE_MSG);
    SimpleRegexpMessageSplitter splitter = new SimpleRegexpMessageSplitter("\n");
    List<AdaptrisMessage> result = splitter.splitMessage(msg);
    assertEquals(4, result.size());
  }

  public void testSplitMultiline() throws Exception {
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(MULTILINE_INPUT);
    SimpleRegexpMessageSplitter splitter = new SimpleRegexpMessageSplitter("(?m)^END.*$");
    List<AdaptrisMessage> result = splitter.splitMessage(msg);
    assertEquals(2, result.size());
  }

  public void testInvalidPattern() throws Exception {
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(SIMPLE_MSG);
    SimpleRegexpMessageSplitter splitter = new SimpleRegexpMessageSplitter("[");
    try {
      List<AdaptrisMessage> result = splitter.splitMessage(msg);
      fail();
    }
    catch (CoreException expected) {
      ;
    }
  }

  public void testSplitByGroupNoMatch() throws Exception {
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(GROUPED_CSV);
    SimpleRegexpMessageSplitter splitter = new SimpleRegexpMessageSplitter();
    splitter.setSplitPattern("\n");
    splitter.setCompareToPreviousMatch(true);
    splitter.setMatchPattern("^[^,]+,[^,],([^,]+)");
    try {
      List<AdaptrisMessage> result = splitter.splitMessage(msg);
      fail();
    }
    catch (CoreException expected) {

    }
  }

  public void testSplitByGroupPreserveFirst() throws Exception {
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(GROUPED_CSV);
    SimpleRegexpMessageSplitter splitter = new SimpleRegexpMessageSplitter();
    splitter.setSplitPattern("\n");
    splitter.setCompareToPreviousMatch(true);
    // splitter.setIgnoreFirstSubMessage(true);
    splitter.setMatchPattern(THIRD_FIELD_OF_CSV);
    List<AdaptrisMessage> result = splitter.splitMessage(msg);
    // There's a blank line if we don't ignore the first split.
    assertEquals(4, result.size());
  }

  public void testSplitByGroupIgnoreFirst() throws Exception {
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(GROUPED_CSV);
    SimpleRegexpMessageSplitter splitter = new SimpleRegexpMessageSplitter();
    splitter.setSplitPattern("\n");
    splitter.setCompareToPreviousMatch(true);
    splitter.setIgnoreFirstSubMessage(true);
    splitter.setMatchPattern(THIRD_FIELD_OF_CSV);
    List<AdaptrisMessage> result = splitter.splitMessage(msg);
    assertEquals(3, result.size());
  }

  public void testSplitMessage() throws Exception {
    Object obj = "ABCDEFG";
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(SIMPLE_MSG);
    msg.getObjectMetadata().put(obj, obj);
    SimpleRegexpMessageSplitter splitter = new SimpleRegexpMessageSplitter("\n");
    List<AdaptrisMessage> result = splitter.splitMessage(msg);
    assertEquals(4, result.size());
    for (AdaptrisMessage m : result) {
      assertFalse("Should not contain object metadata", m.getObjectMetadata().containsKey(obj));
    }
  }

  public void testSplitMessageWithObjectMetadata() throws Exception {
    Object obj = "ABCDEFG";
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(SIMPLE_MSG);
    msg.getObjectMetadata().put(obj, obj);
    SimpleRegexpMessageSplitter splitter = new SimpleRegexpMessageSplitter("\n");
    splitter.setCopyObjectMetadata(true);
    List<AdaptrisMessage> result = splitter.splitMessage(msg);
    assertEquals(4, result.size());
    for (AdaptrisMessage m : result) {
      assertTrue("Should contain object metadata", m.getObjectMetadata().containsKey(obj));
      assertEquals(obj, m.getObjectMetadata().get(obj));
    }
  }

  @Override
  protected String createBaseFileName(Object object) {
    return super.createBaseFileName(object) + "-RegexpSplitter";
  }

  @Override
  protected Object retrieveObjectForSampleConfig() {
    return null; // over-rides retrieveServices below instead
  }

  @Override
  protected String getExampleCommentHeader(Object o) {
    return super.getExampleCommentHeader(o) + "\n<!-- \n The example document for this split process is Field1|Field2|Field3\n"
        + " which would create 3 new messages\n-->\n";
  }

  @Override
  protected List retrieveObjectsForSampleConfig() {
    return createExamples(new SimpleRegexpMessageSplitter("\\|"));
  }

}
