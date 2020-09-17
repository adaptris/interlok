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
import static org.junit.Assert.assertNull;
import java.util.concurrent.TimeUnit;
import org.junit.Test;
import com.adaptris.core.stubs.MockChannel;
import com.adaptris.core.stubs.MockMessageProducer;
import com.adaptris.util.TimeInterval;

public class FixedIntervalPollerTest extends com.adaptris.interlok.junit.scaffolding.BaseCase {

  @Test
  public void testSetConstructors() throws Exception {
    FixedIntervalPoller p = new FixedIntervalPoller();
    p = new FixedIntervalPoller(new TimeInterval(10L, TimeUnit.SECONDS));
  }

  @Test
  public void testSetPollInterval() throws Exception {
    TimeInterval defaultInterval = new TimeInterval(20L, TimeUnit.SECONDS);
    TimeInterval interval = new TimeInterval(60L, TimeUnit.SECONDS);

    FixedIntervalPoller p = new FixedIntervalPoller();

    assertNull(p.getPollInterval());
    assertEquals(defaultInterval.toMilliseconds(), p.pollInterval());

    p.setPollInterval(interval);
    assertEquals(interval, p.getPollInterval());
    assertEquals(interval.toMilliseconds(), p.pollInterval());

    p.setPollInterval(null);
    assertNull(p.getPollInterval());
    assertEquals(defaultInterval.toMilliseconds(), p.pollInterval());

  }

  @Test
  public void testLifecycle() throws Exception {
    PollingTrigger consumer = new PollingTrigger();
    consumer.setPoller(new FixedIntervalPoller(new TimeInterval(100L, TimeUnit.MILLISECONDS)));
    MockMessageProducer producer = new MockMessageProducer();

    MockChannel channel = new MockChannel();
    StandardWorkflow workflow = new StandardWorkflow();
    workflow.setConsumer(consumer);
    workflow.setProducer(producer);
    channel.getWorkflowList().add(workflow);
    try {
      channel.requestClose();
      channel.requestStart();
      waitForMessages(producer, 1);

      channel.requestStop();
      producer.getMessages().clear();

      channel.requestStart();
      waitForMessages(producer, 1);

      channel.requestClose();
      producer.getMessages().clear();

      channel.requestStart();
      waitForMessages(producer, 1);
    }
    finally {
      channel.requestClose();
    }
  }
}
