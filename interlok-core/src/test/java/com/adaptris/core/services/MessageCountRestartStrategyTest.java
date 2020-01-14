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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.DefaultMessageFactory;

public class MessageCountRestartStrategyTest {
  
  private MessageCountRestartStrategy restartStrategy;
  private AdaptrisMessageFactory messageFactory;

  @Before
  public void setUp() throws Exception {
    restartStrategy = new MessageCountRestartStrategy();
    messageFactory = DefaultMessageFactory.getDefaultInstance();
  }

  @Test
  public void testNoMessage() throws Exception {
    assertFalse(restartStrategy.requiresRestart());
  }

  @Test
  public void testSingleMessage() throws Exception {
    restartStrategy.setMaxMessagesCount(1);
    restartStrategy.messageProcessed(messageFactory.newMessage());
    
    assertTrue(restartStrategy.requiresRestart());
  }

  @Test
  public void testMultipleMessagesNotExceedMax() throws Exception {
    restartStrategy.setMaxMessagesCount(5);
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

  @Test
  public void testMultipleMessagesExceedsMax() throws Exception {
    restartStrategy.setMaxMessagesCount(5);
    assertFalse(restartStrategy.requiresRestart());
    
    restartStrategy.messageProcessed(messageFactory.newMessage());
    assertFalse(restartStrategy.requiresRestart());
    restartStrategy.messageProcessed(messageFactory.newMessage());
    assertFalse(restartStrategy.requiresRestart());
    restartStrategy.messageProcessed(messageFactory.newMessage());
    assertFalse(restartStrategy.requiresRestart());
    restartStrategy.messageProcessed(messageFactory.newMessage());
    assertFalse(restartStrategy.requiresRestart());
    restartStrategy.messageProcessed(messageFactory.newMessage());
    assertTrue(restartStrategy.requiresRestart());
  }

  @Test
  public void testMultipleRoundsOfExceedsMax() throws Exception {
    restartStrategy.setMaxMessagesCount(5);
    assertFalse(restartStrategy.requiresRestart());
    
    restartStrategy.messageProcessed(messageFactory.newMessage());
    assertFalse(restartStrategy.requiresRestart());
    restartStrategy.messageProcessed(messageFactory.newMessage());
    assertFalse(restartStrategy.requiresRestart());
    restartStrategy.messageProcessed(messageFactory.newMessage());
    assertFalse(restartStrategy.requiresRestart());
    restartStrategy.messageProcessed(messageFactory.newMessage());
    assertFalse(restartStrategy.requiresRestart());
    restartStrategy.messageProcessed(messageFactory.newMessage());
    assertTrue(restartStrategy.requiresRestart());
    
    restartStrategy.messageProcessed(messageFactory.newMessage());
    assertFalse(restartStrategy.requiresRestart());
    restartStrategy.messageProcessed(messageFactory.newMessage());
    assertFalse(restartStrategy.requiresRestart());
    restartStrategy.messageProcessed(messageFactory.newMessage());
    assertFalse(restartStrategy.requiresRestart());
    restartStrategy.messageProcessed(messageFactory.newMessage());
    assertFalse(restartStrategy.requiresRestart());
    restartStrategy.messageProcessed(messageFactory.newMessage());
    assertTrue(restartStrategy.requiresRestart());
  }

}
