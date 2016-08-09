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

import java.util.concurrent.TimeUnit;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.NullService;
import com.adaptris.core.services.LogMessageService;
import com.adaptris.core.services.splitter.SplitJoinService;
import com.adaptris.core.services.splitter.SplitJoinServiceTest;
import com.adaptris.core.services.splitter.SplitterCase;
import com.adaptris.core.services.splitter.XpathMessageSplitter;
import com.adaptris.core.util.XmlHelper;
import com.adaptris.util.TimeInterval;
import com.adaptris.util.text.xml.InsertNode;
import com.adaptris.util.text.xml.XPath;

@SuppressWarnings("deprecation")
public class XmlAggregatorTest extends XmlAggregatorCase {

  public XmlAggregatorTest(String name) {
    super(name);
  }

  @Override
  protected void setUp() throws Exception {
  }

  @Override
  protected void tearDown() throws Exception {
  }

  public void testSplitJoinService_WithExplicitDocumentEnoding() throws Exception {
    // This is a XML doc with 3 iterable elements...
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(SplitterCase.XML_MESSAGE);
    SplitJoinService service = new SplitJoinService();
    // The service doesn't actually matter right now.
    service.setService(SplitJoinServiceTest.wrap(new NullService()));
    service.setTimeout(new TimeInterval(10L, TimeUnit.SECONDS));
    service.setSplitter(new XpathMessageSplitter(ENVELOPE_DOCUMENT, ENCODING_UTF8));
    XmlDocumentAggregator aggr = new XmlDocumentAggregator(new InsertNode(SplitJoinServiceTest.XPATH_ENVELOPE));
    aggr.setDocumentEncoding("UTF-8");
    service.setAggregator(aggr);
    execute(service, msg);

    // Should now be 6 document nodes
    XPath xpath = new XPath();
    assertEquals(6, xpath.selectNodeList(XmlHelper.createDocument(msg, true), ENVELOPE_DOCUMENT).getLength());
    assertEquals("UTF-8", msg.getContentEncoding());
  }

  public void testSplitJoinService_WithImplicitDocumentEnoding() throws Exception {
    // This is a XML doc with 3 iterable elements...
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(SplitterCase.XML_MESSAGE, "ISO-8859-1");
    SplitJoinService service = new SplitJoinService();
    // The service doesn't actually matter right now.
    service.setService(SplitJoinServiceTest.wrap(new NullService()));
    service.setTimeout(new TimeInterval(10L, TimeUnit.SECONDS));
    service.setSplitter(new XpathMessageSplitter(ENVELOPE_DOCUMENT, ENCODING_UTF8));
    service.setAggregator(new XmlDocumentAggregator(new InsertNode(SplitJoinServiceTest.XPATH_ENVELOPE)));
    execute(service, msg);
    // Should now be 6 document nodes
    XPath xpath = new XPath();
    assertEquals(6, xpath.selectNodeList(XmlHelper.createDocument(msg, true), ENVELOPE_DOCUMENT).getLength());
    assertEquals("ISO-8859-1", msg.getContentEncoding());
  }

  @Override
  protected String getExampleCommentHeader(Object o) {
    return super.getExampleCommentHeader(o) + "\n<!-- \n The example document for this split/join process is\n"
        + SplitterCase.XML_MESSAGE + "\n which would create 3 new messages.\n"
        + "With the services that are executed; you would actually end up with 6 document elements as \n"
        + "each of the split messages would be inserted back into the original document\n-->\n";
  }

  @Override
  protected Object retrieveObjectForSampleConfig() {
    SplitJoinService service = new SplitJoinService();
    service.setService(SplitJoinServiceTest.wrap(new LogMessageService(), new NullService()));
    service.setSplitter(new XpathMessageSplitter(ENVELOPE_DOCUMENT, ENCODING_UTF8));
    service.setAggregator(new XmlDocumentAggregator(new InsertNode(SplitJoinServiceTest.XPATH_ENVELOPE)));
    return service;
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
