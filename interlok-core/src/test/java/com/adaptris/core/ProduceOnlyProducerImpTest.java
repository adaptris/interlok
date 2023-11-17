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

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.adaptris.core.stubs.MockMessageProducer;
import com.adaptris.core.util.LifecycleHelper;

public class ProduceOnlyProducerImpTest {

  private MockMessageProducer createAndStart() throws Exception {
    return LifecycleHelper.initAndStart(new MockMessageProducer());
  }

  @Test
  public void testProduce_Message() throws Exception {
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage("dummy");
    MockMessageProducer mock = createAndStart();
    try {
      mock.produce(msg);
      assertEquals(1, mock.getMessages().size());
    } finally {
      LifecycleHelper.stopAndClose(mock);
    }
  }

  @Test
  public void testProduce_Message_ProduceDestination() throws Exception {
    MockMessageProducer mock = createAndStart();
    try {
      AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage("dummy");
      mock.produce(msg);
      assertEquals(1, mock.getMessages().size());
    } finally {
      LifecycleHelper.stopAndClose(mock);
    }
  }

  @Test
  public void testRequest_Message() throws Exception {
    Assertions.assertThrows(UnsupportedOperationException.class, () -> {
      AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage("dummy");
      MockMessageProducer mock = createAndStart();
      try {
        mock.request(msg);
      } finally {
        LifecycleHelper.stopAndClose(mock);
      }
    });
  }

  @Test
  public void testRequest_Message_Long() throws Exception {
    Assertions.assertThrows(UnsupportedOperationException.class, () -> {
      AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage("dummy");
      MockMessageProducer mock = createAndStart();
      try {
        mock.request(msg, 100L);
      } finally {
        LifecycleHelper.stopAndClose(mock);
      }
    });
  }

  @Test
  public void testRequest_Message_Destination() throws Exception {
    Assertions.assertThrows(UnsupportedOperationException.class, () -> {
      AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage("dummy");
      MockMessageProducer mock = createAndStart();
      try {
        mock.request(msg);
      } finally {
        LifecycleHelper.stopAndClose(mock);
      }
    });
  }

  @Test
  public void testRequest_Message_Destination_Long() throws Exception {
    Assertions.assertThrows(UnsupportedOperationException.class, () -> {
      AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage("dummy");
      MockMessageProducer mock = createAndStart();
      try {
        mock.request(msg, 100L);
      } finally {
        LifecycleHelper.stopAndClose(mock);
      }
    });
  }
}
