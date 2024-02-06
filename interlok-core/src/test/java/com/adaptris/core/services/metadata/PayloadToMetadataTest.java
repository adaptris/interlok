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

package com.adaptris.core.services.metadata;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import org.junit.jupiter.api.Test;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.ServiceException;
import com.adaptris.core.stubs.DefectiveMessageFactory;
import com.adaptris.core.stubs.DefectiveMessageFactory.WhenToBreak;
import com.adaptris.core.util.EncodingHelper.Encoding;

@SuppressWarnings("deprecation")
public class PayloadToMetadataTest extends MetadataServiceExample {

  private static final String DEFAULT_PAYLOAD = "zzzzzzzz";
  private static final String DEFAULT_METADATA_KEY = "helloMetadataKey";

  private PayloadToMetadataService createService(MetadataTarget target) {
    return new PayloadToMetadataService(DEFAULT_METADATA_KEY, target);
  }

  private AdaptrisMessage createMessage() {
    return AdaptrisMessageFactory.getDefaultInstance().newMessage(DEFAULT_PAYLOAD);
  }

  @Test
  public void testService_Error() throws Exception {
    PayloadToMetadataService service = createService(MetadataTarget.Standard);
    AdaptrisMessage msg = new DefectiveMessageFactory(WhenToBreak.BOTH).newMessage();
    try {
      execute(service, msg);
      fail();
    } catch (ServiceException e) {

    }
  }

  @Test
  public void testService_Metadata() throws Exception {
    PayloadToMetadataService service = createService(MetadataTarget.Standard);
    AdaptrisMessage msg = createMessage();
    execute(service, msg);
    assertTrue(msg.containsKey(DEFAULT_METADATA_KEY));
    assertEquals(DEFAULT_PAYLOAD, msg.getMetadataValue(DEFAULT_METADATA_KEY));
    assertFalse(msg.getObjectHeaders().containsKey(DEFAULT_METADATA_KEY));
  }

  @Test
  public void testService_Metadata_Encoded() throws Exception {
    PayloadToMetadataService service = createService(MetadataTarget.Standard);
    service.setEncoding(Encoding.Basic_Base64);
    AdaptrisMessage msg = createMessage();
    execute(service, msg);
    assertTrue(msg.containsKey(DEFAULT_METADATA_KEY));
    assertNotSame(DEFAULT_PAYLOAD, msg.getMetadataValue(DEFAULT_METADATA_KEY));
    assertFalse(msg.getObjectHeaders().containsKey(DEFAULT_METADATA_KEY));
  }

  @Test
  public void testService_ObjectMetadata() throws Exception {
    PayloadToMetadataService service = createService(MetadataTarget.Object);
    AdaptrisMessage msg = createMessage();
    execute(service, msg);
    assertFalse(msg.containsKey(DEFAULT_METADATA_KEY));
    assertTrue(msg.getObjectHeaders().containsKey(DEFAULT_METADATA_KEY));
  }

  @Test
  public void testService_ObjectMetadata_Encoded() throws Exception {
    PayloadToMetadataService service = createService(MetadataTarget.Object);
    service.setEncoding(Encoding.Base64);
    AdaptrisMessage msg = createMessage();
    execute(service, msg);
    assertFalse(msg.containsKey(DEFAULT_METADATA_KEY));
    assertTrue(msg.getObjectHeaders().containsKey(DEFAULT_METADATA_KEY));
  }

  @Override
  protected Object retrieveObjectForSampleConfig() {
    PayloadToMetadataService service = new PayloadToMetadataService("theMetadataKey", MetadataTarget.Standard);
    return service;
  }

}
