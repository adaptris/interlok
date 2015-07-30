package com.adaptris.core.services;

import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.DefaultMessageFactory;

import junit.framework.TestCase;

public class MessageCountRestartStrategyTest extends TestCase {
  
  private MessageCountRestartStrategy restartStrategy;
  private AdaptrisMessageFactory messageFactory;
  
  public void setUp() throws Exception {
    restartStrategy = new MessageCountRestartStrategy();
    messageFactory = DefaultMessageFactory.getDefaultInstance();
  }
  
  public void tearDown() throws Exception {
  }

  public void testNoMessage() throws Exception {
    assertFalse(restartStrategy.requiresRestart());
  }
  
  public void testSingleMessage() throws Exception {
    restartStrategy.setMaxMessagesCount(1);
    restartStrategy.messageProcessed(messageFactory.newMessage());
    
    assertTrue(restartStrategy.requiresRestart());
  }
  
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
