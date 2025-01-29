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

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class AddMetadataExpressionServiceTest extends AddMetadataServiceTest {

  @Override
  AddMetadataService buildService() {
    return new AddMetadataExpressionService();
  }

  @Test
  public void testExpressions() throws Exception {
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage();
    msg.addMetadata("key1", "value1");
    msg.addMetadata("key2", "value2");
    msg.addMetadata("key3", "value3");
    msg.addMetadata("resultKey", "result");
    m1.setKey("%message{resultKey}");
    m1.setValue("%message{key1}, %message{key2}, %message{key3}");

    execute(service, msg);
    assertEquals("value1, value2, value3", msg.getMetadataValue("result"));

  }

}
