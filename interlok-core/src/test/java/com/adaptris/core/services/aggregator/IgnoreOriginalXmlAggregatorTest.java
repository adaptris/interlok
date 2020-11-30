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
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.junit.Test;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.CoreException;
import com.adaptris.core.NullService;
import com.adaptris.core.Service;
import com.adaptris.core.services.aggregator.MessageAggregatorTest.EvenOddCondition;
import com.adaptris.core.services.splitter.SplitJoinService;
import com.adaptris.core.services.splitter.SplitJoinServiceTest;
import com.adaptris.core.services.splitter.SplitterCase;
import com.adaptris.core.services.splitter.XpathMessageSplitter;
import com.adaptris.core.stubs.DefectiveMessageFactory;
import com.adaptris.core.util.XmlHelper;
import com.adaptris.util.TimeInterval;
import com.adaptris.util.text.xml.InsertNode;
import com.adaptris.util.text.xml.XPath;

@SuppressWarnings("deprecation")
public class IgnoreOriginalXmlAggregatorTest extends XmlAggregatorCase {


  @Test
  public void testSplitJoinService_WithExplicitDocumentEnoding() throws Exception {
    // This is a XML doc with 3 iterable elements...
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(SplitterCase.XML_MESSAGE);
    SplitJoinService service = new SplitJoinService();
    // The service doesn't actually matter right now.
    service.setService(asCollection(new NullService()));
    service.setTimeout(new TimeInterval(10L, TimeUnit.SECONDS));
    service.setSplitter(new XpathMessageSplitter(ENVELOPE_DOCUMENT, ENCODING_UTF8));
    IgnoreOriginalXmlDocumentAggregator aggr = new IgnoreOriginalXmlDocumentAggregator("<new/>", new InsertNode("/new"));
    aggr.setDocumentEncoding("UTF-8");
    service.setAggregator(aggr);
    execute(service, msg);

    // Should now be 3 document nodes
    // because we ignore the original
    XPath xpath = new XPath();
    assertEquals(3, xpath.selectNodeList(XmlHelper.createDocument(msg, true), "/new/document").getLength());
    assertEquals("UTF-8", msg.getContentEncoding());
  }

  @Test
  public void testSplitJoinService_WithImplicitDocumentEnoding() throws Exception {
    // This is a XML doc with 3 iterable elements...
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(SplitterCase.XML_MESSAGE, "ISO-8859-1");
    SplitJoinService service = new SplitJoinService();
    // The service doesn't actually matter right now.
    service.setService(asCollection(new NullService()));
    service.setTimeout(new TimeInterval(10L, TimeUnit.SECONDS));
    service.setSplitter(new XpathMessageSplitter(ENVELOPE_DOCUMENT, ENCODING_UTF8));
    service.setAggregator(new IgnoreOriginalXmlDocumentAggregator("<new/>", new InsertNode("/new")));
    execute(service, msg);
    // Should now be 6 document nodes
    XPath xpath = new XPath();
    assertEquals(3, xpath.selectNodeList(XmlHelper.createDocument(msg, true), "/new/document").getLength());
    assertEquals("ISO-8859-1", msg.getContentEncoding());
  }

  @Test
  public void testJoinMessage_NoTemplate() throws Exception {
    XmlDocumentAggregator aggr = new IgnoreOriginalXmlDocumentAggregator();
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

  @Test
  public void testSplitJoinService_withFilter() throws Exception {
    // This is a XML doc with 3 iterable elements...
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(SplitterCase.XML_MESSAGE);
    SplitJoinService service = new SplitJoinService();
    // The service doesn't actually matter right now.
    service.setService(asCollection(new NullService()));
    service.setTimeout(new TimeInterval(10L, TimeUnit.SECONDS));
    service.setSplitter(new XpathMessageSplitter(ENVELOPE_DOCUMENT, ENCODING_UTF8));
    IgnoreOriginalXmlDocumentAggregator aggr = new IgnoreOriginalXmlDocumentAggregator("<new/>", new InsertNode("/new"));
    aggr.setDocumentEncoding("UTF-8");
    aggr.setFilterCondition(new EvenOddCondition());
    service.setAggregator(aggr);
    execute(service, msg);

    // Should now be 3 document nodes
    // because we ignore the original
    XPath xpath = new XPath();
    assertEquals(1, xpath.selectNodeList(XmlHelper.createDocument(msg, true), "/new/document").getLength());
    assertEquals("UTF-8", msg.getContentEncoding());
  }

  @Override
  protected String getExampleCommentHeader(Object o) {
    return super.getExampleCommentHeader(o) + "\n<!-- \n The example document for this split/join process is\n"
        + SplitterCase.XML_MESSAGE + "\n which would create 3 new messages.\n"
        + "Once the services are executed; you would actually end up with 3 document elements as \n"
        + "each of the split messages would be inserted back into a new document with a root element of <new>\n-->\n";
  }

  @Override
  protected List<Service> retrieveObjectsForSampleConfig() {
    return createExamples(new XpathMessageSplitter(ENVELOPE_DOCUMENT, ENCODING_UTF8),
        new IgnoreOriginalXmlDocumentAggregator("<new/>", new InsertNode("/new")));
  }

  @Override
  protected String createBaseFileName(Object object) {
    return object.getClass().getName() + "-IgnoreOriginalXmlDocumentAggregator";
  }

  @Override
  protected IgnoreOriginalXmlDocumentAggregator createAggregatorForTests() {
    return new IgnoreOriginalXmlDocumentAggregator("<envelope/>");
  }

}
