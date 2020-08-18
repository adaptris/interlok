/*
 * Copyright 2020 Adaptris Ltd.
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
import org.junit.Test;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;

public class MetadataValueEscapeDoubleQuoteTest extends MetadataServiceExample {


  private AdaptrisMessage createMessage() {
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage("The quick brown fox jumps over the lazy dog");
    msg.addMetadata("key", "val\\\"\"ue \" \"\"");
    msg.addMetadata("twoKey", "");
    return msg;
  }

  @Test
  public void testEscapeDoubleQuoteCase() throws Exception {
    MetadataValueEscapeDoubleQuote service = new MetadataValueEscapeDoubleQuote();
    service.setMetadataKeyRegexp("key");
    AdaptrisMessage msg = createMessage();
    execute(service, msg);
    String result = msg.getMetadataValue("key");
    assertEquals("val\\\\\"\\\"ue \\\" \\\"\\\"", result);
  }
  
  @Test
  public void testEscapeDoubleQuoteNullValue() throws Exception {
    MetadataValueEscapeDoubleQuote service = new MetadataValueEscapeDoubleQuote();
    service.setMetadataKeyRegexp("twoKey");
    AdaptrisMessage msg = createMessage();
    execute(service, msg);
    String result = msg.getMetadataValue("twoKey");
    assertEquals("", result);
  }

  @Override
  protected Object retrieveObjectForSampleConfig() {
    MetadataValueEscapeDoubleQuote service = new MetadataValueEscapeDoubleQuote();
    service.setMetadataKeyRegexp(".*MetadataKeyRegularExpression.*");
    return service;
  }
  
}
