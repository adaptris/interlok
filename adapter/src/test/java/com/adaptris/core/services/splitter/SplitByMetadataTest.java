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

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.stubs.MockMessageProducer;

public class SplitByMetadataTest extends SplitterCase {

  private static final String A_B_C_D = "a,b,c,d";
  private static final String SPLIT_ON_METADATA_KEY = "metadataKey";
  private static final String SPLIT_METADATA_KEY = "key";
  private static Log logR = LogFactory.getLog(SplitByMetadataTest.class);

  public SplitByMetadataTest(java.lang.String testName) {
    super(testName);
  }

  @Override
  protected void setUp() throws Exception {
  }

  @Override
  protected void tearDown() throws Exception {
  }

  @Override
  protected SplitByMetadata createSplitterForTests() {
    return new SplitByMetadata();
  }

  public void testSplit() throws Exception {
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(XML_MESSAGE);
    Object obj = "ABCDEFG";
    msg.getObjectHeaders().put(obj, obj);
    SplitByMetadata splitter = new SplitByMetadata(SPLIT_ON_METADATA_KEY, SPLIT_METADATA_KEY);
    msg.addMetadata(SPLIT_ON_METADATA_KEY, A_B_C_D);
    List<AdaptrisMessage> result = splitter.splitMessage(msg);
    assertEquals("Number of messages", 4, result.size());
    for (AdaptrisMessage m : result) {
      assertFalse("No Object Metadata", m.getObjectHeaders().containsKey(obj));
      doStandardAssertions(m);
    }
  }

  public void testSplitWithObjectMetadata() throws Exception {
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(XML_MESSAGE);
    Object obj = "ABCDEFG";
    msg.getObjectHeaders().put(obj, obj);
    SplitByMetadata splitter = new SplitByMetadata(SPLIT_ON_METADATA_KEY, SPLIT_METADATA_KEY);
    splitter.setCopyObjectMetadata(true);
    msg.addMetadata(SPLIT_ON_METADATA_KEY, A_B_C_D);
    List<AdaptrisMessage> result = splitter.splitMessage(msg);
    assertEquals("Number of messages", 4, result.size());
    for (AdaptrisMessage m : result) {
      assertTrue("Object Metadata", m.getObjectHeaders().containsKey(obj));
      doStandardAssertions(m);
    }
  }

  public void testDoServiceWithSplitByMetadata() throws Exception {
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(XML_MESSAGE);
    MockMessageProducer producer = new MockMessageProducer();
    BasicMessageSplitterService service = createBasic(new SplitByMetadata(SPLIT_ON_METADATA_KEY, SPLIT_METADATA_KEY));
    service.setProducer(producer);
    msg.addMetadata(SPLIT_ON_METADATA_KEY, A_B_C_D);
    execute(service, msg);
    assertEquals("Number of messages", 4, producer.getMessages().size());
    for (AdaptrisMessage m : producer.getMessages()) {
      doStandardAssertions(m);
    }
  }

  public void testSplitWithNoMetadata() throws Exception {
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(XML_MESSAGE);
    MockMessageProducer producer = new MockMessageProducer();
    BasicMessageSplitterService service = createBasic(new SplitByMetadata(SPLIT_ON_METADATA_KEY, SPLIT_METADATA_KEY));
    service.setProducer(producer);
    execute(service, msg);
    assertEquals("Number of messages", 1, producer.getMessages().size());
    for (AdaptrisMessage m : producer.getMessages()) {
      assertFalse(m.containsKey(SPLIT_METADATA_KEY));
      assertEquals(XML_MESSAGE, m.getStringPayload());
    }
  }

  private void doStandardAssertions(AdaptrisMessage msg) {
    assertTrue(msg.containsKey(SPLIT_METADATA_KEY));
    String s = msg.getMetadataValue(SPLIT_METADATA_KEY);
    assertTrue(s.matches("[abcd]{1}"));
    assertEquals(A_B_C_D, msg.getMetadataValue(SPLIT_ON_METADATA_KEY));
    assertEquals(XML_MESSAGE, msg.getStringPayload());
  }

  @Override
  protected String createBaseFileName(Object object) {
    return super.createBaseFileName(object) + "-SplitByMetadata";
  }

  @Override
  protected Object retrieveObjectForSampleConfig() {
    return null; // over-rides retrieveServices below instead
  }

  @Override
  protected List retrieveObjectsForSampleConfig() {
    return createExamples(new SplitByMetadata(SPLIT_ON_METADATA_KEY, "splitMetadataKey"));
  }

}
