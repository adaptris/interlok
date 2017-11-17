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

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import com.adaptris.core.util.DocumentBuilderFactoryBuilder;
import com.adaptris.core.util.XmlHelper;

import net.sf.saxon.xpath.XPathFactoryImpl;

public class XPathTest {
  private static String XML = "<root><test att='1'>one</test><test att='2'>two</test></root>";

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

  @Test
  public void testSelectMultipleTextItems() throws Exception {
    XPath xpath = new XPath();
    Document d = XmlHelper.createDocument(XML, DocumentBuilderFactoryBuilder.newInstance());
    String[] vals = xpath.selectMultipleTextItems(d, "/root/test");
    assertEquals("one", vals[0]);
    assertEquals("two", vals[1]);
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
