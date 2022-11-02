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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.ByteArrayOutputStream;

import org.junit.Before;
import org.junit.Test;

import com.adaptris.util.text.mime.ByteArrayIterator;
import com.adaptris.util.text.mime.MimeConstants;
import com.adaptris.util.text.mime.MultiPartOutput;

public class TestMultipartOutput implements MimeConstants {
  private static final String NUMBER_OF_PARTS = "Number of parts";

  private static final String PROPERTY_FILE_HEADER = "System Properties";

  private static final String PAYLOAD_1 = "The quick brown fox jumps over the lazy dog";
  private static final String PAYLOAD_2 = "Sixty zippers were quickly picked "
      + "from the woven jute bag";
  private GuidGenerator guid;

  @Before
  public void setUp() throws Exception {
    guid = new GuidGenerator();
  }

  @Test
  public void testCreatePlainMimeOutput() {
    try {

      MultiPartOutput output = new MultiPartOutput(guid.getUUID());
      output.addPart(PAYLOAD_1, guid.getUUID());
      ByteArrayIterator input = new ByteArrayIterator(output.getBytes());
      assertEquals(NUMBER_OF_PARTS, 1, input.size());
    }
    catch (Exception e) {
      fail(e.getMessage());
    }
  }

  @Test
  public void testCreateBase64MimeOutput() {
    try {

      MultiPartOutput output = new MultiPartOutput(guid.getUUID());
      output.addPart(PAYLOAD_1, ENCODING_BASE64, guid.getUUID());
      ByteArrayIterator input = new ByteArrayIterator(output.getBytes());
      assertEquals(NUMBER_OF_PARTS, 1, input.size());
    }
    catch (Exception e) {
      fail(e.getMessage());
    }
  }

  @Test
  public void testCreate7bitMimeOutput() {
    try {

      MultiPartOutput output = new MultiPartOutput(guid.getUUID());
      output.addPart(PAYLOAD_1, ENCODING_7BIT, guid.getUUID());
      ByteArrayIterator input = new ByteArrayIterator(output.getBytes());
      assertEquals(NUMBER_OF_PARTS, 1, input.size());
    }
    catch (Exception e) {
      fail(e.getMessage());
    }
  }

  @Test
  public void testCreate8bitMimeOutput() {
    try {

      MultiPartOutput output = new MultiPartOutput(guid.getUUID());
      output.addPart(PAYLOAD_1, ENCODING_8BIT, guid.getUUID());
      ByteArrayIterator input = new ByteArrayIterator(output.getBytes());
      assertEquals(NUMBER_OF_PARTS, 1, input.size());
    }
    catch (Exception e) {
      fail(e.getMessage());
    }
  }

  @Test
  public void testCreateQuotedPrintableMimeOutput() {
    try {

      MultiPartOutput output = new MultiPartOutput(guid.getUUID());
      output.addPart(PAYLOAD_1, ENCODING_QUOTED, guid.getUUID());
      ByteArrayIterator input = new ByteArrayIterator(output.getBytes());
      assertEquals(NUMBER_OF_PARTS, 1, input.size());
    }
    catch (Exception e) {
      fail(e.getMessage());
    }
  }

  @Test
  public void testCreateMultiPartMimeOutput() {
    try {

      MultiPartOutput output = new MultiPartOutput(guid.getUUID());
      output.addPart(PAYLOAD_1, guid.getUUID());
      output.addPart(PAYLOAD_2, guid.getUUID());
      ByteArrayOutputStream out = new ByteArrayOutputStream();
      System.getProperties().store(out, PROPERTY_FILE_HEADER);
      output.addPart(out.toByteArray(), guid.getUUID());

      ByteArrayIterator input = new ByteArrayIterator(output.getBytes());
      assertEquals(NUMBER_OF_PARTS, 3, input.size());
    }
    catch (Exception e) {
      fail(e.getMessage());
    }
  }

  @Test
  public void testCreateMultiPartWithNullPayload() {
    try {

      MultiPartOutput output = new MultiPartOutput(guid.getUUID());
      byte[] nullOutput = null;
      output.addPart(nullOutput, guid.getUUID());
      byte[] mimePayload = output.getBytes();
      ByteArrayIterator input = new ByteArrayIterator(output.getBytes());
      assertEquals(NUMBER_OF_PARTS, 1, input.size());
    }
    catch (Exception e) {
      fail(e.getMessage());
    }
  }

  @Test
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
      ByteArrayIterator input = new ByteArrayIterator(output.getBytes());
      assertEquals(NUMBER_OF_PARTS, 2, input.size());
    }
    catch (Exception e) {
      fail(e.getMessage());
    }
  }
}
