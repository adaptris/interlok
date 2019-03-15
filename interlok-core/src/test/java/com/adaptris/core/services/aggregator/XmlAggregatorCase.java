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

package com.adaptris.core.services.aggregator;

import java.util.Arrays;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.CoreException;
import com.adaptris.core.services.splitter.SplitJoinServiceTest;
import com.adaptris.core.stubs.DefectiveMessageFactory;
import com.adaptris.util.text.xml.InsertNode;

public abstract class XmlAggregatorCase extends AggregatorCase {

  public XmlAggregatorCase(String name) {
    super(name);
  }

  @Override
  protected void setUp() throws Exception {
  }

  @Override
  protected void tearDown() throws Exception {
  }

  public void testSetDocumentEncoding() throws Exception {
    XmlDocumentAggregator aggr = createAggregatorForTests();
    assertNull(aggr.getDocumentEncoding());
    aggr.setDocumentEncoding("ISO-8859-1");
    assertEquals("ISO-8859-1", aggr.getDocumentEncoding());
  }

  public void testSetDocumentMerge() throws Exception {
    XmlDocumentAggregator aggr = createAggregatorForTests();
    assertNull(aggr.getMergeImplementation());
    InsertNode inserter = new InsertNode(SplitJoinServiceTest.XPATH_ENVELOPE);
    aggr.setMergeImplementation(inserter);
    assertEquals(inserter, aggr.getMergeImplementation());
    try {
      aggr.setMergeImplementation(null);
      fail();
    }
    catch (IllegalArgumentException expected) {

    }
    assertEquals(inserter, aggr.getMergeImplementation());
  }

  public void testJoinMessage_NoOverwriteMetadata() throws Exception {
    XmlDocumentAggregator aggr = createAggregatorForTests();
    aggr.setMergeImplementation(new InsertNode(SplitJoinServiceTest.XPATH_ENVELOPE));
    aggr.setOverwriteMetadata(false);
    AdaptrisMessage original = AdaptrisMessageFactory.getDefaultInstance().newMessage("<envelope/>");
    original.addMetadata("originalKey", "originalValue");
    AdaptrisMessage splitMsg1 = AdaptrisMessageFactory.getDefaultInstance().newMessage("<document>hello</document>");
    AdaptrisMessage splitMsg2 = AdaptrisMessageFactory.getDefaultInstance().newMessage("<document>world</document>");
    splitMsg2.addMetadata("originalKey", "newValue");
    aggr.joinMessage(original, Arrays.asList(new AdaptrisMessage[]
    {
        splitMsg1, splitMsg2
    }));
    assertEquals("originalValue", original.getMetadataValue("originalKey"));
  }

  public void testJoinMessage_OverwriteMetadata() throws Exception {
    XmlDocumentAggregator aggr = createAggregatorForTests();
    aggr.setMergeImplementation(new InsertNode(SplitJoinServiceTest.XPATH_ENVELOPE));
    aggr.setOverwriteMetadata(true);
    AdaptrisMessage original = AdaptrisMessageFactory.getDefaultInstance().newMessage("<envelope/>");
    original.addMetadata("originalKey", "originalValue");
    AdaptrisMessage splitMsg1 = AdaptrisMessageFactory.getDefaultInstance().newMessage("<document>hello</document>");
    AdaptrisMessage splitMsg2 = AdaptrisMessageFactory.getDefaultInstance().newMessage("<document>world</document>");
    splitMsg2.addMetadata("originalKey", "newValue");
    aggr.joinMessage(original, Arrays.asList(new AdaptrisMessage[]
    {
        splitMsg1, splitMsg2
    }));
    assertEquals("newValue", original.getMetadataValue("originalKey"));
  }

  public void testJoinMessage_Fails() throws Exception {
    XmlDocumentAggregator aggr = createAggregatorForTests();
    aggr.setMergeImplementation(new InsertNode(SplitJoinServiceTest.XPATH_ENVELOPE));
    AdaptrisMessage original = AdaptrisMessageFactory.getDefaultInstance().newMessage("<envelope/>");
    AdaptrisMessage splitMsg1 = AdaptrisMessageFactory.getDefaultInstance().newMessage("<document>hello</document>");
    AdaptrisMessage splitMsg2 = new DefectiveMessageFactory().newMessage("<document>world</document>");
    try {
      aggr.joinMessage(original, Arrays.asList(new AdaptrisMessage[]
      {
          splitMsg1, splitMsg2
      }));
      fail();
    }
    catch (CoreException expected) {

    }
  }

  @Override
  protected XmlDocumentAggregator createAggregatorForTests() {
    return new XmlDocumentAggregator();
  }
}
