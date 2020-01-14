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
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import org.junit.Test;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.ServiceException;

@SuppressWarnings("deprecation")
public class StringToHexServiceTest extends MetadataServiceExample {


  private static AdaptrisMessage createMessage() {
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage();
    msg.addMetadata(HexToStringServiceTest.SOURCE_METADATA_KEY, HexToStringServiceTest.PLAIN_TEXT);
    return msg;
  }

  @Override
  public boolean isAnnotatedForJunit4() {
    return true;
  }

  @Test
  public void testService() throws Exception {
    StringToHexService service = new StringToHexService();
    service.setCharset(HexToStringService.UTF_8);
    service.setMetadataKeyRegexp(HexToStringServiceTest.SOURCE_METADATA_KEY);
    AdaptrisMessage msg = createMessage();
    execute(service, msg);
    assertTrue(msg.containsKey(HexToStringServiceTest.SOURCE_METADATA_KEY));
    assertEquals(HexToStringServiceTest.HEX_TEXT, msg.getMetadataValue(HexToStringServiceTest.SOURCE_METADATA_KEY));
  }

  @Test
  public void testService_BadEncodingChoice() throws Exception {
    StringToHexService service = new StringToHexService();
    service.setCharset("RandomEncoding!");
    service.setMetadataKeyRegexp(HexToStringServiceTest.SOURCE_METADATA_KEY);
    AdaptrisMessage msg = createMessage();
    try {
      execute(service, msg);
      fail();
    }
    catch (ServiceException expected) {

    }
  }

  @Test
  public void testService_NoEncoding() throws Exception {
    StringToHexService service = new StringToHexService();
    service.setMetadataKeyRegexp(HexToStringServiceTest.SOURCE_METADATA_KEY);
    AdaptrisMessage msg = createMessage();
    execute(service, msg);
    assertTrue(msg.containsKey(HexToStringServiceTest.SOURCE_METADATA_KEY));
    assertEquals(HexToStringServiceTest.HEX_TEXT, msg.getMetadataValue(HexToStringServiceTest.SOURCE_METADATA_KEY));
  }

  @Override
  protected Object retrieveObjectForSampleConfig() {
    return new StringToHexService(".*Matching_MetadataKeys.*");
  }

  @Override
  protected String getExampleCommentHeader(Object o) {
    return super.getExampleCommentHeader(o) + "<!-- This is used to reformat strings stored in metadata.\n"
        + "It takes the metadata value and converts it from string into its hex representation\n" + "-->\n";
  }
}
