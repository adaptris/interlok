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

package com.adaptris.core.mail.attachment;

import java.security.MessageDigest;
import java.util.List;

import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.BaseCase;
import com.adaptris.util.KeyValuePair;
import com.adaptris.util.KeyValuePairSet;
import com.adaptris.util.text.mime.MimeConstants;

/**
 * @author lchan
 * @author $Author: lchan $
 */
public class XmlMailCreatorTest extends BaseCase {

  private static final String LF = System.lineSeparator();
  private static final String XML_DOCUMENT = "<?xml version=\"1.0\"?>" + LF +

  "<document>" + LF + "<subject>an email with attachemnts perhaps</subject>" + LF
      + "<content>Quick zephyrs blow, vexing daft Jim</content>" + LF
      + "<!-- This is ADP-01 MD5 Base64 -->" + LF
      + "<attachment encoding=\"base64\" filename=\"attachment1.txt\">dp/HSJfonUsSMM7QRBSRfg==</attachment>" + LF
      + "<!-- This is PENRY MD5 Base64 -->" + LF
      + "<attachment encoding=\"base64\" filename=\"attachment2.txt\">OdjozpCZB9PbCCLZlKregQ==</attachment>" + LF + "</document>";

  /**
   * @param name
   */
  public XmlMailCreatorTest(String name) {
    super(name);
  }

  public void testSetNamespaceContext_XmlBodyHandler() {
    XmlBodyHandler obj = new XmlBodyHandler();
    assertNull(obj.getNamespaceContext());
    KeyValuePairSet kvps = new KeyValuePairSet();
    kvps.add(new KeyValuePair("hello", "world"));
    obj.setNamespaceContext(kvps);
    assertEquals(kvps, obj.getNamespaceContext());
    obj.setNamespaceContext(null);
    assertNull(obj.getNamespaceContext());
  }

  public void testSetNamespaceContext_XmlAttachmentHandler() {
    XmlAttachmentHandler obj = new XmlAttachmentHandler();
    assertNull(obj.getNamespaceContext());
    KeyValuePairSet kvps = new KeyValuePairSet();
    kvps.add(new KeyValuePair("hello", "world"));
    obj.setNamespaceContext(kvps);
    assertEquals(kvps, obj.getNamespaceContext());
    obj.setNamespaceContext(null);
    assertNull(obj.getNamespaceContext());
  }

  public void testBodyHandler() throws Exception {
    XmlMailCreator xmc = new XmlMailCreator();
    xmc.setBodyHandler(new XmlBodyHandler("/document/content", "plain/text"));
    MailContent mc = xmc.createBody(AdaptrisMessageFactory.getDefaultInstance().newMessage(XML_DOCUMENT));
    log.trace(mc);
    assertEquals("plain/text", mc.getContentType());
    assertEquals("Quick zephyrs blow, vexing daft Jim", new String(mc.getBytes()));
  }

  public void testAttachmentHandler() throws Exception {
    XmlMailCreator xmc = new XmlMailCreator();
    xmc.setAttachmentHandler(new XmlAttachmentHandler("/document/attachment", "@filename", "@encoding"));
    List<MailAttachment> attachments = xmc.createAttachments(AdaptrisMessageFactory.getDefaultInstance().newMessage(XML_DOCUMENT));
    assertEquals(2, attachments.size());
    MailAttachment a = attachments.get(0);
    log.trace(a);
    assertTrue("Check digests", MessageDigest.isEqual(calculateHash("ADP-01"), a.getBytes()));
    assertEquals("attachment1.txt", a.getFilename());
    assertEquals(MimeConstants.ENCODING_BASE64, a.getContentTransferEncoding());
    a = attachments.get(1);
    log.trace(a);
    assertTrue("Check digests", MessageDigest.isEqual(calculateHash("PENRY"), a.getBytes()));
    assertEquals("attachment2.txt", a.getFilename());
    assertEquals(MimeConstants.ENCODING_BASE64, a.getContentTransferEncoding());
  }

  public void testAttachmentHandler_WithEncoding() throws Exception {
    XmlMailCreator xmc = new XmlMailCreator();
    xmc.setAttachmentHandler(
        new XmlAttachmentHandler("/document/attachment", "@filename", "@encoding").withAttachmentEncoding(MimeConstants.ENCODING_8BIT));
    List<MailAttachment> attachments = xmc.createAttachments(AdaptrisMessageFactory.getDefaultInstance().newMessage(XML_DOCUMENT));
    assertEquals(2, attachments.size());
    MailAttachment a = attachments.get(0);
    log.trace(a);
    assertTrue("Check digests", MessageDigest.isEqual(calculateHash("ADP-01"), a.getBytes()));
    assertEquals("attachment1.txt", a.getFilename());
    assertEquals(MimeConstants.ENCODING_8BIT, a.getContentTransferEncoding());
    a = attachments.get(1);
    log.trace(a);
    assertTrue("Check digests", MessageDigest.isEqual(calculateHash("PENRY"), a.getBytes()));
    assertEquals("attachment2.txt", a.getFilename());
    assertEquals(MimeConstants.ENCODING_8BIT, a.getContentTransferEncoding());
  }

  private static byte[] calculateHash(String s) throws Exception {
    MessageDigest md = MessageDigest.getInstance("MD5");
    md.update(s.getBytes());
    return md.digest();
  }
}
