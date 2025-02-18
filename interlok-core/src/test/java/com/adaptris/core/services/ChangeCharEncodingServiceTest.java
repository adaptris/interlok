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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import com.adaptris.core.DefaultMessageFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.GeneralServiceExample;

public class ChangeCharEncodingServiceTest extends GeneralServiceExample {

  private ChangeCharEncodingService srv;
  private AdaptrisMessage msg;

  @BeforeEach
  public void setUp() throws Exception {
    srv = new ChangeCharEncodingService();
    msg = AdaptrisMessageFactory.getDefaultInstance().newMessage("Hello");
  }

  @Override
  protected Object retrieveObjectForSampleConfig() {
    return new ChangeCharEncodingService(StandardCharsets.ISO_8859_1.name());
  }

  @Test
  public void testSetCharEncoding() {
    srv.setCharEncoding(null);
    assertNull(srv.getCharEncoding());
    srv.setCharEncoding("UTF-8");
    assertEquals("UTF-8", srv.getCharEncoding());
    srv.setCharEncoding(null);
    assertEquals(null, srv.getCharEncoding());
  }

  @Test
  public void testChangeCharset() throws Exception {
    srv.setCharEncoding("iso-8859-1");
    assertNull(msg.getContentEncoding());
    execute(srv, msg);
    assertEquals(Charset.forName("iso-8859-1"), Charset.forName(msg.getContentEncoding()));
  }

  @Test
  public void testSetCharEncodingIsNull() throws Exception {
    assertNull(srv.getCharEncoding());
    execute(srv, msg);
    assertNull(msg.getCharEncoding());
  }

  @Test
  public void testSetCharEncodingExpression() throws Exception {
    msg.addMetadata("pn.processing.worker.transform.encoding-target", "UTF-8");

    //Set to expression value
    srv.setCharEncoding("%message{pn.processing.worker.transform.encoding-target}");
    execute(srv, msg);
    assertEquals("UTF-8", msg.getCharEncoding());

    //Change to non-expression value
    srv.setCharEncoding("iso-8859-1");
    execute(srv, msg);
    assertEquals(Charset.forName("iso-8859-1"), Charset.forName(msg.getContentEncoding()));
  }
}
