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

package com.adaptris.core.services;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.DefaultMessageFactory;
import com.adaptris.core.GeneralServiceExample;
import com.adaptris.core.MetadataElement;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;


public class MessageUniqueIdServiceTest extends GeneralServiceExample {


  @Test
  public void testSettingAndResolvingUniqueId() throws Exception {
    String MSG_ID = "%message{message-id}";
    String newUniqueId = UUID.randomUUID().toString();
    AdaptrisMessage msg = new DefaultMessageFactory().newMessage();
    msg.addMetadata(new MetadataElement("message-id", newUniqueId));
    String originalUniqueId = msg.getUniqueId();
    MessageUniqueIdService service = new MessageUniqueIdService();
    service.setGenerate(false);
    service.setMessageUniqueId(MSG_ID);

    // test resolving
    execute(service, msg);
    assertNotNull(msg.getUniqueId());
    assertNotEquals(msg.getUniqueId(), originalUniqueId);
    assertEquals(msg.getUniqueId(), newUniqueId);

    // test setting
    newUniqueId = UUID.randomUUID().toString();
    service.setMessageUniqueId(newUniqueId);
    execute(service, msg);
    assertNotNull(msg.getUniqueId());
    assertNotEquals(msg.getUniqueId(), originalUniqueId);
    assertEquals(msg.getUniqueId(), newUniqueId);

  }

  @Test
  public void testGenerateUniqueId() throws Exception {
    final String MSG_ID = "test";
    AdaptrisMessage msg = new DefaultMessageFactory().newMessage();
    String originalUniqueId = msg.getUniqueId();
    MessageUniqueIdService service = new MessageUniqueIdService();
    service.setGenerate(true);
    service.setMessageUniqueId(MSG_ID);
    execute(service, msg);
    assertNotNull(msg.getUniqueId());
    assertNotEquals(msg.getUniqueId(), originalUniqueId);
    assertNotEquals(msg.getUniqueId(), MSG_ID);
  }

  @Override
  protected Object retrieveObjectForSampleConfig() {
    return new MessageUniqueIdService();
  }

}
