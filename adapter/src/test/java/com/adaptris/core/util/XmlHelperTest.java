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

import org.w3c.dom.Document;

import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.BaseCase;
import com.adaptris.util.XmlUtils;

@SuppressWarnings("deprecation")
public class XmlHelperTest extends BaseCase {
  private static final String EXAMPLE_XML =
      "<document>" + System.lineSeparator() + "  <content>text body</content>" + System.lineSeparator()
          + "  <attachment encoding=\"base64\" filename=\"attachment1.txt\">dp/HSJfonUsSMM7QRBSRfg==</attachment>"
          + System.lineSeparator()
          + "  <attachment encoding=\"base64\" filename=\"attachment2.txt\">OdjozpCZB9PbCCLZlKregQ</attachment>"
          + System.lineSeparator() + "</document>";

  public XmlHelperTest(String s) {
    super(s);
  }


  public void testCreateDocument() throws Exception {
    Document d = XmlHelper.createDocument(EXAMPLE_XML);
    assertNotNull(d);
    XmlUtils xu = new XmlUtils();
    xu.setSource(d);
    assertEquals("text body", xu.getSingleTextItem("/document/content"));
  }

  public void testCreateDocumentFromMessage() throws Exception {
    Document d = XmlHelper.createDocument(AdaptrisMessageFactory.getDefaultInstance().newMessage(EXAMPLE_XML));
    assertNotNull(d);
    XmlUtils xu = new XmlUtils();
    xu.setSource(d);
    assertEquals("text body", xu.getSingleTextItem("/document/content"));
  }

  public void testCreateDocumentInvalidXmlMessage() throws Exception {
    try {
      XmlHelper.createDocument(AdaptrisMessageFactory.getDefaultInstance().newMessage("AAAAAAAA"));
      fail("Failed");
    }
    catch (Exception expected) {

    }
  }

  public void testCreateDocumentInvalidXmlString() throws Exception {
    try {
      XmlHelper.createDocument("AAAAAAAA");
      fail("Failed");
    }
    catch (Exception expected) {
    }
  }

}
