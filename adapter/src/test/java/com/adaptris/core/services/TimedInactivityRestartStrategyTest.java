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

import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.DefaultMessageFactory;
import com.adaptris.util.TimeInterval;

import junit.framework.TestCase;

public class TimedInactivityRestartStrategyTest extends TestCase {

  private TimedInactivityRestartStrategy restartStrategy;
  private AdaptrisMessageFactory messageFactory;
  
  public void setUp() throws Exception {
    restartStrategy = new TimedInactivityRestartStrategy();
    messageFactory = DefaultMessageFactory.getDefaultInstance();
  }
  
  public void tearDown() throws Exception {
  }
  
  public void testNoMessage() throws Exception {
    assertFalse(restartStrategy.requiresRestart());
  }
  
  public void testDoesNotExceedTimeout() throws Exception {
    restartStrategy.setInactivityPeriod(new TimeInterval(5L, "SECONDS"));
    
    restartStrategy.messageProcessed(messageFactory.newMessage());
    assertFalse(restartStrategy.requiresRestart());
    
    restartStrategy.messageProcessed(messageFactory.newMessage());
    assertFalse(restartStrategy.requiresRestart());
    
    restartStrategy.messageProcessed(messageFactory.newMessage());
    assertFalse(restartStrategy.requiresRestart());
    
    restartStrategy.messageProcessed(messageFactory.newMessage());
    assertFalse(restartStrategy.requiresRestart());
    
    restartStrategy.messageProcessed(messageFactory.newMessage());
    assertFalse(restartStrategy.requiresRestart());
  }
  
  public void testExceedsTimeoutNoMessage() throws Exception {
    restartStrategy.setInactivityPeriod(new TimeInterval(1L, "SECONDS"));
    assertFalse(restartStrategy.requiresRestart());
    
    try {
      Thread.sleep(1500);
    } catch (Exception ex) {
    }
    
    assertTrue(restartStrategy.requiresRestart());
  }
  
  public void testDoesNotExceedsTimeoutSteadyStreamOfMessage() throws Exception {
    restartStrategy.setInactivityPeriod(new TimeInterval(1L, "SECONDS"));
    assertFalse(restartStrategy.requiresRestart());
    
    // Test will last 2 seconds
    for(int counter = 0; counter < 10; counter ++) {
      restartStrategy.messageProcessed(messageFactory.newMessage());
      assertFalse(restartStrategy.requiresRestart());
      try {
        Thread.sleep(200);
      } catch (Exception ex) {
      }
    }
    
    assertFalse(restartStrategy.requiresRestart());
  }
  
  public void testExceedsTimeoutSteadyStreamOfMessage() throws Exception {
    restartStrategy.setInactivityPeriod(new TimeInterval(1L, "SECONDS"));
    assertFalse(restartStrategy.requiresRestart());
    
    // Test will last 2 seconds
    for(int counter = 0; counter < 10; counter ++) {
      restartStrategy.messageProcessed(messageFactory.newMessage());
      assertFalse(restartStrategy.requiresRestart());
      try {
        Thread.sleep(200);
      } catch (Exception ex) {
      }
    }
    
    try {
      Thread.sleep(1500);
    } catch (Exception ex) {
    }
    
    assertTrue(restartStrategy.requiresRestart());
  }
}
