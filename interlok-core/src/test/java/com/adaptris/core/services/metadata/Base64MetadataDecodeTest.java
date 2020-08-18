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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;
import org.junit.Test;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.util.EncodingHelper.Base64Encoding;
import com.adaptris.util.text.Base64ByteTranslator;

public class Base64MetadataDecodeTest extends MetadataServiceExample {

  private static final String METADATA_KEY = "key";


  @Test
  public void testService() throws Exception {
    Base64DecodeMetadataService service = new Base64DecodeMetadataService(METADATA_KEY).withStyle(Base64Encoding.BASIC);
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage("asdfghjk");

    String metadataValue = new Base64ByteTranslator().translate("Hello World".getBytes());
    msg.addMetadata(METADATA_KEY, metadataValue);

    execute(service, msg);
    assertNotSame(metadataValue, msg.getMetadataValue(METADATA_KEY));
    assertEquals("Hello World", msg.getMetadataValue(METADATA_KEY));
  }

  @Test
  public void testService_Encoding() throws Exception {
    Base64DecodeMetadataService service = new Base64DecodeMetadataService(METADATA_KEY);
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage("asdfghjk", "UTF-8");
    String metadataValue = new Base64ByteTranslator().translate("Hello World".getBytes("UTF-8"));
    msg.addMetadata(METADATA_KEY, metadataValue);

    execute(service, msg);

    assertNotSame(metadataValue, msg.getMetadataValue(METADATA_KEY));
    assertEquals("Hello World", msg.getMetadataValue(METADATA_KEY));
  }

  @Override
  protected Object retrieveObjectForSampleConfig() {
    return new Base64DecodeMetadataService(".*MetadataKeyRegularExpression.*");
  }
}
