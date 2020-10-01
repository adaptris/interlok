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

package com.adaptris.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;
import org.junit.Test;
import com.adaptris.core.stubs.MockMessageProducer;


@SuppressWarnings("deprecation")
public class AdaptrisMessageWorkerImpTest extends com.adaptris.interlok.junit.scaffolding.BaseCase {

  public AdaptrisMessageWorkerImpTest() {
    super();
  }

  @Test
  public void testHandleConnectionException() throws Exception {
    MockMessageProducer worker = new MockMessageProducer();
    NullConnection conn = new NullConnection();

    StandaloneProducer p = new StandaloneProducer(conn, worker);
    start(p);
    try {
      worker.handleConnectionException();
      stop(p);
      conn.setConnectionErrorHandler(new NullConnectionErrorHandler());
      start(p);
      worker.handleConnectionException();
    }
    finally {
      stop(p);
    }
  }

  @Test
  public void testIsTrackingEndpoint() throws Exception {
    MockMessageProducer worker = new MockMessageProducer();
    assertFalse(worker.isTrackingEndpoint());
    worker.setIsTrackingEndpoint(Boolean.FALSE);
    assertEquals(Boolean.FALSE, worker.getIsTrackingEndpoint());
    assertFalse(worker.isTrackingEndpoint());
    worker.setIsTrackingEndpoint(Boolean.TRUE);
    assertTrue(worker.isTrackingEndpoint());
  }

  @Test
  public void testCreateName() throws Exception {
    MockMessageProducer worker = new MockMessageProducer();
    assertEquals(MockMessageProducer.class.getName(), worker.createName());
  }

  @Test
  public void testToString() throws Exception {
    MockMessageProducer worker = new MockMessageProducer();
    worker.setEncoder(new MimeEncoder());
    assertNotNull(worker.toString());
    worker.setEncoder(null);
    assertNotNull(worker.toString());
  }

  @Test
  public void testEncodeDecodeWithEncoder() throws Exception {
    MockMessageProducer worker = new MockMessageProducer();
    MimeEncoder encoder = new MimeEncoder();
    encoder.setRetainUniqueId(true);
    worker.setEncoder(encoder);
    assertEquals(encoder, worker.getEncoder());
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage("XXXX");
    msg.addMetadata(new MetadataElement("key", "data"));
    byte[] bytes = worker.encode(msg);
    AdaptrisMessage msg2 = worker.decode(bytes);
    assertEquals(msg.getUniqueId(), msg2.getUniqueId());
    assertEquals(msg.getContent(), msg2.getContent());
    assertTrue(msg2.containsKey("key"));
    assertEquals("data", msg2.getMetadataValue("key"));
  }

  @Test
  public void testEncodeDecodeWithoutEncoder() throws Exception {
    MockMessageProducer worker = new MockMessageProducer();
    AdaptrisMessageFactory fact = new DefaultMessageFactory();
    fact.setDefaultCharEncoding("UTF-8");
    worker.setMessageFactory(fact);
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage("XXXX");
    msg.addMetadata(new MetadataElement("key", "data"));
    byte[] bytes = worker.encode(msg);
    AdaptrisMessage msg2 = worker.decode(bytes);
    assertNotSame(msg.getUniqueId(), msg2.getUniqueId());
    assertEquals(msg.getContent(), msg2.getContent());
    assertFalse(msg2.containsKey("key"));
  }

}
