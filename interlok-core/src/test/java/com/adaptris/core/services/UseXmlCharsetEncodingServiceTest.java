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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import org.junit.Test;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.DefaultMessageFactory;
import com.adaptris.core.GeneralServiceExample;

public class UseXmlCharsetEncodingServiceTest extends GeneralServiceExample {

  private static final String EXAMPLE_XML = "<?xml version=\"1.0\" encoding=\"ISO-8859-1\" ?><root/>";
  private static final String EXAMPLE_XML_2 = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?><root/>";


  @Override
  protected UseXmlCharsetAsEncodingService retrieveObjectForSampleConfig() {
    return new UseXmlCharsetAsEncodingService();
  }

  @Test
  public void testService() throws Exception {
    AdaptrisMessage msg1 = AdaptrisMessageFactory.getDefaultInstance().newMessage(EXAMPLE_XML);
    assertNull(msg1.getContentEncoding());
    UseXmlCharsetAsEncodingService service = new UseXmlCharsetAsEncodingService();
    execute(service, msg1);
    assertEquals("ISO-8859-1", msg1.getContentEncoding());
    AdaptrisMessage msg2 = new DefaultMessageFactory().newMessage(EXAMPLE_XML_2, "ISO-8859-1");
    assertEquals("ISO-8859-1", msg2.getContentEncoding());
    execute(service, msg2);
    assertEquals("UTF-8", msg2.getContentEncoding());
  }
}
