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

import static com.adaptris.core.services.splitter.XpathSplitterTest.ENCODING_UTF8;
import static com.adaptris.core.services.splitter.XpathSplitterTest.ENVELOPE_DOCUMENT;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.NullService;
import com.adaptris.core.Service;
import com.adaptris.core.services.aggregator.MessageAggregatorTest.EvenOddCondition;
import com.adaptris.core.services.splitter.PooledSplitJoinService;
import com.adaptris.core.services.splitter.SplitterCase;
import com.adaptris.core.services.splitter.XpathMessageSplitter;
import com.adaptris.core.util.DocumentBuilderFactoryBuilder;
import com.adaptris.core.util.XmlHelper;
import com.adaptris.util.TimeInterval;
import com.adaptris.util.text.xml.InsertNode;
import com.adaptris.util.text.xml.XPath;

@SuppressWarnings("deprecation")
public class XmlAggregatorTest extends XmlAggregatorCase {

  public static final String XPATH_ENVELOPE = "/envelope";

  @Test
  public void testSplitJoinService_WithExplicitDocumentEnoding() throws Exception {
    // This is a XML doc with 3 iterable elements...
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(SplitterCase.XML_MESSAGE);
    PooledSplitJoinService service = new PooledSplitJoinService();
    // The service doesn't actually matter right now.
    service.setService(asCollection(new NullService()));
    service.setTimeout(new TimeInterval(10L, TimeUnit.SECONDS));
    service.setSplitter(new XpathMessageSplitter(ENVELOPE_DOCUMENT, ENCODING_UTF8));
    XmlDocumentAggregator aggr = new XmlDocumentAggregator(new InsertNode(XPATH_ENVELOPE));
    aggr.setDocumentEncoding("UTF-8");
    service.setAggregator(aggr);
    execute(service, msg);

    // Should now be 6 document nodes
    XPath xpath = new XPath();
    assertEquals(6, xpath.selectNodeList(XmlHelper.createDocument(msg, true), ENVELOPE_DOCUMENT).getLength());
    assertEquals("UTF-8", msg.getContentEncoding());
  }

  @Test
  public void testSplitJoinService_WithImplicitDocumentEnoding() throws Exception {
    // This is a XML doc with 3 iterable elements...
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(SplitterCase.XML_MESSAGE, "ISO-8859-1");
    PooledSplitJoinService service = new PooledSplitJoinService();
    // The service doesn't actually matter right now.
    service.setService(asCollection(new NullService()));
    service.setTimeout(new TimeInterval(10L, TimeUnit.SECONDS));
    service.setSplitter(new XpathMessageSplitter(ENVELOPE_DOCUMENT, ENCODING_UTF8));
    service.setAggregator(new XmlDocumentAggregator(new InsertNode(XPATH_ENVELOPE)));
    execute(service, msg);
    // Should now be 6 document nodes
    XPath xpath = new XPath();
    assertEquals(6, xpath.selectNodeList(XmlHelper.createDocument(msg, true), ENVELOPE_DOCUMENT).getLength());
    assertEquals("ISO-8859-1", msg.getContentEncoding());
  }

  @Test
  public void testAggregate_WithFilter() throws Exception {
    XmlDocumentAggregator aggr = createAggregatorForTests();
    aggr.setMergeImplementation(new InsertNode(XPATH_ENVELOPE));
    aggr.setFilterCondition(new EvenOddCondition());
    AdaptrisMessageFactory fac = AdaptrisMessageFactory.getDefaultInstance();
    AdaptrisMessage original = fac.newMessage("<envelope/>");
    AdaptrisMessage splitMsg1 = fac.newMessage("<document>hello</document>");
    AdaptrisMessage splitMsg2 = fac.newMessage("<document>world</document>");
    aggr.aggregate(original, Arrays.asList(new AdaptrisMessage[] {splitMsg1, splitMsg2}));
    XPath xpath = new XPath();
    Document d = XmlHelper.createDocument(original, DocumentBuilderFactoryBuilder.newInstance());
    assertEquals(1, xpath.selectNodeList(d, ENVELOPE_DOCUMENT).getLength());
  }

  @Override
  protected String getExampleCommentHeader(Object o) {
    return super.getExampleCommentHeader(o) + "\n<!-- \n The example document for this split/join process is\n"
        + SplitterCase.XML_MESSAGE + "\n which would create 3 new messages.\n"
        + "With the services that are executed; you would actually end up with 6 document elements as \n"
        + "each of the split messages would be inserted back into the original document\n-->\n";
  }

  @Override
  protected List<Service> retrieveObjectsForSampleConfig() {
    return createExamples(new XpathMessageSplitter(ENVELOPE_DOCUMENT, ENCODING_UTF8),
        new XmlDocumentAggregator(new InsertNode(XPATH_ENVELOPE)));
  }

  @Override
  protected String createBaseFileName(Object object) {
    return super.createBaseFileName(object) + "-XmlDocumentAggregator";
  }

  @Override
  protected XmlDocumentAggregator createAggregatorForTests() {
    return new XmlDocumentAggregator();
  }
}
