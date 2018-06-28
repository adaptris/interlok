/*
 * Copyright 2017 Adaptris Ltd.
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
package com.adaptris.util.text.xml;

import static com.adaptris.util.text.xml.SimpleNamespaceContextTest.createNamespaceEntries;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;

import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import com.adaptris.core.util.DocumentBuilderFactoryBuilder;
import com.adaptris.core.util.XmlHelper;

import net.sf.saxon.xpath.XPathFactoryImpl;

public class XPathTest {
  private final static String XML = "<root><test att='1'>one</test><test att='2'>two</test><node>one<child/>two</node></root>";
  private static final String XML_WITH_EMPTY_NODES = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n" + 
      "<root>\n" + 
      "  <segment_PIX>\n" + 
      "    <segment_Contents>\n" + 
      "      <record_>\n" + 
      "        <PXREF1/>\n" + 
      "      </record_>\n" + 
      "      <record_>\n" + 
      "        <PXREF1/>\n" + 
      "      </record_>\n" + 
      "      <record_>\n" + 
      "        <PXREF1/>\n" + 
      "      </record_>\n" + 
      "      <record_>\n" + 
      "        <PXREF1>91/01</PXREF1>\n" + 
      "      </record_>\n" + 
      "      <record_>\n" + 
      "        <PXREF1>91/01</PXREF1>\n" + 
      "      </record_>\n" + 
      "      <record_>\n" + 
      "        <PXREF1>91/01</PXREF1>\n" + 
      "      </record_>\n" + 
      "    </segment_Contents>\n" + 
      "  </segment_PIX>\n" + 
      "</root>";
  
  private static final String XPATH_EMPTY_NODES = "/root/segment_PIX/segment_Contents/record_/PXREF1";

  @Before
  public void setUp() throws Exception {
  }

  @After
  public void tearDown() throws Exception {
  }

  @Test
  public void testConstructors() {
    assertNotNull(new XPath());
    assertNotNull(new XPath(SimpleNamespaceContext.create(createNamespaceEntries())));
    assertNotNull(new XPath(SimpleNamespaceContext.create(createNamespaceEntries()), XPathFactory.newInstance()));
  }

  @Test
  public void testSelectSingleTextItem() throws Exception {
    XPath xpath = new XPath();
    Document d = XmlHelper.createDocument(XML, DocumentBuilderFactoryBuilder.newInstance());
    assertEquals("one", xpath.selectSingleTextItem(d, "/root/test"));
  }

  @Test
  public void testSelectSingleTextItem_Xpath2_Saxon() throws Exception {
    XPath xpath = new XPath();
    Document d = XmlHelper.createDocument(XML, DocumentBuilderFactoryBuilder.newInstance());
    // local-name isn't XPath 1
    assertEquals("test", xpath.selectSingleTextItem(d, "//*[@att='2'][1]/local-name()"));
  }

  @Test(expected = javax.xml.xpath.XPathExpressionException.class)
  public void testXpath_Xpath2Unsupported() throws Exception {
    // so we expect XPathFactory.newInstance() equivalent to fail as it's not XPATH2.0
    Document d = XmlHelper.createDocument(XML, DocumentBuilderFactoryBuilder.newInstance());
    XPathFactory.newInstance().newXPath().evaluate("//*[@att='2'][1]/local-name()", d, XPathConstants.STRING);
  }

  @Test
  public void testSelectMultipleTextItems() throws Exception {
    XPath xpath = new XPath(SimpleNamespaceContext.create(SimpleNamespaceContextTest.createNamespaceEntries()));
    Document d = XmlHelper.createDocument(XML, DocumentBuilderFactoryBuilder.newInstance());
    String[] vals = xpath.selectMultipleTextItems(d, "/root/test");
    assertEquals(2, vals.length);
    assertEquals("one", vals[0]);
    assertEquals("two", vals[1]);
  }

  @Test
  public void testSelectMultipleTextItems_Attributes() throws Exception {
    XPath xpath = new XPath();
    Document d = XmlHelper.createDocument(XML, DocumentBuilderFactoryBuilder.newInstance());
    String[] vals = xpath.selectMultipleTextItems(d, "/root/test/@att");
    assertEquals(2, vals.length);
    assertEquals("1", vals[0]);
    assertEquals("2", vals[1]);
  }

  @Test
  public void testSelectMultipleTextItems_TextNode() throws Exception {
    XPath xpath = new XPath();
    Document d = XmlHelper.createDocument(XML, DocumentBuilderFactoryBuilder.newInstance());
    String[] vals = xpath.selectMultipleTextItems(d, "/root/node/text()");
    assertEquals(2, vals.length);
    assertEquals("one", vals[0]);
    assertEquals("two", vals[1]);
  }

  @Test
  public void testSelectMultipleTextItems_WithNullValues() throws Exception {
    XPath xpath = new XPath();
    Document d = XmlHelper.createDocument(XML_WITH_EMPTY_NODES, DocumentBuilderFactoryBuilder.newInstance());
    String[] vals = xpath.selectMultipleTextItems(d, XPATH_EMPTY_NODES);
    // first 3 nodes will be "null"
    assertEquals(6, vals.length);
    for (int i = 0; i < 3; i++) {
      assertNull(vals[i]);
    }
    for (int i = 3; i < 6; i++) {
      assertEquals("91/01", vals[i]);
    }
  }

  @Test
  public void testSelectNodeList() throws Exception {
    XPath xpath = new XPath();
    Document d = XmlHelper.createDocument(XML, DocumentBuilderFactoryBuilder.newInstance());
    NodeList nl = xpath.selectNodeList(d, "/root/test");
    assertEquals(2, nl.getLength());
  }

  @Test
  public void testSelectSingleNode() throws Exception {
    XPath xpath = new XPath();
    Document d = XmlHelper.createDocument(XML, DocumentBuilderFactoryBuilder.newInstance());
    assertNotNull(xpath.selectSingleNode(d, "/root/test"));
  }

  @Test
  public void testBuild() {
    assertEquals(XPathFactoryImpl.class, XPath.build(true).getClass());
    assertNotSame(XPathFactoryImpl.class, XPath.build(false).getClass());
  }

}
