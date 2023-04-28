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

package com.adaptris.core.fs;

import static org.junit.jupiter.api.Assertions.fail;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.adaptris.core.CoreException;
import com.adaptris.core.PollingTrigger;
import com.adaptris.util.TimeInterval;

public class FsImmediateEventPollerTest {

  private FsConsumer consumer;
  private FsImmediateEventPoller poller;

  @BeforeEach
  public void setUp() throws Exception {
    consumer = new FsConsumer();
    consumer.setBaseDirectoryUrl(".");
    consumer.setCreateDirs(false);
    consumer.setQuietInterval(new TimeInterval(500L, "MILLISECONDS"));
    poller = new FsImmediateEventPoller();

    consumer.setPoller(poller);
  }

  public void tearDown() throws Exception {

  }

  @Test
  public void testStandardInitNoExc() throws Exception {
    consumer.init();
  }

  @Test
  public void testNoQuietPeriod() throws Exception {
    try {
      consumer.setQuietInterval(null);
      consumer.init();
      fail("Should fail with a null QuietPeriod");
    } catch (CoreException ex) {
      //expected.
    }
  }

  @Test
  public void testWrongConsumerType() throws Exception {
    try {
      PollingTrigger ptConsumer = new PollingTrigger();
      ptConsumer.setPoller(poller);
      ptConsumer.init();
      fail("Should fail, consumer is not a");
    } catch (Exception ex) {
      //expected.
    }
  }

}
