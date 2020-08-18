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
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import org.junit.Test;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.GeneralServiceExample;

public class ChangeCharEncodingServiceTest extends GeneralServiceExample {

  @Override
  protected Object retrieveObjectForSampleConfig() {
    return new ChangeCharEncodingService(StandardCharsets.ISO_8859_1.name());
  }

  @Test
  public void testSetCharEncoding() {
    ChangeCharEncodingService srv = new ChangeCharEncodingService();
    assertNull(srv.getCharEncoding());
    srv.setCharEncoding("UTF-8");
    assertEquals("UTF-8", srv.getCharEncoding());
    srv.setCharEncoding(null);
    assertEquals(null, srv.getCharEncoding());
  }

  @Test
  public void testChangeCharset() throws Exception {
    ChangeCharEncodingService srv = new ChangeCharEncodingService();
    srv.setCharEncoding("iso-8859-1");
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage("Hello");
    assertNull(msg.getContentEncoding());
    execute(srv, msg);
    assertEquals(Charset.forName("iso-8859-1"), Charset.forName(msg.getContentEncoding()));
  }

}
