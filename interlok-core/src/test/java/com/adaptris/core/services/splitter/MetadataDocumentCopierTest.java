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
import java.util.List;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.stubs.DefectiveMessageFactory;
import com.adaptris.core.stubs.MockMessageProducer;
import com.adaptris.interlok.util.CloseableIterable;

@SuppressWarnings("deprecation")
public class MetadataDocumentCopierTest extends SplitterCase {

  private static final String METADATA_KEY_INDEX = "MyMetadataKeyIndex";

  private static final String METADATA_KEY = "MyMetadataKey";

  private static Log logR = LogFactory.getLog(MetadataDocumentCopierTest.class);

  @Override
  public boolean isAnnotatedForJunit4() {
    return true;
  }


  @Override
  protected String createBaseFileName(Object object) {
    return super.createBaseFileName(object) + "-MetadataDocumentCopier";
  }

  @Override
  protected Object retrieveObjectForSampleConfig() {
    return null; // over-rides retrieveServices below instead
  }

  @Override
  protected List retrieveObjectsForSampleConfig() {
    return createExamples(new MetadataDocumentCopier(METADATA_KEY));
  }

  @Override
  protected String getExampleCommentHeader(Object o) {
    return super.getExampleCommentHeader(o) + "\n<!-- \n Creates 'n' copies of a message where 'n' is determined by\n"
        + "the value associated with 'MyMetadataKey'\n" + "-->\n";
  }

  @Override
  protected MetadataDocumentCopier createSplitterForTests() {
    return new MetadataDocumentCopier();
  }

  @Test
  public void testConstructors() throws Exception {
    MetadataDocumentCopier splitter = new MetadataDocumentCopier(METADATA_KEY);
    assertEquals(METADATA_KEY, splitter.getMetadataKey());
    splitter = new MetadataDocumentCopier();
    assertNull(splitter.getMetadataKey());
  }

  @Test
  public void testSetMetadataKey() throws Exception {
    MetadataDocumentCopier splitter = new MetadataDocumentCopier();
    assertNull(splitter.getMetadataKey());
    splitter.setMetadataKey("FRED");
    assertEquals("FRED", splitter.getMetadataKey());
    try {
      splitter.setMetadataKey(null);
    } catch (IllegalArgumentException e) {

    }
    try {
      splitter.setMetadataKey("");
    } catch (IllegalArgumentException e) {

    }
  }

  @Test
  public void testSetIndexMetadataKey() throws Exception {
    MetadataDocumentCopier splitter = new MetadataDocumentCopier();
    assertNull(splitter.getIndexMetadataKey());
    splitter.setIndexMetadataKey("FRED");
    assertEquals("FRED", splitter.getIndexMetadataKey());
    splitter.setIndexMetadataKey(null);
    assertNull(splitter.getIndexMetadataKey());
    splitter.setIndexMetadataKey("");
    assertEquals("", splitter.getIndexMetadataKey());
  }

  @Test
  public void testSplit() throws Exception {
    final int expectedSplitCount = 10;
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(LINE);
    String obj = "ABCDEFG";
    msg.addObjectHeader(obj, obj);
    msg.addMetadata(METADATA_KEY, String.valueOf(expectedSplitCount));
    MetadataDocumentCopier splitter = new MetadataDocumentCopier(METADATA_KEY, METADATA_KEY_INDEX);
    try (CloseableIterable<AdaptrisMessage> result = splitter.splitMessage(msg)) {
      int count = 0;
      for (AdaptrisMessage m : result) {
        assertFalse("No Object Metadata", m.getObjectHeaders().containsKey(obj));
        assertEquals(String.valueOf(count), m.getMetadataValue(METADATA_KEY_INDEX));
        count++;
      }
      assertEquals(expectedSplitCount, count);
    }
  }

  @Test
  public void testSplit_NoIndex() throws Exception {
    final int expectedSplitCount = 10;
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(LINE);
    String obj = "ABCDEFG";
    msg.addObjectHeader(obj, obj);
    msg.addMetadata(METADATA_KEY, String.valueOf(expectedSplitCount));
    MetadataDocumentCopier splitter = new MetadataDocumentCopier(METADATA_KEY);
    try (CloseableIterable<AdaptrisMessage> result = splitter.splitMessage(msg)) {
      int count = 0;
      for (AdaptrisMessage m : result) {
        assertFalse("No Object Metadata", m.getObjectHeaders().containsKey(obj));
        assertFalse(m.containsKey(METADATA_KEY_INDEX));
        count++;
      }
      assertEquals(expectedSplitCount, count);
    }
  }

  @Test
  public void testSplit_EmptyMetadata() throws Exception {
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(LINE);
    String obj = "ABCDEFG";
    msg.addObjectHeader(obj, obj);
    MetadataDocumentCopier splitter = new MetadataDocumentCopier(METADATA_KEY, METADATA_KEY_INDEX);
    try (CloseableIterable<AdaptrisMessage> result = splitter.splitMessage(msg)) {
      int count = 0;
      for (AdaptrisMessage m : result) {
        assertFalse("No Object Metadata", m.getObjectHeaders().containsKey(obj));
        count++;
      }
      assertEquals(0, count);
    }
  }

  @Test
  public void testSplitWithObjectMetadata() throws Exception {
    final int expectedSplitCount = 10;
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(LINE);
    String obj = "ABCDEFG";
    msg.addObjectHeader(obj, obj);
    msg.addMetadata(METADATA_KEY, String.valueOf(expectedSplitCount));
    MetadataDocumentCopier splitter = new MetadataDocumentCopier(METADATA_KEY, METADATA_KEY_INDEX);
    splitter.setCopyObjectMetadata(true);
    try (CloseableIterable<AdaptrisMessage> result = splitter.splitMessage(msg)) {
      int count = 0;
      for (AdaptrisMessage m : result) {
        assertTrue("Object Metadata", m.getObjectHeaders().containsKey(obj));
        assertEquals(obj, m.getObjectHeaders().get(obj));
        assertEquals(String.valueOf(count), m.getMetadataValue(METADATA_KEY_INDEX));
        count++;
      }
      assertEquals(expectedSplitCount, count);
    }
  }

  @Test
  public void testService() throws Exception {
    final int expectedSplitCount = 3;
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(LINE);
    msg.addMetadata(METADATA_KEY, String.valueOf(expectedSplitCount));
    BasicMessageSplitterService service = createBasic(new MetadataDocumentCopier(METADATA_KEY, METADATA_KEY_INDEX));
    MockMessageProducer producer = new MockMessageProducer();
    service.setProducer(producer);
    execute(service, msg);
    assertEquals(expectedSplitCount, producer.getMessages().size());
  }

  @Test
  public void testSplit_IOException() throws Exception {
    final int expectedSplitCount = 10;
    AdaptrisMessage msg = new DefectiveMessageFactory().newMessage(LINE);
    msg.addMetadata(METADATA_KEY, String.valueOf(expectedSplitCount));
    MetadataDocumentCopier splitter = new MetadataDocumentCopier(METADATA_KEY, METADATA_KEY_INDEX);
    try (CloseableIterable<AdaptrisMessage> result = splitter.splitMessage(msg)) {
      for (AdaptrisMessage m : result) {
        fail();
      }
    } catch (RuntimeException expected) {

    }
  }
}
