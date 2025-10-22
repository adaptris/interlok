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

import com.adaptris.core.stubs.*;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

public class ConnectionErrorHandlingProducerTest extends com.adaptris.interlok.junit.scaffolding.BaseCase {

  public ConnectionErrorHandlingProducerTest() {

  }

  @Test
  public void testDelegate() throws Exception {
    ConnectionErrorHandlingProducer a = new ConnectionErrorHandlingProducer();
    NullMessageProducer p = spy(new NullMessageProducer());
    a.setDelegate(p);
    assertEquals(p, a.getDelegate());

    p.setEncoder(mock(AdaptrisMessageEncoder.class));
    p.setMessageFactory(mock(AdaptrisMessageFactory.class));
    p.setUniqueId("id");
    p.setIsTrackingEndpoint(true);


    assertEquals(p.getEncoder(), a.getEncoder());
    assertEquals(p.getMessageFactory(), a.getMessageFactory());
    assertEquals(p.getUniqueId(), a.getUniqueId());
    assertEquals(p.isTrackingEndpoint(), a.isTrackingEndpoint());
    assertEquals(p.createName(), a.createName());
    assertEquals(p.createQualifier(), a.createQualifier());

    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage();
    byte[] bytes = new byte[0];
    a.decode(bytes);
    verify(p).decode(ArgumentMatchers.eq(bytes));

    a.encode(msg);
    verify(p).encode((ArgumentMatchers.eq(msg)));

    AdaptrisConnection c = mock(AdaptrisConnection.class);
    a.registerConnection(c);
    assertEquals(p.retrieveConnection(AdaptrisConnection.class), a.retrieveConnection(AdaptrisConnection.class));

    a.request(msg);
    verify(p).request(ArgumentMatchers.eq(msg));

    a.produce(msg);
    verify(p).produce(ArgumentMatchers.eq(msg));
  }
}
