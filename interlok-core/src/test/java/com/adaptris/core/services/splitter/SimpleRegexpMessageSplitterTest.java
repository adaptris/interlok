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
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import java.util.List;
import org.junit.Test;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.CoreException;

public class SimpleRegexpMessageSplitterTest extends SplitterCase {

  private static final String THIRD_FIELD_OF_CSV = "^[^,]+,[^,]+,([^,]+)";
  private static final String SIMPLE_MSG = "****\n****\n****\n****\n";
  private static final String GROUPED_CSV = "A1,A2,Group1\n" + "B1,B2,Group1\n" + "C1,C2,Group1\n" + "D1,D2,Group2\n"
      + "E1,E2,Group3\n" + "F1,F2,Group3\n";

  private static final String MULTILINE_INPUT = "ABC\nDEF\nGHI\nEND\nABC\nDEF\nGHI\nEND\n";


  @Override
  protected SimpleRegexpMessageSplitter createSplitterForTests() {
    return new SimpleRegexpMessageSplitter();
  }

  @Test
  public void testSplit() throws Exception {
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(SIMPLE_MSG);
    SimpleRegexpMessageSplitter splitter = new SimpleRegexpMessageSplitter("\n");
    List<AdaptrisMessage> result = splitter.splitMessage(msg);
    assertEquals(4, result.size());
  }

  @Test
  public void testSplitMultiline() throws Exception {
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(MULTILINE_INPUT);
    SimpleRegexpMessageSplitter splitter = new SimpleRegexpMessageSplitter("(?m)^END.*$");
    List<AdaptrisMessage> result = splitter.splitMessage(msg);
    assertEquals(2, result.size());
  }

  @Test
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

  @Test
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

  @Test
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

  @Test
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

  @Test
  public void testSplitMessage() throws Exception {
    Object obj = "ABCDEFG";
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(SIMPLE_MSG);
    msg.getObjectHeaders().put(obj, obj);
    SimpleRegexpMessageSplitter splitter = new SimpleRegexpMessageSplitter("\n");
    List<AdaptrisMessage> result = splitter.splitMessage(msg);
    assertEquals(4, result.size());
    for (AdaptrisMessage m : result) {
      assertFalse("Should not contain object metadata", m.getObjectHeaders().containsKey(obj));
    }
  }

  @Test
  public void testSplitMessageWithObjectMetadata() throws Exception {
    Object obj = "ABCDEFG";
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(SIMPLE_MSG);
    msg.getObjectHeaders().put(obj, obj);
    SimpleRegexpMessageSplitter splitter = new SimpleRegexpMessageSplitter("\n");
    splitter.setCopyObjectMetadata(true);
    List<AdaptrisMessage> result = splitter.splitMessage(msg);
    assertEquals(4, result.size());
    for (AdaptrisMessage m : result) {
      assertTrue("Should contain object metadata", m.getObjectHeaders().containsKey(obj));
      assertEquals(obj, m.getObjectHeaders().get(obj));
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
