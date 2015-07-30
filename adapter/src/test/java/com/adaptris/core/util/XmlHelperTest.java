package com.adaptris.core.util;

import org.w3c.dom.Document;

import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.BaseCase;
import com.adaptris.util.XmlUtils;

@SuppressWarnings("deprecation")
public class XmlHelperTest extends BaseCase {
  private static final String EXAMPLE_XML = "<document>" + System.getProperty("line.separator")
 + "  <content>text body</content>"
      + System.getProperty("line.separator")
      + "  <attachment encoding=\"base64\" filename=\"attachment1.txt\">dp/HSJfonUsSMM7QRBSRfg==</attachment>"
      + System.getProperty("line.separator")
      + "  <attachment encoding=\"base64\" filename=\"attachment2.txt\">OdjozpCZB9PbCCLZlKregQ</attachment>"
      + System.getProperty("line.separator") + "</document>";

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
