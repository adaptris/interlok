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

import org.junit.Before;
import org.junit.Test;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.CoreException;
import com.adaptris.core.Service;
import com.adaptris.core.stubs.MockMessageProducer;
import com.adaptris.core.util.DocumentBuilderFactoryBuilder;
import com.adaptris.util.KeyValuePair;

public class XpathDocumentCopierTest extends SplitterCase {

  static final String XPATH_DOCUMENT_COUNT = "count(/envelope/document)";

  private MockMessageProducer producer;
  private BasicMessageSplitterService service;



  @Before
  public void setUp() throws Exception {
    producer = new MockMessageProducer();
    service = createBasic(new XpathDocumentCopier(XPATH_DOCUMENT_COUNT));
    service.setProducer(producer);
  }

  @Override
  protected String createBaseFileName(Object object) {
    return super.createBaseFileName(object) + "-XpathDocumentCopier";
  }

  @Override
  protected Object retrieveObjectForSampleConfig() {
    return null; // over-rides retrieveServices below instead
  }

  @Override
  protected List<Service> retrieveObjectsForSampleConfig() {
    return createExamples(new XpathDocumentCopier(XPATH_DOCUMENT_COUNT));
  }

  @Override
  protected String getExampleCommentHeader(Object o) {
    return super.getExampleCommentHeader(o) + "\n<!-- \n The example document for this split process is\n"
        + SplitterCase.XML_MESSAGE + "\n which would create 3 new messages each containing the entire XML document\n-->\n";
  }

  @Override
  protected XpathDocumentCopier createSplitterForTests() {
    return new XpathDocumentCopier();
  }

  @Test
  public void testConstructors() throws Exception {
    XpathDocumentCopier splitter = new XpathDocumentCopier(XPATH_DOCUMENT_COUNT);
    assertEquals(XPATH_DOCUMENT_COUNT, splitter.getXpath());
    splitter = new XpathDocumentCopier();
    assertNull(splitter.getXpath());
  }

  @Test
  public void testSetters() throws Exception {
    XpathDocumentCopier splitter = new XpathDocumentCopier();
    assertNull(splitter.getXpath());
    splitter.setXpath("FRED");
    assertEquals("FRED", splitter.getXpath());
    splitter.setXpath(null);
    assertNull(splitter.getXpath());
    splitter.setXpath("");
    assertEquals("", splitter.getXpath());
  }

  @Test
  public void testSplit() throws Exception {
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(XML_MESSAGE);
    String obj = "ABCDEFG";
    msg.addObjectHeader(obj, obj);
    XpathDocumentCopier splitter = new XpathDocumentCopier(XPATH_DOCUMENT_COUNT);
    List<AdaptrisMessage> result = splitToList(splitter, msg);
    assertEquals(3, result.size());
    for (AdaptrisMessage m : result) {
      assertFalse("No Object Metadata", m.getObjectHeaders().containsKey(obj));
    }
  }

  @Test
  public void testSplit_EmptyXPath() throws Exception {
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(XML_MESSAGE);
    String obj = "ABCDEFG";
    msg.addObjectHeader(obj, obj);
    XpathDocumentCopier splitter = new XpathDocumentCopier("XXXX");
    List<AdaptrisMessage> result = splitToList(splitter, msg);
    assertEquals(0, result.size());
  }

  @Test
  public void testSplitWithObjectMetadata() throws Exception {
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(XML_MESSAGE);
    String obj = "ABCDEFG";
    msg.addObjectHeader(obj, obj);
    XpathDocumentCopier splitter = new XpathDocumentCopier(XPATH_DOCUMENT_COUNT);
    splitter.setCopyObjectMetadata(true);
    List<AdaptrisMessage> result = splitToList(splitter, msg);
    assertEquals(3, result.size());
    for (AdaptrisMessage m : result) {
      assertTrue("Object Metadata", m.getObjectHeaders().containsKey(obj));
      assertEquals(obj, m.getObjectHeaders().get(obj));
    }
  }

  @Test
  public void testSplitThrowsException() throws Exception {
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(XML_MESSAGE);
    msg.setContent(XML_MESSAGE, msg.getContentEncoding());
    XpathDocumentCopier splitter = new XpathDocumentCopier("/document/envelope[");
    try {
      splitToList(splitter, msg);
      fail();
    }
    catch (CoreException expected) {

    }
  }

  @Test
  public void testSplit_DocTypeNotAllowed() throws Exception {
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage();
    msg.setContent(XML_WITH_DOCTYPE, msg.getContentEncoding());
    XpathDocumentCopier splitter = new XpathDocumentCopier(XPATH_DOCUMENT_COUNT);
    DocumentBuilderFactoryBuilder builder = new DocumentBuilderFactoryBuilder();
    builder.getFeatures().add(new KeyValuePair("http://apache.org/xml/features/disallow-doctype-decl", "true"));
    splitter.setXmlDocumentFactoryConfig(builder);
    try {
      splitToList(splitter, msg);
      fail();
    } catch (CoreException expected) {
      assertTrue(expected.getMessage().contains("DOCTYPE is disallowed"));
    }
  }

  @Test
  public void testService() throws Exception {
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(XML_MESSAGE);
    msg.addMetadata("key", "value");
    execute(service, msg);
    assertEquals("Number of messages", 3, producer.getMessages().size());
  }

}
