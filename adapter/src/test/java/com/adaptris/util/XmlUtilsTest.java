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

package com.adaptris.util;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;

import javax.xml.namespace.NamespaceContext;
import javax.xml.parsers.DocumentBuilderFactory;

import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.BaseCase;
import com.adaptris.core.stubs.MessageHelper;
import com.adaptris.core.transform.XmlValidationServiceTest;
import com.adaptris.core.util.XmlHelper;
import com.adaptris.util.text.xml.Resolver;
import com.adaptris.util.text.xml.SimpleNamespaceContext;
import com.adaptris.util.text.xml.XPath;

/**
 *
 * @author Stuart Ellidge
 */
@SuppressWarnings("deprecation")
public class XmlUtilsTest extends BaseCase {

  private static final String CHAR_80 = "this is a string of text which can be demonstrated " + "to be eighty characters long";

  static String unpadded = "<root><test>" + CHAR_80 + CHAR_80 + "</test></root>";

  static String multiLine = "<root><test att='1'>one</test><test att='2'>two</test></root>";

  @Test
  public void testConstructors() {
    new XmlUtils();
    new XmlUtils(createNamespaceContext());
    new XmlUtils(createNamespaceContext(), DocumentBuilderFactory.newInstance());
    new XmlUtils(new Resolver(), new Resolver(), createNamespaceContext(), null);
    new XmlUtils(new Resolver(), new Resolver(), null, null);
    new XmlUtils(new Resolver(), new Resolver());
    new XmlUtils(new Resolver(), new Resolver(), createNamespaceContext());
  }

  @Test
  public void testSchemaWithSource() throws Exception {
    XmlUtils xu = new XmlUtils();
    xu.setSchema(PROPERTIES.getProperty(XmlValidationServiceTest.KEY_WILL_VALIDATE_SCHEMA));
    AdaptrisMessage msg = MessageHelper.createMessage(PROPERTIES.getProperty(XmlValidationServiceTest.KEY_INPUT_FILE));
    try (InputStream in = msg.getInputStream()) {
      xu.setSource(in);
      assertTrue(xu.isDocumentValid());
      assertNotNull(xu.getCurrentDoc());
      assertEquals("", xu.getParseMessage());
    }
    xu.reset();
    xu.setSchema(PROPERTIES.getProperty(XmlValidationServiceTest.KEY_WILL_VALIDATE_SCHEMA));
    try (Reader in = msg.getReader()) {
      xu.setSource(in);
      assertTrue(xu.isDocumentValid());
      assertEquals("", xu.getParseMessage());
      assertNotNull(xu.getCurrentDoc());
    }
  }

  @Test
  public void testSchemaWithInvalidSource() throws Exception {
    XmlUtils xu = new XmlUtils();
    xu.setSchema(PROPERTIES.getProperty(XmlValidationServiceTest.KEY_WILL_NOT_VALIDATE));
    AdaptrisMessage msg = MessageHelper.createMessage(PROPERTIES.getProperty(XmlValidationServiceTest.KEY_INPUT_FILE));
    try (InputStream in = msg.getInputStream()) {
      xu.setSource(in);
      assertFalse(xu.isDocumentValid());
      assertNotSame("", xu.getParseMessage());
      assertNull(xu.getCurrentDoc());
    }
  }

  @Test
  public void testWrite() throws Exception {
    XmlUtils xu = new XmlUtils();
    xu.setSchema(PROPERTIES.getProperty(XmlValidationServiceTest.KEY_WILL_VALIDATE_SCHEMA));
    AdaptrisMessage msg = MessageHelper.createMessage(PROPERTIES.getProperty(XmlValidationServiceTest.KEY_INPUT_FILE));
    try (InputStream in = msg.getInputStream(); OutputStream out = msg.getOutputStream()) {
      xu.setSource(in);
      xu.writeDocument(out);
    }
    xu.reset();
    xu.setSchema(PROPERTIES.getProperty(XmlValidationServiceTest.KEY_WILL_VALIDATE_SCHEMA));
    try (Reader in = msg.getReader(); Writer out = msg.getWriter("ISO-8859-1")) {
      xu.setSource(in);
      xu.writeDocument(out);
    }
  }

  @Test
  public void testWriteDocumentToWriter() throws Exception {
    XmlUtils xu = new XmlUtils(null, null, DocumentBuilderFactory.newInstance());
    xu.setSource(new java.io.StringReader(unpadded));
    Document d = (Document) xu.getSingleNode("/");
    StringWriter sw = new StringWriter();
    xu.writeDocument(d, sw);
    // Let's check that XPAth resolves.
    XPath xpath = new XPath();
    XmlHelper.createDocument(sw.toString());
    String text = xpath.selectSingleTextItem(XmlHelper.createDocument(sw.toString()), "/root/test");
    assertEquals(CHAR_80 + CHAR_80, text);
  }

  @Test
  public void testWriteDocumentToOutputStream() throws Exception {
    XmlUtils xu = new XmlUtils();
    xu.setSource(new java.io.StringReader(unpadded));

    Document d = (Document) xu.getSingleNode("/");
    ByteArrayOutputStream sw = new ByteArrayOutputStream();
    xu.writeDocument(d, sw);
    // Let's check that XPAth resolves.
    XPath xpath = new XPath();
    String text = xpath.selectSingleTextItem(XmlHelper.createDocument(new String(sw.toByteArray())), "/root/test");
    assertEquals(CHAR_80 + CHAR_80, text);

  }

  @Test
  public void testReturnFunction() throws Exception {
    XmlUtils xu = new XmlUtils();
    xu.setSource(new StringReader(unpadded));
    String name = xu.getSingleTextItem("local-name(/*)");
    assertEquals("root", name);
  }

  @Test
  public void testGetSingleTextItem() throws Exception {
    XmlUtils xu = new XmlUtils();
    xu.setSource(new StringReader(unpadded));
    String name = xu.getSingleTextItem("/root/test");
    assertEquals("this", name.substring(0, 4));
    assertNull(xu.getSingleTextItem("/root/test", null));
  }

  @Test
  public void testBug1223() throws Exception {
    InputSource in = new InputSource(new StringReader(unpadded));
    Document d = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(in);
    XmlUtils xu = new XmlUtils();
    xu.setSource(d);
    String name = xu.getSingleTextItem("/root/test");
    assertEquals("this", name.substring(0, 4));
  }

  @Test
  public void testGetMultipleTextItems() throws Exception {
    XmlUtils xu = new XmlUtils();
    xu.setSource(new StringReader(multiLine));
    String[] name = xu.getMultipleTextItems("/root/test");
    assertEquals("one", name[0]);
    assertEquals("two", name[1]);
    assertNull(xu.getMultipleTextItems("/root/test", null));
  }

  @Test
  public void testGetSingleNode() throws Exception {
    XmlUtils xu = new XmlUtils();
    xu.setSource(new StringReader(multiLine));
    Node n = xu.getSingleNode("/root/test");
    assertNotNull(n);
    assertNull(xu.getSingleNode("/root/test", null));
  }

  @Test
  public void testGetNodeList() throws Exception {
    XmlUtils xu = new XmlUtils();
    xu.setSource(new StringReader(multiLine));
    NodeList n = xu.getNodeList("/root/test");
    assertNotNull(n);
    assertTrue(n.getLength() == 2);
    assertNull(xu.getNodeList("/root/test", null));
  }

  @Test
  public void testGetSingleTextItem_Attribute() throws Exception {
    XmlUtils xu = new XmlUtils();
    xu.setSource(new StringReader(multiLine));
    String name = xu.getSingleTextItem("/root/test/@att");
    assertEquals("1", name);
  }

  @Test
  public void testGetMultipleTextItem_Attributes() throws Exception {
    XmlUtils xu = new XmlUtils();
    xu.setSource(new StringReader(multiLine));
    String[] name = xu.getMultipleTextItems("/root/test/@att");
    assertEquals("1", name[0]);
    assertEquals("2", name[1]);
  }

  @Test
  public void testSetSourceNode() throws Exception {
    XmlUtils xu = new XmlUtils();
    xu.setSource(new StringReader(unpadded));
    Document altDoc = xu.getCurrentDoc();
    xu.reset();
    xu.setSource(altDoc);
    assertTrue(xu.isDocumentValid());
    assertEquals("Using supplied Document node - not validated", xu.getParseMessage());
    xu.reset();
    xu.setSource(altDoc.getDocumentElement());
    assertFalse(xu.isDocumentValid());
    assertEquals("Supplied Node not of type org.w3c.dom.Document", xu.getParseMessage());
  }

  @Test
  public void testNodeValue() throws Exception {
    XmlUtils xu = new XmlUtils();
    xu.setSource(new StringReader(unpadded));
    Document altDoc = xu.getCurrentDoc();
    assertNotNull(xu.createNode("/root/test/node"));
    assertNotNull(xu.createNode("/root/node/node"));
    xu.setNodeValue("/root/node/other", "value");
    assertEquals("value", xu.getSingleTextItem("/root/node/other"));
    xu.setNodeValue("/root/node/other", "differentValue");
    assertEquals("differentValue", xu.getSingleTextItem("/root/node/other"));
  }

  @Test
  public void testAttributes() throws Exception {
    XmlUtils xu = new XmlUtils();
    xu.setSource(new StringReader(unpadded));
    Document altDoc = xu.getCurrentDoc();
    Node n = xu.createNode("/root/test/node");
    xu.setAttribute("/root/test/node", "attr", "value");
    assertEquals("value", xu.getSingleTextItem("/root/test/node/@attr"));
    xu.deleteAttribute("attr", n);
    try {
      xu.deleteAttribute("attr", altDoc);
      fail();
    }
    catch (Exception expected) {

    }
  }

  @Test
  public void testInsertNodeBefore() throws Exception {
    XmlUtils xu = new XmlUtils();
    xu.setSource(new StringReader(unpadded));
    Node newNodeDiffOwner = xu.createNode("/root/test/node1");
    xu.reset();
    xu.setSource(new StringReader(unpadded));
    Node existingNode = xu.createNode("/root/test/node2");
    Node newNode = xu.createNode("/root/test/node3");
    Node parent = xu.getSingleNode("/root/test");
    xu.insertNodeBefore(newNode, existingNode, parent);
    xu.insertNodeBefore(newNodeDiffOwner, existingNode, parent);

  }

  @Test
  public void testAppendNode() throws Exception {
    XmlUtils xu = new XmlUtils();
    xu.setSource(new StringReader(unpadded));
    Node newNodeDiffOwner = xu.createNode("/root/test/node1");
    xu.reset();
    xu.setSource(new StringReader(unpadded));
    Node existingNode = xu.createNode("/root/test/node2");
    Node newNode = xu.createNode("/root/test/node3");
    Node parent = xu.getSingleNode("/root/test");
    xu.appendNode(newNode, parent);
    xu.appendNode(newNodeDiffOwner, parent);
  }

  @Test
  public void testRemoveNode() throws Exception {
    XmlUtils xu = new XmlUtils();
    xu.setSource(new StringReader(unpadded));
    Node root = xu.getSingleNode("/root");
    Node test = xu.getSingleNode("/root/test");
    xu.removeNode(test, root);
  }

  public static NamespaceContext createNamespaceContext() {
    KeyValuePairSet result = new KeyValuePairSet();
    result.add(new KeyValuePair("xsd", "http://www.w3.org/2001/XMLSchema"));
    result.add(new KeyValuePair("xs", "http://www.w3.org/2001/XMLSchema"));
    return SimpleNamespaceContext.create(result);
  }
}
