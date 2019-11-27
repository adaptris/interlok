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

package com.adaptris.core.lms;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.Properties;

import com.adaptris.core.MultiPayloadAdaptrisMessage;
import com.adaptris.core.MultiPayloadMessageFactory;
import org.junit.Test;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.CoreConstants;
import com.adaptris.core.CoreException;
import com.adaptris.core.stubs.TempFileUtils;
import com.adaptris.util.GuidGenerator;
import com.adaptris.util.text.mime.MultiPartOutput;

public class FileBackedMimeEncoderTest {
  private static final String STANDARD_PAYLOAD = "Pack My Box with a dozen Liquor jugs";
  private static final String STANDARD_PAYLOAD_NON_JUST_ALPHA = "The quick brown fox jumps over the lazy dog\t\r\n";

  private static final String METADATA_VALUE = "value";
  private static final String METADATA_KEY = "key";

  @Test(expected = CoreException.class)
  public void testEncode_NotFile() throws Exception {
    FileBackedMimeEncoder mimeEncoder = new FileBackedMimeEncoder();

    mimeEncoder.writeMessage(AdaptrisMessageFactory.getDefaultInstance().newMessage(STANDARD_PAYLOAD), new Object());
  }

  @Test(expected = CoreException.class)
  public void testDecode_NotFile() throws Exception {
    FileBackedMimeEncoder mimeEncoder = new FileBackedMimeEncoder();
    mimeEncoder.readMessage(new Object());
  }

  @Test
  public void testRoundTrip() throws Exception {
    FileBackedMimeEncoder mimeEncoder = new FileBackedMimeEncoder();
    mimeEncoder.setRetainUniqueId(true);

    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(STANDARD_PAYLOAD);
    msg.addMetadata(METADATA_KEY, METADATA_VALUE);
    File outputFile = TempFileUtils.createTrackedFile(msg);
    mimeEncoder.writeMessage(msg, outputFile);

    AdaptrisMessage result = mimeEncoder.readMessage(outputFile);
    assertEquals(msg.getUniqueId(), result.getUniqueId());
    assertEquals(METADATA_VALUE, result.getMetadataValue(METADATA_KEY));
    assertEquals(STANDARD_PAYLOAD, result.getContent());
  }

  @Test
  public void testRoundTrip_FileBackedFactory() throws Exception {
    FileBackedMimeEncoder mimeEncoder = new FileBackedMimeEncoder();
    mimeEncoder.registerMessageFactory(new FileBackedMessageFactory());

    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(STANDARD_PAYLOAD);
    msg.addMetadata(METADATA_KEY, METADATA_VALUE);
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    File outputFile = TempFileUtils.createTrackedFile(msg);
    mimeEncoder.writeMessage(msg, outputFile);

    AdaptrisMessage result = mimeEncoder.readMessage(outputFile);
    assertEquals(METADATA_VALUE, result.getMetadataValue(METADATA_KEY));
    assertEquals(STANDARD_PAYLOAD, result.getContent());
  }

  @Test
  public void testDecode_IgnoreExtraParts() throws Exception {
    FileBackedMimeEncoder mimeEncoder = new FileBackedMimeEncoder();
    mimeEncoder.registerMessageFactory(new FileBackedMessageFactory());

    AdaptrisMessage result = mimeEncoder.readMessage(generateOutput());
    assertEquals(METADATA_VALUE, result.getMetadataValue(METADATA_KEY));
    assertEquals(STANDARD_PAYLOAD, result.getContent());
  }

  @Test
  public void testEncode_WithException() throws Exception {
    FileBackedMimeEncoder mimeEncoder = new FileBackedMimeEncoder();

    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(STANDARD_PAYLOAD);
    msg.addMetadata(METADATA_KEY, METADATA_VALUE);
    msg.getObjectHeaders().put(CoreConstants.OBJ_METADATA_EXCEPTION, new Exception("testEncode_WithException"));
    File outputFile = TempFileUtils.createTrackedFile(msg);
    mimeEncoder.writeMessage(msg, outputFile);

    AdaptrisMessage result = mimeEncoder.readMessage(outputFile);
    assertNotSame(msg.getUniqueId(), result.getUniqueId());
    assertEquals(METADATA_VALUE, result.getMetadataValue(METADATA_KEY));
    assertEquals(STANDARD_PAYLOAD, result.getContent());
  }

  private File generateOutput() throws Exception {
    File file = TempFileUtils.createTrackedFile(this);
    MultiPartOutput output = new MultiPartOutput(new GuidGenerator().getUUID());
    output.addPart(STANDARD_PAYLOAD.getBytes(), "base64", "AdaptrisMessage/payload");
    Properties p = new Properties();
    p.setProperty(METADATA_KEY, METADATA_VALUE);
    ByteArrayOutputStream bytes = new ByteArrayOutputStream();
    p.store(bytes, "");
    bytes.close();
    output.addPart(bytes.toByteArray(), "base64", "AdaptrisMessage/metadata");
    output.addPart(STANDARD_PAYLOAD_NON_JUST_ALPHA.getBytes(), "base64", "Dude/SomeOtherPart");
    try (FileOutputStream out = new FileOutputStream(file)) {
      output.writeTo(out);
    }
    return file;
  }
}
