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

import static org.junit.Assert.assertNotSame;
import org.junit.Test;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;

public class Base64MetadataEncodeTest extends MetadataServiceExample {

  private static final String METADATA_KEY = "key";
  private static final String METADATA_VALUE = "Hello World";


  private AdaptrisMessage createMessage(String encoding) throws Exception {
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage("asdfghjk", encoding);
    msg.addMetadata(METADATA_KEY, METADATA_VALUE);
    msg.addMetadata("yetAnotherKey", "");
    return msg;
  }

  @Test
  public void testService() throws Exception {
    Base64EncodeMetadataService service = new Base64EncodeMetadataService(METADATA_KEY);
    AdaptrisMessage msg = createMessage(null);
    execute(service, msg);
    assertNotSame(METADATA_VALUE, msg.getMetadataValue(METADATA_KEY));
  }

  @Test
  public void testService_Encoding() throws Exception {
    Base64EncodeMetadataService service = new Base64EncodeMetadataService(METADATA_KEY);
    AdaptrisMessage msg = createMessage("UTF-8");
    execute(service, msg);
    assertNotSame(METADATA_VALUE, msg.getMetadataValue(METADATA_KEY));
  }

  @Override
  protected Object retrieveObjectForSampleConfig() {
    return new Base64EncodeMetadataService(".*MetadataKeyRegularExpression.*");
  }
}
