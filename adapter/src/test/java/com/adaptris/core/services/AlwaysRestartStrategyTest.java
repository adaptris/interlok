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
