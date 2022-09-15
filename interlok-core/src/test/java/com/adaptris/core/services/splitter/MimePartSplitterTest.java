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

import static com.adaptris.interlok.junit.scaffolding.util.MimeJunitHelper.create;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Test;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.Service;

public class MimePartSplitterTest extends SplitterCase {

  @Override
  protected MimePartSplitter createSplitterForTests() {
    return new MimePartSplitter();
  }

  @Test
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

  @Test
  public void testSplitMessage() throws Exception {
    Object obj = "ABCDEFG";
    AdaptrisMessage msg = create();
    msg.getObjectHeaders().put(obj, obj);
    MimePartSplitter m = new MimePartSplitter();
    List<AdaptrisMessage> result = m.splitMessage(msg);
    assertEquals(3, result.size());
    for (AdaptrisMessage smsg : result) {
      assertFalse("Should not contain object metadata", smsg.getObjectHeaders().containsKey(obj));
    }
  }

  @Test
  public void testSplitMessageWithObjectMetadata() throws Exception {
    Object obj = "ABCDEFG";
    AdaptrisMessage msg = create();
    msg.getObjectHeaders().put(obj, obj);
    MimePartSplitter m = new MimePartSplitter();
    m.setCopyObjectMetadata(true);
    List<AdaptrisMessage> result = m.splitMessage(msg);
    assertEquals(3, result.size());
    for (AdaptrisMessage smsg : result) {
      assertTrue("Should contain object metadata", smsg.getObjectHeaders().containsKey(obj));
      assertEquals(obj, smsg.getObjectHeaders().get(obj));
    }
  }

  @Test
  public void testSplitMessage_WithPreserveHeaders() throws Exception {
    AdaptrisMessage msg = create();
    MimePartSplitter m = new MimePartSplitter();
    m.setPreserveHeaders(Boolean.TRUE);
    List<AdaptrisMessage> result = m.splitMessage(msg);
    assertEquals(3, result.size());
    for (AdaptrisMessage smsg : result) {
      assertTrue(smsg.headersContainsKey("Content-Id"));
      assertTrue(smsg.headersContainsKey("Content-Transfer-Encoding"));
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
  protected List<Service> retrieveObjectsForSampleConfig() {
    return createExamples(new MimePartSplitter());
  }

}
