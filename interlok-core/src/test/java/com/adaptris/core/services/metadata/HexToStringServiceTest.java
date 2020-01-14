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
public class HexToStringServiceTest extends MetadataServiceExample {

  public static final String PLAIN_TEXT = "hello world";
  public static final String HEX_TEXT = "68656c6c6f20776f726c64";

  public static final String SOURCE_METADATA_KEY = "sourceMetadataKey";
  public static final String BAD_METADATA_KEY = "badMetadataKey";

  @Override
  public boolean isAnnotatedForJunit4() {
    return true;
  }

  private static AdaptrisMessage createMessage() {
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage();
    msg.addMetadata(SOURCE_METADATA_KEY, HEX_TEXT);
    msg.addMetadata(BAD_METADATA_KEY, "SomethingThatIsn'tHex");
    return msg;
  }

  @Test
  public void testService() throws Exception {
    HexToStringService service = new HexToStringService();
    service.setCharset("UTF-8");
    service.setMetadataKeyRegexp(SOURCE_METADATA_KEY);
    AdaptrisMessage msg = createMessage();
    execute(service, msg);
    assertTrue(msg.containsKey(SOURCE_METADATA_KEY));
    assertEquals(PLAIN_TEXT, msg.getMetadataValue(SOURCE_METADATA_KEY));
  }

  @Test
  public void testService_NotHex() throws Exception {
    HexToStringService service = new HexToStringService();
    service.setCharset("UTF-8");
    service.setMetadataKeyRegexp(BAD_METADATA_KEY);
    AdaptrisMessage msg = createMessage();
    try {
      execute(service, msg);
      fail();
    }
    catch (ServiceException expected) {

    }
  }

  @Test
  public void testService_BadEncodingChoice() throws Exception {
    HexToStringService service = new HexToStringService();
    service.setCharset("RandomEncoding!");
    service.setMetadataKeyRegexp(SOURCE_METADATA_KEY);
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
    HexToStringService service = new HexToStringService();
    service.setMetadataKeyRegexp(SOURCE_METADATA_KEY);
    AdaptrisMessage msg = createMessage();
    execute(service, msg);
    assertTrue(msg.containsKey(SOURCE_METADATA_KEY));
    assertEquals(PLAIN_TEXT, msg.getMetadataValue(SOURCE_METADATA_KEY));
  }

  @Override
  protected Object retrieveObjectForSampleConfig() {
    return new HexToStringService(".*Matching_MetadataKeys_With_HexValues.*");
  }

  @Override
  protected String getExampleCommentHeader(Object o) {
    return super.getExampleCommentHeader(o) + "<!-- This is used to reformat strings stored in metadata.\n"
        + "It takes the metadata value and converts it from hex into its string representation\n"
        + "Note that while the conversion is strictly correct (depending on your encoding choice),\n"
        + "depending on the hex value, you might end up with a String that may not be valid for \n" + "some systems.\n"
        + "For instance it might have a reserved character, and not be valid as JMS metadata;\n"
        + "It might contain a vertical tab which would not be valid for insertion into XML.\n" + "-->\n";
  }
}
