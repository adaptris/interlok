package com.adaptris.core;

import java.util.concurrent.TimeUnit;

import com.adaptris.util.TimeInterval;

import junit.framework.TestCase;

public class WaitingOutOfStateHandlerTest extends TestCase {
  
  private DummyComponent component;
  
  public void setUp() throws Exception {
    component = new DummyComponent();
  }

  public void tearDown() throws Exception {
    
  }
  
  public void testExpectedStateOnInit() {
    WaitingOutOfStateHandler outOfStateHandler = new WaitingOutOfStateHandler();
    
    assertEquals(StartedState.getInstance(), outOfStateHandler.getCorrectState().getComponentState());
  }
  
  public void testIsInCorrectStateFromNew() throws OutOfStateException {
    WaitingOutOfStateHandler outOfStateHandler = new WaitingOutOfStateHandler();
    
    component.changeState(ClosedState.getInstance());
    assertFalse(outOfStateHandler.isInCorrectState(component));
    
    component.changeState(InitialisedState.getInstance());
    assertFalse(outOfStateHandler.isInCorrectState(component));
    
    component.changeState(StoppedState.getInstance());
    assertFalse(outOfStateHandler.isInCorrectState(component));
    
    component.changeState(StartedState.getInstance());
    assertTrue(outOfStateHandler.isInCorrectState(component));
  }
  
  public void testIsInCorrectStateModified() throws OutOfStateException {
    WaitingOutOfStateHandler outOfStateHandler = new WaitingOutOfStateHandler();
    outOfStateHandler.setCorrectState(ConfiguredComponentState.INITIALISED);
    
    component.changeState(ClosedState.getInstance());
    assertFalse(outOfStateHandler.isInCorrectState(component));
    
    component.changeState(StartedState.getInstance());
    assertFalse(outOfStateHandler.isInCorrectState(component));
    
    component.changeState(StoppedState.getInstance());
    assertFalse(outOfStateHandler.isInCorrectState(component));
    
    component.changeState(InitialisedState.getInstance());
    assertTrue(outOfStateHandler.isInCorrectState(component));
  }
  
  public void testAlreadyInCorrectState() throws Exception {
    WaitingOutOfStateHandler outOfStateHandler = new WaitingOutOfStateHandler();
    outOfStateHandler.setCorrectState(ConfiguredComponentState.STARTED);
    
    component.changeState(StartedState.getInstance());
    outOfStateHandler.handleOutOfState(component);
  }
  
  public void testExceptionAfterTimeout() throws Exception {
    WaitingOutOfStateHandler outOfStateHandler = new WaitingOutOfStateHandler();
    outOfStateHandler.setCorrectState(ConfiguredComponentState.STARTED);
    outOfStateHandler.setMaximumWaitTime(new TimeInterval(2L, TimeUnit.SECONDS));
    outOfStateHandler.setIntervalToCheck(new TimeInterval(1L, TimeUnit.SECONDS));
    
    component.changeState(InitialisedState.getInstance());
    try {
      outOfStateHandler.handleOutOfState(component);
      fail("Should fail after waiting for 2 seconds");
    } catch (OutOfStateException ex) {
      // expected
    }
  }
  
  public void testNoExceptionNoTimeout() throws Exception {
    WaitingOutOfStateHandler outOfStateHandler = new WaitingOutOfStateHandler();
    outOfStateHandler.setCorrectState(ConfiguredComponentState.STARTED);
    outOfStateHandler.setMaximumWaitTime(new TimeInterval(3L, TimeUnit.SECONDS));
    outOfStateHandler.setIntervalToCheck(new TimeInterval(1L, TimeUnit.SECONDS));

    component.changeState(InitialisedState.getInstance());
    
    // update the state to the correct one before the max time runs out.
    new Thread() {
      public void run () {
        try {
          Thread.sleep(1000);
          component.changeState(StartedState.getInstance());
        } catch (InterruptedException ex) {}
      }
      
    }.start();
    
    assertEquals(InitialisedState.getInstance(), component.retrieveComponentState());
    
    outOfStateHandler.handleOutOfState(component);

    assertEquals(StartedState.getInstance(), component.retrieveComponentState());
  }
 
}
