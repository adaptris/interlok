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

package com.adaptris.core.util;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.CoreException;
import com.adaptris.util.XmlUtils;
import org.junit.Test;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;

import javax.xml.namespace.NamespaceContext;
import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;

@SuppressWarnings("deprecation")
public class XmlHelperTest extends XmlHelper {
  private static final String EXAMPLE_XML =
      "<document>\n   <content>text body</content>\n"
          + "   <attachment encoding=\"base64\" filename=\"attachment1.txt\">dp/HSJfonUsSMM7QRBSRfg==</attachment>\n"
          + "   <attachment encoding=\"base64\" filename=\"attachment2.txt\">OdjozpCZB9PbCCLZlKregQ</attachment>\n"
          + "</document>";

  private static final String ILLEGAL_XML_CHAR = new String(new byte[]
  {
      (byte) 0x02
  });

  @Test
  public void testCreateDocument() throws Exception {
    Document d = XmlHelper.createDocument(EXAMPLE_XML);
    assertNotNull(d);
    XmlUtils xu = new XmlUtils();
    xu.setSource(d);
    assertEquals("text body", xu.getSingleTextItem("/document/content"));
    assertNotNull(createDocument(EXAMPLE_XML, DocumentBuilderFactoryBuilderTest.createNamespaceContext()));
    assertNotNull(createDocument(EXAMPLE_XML, (NamespaceContext) null));
  }

  @Test
  public void testCreateDocumentFromMessage() throws Exception {
    Document d = XmlHelper.createDocument(AdaptrisMessageFactory.getDefaultInstance().newMessage(EXAMPLE_XML));
    assertNotNull(d);
    XmlUtils xu = new XmlUtils();
    xu.setSource(d);
    assertEquals("text body", xu.getSingleTextItem("/document/content"));
    assertNotNull(createDocument(AdaptrisMessageFactory.getDefaultInstance().newMessage(EXAMPLE_XML),
        DocumentBuilderFactoryBuilderTest.createNamespaceContext()));
    assertNotNull(
        createDocument(AdaptrisMessageFactory.getDefaultInstance().newMessage(EXAMPLE_XML), (NamespaceContext) null));

  }

  @Test(expected = Exception.class)
  public void testCreateDocumentInvalidXmlMessage() throws Exception {
    createDocument(AdaptrisMessageFactory.getDefaultInstance().newMessage("AAAAAAAA"));
  }

  @Test
  public void testCreateDocumentInvalidXmlMessage_NewDocOnFailure() throws Exception {
    Document doc = createDocument(AdaptrisMessageFactory.getDefaultInstance().newMessage("AAAAAAAA"), DocumentBuilderFactoryBuilder.newInstance(), true);
    assertNotNull(doc);
  }
  
  @Test(expected = Exception.class)
  public void testCreateDocumentInvalidXmlString() throws Exception {
    createDocument("AAAAAAAA");
  }
  
  @Test
  public void testCreateDocumentInvalidXmlString_NoDocOnFailure() throws Exception {
    Document doc = createDocument("AAAAAAAA", DocumentBuilderFactoryBuilder.newInstance(), true);
    assertNotNull(doc);
  }
  
  @Test
  public void testCreateXmlUtils() throws Exception {
    assertNotNull(createXmlUtils(AdaptrisMessageFactory.getDefaultInstance().newMessage(EXAMPLE_XML)));
    assertNotNull(createXmlUtils(AdaptrisMessageFactory.getDefaultInstance().newMessage(EXAMPLE_XML),
        DocumentBuilderFactoryBuilderTest.createNamespaceContext()));
    assertNotNull(XmlHelper.createXmlUtils(AdaptrisMessageFactory.getDefaultInstance().newMessage(EXAMPLE_XML),
        DocumentBuilderFactoryBuilderTest.createNamespaceContext(), null));
  }

  @Test(expected = CoreException.class)
  public void testXmlUtilsInvalidXmlString() throws Exception {
    createXmlUtils(AdaptrisMessageFactory.getDefaultInstance().newMessage("AAAAAAAA"));
  }

  @Test(expected = CoreException.class)
  public void testXmlUtilsBrokenInput() throws Exception {
    AdaptrisMessage msg = mock(AdaptrisMessage.class);
    doThrow(new IOException()).when(msg).getInputStream();
    createXmlUtils(msg);
  }

  @Test
  public void testSafeElementName() throws Exception {
    assertEquals("default", safeElementName("", "default"));
    assertEquals("hello", safeElementName("hello", "default"));
    assertEquals("_0hello", safeElementName("0hello", "default"));
    assertEquals("_0hel_lo", safeElementName("_0hel&lo", "default"));
    // We add a _ to the start -> _?hello but ? is still invalid so we replace with another _ -> __hello
    assertEquals("__hello", safeElementName("?hello", "default"));
  }

  @Test
  public void testStripIllegalChars() throws Exception {
    assertEquals("hello", stripIllegalXmlCharacters("hel" + ILLEGAL_XML_CHAR + "lo"));
    assertEquals("hello", stripIllegalXmlCharacters("hello"));
  }

  @Test
  public void testNodeToString() throws Exception {
    Document d = XmlHelper.createDocument(EXAMPLE_XML);
    String s = XmlHelper.nodeToString(d);
    assertEquals(EXAMPLE_XML, s);
  }

  @Test
  public void testNodeToStringException() throws Exception {
    Document d = XmlHelper.createDocument(EXAMPLE_XML);
    Attr a = d.createAttribute("x");
    assertNull(XmlHelperTest.nodeToString(a));
  }
}
