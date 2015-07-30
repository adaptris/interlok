/*
 * XmlUtilsTest.java JUnit based test
 *
 * Created on 19 July 2004, 14:09
 */

package com.adaptris.util;

import java.io.ByteArrayOutputStream;
import java.io.StringReader;
import java.io.StringWriter;

import javax.xml.parsers.DocumentBuilderFactory;

import junit.framework.TestCase;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import com.adaptris.core.util.XmlHelper;
import com.adaptris.util.text.xml.XPath;

/**
 *
 * @author Stuart Ellidge
 */
public class XmlUtilsTest extends TestCase {

  private static Log logR = LogFactory.getLog(XmlUtilsTest.class);

  private static final String CHAR_80 = "this is a string of text which can be demonstrated " + "to be eighty characters long";

  static String unpadded = "<root><test>" + CHAR_80 + CHAR_80 + "</test></root>";

  static String multiLine = "<root><test att='1'>one</test><test att='2'>two</test></root>";

  public XmlUtilsTest(java.lang.String testName) {
    super(testName);
  }

  /**
   * Test of writeDocument method, of class com.adaptris.util.XmlUtils.
   */
  public void testWriteDocumentToWriter() {
    logR.debug("testWriteDocumentToWriter");

    try {
      XmlUtils xu = new XmlUtils();
      xu.setSource(new java.io.StringReader(unpadded));
      Document d = (Document) xu.getSingleNode("/");
      StringWriter sw = new StringWriter();
      xu.writeDocument(d, sw);
      logR.debug("Produced XML: \n" + sw.toString());
      // Let's check that XPAth resolves.
      XPath xpath = new XPath();
      XmlHelper.createDocument(sw.toString());
      String text = xpath.selectSingleTextItem(XmlHelper.createDocument(sw.toString()), "/root/test");
      assertEquals(CHAR_80 + CHAR_80, text);
    }
    catch (Exception e) {
      fail("Failed to writeDocument:" + e.getMessage());
    }
  }

  /**
   * Test of writeDocument method, of class com.adaptris.util.XmlUtils.
   */
  public void testWriteDocumentToOutputStream() {
    logR.debug("testWriteDocumentToOutputStream");

    try {
      XmlUtils xu = new XmlUtils();
      xu.setSource(new java.io.StringReader(unpadded));

      Document d = (Document) xu.getSingleNode("/");
      ByteArrayOutputStream sw = new ByteArrayOutputStream();
      xu.writeDocument(d, sw);
      logR.debug("Produced XML: \n" + sw.toString());
      // Let's check that XPAth resolves.
      XPath xpath = new XPath();
      String text = xpath.selectSingleTextItem(XmlHelper.createDocument(new String(sw.toByteArray())), "/root/test");
      assertEquals(CHAR_80 + CHAR_80, text);

    }
    catch (Exception e) {
      fail("Failed to writeDocument:" + e.getMessage());
    }
  }

  public void testReturnFunction() throws Exception {
    XmlUtils xu = new XmlUtils();
    xu.setSource(new StringReader(unpadded));
    String name = xu.getSingleTextItem("local-name(/*)");
    assertEquals("root", name);
  }

  public void testReturnString() throws Exception {
    XmlUtils xu = new XmlUtils();
    xu.setSource(new StringReader(unpadded));
    String name = xu.getSingleTextItem("/root/test");
    assertEquals("this", name.substring(0, 4));
  }

  public void testBug1223() throws Exception {
    InputSource in = new InputSource(new StringReader(unpadded));
    Document d = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(in);
    XmlUtils xu = new XmlUtils();
    xu.setSource(d);
    String name = xu.getSingleTextItem("/root/test");
    assertEquals("this", name.substring(0, 4));
  }

  public void testReturnStrings() throws Exception {
    XmlUtils xu = new XmlUtils();
    xu.setSource(new StringReader(multiLine));
    String[] name = xu.getMultipleTextItems("/root/test");
    assertEquals("one", name[0]);
    assertEquals("two", name[1]);
  }

  public void testReturnNode() throws Exception {
    XmlUtils xu = new XmlUtils();
    xu.setSource(new StringReader(multiLine));
    Node n = xu.getSingleNode("/root/test");
    assertTrue(n != null);
    assertTrue(n instanceof Node);
  }

  public void testReturnNodeList() throws Exception {
    XmlUtils xu = new XmlUtils();
    xu.setSource(new StringReader(multiLine));
    NodeList n = xu.getNodeList("/root/test");
    assertTrue(n != null);
    assertTrue(n.getLength() == 2);
  }

  public void testReturnAttribute() throws Exception {
    XmlUtils xu = new XmlUtils();
    xu.setSource(new StringReader(multiLine));
    String name = xu.getSingleTextItem("/root/test/@att");
    assertEquals("1", name);
  }

  public void testReturnAttributes() throws Exception {
    XmlUtils xu = new XmlUtils();
    xu.setSource(new StringReader(multiLine));
    String[] name = xu.getMultipleTextItems("/root/test/@att");
    assertEquals("1", name[0]);
    assertEquals("2", name[1]);
  }
}
