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

import junit.framework.TestCase;

public class AlwaysRestartStrategyTest extends TestCase {
  
  private RestartStrategy restartStrategy;
  private AdaptrisMessageFactory messageFactory;
  
  public void setUp() throws Exception {
    restartStrategy = new AlwaysRestartStrategy();
    messageFactory = DefaultMessageFactory.getDefaultInstance();
  }
  
  public void tearDown() throws Exception {
  }
  
  public void testNoMessage() throws Exception {
    assertTrue(restartStrategy.requiresRestart());
  }
  
  public void testSingleMessage() throws Exception {
    restartStrategy.messageProcessed(messageFactory.newMessage());
    
    assertTrue(restartStrategy.requiresRestart());
  }
  
  public void testMultipleMessages() throws Exception {
    assertTrue(restartStrategy.requiresRestart());
    restartStrategy.messageProcessed(messageFactory.newMessage());
    assertTrue(restartStrategy.requiresRestart());
    restartStrategy.messageProcessed(messageFactory.newMessage());
    assertTrue(restartStrategy.requiresRestart());
  }

}
