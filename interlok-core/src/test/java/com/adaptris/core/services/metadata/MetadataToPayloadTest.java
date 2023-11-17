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
import static org.junit.jupiter.api.Assertions.fail;

import org.junit.jupiter.api.Test;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.ServiceException;
import com.adaptris.core.services.metadata.MetadataToPayloadService.MetadataSource;
import com.adaptris.core.util.EncodingHelper.Encoding;
import com.adaptris.util.text.Conversion;

@SuppressWarnings("deprecation")
public class MetadataToPayloadTest extends MetadataServiceExample {

  private static final String DEFAULT_PAYLOAD = "zzzzzzzz";
  private static final String DEFAULT_METADATA_KEY = "helloMetadataKey";

  private MetadataToPayloadService createService(MetadataSource target) {
    return new MetadataToPayloadService(DEFAULT_METADATA_KEY, target);
  }


  private AdaptrisMessage createMessage(boolean base64) {
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage();
    if (!base64) {
      msg.addMessageHeader(DEFAULT_METADATA_KEY, DEFAULT_PAYLOAD);
      msg.addObjectHeader(DEFAULT_METADATA_KEY, DEFAULT_PAYLOAD.getBytes());
    } else {
      msg.addMessageHeader(DEFAULT_METADATA_KEY, Conversion.byteArrayToBase64String(DEFAULT_PAYLOAD.getBytes()));
      msg.addObjectHeader(DEFAULT_METADATA_KEY, Conversion.byteArrayToBase64String(DEFAULT_PAYLOAD.getBytes()).getBytes());
    }
    return msg;
  }

  @Test
  public void testService_Metadata() throws Exception {
    MetadataToPayloadService service = createService(MetadataSource.Standard);
    AdaptrisMessage msg = createMessage(false);
    execute(service, msg);
    assertEquals(DEFAULT_PAYLOAD, msg.getContent());
  }

  @Test
  public void testService_NoMetadata() throws Exception {
    MetadataToPayloadService service = new MetadataToPayloadService("DoesNotExistKey", MetadataSource.Standard);
    AdaptrisMessage msg = createMessage(false);
    try {
      execute(service, msg);
      fail("Should fail with no key found");
    } catch (ServiceException ex) {
      // expected.
    }
  }

  @Test
  public void testService_NoObjectMetadata() throws Exception {
    MetadataToPayloadService service = new MetadataToPayloadService("DoesNotExistKey", MetadataSource.Object);
    AdaptrisMessage msg = createMessage(false);
    try {
      execute(service, msg);
      fail("Should fail with no key found");
    } catch (ServiceException ex) {
      // expected.
    }
  }

  @Test
  public void testService_Metadata_Encoded() throws Exception {
    MetadataToPayloadService service = createService(MetadataSource.Standard);
    service.setEncoding(Encoding.Basic_Base64);
    AdaptrisMessage msg = createMessage(true);
    execute(service, msg);
    assertEquals(DEFAULT_PAYLOAD, msg.getContent());
  }

  @Test
  public void testService_ObjectMetadata() throws Exception {
    MetadataToPayloadService service = createService(MetadataSource.Object);
    AdaptrisMessage msg = createMessage(false);
    execute(service, msg);
    assertEquals(DEFAULT_PAYLOAD, msg.getContent());

  }

  @Test
  public void testService_ObjectMetadata_Encoded() throws Exception {
    MetadataToPayloadService service = createService(MetadataSource.Object);
    service.setEncoding(Encoding.Base64);
    AdaptrisMessage msg = createMessage(true);
    execute(service, msg);
    assertEquals(DEFAULT_PAYLOAD, msg.getContent());

  }


  @Override
  protected MetadataToPayloadService retrieveObjectForSampleConfig() {
    MetadataToPayloadService service = new MetadataToPayloadService("theMetadataKey", MetadataSource.Standard);
    return service;
  }
}
