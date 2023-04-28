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

package com.adaptris.core;

import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;

import com.adaptris.core.stubs.MockChannel;
import com.adaptris.core.stubs.MockMessageProducer;
import com.adaptris.util.TimeInterval;

public class RandomIntervalPollerTest extends com.adaptris.interlok.junit.scaffolding.BaseCase {

  private long tenMillis = 10L;
  private long hundredMillis = 100L;
  private int listenerCount = 1;

  @Test
  public void testSetConstructors() throws Exception {
    RandomIntervalPoller p = new RandomIntervalPoller();
    p = new RandomIntervalPoller(new TimeInterval(tenMillis, TimeUnit.SECONDS));
  }

  @Test
  public void testLifecycle() throws Exception {
    PollingTrigger consumer = new PollingTrigger();
    consumer.setPoller(new RandomIntervalPoller(new TimeInterval(hundredMillis, TimeUnit.MILLISECONDS)));
    MockMessageProducer producer = new MockMessageProducer();

    MockChannel channel = new MockChannel();
    StandardWorkflow workflow = new StandardWorkflow();
    workflow.setConsumer(consumer);
    workflow.setProducer(producer);
    channel.getWorkflowList().add(workflow);
    try {
      channel.requestClose();
      channel.requestStart();
      waitForMessages(producer, listenerCount);

      channel.requestStop();
      producer.getMessages().clear();

      channel.requestStart();
      waitForMessages(producer, listenerCount);

      channel.requestClose();
      producer.getMessages().clear();

      channel.requestStart();
      waitForMessages(producer, listenerCount);
    }
    finally {
      channel.requestClose();
    }
  }

}
