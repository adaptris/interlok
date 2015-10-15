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

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.adaptris.util.text.mime.MimeConstants;
import com.adaptris.util.text.mime.MultiPartInput;
import com.adaptris.util.text.mime.MultiPartOutput;

/**
 *
 * @author lchan
 */
public class TestMultipartOutput extends TestCase implements MimeConstants {
  private static final String NUMBER_OF_PARTS = "Number of parts";

  private static final String PROPERTY_FILE_HEADER = "System Properties";

  private static final String PAYLOAD_1 = "The quick brown fox jumps over the lazy dog";
  private static final String PAYLOAD_2 = "Sixty zippers were quickly picked "
      + "from the woven jute bag";
  private GuidGenerator guid;
  static final Log logR = LogFactory.getLog(TestMultipartOutput.class);

  public TestMultipartOutput(java.lang.String testName) {
    super(testName);
  }

  public static void main(java.lang.String[] args) {
    junit.textui.TestRunner.run(suite());
  }

  public static Test suite() {
    TestSuite suite = new TestSuite(TestMultipartOutput.class);
    return suite;
  }

  @Override
  public void setUp() throws Exception {
    guid = new GuidGenerator();
  }

  @Override
  public void tearDown() {
  }

  public void testCreatePlainMimeOutput() {
    try {

      MultiPartOutput output = new MultiPartOutput(guid.getUUID());
      output.addPart(PAYLOAD_1, guid.getUUID());
      MultiPartInput input = new MultiPartInput(output.getBytes());
      assertEquals(NUMBER_OF_PARTS, 1, input.size());
    }
    catch (Exception e) {
      fail(e.getMessage());
    }
  }

  public void testCreateBase64MimeOutput() {
    try {

      MultiPartOutput output = new MultiPartOutput(guid.getUUID());
      output.addPart(PAYLOAD_1, ENCODING_BASE64, guid.getUUID());
      MultiPartInput input = new MultiPartInput(output.getBytes());
      assertEquals(NUMBER_OF_PARTS, 1, input.size());
    }
    catch (Exception e) {
      fail(e.getMessage());
    }
  }

  public void testCreate7bitMimeOutput() {
    try {

      MultiPartOutput output = new MultiPartOutput(guid.getUUID());
      output.addPart(PAYLOAD_1, ENCODING_7BIT, guid.getUUID());
      MultiPartInput input = new MultiPartInput(output.getBytes());
      assertEquals(NUMBER_OF_PARTS, 1, input.size());
    }
    catch (Exception e) {
      fail(e.getMessage());
    }
  }

  public void testCreate8bitMimeOutput() {
    try {

      MultiPartOutput output = new MultiPartOutput(guid.getUUID());
      output.addPart(PAYLOAD_1, ENCODING_8BIT, guid.getUUID());
      MultiPartInput input = new MultiPartInput(output.getBytes());
      assertEquals(NUMBER_OF_PARTS, 1, input.size());
    }
    catch (Exception e) {
      fail(e.getMessage());
    }
  }

  public void testCreateQuotedPrintableMimeOutput() {
    try {

      MultiPartOutput output = new MultiPartOutput(guid.getUUID());
      output.addPart(PAYLOAD_1, ENCODING_QUOTED, guid.getUUID());
      MultiPartInput input = new MultiPartInput(output.getBytes());
      assertEquals(NUMBER_OF_PARTS, 1, input.size());
    }
    catch (Exception e) {
      fail(e.getMessage());
    }
  }

  public void testCreateMultiPartMimeOutput() {
    try {

      MultiPartOutput output = new MultiPartOutput(guid.getUUID());
      output.addPart(PAYLOAD_1, guid.getUUID());
      output.addPart(PAYLOAD_2, guid.getUUID());
      ByteArrayOutputStream out = new ByteArrayOutputStream();
      System.getProperties().store(out, PROPERTY_FILE_HEADER);
      output.addPart(out.toByteArray(), guid.getUUID());

      MultiPartInput input = new MultiPartInput(output.getBytes());
      assertEquals(NUMBER_OF_PARTS, 3, input.size());
    }
    catch (Exception e) {
      fail(e.getMessage());
    }
  }

  public void testCreateMultiPartWithNullPayload() {
    try {

      MultiPartOutput output = new MultiPartOutput(guid.getUUID());
      byte[] nullOutput = null;
      output.addPart(nullOutput, guid.getUUID());
      byte[] mimePayload = output.getBytes();
      MultiPartInput input = new MultiPartInput(mimePayload);
      assertEquals(NUMBER_OF_PARTS, 1, input.size());
    }
    catch (Exception e) {
      fail(e.getMessage());
    }
  }

  public void testCreateMultiPartWithRemovedParts() {
    try {

      MultiPartOutput output = new MultiPartOutput(guid.getUUID());
      String matchingContentId = guid.getUUID();

      output.addPart(PAYLOAD_1, guid.getUUID());
      output.addPart(PAYLOAD_1, matchingContentId);
      output.addPart(PAYLOAD_1, guid.getUUID());
      output.addPart(PAYLOAD_1, matchingContentId);
      output.removePart(matchingContentId);

      byte[] mimePayload = output.getBytes();
      MultiPartInput input = new MultiPartInput(mimePayload);
      assertEquals(NUMBER_OF_PARTS, 2, input.size());
    }
    catch (Exception e) {
      fail(e.getMessage());
    }
  }
}
