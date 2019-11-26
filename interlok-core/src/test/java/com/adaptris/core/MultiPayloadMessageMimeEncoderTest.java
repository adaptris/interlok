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

import junit.framework.TestCase;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.StringWriter;

public class MultiPayloadMessageMimeEncoderTest extends TestCase {

  private static final String ENCODING = "UTF-8";
  private static final String METADATA_VALUE = "value";
  private static final String METADATA_KEY = "key";
  private static final String PAYLOAD_ID[] = { "payload-1", "payload-2" };
  private static final String STANDARD_PAYLOAD[] = { "Cupcake ipsum dolor sit amet bonbon cotton candy ice cream. Pudding chocolate sweet lemon drops carrot cake pastry sweet roll. Wafer cheesecake lemon drops. Fruitcake tiramisu chocolate cake dessert gummies fruitcake bear claw brownie. Bear claw dessert marshmallow chocolate bar. Gummies bonbon oat cake tootsie roll. Tiramisu topping jelly beans powder souffle carrot cake. Gummi bears gingerbread tart pie. Oat cake danish gummies fruitcake. Cake icing sweet roll. Sweet roll cake cheesecake gingerbread. Cake brownie pastry. Lemon drops apple pie caramels sweet jelly beans oat cake jujubes dessert wafer. Oat cake sweet roll fruitcake croissant gummies sweet halvah croissant dessert.",
                                                     "Bacon ipsum dolor amet tri-tip bacon kielbasa flank rump pork belly. Pastrami pork t-bone ground round tenderloin, capicola bresaola ham turducken. Rump turkey boudin biltong, doner short loin swine t-bone buffalo pastrami capicola pork loin alcatra beef ribs jerky. Landjaeger chicken cupim corned beef venison. Jerky turducken pork chop burgdoggen. Landjaeger shankle chislic alcatra flank ribeye, short loin swine corned beef drumstick ham hock tri-tip filet mignon. Pastrami boudin turkey, tongue landjaeger ham hock ball tip cupim ground round ribeye pork loin pig sirloin shoulder." };

  private MimeEncoderImpl mimeEncoder;
  private MultiPayloadMessageFactory messageFactory;

  public MultiPayloadMessageMimeEncoderTest(String name) {
    super(name);
  }

  @Override
  @Before
  protected void setUp() throws Exception {
    messageFactory = new MultiPayloadMessageFactory();
    mimeEncoder = new MultiPayloadMessageMimeEncoder();
    mimeEncoder.registerMessageFactory(messageFactory);
  }

  @Test
  public void testMultiPayloadRoundTrip() throws Exception {
    MultiPayloadAdaptrisMessage message = (MultiPayloadAdaptrisMessage)messageFactory.newMessage(PAYLOAD_ID[0], STANDARD_PAYLOAD[0], ENCODING);
    message.addContent(PAYLOAD_ID[1], STANDARD_PAYLOAD[1]);
    message.addMetadata(METADATA_KEY, METADATA_VALUE);

    MultiPayloadMessageMimeEncoder mimeEncoder = new MultiPayloadMessageMimeEncoder();
    mimeEncoder.setRetainUniqueId(true);

    ByteArrayOutputStream bo = new ByteArrayOutputStream();
    mimeEncoder.writeMessage(message, bo);

    ByteArrayInputStream bi = new ByteArrayInputStream(bo.toByteArray());
    MultiPayloadAdaptrisMessage result = (MultiPayloadAdaptrisMessage)mimeEncoder.readMessage(bi);

    assertEquals(STANDARD_PAYLOAD.length, result.getPayloadCount());
    assertEquals(message.getUniqueId(), result.getUniqueId());
    assertEquals(METADATA_VALUE, result.getMetadataValue(METADATA_KEY));
    assertEquals(STANDARD_PAYLOAD[0], result.getContent(PAYLOAD_ID[0]));
    assertEquals(STANDARD_PAYLOAD[1], result.getContent(PAYLOAD_ID[1]));
  }

  @Test
  public void testNonMultiPayloadMessage() throws Exception {
    AdaptrisMessageFactory messageFactory = DefaultMessageFactory.getDefaultInstance();
    AdaptrisMessage message = messageFactory.newMessage(STANDARD_PAYLOAD[0]);
    message.addMetadata(METADATA_KEY, METADATA_VALUE);

    MultiPayloadMessageMimeEncoder mimeEncoder = new MultiPayloadMessageMimeEncoder();
    mimeEncoder.setRetainUniqueId(true);

    ByteArrayOutputStream bo = new ByteArrayOutputStream();
    mimeEncoder.writeMessage(message, bo);

    ByteArrayInputStream bi = new ByteArrayInputStream(bo.toByteArray());
    AdaptrisMessage result = mimeEncoder.readMessage(bi);

    assertEquals(message.getUniqueId(), result.getUniqueId());
    assertEquals(METADATA_VALUE, result.getMetadataValue(METADATA_KEY));
    assertEquals(STANDARD_PAYLOAD[0], result.getContent());
  }

  public void testEncodeNonOutputStream() {
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(STANDARD_PAYLOAD[0]);
    msg.addMetadata(METADATA_KEY, METADATA_VALUE);
    try {
      mimeEncoder.writeMessage(msg, new StringWriter());
      fail();
    } catch (CoreException e) {
      /* expected; do nothing */
    }
  }

  public void testDecodeNonInputStream() {
    try {
      mimeEncoder.readMessage(new StringWriter());
      fail();
    } catch (CoreException e) {
      /* expected; do nothing */
    }
  }

  /*
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

  public void testRoundTrip_WithException() throws Exception {
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(STANDARD_PAYLOAD);
    msg.addMetadata(METADATA_KEY, METADATA_VALUE);
    msg.addObjectHeader(CoreConstants.OBJ_METADATA_EXCEPTION, new Exception(getName()));
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    mimeEncoder.writeMessage(msg, out);
    ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
    AdaptrisMessage result = mimeEncoder.readMessage(in);
    assertEquals(METADATA_VALUE, result.getMetadataValue(METADATA_KEY));
    assertEquals(STANDARD_PAYLOAD, result.getContent());
    assertTrue(MessageDigest.isEqual(STANDARD_PAYLOAD.getBytes(), result.getPayload()));
    assertFalse(result.getObjectHeaders().containsKey(CoreConstants.OBJ_METADATA_EXCEPTION));
  }

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

  public void testRoundTrip_EncodeMetadataWithBackslash() throws Exception {

    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(STANDARD_PAYLOAD);
    msg.addMetadata(METADATA_KEY, "blah\\blah");
    ByteArrayOutputStream out = new ByteArrayOutputStream();

    mimeEncoder.writeMessage(msg, out);
    ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
    msg = mimeEncoder.readMessage(in);
    assertEquals("blah\\blah", msg.getMetadataValue(METADATA_KEY));
  }

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

  public void testRoundTrip_NoPreserveUniqueId() throws Exception {
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(STANDARD_PAYLOAD);
    msg.setUniqueId("abc-123");
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    mimeEncoder.writeMessage(msg, out);
    ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
    AdaptrisMessage result = mimeEncoder.readMessage(in);
    assertTrue(!"abc-123".equals(result.getUniqueId()));
  }

  public void testRoundTrip_UseConvenienceMethods() throws Exception {

    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(STANDARD_PAYLOAD);
    msg.addMetadata(METADATA_KEY, METADATA_VALUE);
    AdaptrisMessage result = mimeEncoder.decode(mimeEncoder.encode(msg));
    assertEquals(METADATA_VALUE, result.getMetadataValue(METADATA_KEY));
    assertEquals(STANDARD_PAYLOAD, result.getContent());
    assertTrue(MessageDigest.isEqual(STANDARD_PAYLOAD.getBytes(), result.getPayload()));
  }

  public void testDecode_IgnoreExtraParts() throws Exception {
    AdaptrisMessage result = mimeEncoder.decode(createMimeOutput(true, true));
    assertEquals(METADATA_VALUE, result.getMetadataValue(METADATA_KEY));
    assertEquals(STANDARD_PAYLOAD, result.getContent());
    assertTrue(MessageDigest.isEqual(STANDARD_PAYLOAD.getBytes(), result.getPayload()));
  }

  public void testDecode_NoPayloadPart() throws Exception {

    try {
      mimeEncoder.decode(createMimeOutput(false, true));
      fail();
    }
    catch (CoreException e) {
    }
  }

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

   */
}
