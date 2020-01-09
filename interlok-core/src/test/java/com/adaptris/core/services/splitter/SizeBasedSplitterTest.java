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
import java.util.List;
import org.junit.Test;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;

public class SizeBasedSplitterTest extends SplitterCase {

  private static final String METADATA_KEY = "hello";
  private static final String OBJ_KEY = "blah";

  @Override
  public boolean isAnnotatedForJunit4() {
    return true;
  }


  @Override
  protected SizeBasedSplitter createSplitterForTests() {
    return new SizeBasedSplitter();
  }

  @Test
  public void testSetSplitBySize() throws Exception {
    SizeBasedSplitter s = createSplitterForTests();
    assertNull(s.getSplitSizeBytes());
    assertEquals(SizeBasedSplitter.DEFAULT_SPLIT_SIZE, s.splitSizeBytes());

    s.setSplitSizeBytes(1024);
    assertEquals(Integer.valueOf(1024), s.getSplitSizeBytes());
    assertEquals(1024, s.splitSizeBytes());

    s.setSplitSizeBytes(null);
    assertNull(s.getSplitSizeBytes());
    assertEquals(SizeBasedSplitter.DEFAULT_SPLIT_SIZE, s.splitSizeBytes());
  }

  @Test
  public void testSplitMessage() throws Exception {
    SizeBasedSplitter splitter = new SizeBasedSplitter();
    splitter.setSplitSizeBytes(1);
    splitter.setCopyMetadata(true);
    splitter.setCopyObjectMetadata(false);
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(LINE);
    msg.addMetadata(METADATA_KEY, METADATA_KEY);
    msg.addObjectHeader(OBJ_KEY, OBJ_KEY);
    List<AdaptrisMessage> result = toList(splitter.splitMessage(msg));
    assertEquals(LINE.length(), result.size());
    String resultString = "";
    for (AdaptrisMessage m : result) {
      assertFalse(m.getObjectHeaders().containsKey(OBJ_KEY));
      assertTrue(m.headersContainsKey(METADATA_KEY));
      resultString += m.getContent();
    }
    assertEquals(LINE, resultString);
  }

  @Test
  public void testSplitMessageWithObjectMetadata() throws Exception {
    SizeBasedSplitter splitter = new SizeBasedSplitter();
    splitter.setSplitSizeBytes(1);
    splitter.setCopyMetadata(false);
    splitter.setCopyObjectMetadata(true);
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(LINE);
    msg.addMetadata(METADATA_KEY, METADATA_KEY);
    msg.addObjectHeader(OBJ_KEY, OBJ_KEY);
    List<AdaptrisMessage> result = toList(splitter.splitMessage(msg));
    assertEquals(LINE.length(), result.size());
    String resultString = "";
    for (AdaptrisMessage m : result) {
      assertTrue(m.getObjectHeaders().containsKey(OBJ_KEY));
      assertFalse(m.headersContainsKey(METADATA_KEY));
      resultString += m.getContent();
    }
    assertEquals(LINE, resultString);
  }
  

  @Override
  protected String createBaseFileName(Object object) {
    return super.createBaseFileName(object) + "-SizeBasedSplitter";
  }

  @Override
  protected Object retrieveObjectForSampleConfig() {
    return null; // over-rides retrieveServices below instead
  }

  @Override
  protected List retrieveObjectsForSampleConfig() {
    return createExamples(new SizeBasedSplitter());
  }


}
