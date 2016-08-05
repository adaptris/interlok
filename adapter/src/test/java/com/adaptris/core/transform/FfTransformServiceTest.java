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

package com.adaptris.core.transform;

import java.io.ByteArrayInputStream;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXParseException;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreConstants;
import com.adaptris.core.DefaultMessageFactory;
import com.adaptris.core.ServiceException;
import com.adaptris.core.lms.FileBackedMessageFactory;
import com.adaptris.core.util.XmlHelper;
import com.adaptris.util.text.xml.XPath;

public class FfTransformServiceTest extends TransformServiceExample {

  private static final String ISO_8859_1 = "ISO-8859-1";
  private static final String UTF_8 = "UTF-8";
  static final String KEY_FF_TEST_DEFINITION = "FfTransformService.stylesheet";
  static final String KEY_FF_TEST_INPUT = "FfTransformService.inputFile";
  static final String KEY_FF_TEST_INPUT_ISO = "FfTransformService.inputFile_ISO_8859";

  // This is the FF input; and is replicated in what's stored in KEY_FF_INPUT
  private static final String TEST_INPUT_HDR_LINE = "HDRSRC20110601THE TITLE OF DOCSrcDescDestinationDesc  1234567890  ";
  private static final String TEST_INPUT_TRAILER_ = "TRL00000005                                                       ";
  private static final String LINE_SEPARATOR = System.lineSeparator();

  // These detail fields are never used, but included for completeness.
  private static final String TEST_INPUT_DETAIL_1 = "DETField   1.01Field  1.02Field     1.0320110630Field   1.04400821";
  private static final String TEST_INPUT_DETAIL_2 = "DETField   2.01Field  2.02Field     2.0320110630Field   2.04400821";
  private static final String TEST_INPUT_DETAIL_3 = "DETField   2.01Field  2.02Field     2.0320110630Field   2.04400821";

  private static final String KEY_ISSUE_2661_INPUT = "FfTransformService.issue2661.input";
  private static final String KEY_ISSUE_2661_DEFINITION = "FfTransformService.issue2661.definition";
  private static final String ISSUE_2661_XPATH = "/root/segment_shipto_line/record_[1]/Street";

  // Get's HDR from the TEST_INPUT_HDR_LINE
  private static final String XPATH_HDR_RECORD_TYPE = "/root/segment_Document/segment_Header/record_HDR/RecordType";
  // Get's TRL from the TEST_INPUT_TRAILER_
  private static final String XPATH_TRL_RECORD_TYPE = "/root/segment_Document/segment_Trailer/record_TRL/RecordType";
  private static final String XPATH_HDR_SOURCE_DESCRIPTION = "/root/segment_Document/segment_Header/record_HDR/SourceDescription";

  public FfTransformServiceTest(String name) {
    super(name);
  }

  @Override
  public void setUp() throws Exception {
  }

  public void testDoService() throws Exception {
    AdaptrisMessage m = TransformHelper.createMessage(PROPERTIES.getProperty(KEY_FF_TEST_INPUT));
    FfTransformService service = createService();
    execute(service, m);
    assertXml(m);
  }

  public void testDoServiceWithCache() throws Exception {
    PROPERTIES.getProperty(KEY_FF_TEST_INPUT);
    AdaptrisMessage m1 = TransformHelper.createMessage(PROPERTIES.getProperty(KEY_FF_TEST_INPUT));
    AdaptrisMessage m2 = TransformHelper.createMessage(PROPERTIES.getProperty(KEY_FF_TEST_INPUT));
    FfTransformService service = createService();
    try {
      service.setCacheTransforms(true);
      start(service);
      service.doService(m1);
      service.doService(m2);
      assertEquals(m1.getContent(), m2.getContent());
      assertXml(m1);
      assertXml(m2);
    }
    finally {
      stop(service);
    }
  }

  public void testDoServiceWithCacheDisabled() throws Exception {
    PROPERTIES.getProperty(KEY_FF_TEST_INPUT);
    AdaptrisMessage m1 = TransformHelper.createMessage(PROPERTIES.getProperty(KEY_FF_TEST_INPUT));
    AdaptrisMessage m2 = TransformHelper.createMessage(PROPERTIES.getProperty(KEY_FF_TEST_INPUT));
    FfTransformService service = createService();
    try {
      service.setCacheTransforms(false);
      start(service);
      service.doService(m1);
      service.doService(m2);
      assertEquals(m1.getContent(), m2.getContent());
      assertXml(m1);
    }
    finally {
      stop(service);
    }
  }

  public void testDoServiceFileBackedMessage() throws Exception {
    AdaptrisMessage m = TransformHelper.createMessage(new FileBackedMessageFactory(), PROPERTIES.getProperty(KEY_FF_TEST_INPUT));
    FfTransformService service = createService();
    execute(service, m);
    assertXml(m);
  }

  public void testDoServiceWithOverrideNotAllowed() throws Exception {
    FfTransformService service = new FfTransformService();
    AdaptrisMessage msg = TransformHelper.createMessage(PROPERTIES.getProperty(KEY_FF_TEST_INPUT));
    service.setAllowOverride(false);
    service.setCacheTransforms(true);
    try {
      execute(service, msg);
    }
    catch (ServiceException expected) {
      ;
    }
  }

  public void testDoServiceWithOverrideAllowed() throws Exception {
    FfTransformService service = new FfTransformService();
    AdaptrisMessage msg = TransformHelper.createMessage(PROPERTIES.getProperty(KEY_FF_TEST_INPUT));
    service.setAllowOverride(true);
    service.setCacheTransforms(true);
    msg.addMetadata(CoreConstants.TRANSFORM_OVERRIDE, PROPERTIES.getProperty(KEY_FF_TEST_DEFINITION));
    execute(service, msg);
    assertXml(msg);
  }

  public void testDoServiceWithOverrideAllowedAndNoCache() throws Exception {
    FfTransformService service = new FfTransformService();
    AdaptrisMessage m1 = TransformHelper.createMessage(PROPERTIES.getProperty(KEY_FF_TEST_INPUT));
    m1.addMetadata(CoreConstants.TRANSFORM_OVERRIDE, PROPERTIES.getProperty(KEY_FF_TEST_DEFINITION));
    AdaptrisMessage m2 = TransformHelper.createMessage(PROPERTIES.getProperty(KEY_FF_TEST_INPUT));
    m2.addMetadata(CoreConstants.TRANSFORM_OVERRIDE, PROPERTIES.getProperty(KEY_FF_TEST_DEFINITION));
    service.setAllowOverride(true);
    try {
      service.setCacheTransforms(false);
      start(service);
      service.doService(m1);
      service.doService(m2);
      assertEquals(m1.getContent(), m2.getContent());
      assertXml(m1);
      assertXml(m2);
    }
    finally {
      stop(service);
    }
  }

  public void testDoServiceWithOverrideAllowedNoMetataKey() throws Exception {
    FfTransformService service = new FfTransformService();
    AdaptrisMessage msg = TransformHelper.createMessage(PROPERTIES.getProperty(KEY_FF_TEST_INPUT));
    service.setAllowOverride(true);
    service.setCacheTransforms(true);
    try {
      execute(service, msg);
    }
    catch (ServiceException expected) {
      ;
    }
  }

  public void testDoService_ISO8859_IncorrectFactoryEncoding() throws Exception {
    FfTransformService service = createService();
    DefaultMessageFactory factory = new DefaultMessageFactory();
    factory.setDefaultCharEncoding(UTF_8);
    AdaptrisMessage msg = TransformHelper.createMessage(factory, PROPERTIES.getProperty(KEY_FF_TEST_INPUT_ISO));
    execute(service, msg);
    Document doc = XmlHelper.createDocument(msg, false);
    XPath xp = new XPath();
    xp.selectSingleTextItem(doc, XPATH_HDR_SOURCE_DESCRIPTION);
    assertNotSame(createISOString(7), xp.selectSingleTextItem(doc, XPATH_HDR_SOURCE_DESCRIPTION));

  }

  public void testDoService_ISO8859_1() throws Exception {
    FfTransformService service = createService();
    DefaultMessageFactory factory = new DefaultMessageFactory();
    factory.setDefaultCharEncoding(ISO_8859_1);
    AdaptrisMessage msg = TransformHelper.createMessage(factory, PROPERTIES.getProperty(KEY_FF_TEST_INPUT_ISO));
    execute(service, msg);
    Document doc = XmlHelper.createDocument(msg, false);
    XPath xp = new XPath();
    xp.selectSingleTextItem(doc, XPATH_HDR_SOURCE_DESCRIPTION);
    assertEquals(createISOString(7), xp.selectSingleTextItem(doc, XPATH_HDR_SOURCE_DESCRIPTION));
  }

  public void testIssue2661() throws Exception {
    FfTransformService service = new FfTransformService();
    service.setUrl(PROPERTIES.getProperty(KEY_ISSUE_2661_DEFINITION));
    service.setOutputMessageEncoding(UTF_8);
    DefaultMessageFactory factory = new DefaultMessageFactory();
    factory.setDefaultCharEncoding(ISO_8859_1);
    AdaptrisMessage msg = TransformHelper.createMessage(factory, PROPERTIES.getProperty(KEY_ISSUE_2661_INPUT));

    execute(service, msg);
    String srcValue = createExpectedValueFor2661();
    assertEquals(UTF_8, msg.getContentEncoding());

    Document destXml = createDocument(msg.getPayload());
    XPath xpath = new XPath();
    String destValue = xpath.selectSingleTextItem(destXml, ISSUE_2661_XPATH);
    assertEquals(srcValue, destValue);
  }

  public void testIssue2661_WithoutOutputMessageEncoding() throws Exception {
    FfTransformService service = new FfTransformService();
    service.setUrl(PROPERTIES.getProperty(KEY_ISSUE_2661_DEFINITION));
    DefaultMessageFactory factory = new DefaultMessageFactory();
    factory.setDefaultCharEncoding(ISO_8859_1);
    AdaptrisMessage msg = TransformHelper.createMessage(factory, PROPERTIES.getProperty(KEY_ISSUE_2661_INPUT));

    execute(service, msg);
    createExpectedValueFor2661();

    assertEquals(ISO_8859_1, msg.getContentEncoding());
    try {
      createDocument(msg.getPayload());
      // Should fail.
      fail("Really should have failed, UTF-8 should allow you to do this.");
    }
    catch (SAXParseException e) {
      assertEquals("Invalid byte 2 of 3-byte UTF-8 sequence.", e.getMessage());
    }
  }

  private String createExpectedValueFor2661() {
    Charset iso8859 = Charset.forName(ISO_8859_1);
    ByteBuffer input2 = ByteBuffer.wrap(new byte[]
    {
        (byte) 0x48, (byte) 0xe4, (byte) 0x75, (byte) 0x73, (byte) 0x6C, (byte) 0x65, (byte) 0x73, (byte) 0xE4, (byte) 0x63,
        (byte) 0x6B, (byte) 0x65, (byte) 0x72, (byte) 0x20, (byte) 0x37,
    });
    CharBuffer d3 = iso8859.decode(input2);
    return d3.toString();
  }

  @Override
  protected Object retrieveObjectForSampleConfig() {
    return createService();
  }

  private FfTransformService createService() {
    FfTransformService service = new FfTransformService();
    service.setUrl(PROPERTIES.getProperty(KEY_FF_TEST_DEFINITION));
    return service;
  }

  private void assertXml(AdaptrisMessage msg) throws Exception {
    Document doc = XmlHelper.createDocument(msg, false);
    XPath xp = new XPath();
    assertEquals("HDR", xp.selectSingleTextItem(doc, XPATH_HDR_RECORD_TYPE));
    assertEquals("TRL", xp.selectSingleTextItem(doc, XPATH_TRL_RECORD_TYPE));
  }

  private String createISOString(int size) {
    Charset iso8859 = Charset.forName(ISO_8859_1);
    ByteBuffer inputBuffer = ByteBuffer.wrap(createUmlaut_U(size));
    CharBuffer data = iso8859.decode(inputBuffer);
    return data.toString();
  }

  // 0xFC = a u with an Umlaut on it.
  private byte[] createUmlaut_U(int size) {
    byte[] bytes = new byte[size];
    for (int i = 0; i < size; i++) {
      bytes[i] = (byte) 0xFC;
    }
    return bytes;
  }

  private Document createDocument(byte[] bytes) throws Exception {
    DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
    DocumentBuilder builder = domFactory.newDocumentBuilder();
    ByteArrayInputStream in = new ByteArrayInputStream(bytes);
    return builder.parse(new InputSource(in));
  }

}
