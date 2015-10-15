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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Properties;

import javax.mail.internet.MimeBodyPart;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.adaptris.util.stream.StreamUtil;
import com.adaptris.util.text.mime.MimeConstants;
import com.adaptris.util.text.mime.MultiPartInput;
import com.adaptris.util.text.mime.MultiPartOutput;

/**
 *
 * @author lchan
 */
public class TestMultipartInput extends TestCase implements MimeConstants {

  private static final String NUMBER_OF_PARTS = "Number of parts";
  private static final String PROPERTY_FILE_HEADER = "Header";
  private static final String TXT_COMPARE_PAYLOADS = "Compare payloads";
  private static final String TXT_COMPARE_PROPERTY = "Compare property";

  private static final String PROPERTY_KEY_1 = "panagram.1";
  private static final String PROPERTY_KEY_2 = "panagram.2";
  private static final String PROPERTY_KEY_3 = "panagram.3";

  private static final String PAYLOAD_1 = "The quick brown fox jumps over "
      + "the lazy dog";
  private static final String PAYLOAD_2 = "Sixty zippers were quickly picked "
      + "from the woven jute bag";
  private static final String PAYLOAD_3 = "Quick zephyrs blow, vexing daft Jim";

  private GuidGenerator guid;
  static final Log logR = LogFactory.getLog(TestMultipartInput.class);

  private Properties txtProps;

  public TestMultipartInput(java.lang.String testName) {
    super(testName);
  }

  public static void main(java.lang.String[] args) {
    junit.textui.TestRunner.run(suite());
  }

  public static Test suite() {
    TestSuite suite = new TestSuite(TestMultipartInput.class);
    return suite;
  }

  @Override
  public void setUp() throws Exception {
    guid = new GuidGenerator();
    txtProps = new Properties();
    txtProps.setProperty(PROPERTY_KEY_1, PAYLOAD_1);
    txtProps.setProperty(PROPERTY_KEY_2, PAYLOAD_2);
    txtProps.setProperty(PROPERTY_KEY_3, PAYLOAD_3);
  }

  @Override
  public void tearDown() {
  }

  public void testSingleMultipartSelectBodyPartBytesByContentId() throws Exception {
    MultiPartOutput output = new MultiPartOutput(guid.getUUID());
    String contentId = guid.getUUID();
    byte[] payload = buildPayload();
    output.addPart(payload, ENCODING_BASE64, contentId);
    byte[] mimePayload = output.getBytes();

    MultiPartInput input = new MultiPartInput(mimePayload);
    assertEquals(NUMBER_OF_PARTS, input.size(), 1);
    byte[] readPayload = input.getPart(contentId);

    verifyProperties(new ByteArrayInputStream(readPayload));
  }

  public void testSingleMultipartSelectBodyPartByContentId() throws Exception {
    MultiPartOutput output = new MultiPartOutput(guid.getUUID());
    String contentId = guid.getUUID();
    byte[] payload = buildPayload();
    output.addPart(payload, ENCODING_BASE64, contentId);
    byte[] mimePayload = output.getBytes();

    MultiPartInput input = new MultiPartInput(mimePayload);
    assertEquals(NUMBER_OF_PARTS, input.size(), 1);

    MimeBodyPart bodyPart = input.getBodyPart(contentId);
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    StreamUtil.copyStream(bodyPart.getInputStream(), out);
    verifyProperties(new ByteArrayInputStream(out.toByteArray()));
  }

  public void testMultiPartInputSelectBodyPartBytesByContentID() throws Exception {
    MultiPartOutput output = new MultiPartOutput(guid.getUUID());
    String contentId = guid.getUUID();
    byte[] payload = buildPayload();
    output.addPart(payload, ENCODING_BASE64, contentId);
    String contentId2 = guid.getUUID();
    output.addPart(PAYLOAD_1, ENCODING_8BIT, contentId2);

    MultiPartInput input = new MultiPartInput(output.getBytes());
    assertEquals(NUMBER_OF_PARTS, input.size(), 2);

    byte[] body = input.getPart(contentId);
    verifyProperties(new ByteArrayInputStream(body));
    body = input.getPart(contentId2);
    assertEquals(TXT_COMPARE_PAYLOADS, PAYLOAD_1, new String(body));
  }

  public void testMultiPartInputSelectBodyPartBytesByPosition() throws Exception {
    MultiPartOutput output = new MultiPartOutput(guid.getUUID());
    String contentId = guid.getUUID();
    byte[] payload = buildPayload();
    output.addPart(payload, ENCODING_BASE64, contentId);
    String contentId2 = guid.getUUID();
    output.addPart(PAYLOAD_1, ENCODING_8BIT, contentId2);

    MultiPartInput input = new MultiPartInput(output.getBytes());
    assertEquals(NUMBER_OF_PARTS, input.size(), 2);

    byte[] body = input.getPart(0);
    verifyProperties(new ByteArrayInputStream(body));
    body = input.getPart(1);
    assertEquals(TXT_COMPARE_PAYLOADS, PAYLOAD_1, new String(body));
    assertTrue(input.getBodyPart(10) == null);
  }


  public void testMultiPartInputSelectBodyPartByContentID() throws Exception {
    MultiPartOutput output = new MultiPartOutput(guid.getUUID());
    String contentId = guid.getUUID();
    byte[] payload = buildPayload();
    output.addPart(payload, ENCODING_BASE64, contentId);
    String contentId2 = guid.getUUID();
    output.addPart(PAYLOAD_1, ENCODING_8BIT, contentId2);

    MultiPartInput input = new MultiPartInput(output.getBytes(), false);
    assertEquals(NUMBER_OF_PARTS, input.size(), 2);

    MimeBodyPart bodyPart = input.getBodyPart(contentId);
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    StreamUtil.copyStream(bodyPart.getInputStream(), out);
    verifyProperties(new ByteArrayInputStream(out.toByteArray()));

    bodyPart = input.getBodyPart(contentId2);
    out = new ByteArrayOutputStream();
    StreamUtil.copyStream(bodyPart.getInputStream(), out);
    out.flush();
    assertEquals(TXT_COMPARE_PAYLOADS, PAYLOAD_1, out.toString());
  }

  public void testMultiPartInputSelectBodyPartByPosition() throws Exception {
    MultiPartOutput output = new MultiPartOutput(guid.getUUID());
    String contentId = guid.getUUID();
    byte[] payload = buildPayload();
    output.addPart(payload, ENCODING_BASE64, contentId);
    String contentId2 = guid.getUUID();
    output.addPart(PAYLOAD_1, ENCODING_8BIT, contentId2);

    MultiPartInput input = new MultiPartInput(output.getBytes(), false);
    assertEquals(NUMBER_OF_PARTS, input.size(), 2);

    MimeBodyPart bodyPart = input.getBodyPart(0);
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    StreamUtil.copyStream(bodyPart.getInputStream(), out);
    verifyProperties(new ByteArrayInputStream(out.toByteArray()));

    bodyPart = input.getBodyPart(1);
    out = new ByteArrayOutputStream();
    StreamUtil.copyStream(bodyPart.getInputStream(), out);
    out.flush();
    assertEquals(TXT_COMPARE_PAYLOADS, PAYLOAD_1, out.toString());
    assertTrue(input.getBodyPart(10) == null);
  }


  public void testSimpleMultiPartInputIterator() throws Exception {
    MultiPartOutput output = new MultiPartOutput(guid.getUUID());
    //int max = r.nextInt(5);
    int max = 20;
    for (int i = 0; i < max; i++) {
      byte[] payload = buildPayload();
      output.addPart(payload, guid.getUUID());
    }

    MultiPartInput input = new MultiPartInput(output.getBytes(), true);
    assertEquals(NUMBER_OF_PARTS, input.size(), max);
    while(input.hasNext()) {
      Object o = input.next();
      assertNotNull(o);
      assertTrue((o instanceof byte[]));
    }
  }


  public void testMultiPartInputIterator() throws Exception {
    MultiPartOutput output = new MultiPartOutput(guid.getUUID());
//    int max = r.nextInt(5);
    int max = 20;
    for (int i = 0; i < max; i++) {
      byte[] payload = buildPayload();
      output.addPart(payload, guid.getUUID());
    }

    MultiPartInput input = new MultiPartInput(output.getBytes(), false);
    assertEquals(NUMBER_OF_PARTS, input.size(), max);
    while(input.hasNext()) {
      Object o = input.next();
      assertNotNull(o);
      assertTrue((o instanceof MimeBodyPart));
    }
  }

  private void verifyProperties(InputStream in) throws Exception {
    Properties readProperties = new Properties();
    readProperties.load(in);
    assertEquals(TXT_COMPARE_PROPERTY, txtProps.getProperty(PROPERTY_KEY_1),
        readProperties.getProperty(PROPERTY_KEY_1));
    assertEquals(TXT_COMPARE_PROPERTY, txtProps.getProperty(PROPERTY_KEY_2),
        readProperties.getProperty(PROPERTY_KEY_2));
    assertEquals(TXT_COMPARE_PROPERTY, txtProps.getProperty(PROPERTY_KEY_3),
        readProperties.getProperty(PROPERTY_KEY_3));
  }

  private byte[] buildPayload() throws Exception {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    txtProps.store(out, PROPERTY_FILE_HEADER);
    out.flush();
    return out.toByteArray();
  }
}
