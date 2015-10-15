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

import com.adaptris.core.BaseCase;
import com.adaptris.core.ConfiguredConsumeDestination;
import com.adaptris.core.CoreException;
import com.adaptris.core.PollingTrigger;
import com.adaptris.util.TimeInterval;

public class FsImmediateEventPollerTest extends BaseCase {
  
  public FsImmediateEventPollerTest(String name) {
    super(name);
  }

  private FsConsumer consumer;
  private FsImmediateEventPoller poller;
  
  public void setUp() throws Exception {
    consumer = new FsConsumer();
    consumer.setDestination(new ConfiguredConsumeDestination("."));
    consumer.setCreateDirs(false);
    consumer.setQuietInterval(new TimeInterval(500L, "MILLISECONDS"));
    poller = new FsImmediateEventPoller();
    
    consumer.setPoller(poller);
  }
  
  public void tearDown() throws Exception {
    
  }
  
  public void testStandardInitNoExc() throws Exception {
    consumer.init();
  }
  
  public void testNoQuitePeriod() throws Exception {
    try {
      consumer.setQuietInterval(null);
      consumer.init();
      fail("Should fail with a null QuietPeriod");
    } catch (CoreException ex) {
      //expected.
    }
  }
  
  public void testWrongConsumerType() throws Exception {
    try {
      PollingTrigger ptConsumer = new PollingTrigger();
      ptConsumer.setPoller(poller);
      ptConsumer.init();
      fail("Should fail, consumer is not a");
    } catch (CoreException ex) {
      //expected.
    }
  }

}
