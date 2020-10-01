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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import java.io.ByteArrayInputStream;
import java.util.List;
import javax.xml.namespace.NamespaceContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.CoreException;
import com.adaptris.core.services.metadata.XpathMetadataServiceTest;
import com.adaptris.core.stubs.DefectiveMessageFactory;
import com.adaptris.core.stubs.MessageHelper;
import com.adaptris.core.stubs.MockMessageProducer;
import com.adaptris.core.stubs.StubMessageFactory;
import com.adaptris.core.util.DocumentBuilderFactoryBuilder;
import com.adaptris.core.util.XmlHelper;
import com.adaptris.interlok.util.CloseableIterable;
import com.adaptris.util.KeyValuePair;
import com.adaptris.util.KeyValuePairSet;
import com.adaptris.util.text.xml.SimpleNamespaceContext;
import com.adaptris.util.text.xml.XPath;

public class XpathSplitterTest extends SplitterCase {

  public static final String ENCODING_ISO_8859_1 = "iso-8859-1";
  public static final String ENCODING_UTF8 = "UTF-8";
  public static final String ENVELOPE_DOCUMENT = "/envelope/document";

  private static final String KEY_ISSUE_2658_INPUT = "XpathSplitter.issue2658.input";
  private static final String ISSUE_2658_XPATH = "/root/segment_shipto_line/record_";
  private static final String ISSUE_2658_SRC_XPATH = "/root/segment_shipto_line/record_[1]/Street";
  private static final String ISSUE_2658_DEST_XPATH = "/record_/Street";

  private static Log logR = LogFactory.getLog(XpathSplitterTest.class);
  private MockMessageProducer producer;
  private BasicMessageSplitterService service;



  @Before
  public void setUp() throws Exception {
    producer = new MockMessageProducer();
    service = createBasic(new XpathMessageSplitter(ENVELOPE_DOCUMENT, ENCODING_UTF8));
    service.setProducer(producer);
  }

  @Override
  protected String createBaseFileName(Object object) {
    return super.createBaseFileName(object) + "-XpathSplitter";
  }

  @Override
  protected Object retrieveObjectForSampleConfig() {
    return null; // over-rides retrieveServices below instead
  }

  @Override
  protected List retrieveObjectsForSampleConfig() {
    return createExamples(new XpathMessageSplitter(ENVELOPE_DOCUMENT, ENCODING_UTF8));
  }

  @Override
  protected String getExampleCommentHeader(Object o) {
    return super.getExampleCommentHeader(o) + "\n<!-- \n The example document for this split process is\n"
        + SplitterCase.XML_MESSAGE + "\n which would create 3 new messages\n-->\n";
  }

  @Override
  protected XpathMessageSplitter createSplitterForTests() {
    return new XpathMessageSplitter();
  }

  @Test
  public void testConstructors() throws Exception {
    XpathMessageSplitter splitter = new XpathMessageSplitter(ENVELOPE_DOCUMENT, ENCODING_UTF8);
    assertEquals(ENVELOPE_DOCUMENT, splitter.getXpath());
    assertEquals(ENCODING_UTF8, splitter.getEncoding());
    splitter = new XpathMessageSplitter();
    assertNull(splitter.getXpath());
    assertNull(splitter.getEncoding());
    splitter = new XpathMessageSplitter(ENVELOPE_DOCUMENT);
    assertEquals(ENVELOPE_DOCUMENT, splitter.getXpath());
    assertNull(splitter.getEncoding());

  }

  @Test
  public void testSetters() throws Exception {
    XpathMessageSplitter splitter = new XpathMessageSplitter();
    assertNull(splitter.getXpath());
    assertNull(splitter.getEncoding());
    splitter.setXpath("FRED");
    assertEquals("FRED", splitter.getXpath());
    splitter.setXpath(null);
    assertNull(splitter.getXpath());
    splitter.setXpath("");
    assertEquals("", splitter.getXpath());

    splitter = new XpathMessageSplitter();
    assertNull(splitter.getEncoding());
    splitter.setEncoding("FRED");
    assertEquals("FRED", splitter.getEncoding());
    splitter.setEncoding(null);
    assertNull(splitter.getEncoding());
    splitter.setEncoding("");
    assertEquals("", splitter.getEncoding());
  }

  @Test
  public void testSetNamespaceContext() throws Exception {
    XpathMessageSplitter obj = new XpathMessageSplitter();
    assertNull(obj.getNamespaceContext());
    KeyValuePairSet kvps = new KeyValuePairSet();
    kvps.add(new KeyValuePair("hello", "world"));
    obj.setNamespaceContext(kvps);
    assertEquals(kvps, obj.getNamespaceContext());
    obj.setNamespaceContext(null);
    assertNull(obj.getNamespaceContext());
  }

  @Test
  public void testSplit() throws Exception {
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(XML_MESSAGE);
    String obj = "ABCDEFG";
    msg.addObjectHeader(obj, obj);
    XpathMessageSplitter splitter = new XpathMessageSplitter(ENVELOPE_DOCUMENT);
    int count = 0;
    try (CloseableIterable<AdaptrisMessage> closeable = splitter.splitMessage(msg)) {
      for (AdaptrisMessage m : closeable) {
        assertFalse("No Object Metadata", m.getObjectHeaders().containsKey(obj));
        count++;
      }
    }
    assertEquals("Number of messages", 3, count);

  }

  @Test
  public void testSplit_AlternativeMessageFactory() throws Exception {
    AdaptrisMessage msg = new StubMessageFactory().newMessage(XML_MESSAGE, ENCODING_UTF8);
    String obj = "ABCDEFG";
    msg.addObjectHeader(obj, obj);
    XpathMessageSplitter splitter = new XpathMessageSplitter(ENVELOPE_DOCUMENT);
    int count = 0;
    try (CloseableIterable<AdaptrisMessage> closeable = splitter.splitMessage(msg)) {
      for (AdaptrisMessage m : closeable) {
        assertEquals(StubMessageFactory.class, m.getFactory().getClass());
        count++;
      }
    }
    assertEquals("Number of messages", 3, count);
  }

  @Test
  public void testSplitWithObjectMetadata() throws Exception {
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(XML_MESSAGE);
    String obj = "ABCDEFG";
    msg.addObjectHeader(obj, obj);
    XpathMessageSplitter splitter = new XpathMessageSplitter(ENVELOPE_DOCUMENT, ENCODING_UTF8);
    splitter.setCopyObjectMetadata(true);
    int count = 0;
    try (CloseableIterable<AdaptrisMessage> closeable = splitter.splitMessage(msg)) {
      for (AdaptrisMessage m : closeable) {
        assertTrue("Object Metadata", m.getObjectHeaders().containsKey(obj));
        assertEquals(obj, m.getObjectHeaders().get(obj));
        count++;
      }
    }
    assertEquals("Number of messages", 3, count);
  }

  @Test
  public void testSplitThrowsException() throws Exception {
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(XML_MESSAGE);
    msg.setContent(XML_MESSAGE, msg.getContentEncoding());
    XpathMessageSplitter splitter = new XpathMessageSplitter(ENVELOPE_DOCUMENT, ENCODING_UTF8);
    splitter.setMessageFactory(new DefectiveMessageFactory());
    try (CloseableIterable<AdaptrisMessage> closeable = splitter.splitMessage(msg)) {
      int count = 0;
      for (AdaptrisMessage m : closeable) {
        count++;
      }
    }
    catch (RuntimeException expected) {

    }
  }

  @Test
  public void testIssue2658() throws Exception {
    AdaptrisMessage msg = MessageHelper.createMessage(PROPERTIES.getProperty(KEY_ISSUE_2658_INPUT));
    Document srcXml = createDocument(msg.getPayload());
    XPath srcXpath = new XPath();
    String srcValue = srcXpath.selectSingleTextItem(srcXml, ISSUE_2658_SRC_XPATH);

    XpathMessageSplitter splitter = new XpathMessageSplitter(ISSUE_2658_XPATH, "UTF-8");

    int count = 0;
    try (CloseableIterable<AdaptrisMessage> closeable = splitter.splitMessage(msg)) {
      for (AdaptrisMessage m : closeable) {
        Document destXml = createDocument(m.getPayload());
        XPath destXpath = new XPath();
        String destValue = destXpath.selectSingleTextItem(destXml, ISSUE_2658_DEST_XPATH);
        assertEquals(srcValue, destValue);
        count++;
      }
    }
    assertEquals("Number of messages", 2, count);
  }

  @Test
  public void testDoServiceWithXmlSplitter() throws Exception {
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(XML_MESSAGE);
    msg.addMetadata("key", "value");
    execute(service, msg);
    assertEquals("Number of messages", 3, producer.getMessages().size());
  }

  @Test
  public void testXmlSplitter_Namespace() throws Exception {
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(XpathMetadataServiceTest.XML_WITH_NAMESPACE);
    XpathMessageSplitter splitter = new XpathMessageSplitter("/svrl:schematron-output/svrl:failed-assert", "UTF-8");
    splitter.setNamespaceContext(XpathMetadataServiceTest.createContextEntries());
    NamespaceContext  namespaceCtx = SimpleNamespaceContext.create(XpathMetadataServiceTest.createContextEntries());
    DocumentBuilderFactoryBuilder builder = DocumentBuilderFactoryBuilder.newInstance().withNamespaceAware(namespaceCtx);
    // Should be 2 splits
    int count = 0;
    XPath xpath = XPath.newXPathInstance(builder, namespaceCtx);
    try (CloseableIterable<AdaptrisMessage> closeable = splitter.splitMessage(msg)) {
      for (AdaptrisMessage m : closeable) {
        count++;
        assertNotNull(xpath.selectSingleNode(XmlHelper.createDocument(m, builder), "/svrl:failed-assert"));
      }
    }
    assertEquals("Number of messages", 2, count);
  }

  @Test
  public void testSplit_DocTypeNotAllowed() throws Exception {
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage();
    msg.setContent(XML_WITH_DOCTYPE, msg.getContentEncoding());
    XpathMessageSplitter splitter = new XpathMessageSplitter(ENVELOPE_DOCUMENT, ENCODING_UTF8);
    DocumentBuilderFactoryBuilder builder = new DocumentBuilderFactoryBuilder();
    builder.getFeatures().add(new KeyValuePair("http://apache.org/xml/features/disallow-doctype-decl", "true"));
    splitter.setXmlDocumentFactoryConfig(builder);
    try {
      splitter.splitMessage(msg);
      fail();
    } catch (CoreException expected) {
      assertTrue(expected.getMessage().contains("DOCTYPE is disallowed"));
    }
  }

  private Document createDocument(byte[] bytes) throws Exception {
    DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
    DocumentBuilder builder = domFactory.newDocumentBuilder();
    ByteArrayInputStream in = new ByteArrayInputStream(bytes);
    return builder.parse(new InputSource(in));
  }

}
