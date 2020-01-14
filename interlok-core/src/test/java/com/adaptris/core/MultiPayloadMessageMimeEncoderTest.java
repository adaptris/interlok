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
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.StringWriter;
import java.security.MessageDigest;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

public class MultiPayloadMessageMimeEncoderTest {

  private static final String ENCODING = "UTF-8";
  private static final String METADATA_VALUE = "value";
  private static final String METADATA_KEY = "key";
  private static final String PAYLOAD_ID[] = { "payload-1", "payload-2" };
  private static final String STANDARD_PAYLOAD[] = { "Cupcake ipsum dolor sit amet bonbon cotton candy ice cream. Pudding chocolate sweet lemon drops carrot cake pastry sweet roll. Wafer cheesecake lemon drops. Fruitcake tiramisu chocolate cake dessert gummies fruitcake bear claw brownie. Bear claw dessert marshmallow chocolate bar. Gummies bonbon oat cake tootsie roll. Tiramisu topping jelly beans powder souffle carrot cake. Gummi bears gingerbread tart pie. Oat cake danish gummies fruitcake. Cake icing sweet roll. Sweet roll cake cheesecake gingerbread. Cake brownie pastry. Lemon drops apple pie caramels sweet jelly beans oat cake jujubes dessert wafer. Oat cake sweet roll fruitcake croissant gummies sweet halvah croissant dessert.",
                                                     "Bacon ipsum dolor amet tri-tip bacon kielbasa flank rump pork belly. Pastrami pork t-bone ground round tenderloin, capicola bresaola ham turducken. Rump turkey boudin biltong, doner short loin swine t-bone buffalo pastrami capicola pork loin alcatra beef ribs jerky. Landjaeger chicken cupim corned beef venison. Jerky turducken pork chop burgdoggen. Landjaeger shankle chislic alcatra flank ribeye, short loin swine corned beef drumstick ham hock tri-tip filet mignon. Pastrami boudin turkey, tongue landjaeger ham hock ball tip cupim ground round ribeye pork loin pig sirloin shoulder." };

  private MimeEncoderImpl mimeEncoder;
  private MultiPayloadMessageFactory messageFactory;


  @Rule
  public TestName testName = new TestName();

  @Before
  public void setUp() throws Exception {
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

  @Test
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

  @Test
  public void testDecodeNonInputStream() {
    try {
      mimeEncoder.readMessage(new StringWriter());
      fail();
    } catch (CoreException e) {
      /* expected; do nothing */
    }
  }

  @Test
  public void testRoundTripWithException() throws Exception {
    AdaptrisMessage message = messageFactory.newMessage(PAYLOAD_ID[0], STANDARD_PAYLOAD[0], ENCODING);
    message.addMetadata(METADATA_KEY, METADATA_VALUE);
    message.addObjectHeader(CoreConstants.OBJ_METADATA_EXCEPTION, new Exception(testName.getMethodName()));
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    mimeEncoder.writeMessage(message, out);
    AdaptrisMessage result = mimeEncoder.readMessage(new ByteArrayInputStream(out.toByteArray()));
    assertEquals(METADATA_VALUE, result.getMetadataValue(METADATA_KEY));
    assertEquals(STANDARD_PAYLOAD[0], result.getContent());
    assertTrue(MessageDigest.isEqual(STANDARD_PAYLOAD[0].getBytes(), result.getPayload()));
    assertFalse(result.getObjectHeaders().containsKey(CoreConstants.OBJ_METADATA_EXCEPTION));
  }
}
