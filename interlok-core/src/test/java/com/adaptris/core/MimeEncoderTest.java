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

package com.adaptris.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.StringWriter;
import java.security.MessageDigest;
import java.util.Properties;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import com.adaptris.util.GuidGenerator;
import com.adaptris.util.text.mime.MultiPartOutput;

public class MimeEncoderTest {

  private static final String METADATA_VALUE = "value";
  private static final String METADATA_KEY = "key";
  private static final String STANDARD_PAYLOAD_NON_JUST_ALPHA = "The quick brown fox jumps over the lazy dog\t\r\n";
  private static final String STANDARD_PAYLOAD = "Pack My Box with a dozen Liquor jugs";

  private MimeEncoder mimeEncoder;

  @Rule
  public TestName testName = new TestName();

  @Before
  public void setUp() throws Exception {
    mimeEncoder = new MimeEncoder();
    mimeEncoder.registerMessageFactory(new DefaultMessageFactory());
  }

  @Test
  public void testSetters() throws Exception {
    MimeEncoder mime = new MimeEncoder();
    assertNull(mime.getPayloadEncoding());
    assertNull(mime.getMetadataEncoding());
    mime.setPayloadEncoding("blahblah");
    assertEquals("blahblah", mime.getPayloadEncoding());
    mime.setMetadataEncoding("blahblah");
    assertEquals("blahblah", mime.getMetadataEncoding());
    assertNull(mime.getRetainUniqueId());
    assertFalse(mime.retainUniqueId());
    mime.setRetainUniqueId(Boolean.TRUE);
    assertNotNull(mime.getRetainUniqueId());
    assertTrue(mime.retainUniqueId());
    assertEquals(Boolean.TRUE, mime.getRetainUniqueId());
    mime.toString();
  }

  @Test
  public void testRoundTrip() throws Exception {

    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(STANDARD_PAYLOAD);
    msg.addMetadata(METADATA_KEY, METADATA_VALUE);
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    mimeEncoder.writeMessage(msg, out);
    ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
    AdaptrisMessage result = mimeEncoder.readMessage(in);
    assertEquals(METADATA_VALUE, result.getMetadataValue(METADATA_KEY));
    assertEquals(STANDARD_PAYLOAD, result.getContent());
    assertTrue(MessageDigest.isEqual(STANDARD_PAYLOAD.getBytes(), result.getPayload()));
  }

  @Test
  public void testRoundTrip_WithOddChars() throws Exception {

    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(STANDARD_PAYLOAD_NON_JUST_ALPHA);
    msg.addMetadata(METADATA_KEY, METADATA_VALUE);
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    mimeEncoder.writeMessage(msg, out);
    ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
    AdaptrisMessage result = mimeEncoder.readMessage(in);
    assertEquals(METADATA_VALUE, result.getMetadataValue(METADATA_KEY));
    assertEquals(STANDARD_PAYLOAD_NON_JUST_ALPHA, result.getContent());
    assertTrue(MessageDigest.isEqual(STANDARD_PAYLOAD_NON_JUST_ALPHA.getBytes(), result.getPayload()));
  }

  @Test
  public void testRoundTrip_WithException() throws Exception {
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(STANDARD_PAYLOAD);
    msg.addMetadata(METADATA_KEY, METADATA_VALUE);
    msg.addObjectHeader(CoreConstants.OBJ_METADATA_EXCEPTION, new Exception(testName.getMethodName()));
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    mimeEncoder.writeMessage(msg, out);
    ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
    AdaptrisMessage result = mimeEncoder.readMessage(in);
    assertEquals(METADATA_VALUE, result.getMetadataValue(METADATA_KEY));
    assertEquals(STANDARD_PAYLOAD, result.getContent());
    assertTrue(MessageDigest.isEqual(STANDARD_PAYLOAD.getBytes(), result.getPayload()));
    assertFalse(result.getObjectHeaders().containsKey(CoreConstants.OBJ_METADATA_EXCEPTION));
  }

  @Test
  public void testRoundTrip_Encoded() throws Exception {

    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(STANDARD_PAYLOAD);
    msg.addMetadata(METADATA_KEY, METADATA_VALUE);
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    mimeEncoder.writeMessage(msg, out);
    mimeEncoder.setPayloadEncoding("base64");
    mimeEncoder.setMetadataEncoding("base64");
    ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
    AdaptrisMessage result = mimeEncoder.readMessage(in);
    assertEquals(METADATA_VALUE, result.getMetadataValue(METADATA_KEY));
    assertEquals(STANDARD_PAYLOAD, result.getContent());
    assertTrue(MessageDigest.isEqual(STANDARD_PAYLOAD.getBytes(), result.getPayload()));
  }

  @Test
  public void testRoundTrip_EncodedEncodePlainDecoder() throws Exception {

    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(STANDARD_PAYLOAD);
    msg.addMetadata(METADATA_KEY, METADATA_VALUE);
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    mimeEncoder.writeMessage(msg, out);
    mimeEncoder.setPayloadEncoding("base64");
    mimeEncoder.setMetadataEncoding("base64");
    MimeEncoder roundtripper = new MimeEncoder();
    ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
    AdaptrisMessage result = roundtripper.readMessage(in);
    assertEquals(METADATA_VALUE, result.getMetadataValue(METADATA_KEY));
    assertEquals(STANDARD_PAYLOAD, result.getContent());
    assertTrue(MessageDigest.isEqual(STANDARD_PAYLOAD.getBytes(), result.getPayload()));
  }

  @Test
  public void testRoundTrip_EncodeMetadataWithBackslash() throws Exception {

    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(STANDARD_PAYLOAD);
    msg.addMetadata(METADATA_KEY, "blah\\blah");
    ByteArrayOutputStream out = new ByteArrayOutputStream();

    mimeEncoder.writeMessage(msg, out);
    ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
    msg = mimeEncoder.readMessage(in);
    assertEquals("blah\\blah", msg.getMetadataValue(METADATA_KEY));
  }

  @Test
  public void testRoundTrip_PreserveUniqueId() throws Exception {

    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(STANDARD_PAYLOAD);
    msg.addMetadata(METADATA_KEY, METADATA_VALUE);
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    mimeEncoder.setRetainUniqueId(true);
    mimeEncoder.writeMessage(msg, out);
    ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
    AdaptrisMessage result = mimeEncoder.readMessage(in);
    assertEquals(msg.getUniqueId(), result.getUniqueId());
  }

  @Test
  public void testRoundTrip_NoPreserveUniqueId() throws Exception {
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(STANDARD_PAYLOAD);
    msg.setUniqueId("abc-123");
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    mimeEncoder.writeMessage(msg, out);
    ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
    AdaptrisMessage result = mimeEncoder.readMessage(in);
    assertTrue(!"abc-123".equals(result.getUniqueId()));
  }

  @Test
  public void testRoundTrip_UseConvenienceMethods() throws Exception {

    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(STANDARD_PAYLOAD);
    msg.addMetadata(METADATA_KEY, METADATA_VALUE);
    AdaptrisMessage result = mimeEncoder.decode(mimeEncoder.encode(msg));
    assertEquals(METADATA_VALUE, result.getMetadataValue(METADATA_KEY));
    assertEquals(STANDARD_PAYLOAD, result.getContent());
    assertTrue(MessageDigest.isEqual(STANDARD_PAYLOAD.getBytes(), result.getPayload()));
  }

  @Test
  public void testDecode_IgnoreExtraParts() throws Exception {
    AdaptrisMessage result = mimeEncoder.decode(createMimeOutput(true, true));
    assertEquals(METADATA_VALUE, result.getMetadataValue(METADATA_KEY));
    assertEquals(STANDARD_PAYLOAD, result.getContent());
    assertTrue(MessageDigest.isEqual(STANDARD_PAYLOAD.getBytes(), result.getPayload()));
  }

  @Test
  public void testDecode_NoPayloadPart() throws Exception {

    try {
      mimeEncoder.decode(createMimeOutput(false, true));
      fail();
    }
    catch (CoreException e) {
    }
  }

  @Test
  public void testDecode_NoMetadataPart() throws Exception {

    try {
      mimeEncoder.decode(createMimeOutput(true, false));
      fail();
    }
    catch (CoreException e) {
    }
  }

  private static byte[] createMimeOutput(boolean includePayloadPart, boolean includeMetadataPart) throws Exception {
    MultiPartOutput output = new MultiPartOutput(new GuidGenerator().getUUID());
    if (includePayloadPart) {
      output.addPart(STANDARD_PAYLOAD.getBytes(), "base64", "AdaptrisMessage/payload");
    }
    if (includeMetadataPart) {
      Properties p = new Properties();
      p.setProperty(METADATA_KEY, METADATA_VALUE);
      ByteArrayOutputStream bytes = new ByteArrayOutputStream();
      p.store(bytes, "");
      bytes.close();
      output.addPart(bytes.toByteArray(), "base64", "AdaptrisMessage/metadata");
    }
    output.addPart(STANDARD_PAYLOAD_NON_JUST_ALPHA.getBytes(), "base64", "Dude/SomeOtherPart");
    return output.getBytes();
  }
}
