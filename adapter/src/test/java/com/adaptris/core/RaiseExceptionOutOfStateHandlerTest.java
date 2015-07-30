package com.adaptris.core;

import junit.framework.TestCase;

public class RaiseExceptionOutOfStateHandlerTest extends TestCase {
  
  private DummyComponent component;
  
  public void setUp() throws Exception {
    component = new DummyComponent();
  }
  
  public void tearDown() throws Exception {
  }

  public void testExpectedStateOnInit() {
    RaiseExceptionOutOfStateHandler outOfStateHandler = new RaiseExceptionOutOfStateHandler();
    
    assertEquals(ConfiguredComponentState.STARTED, outOfStateHandler.getCorrectState());
  }
  
  public void testIsInCorrectStateFromNew() throws OutOfStateException {
    RaiseExceptionOutOfStateHandler outOfStateHandler = new RaiseExceptionOutOfStateHandler();
    
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
    RaiseExceptionOutOfStateHandler outOfStateHandler = new RaiseExceptionOutOfStateHandler();
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
  
  public void testHandleInitialised() {
    RaiseExceptionOutOfStateHandler outOfStateHandler = new RaiseExceptionOutOfStateHandler();
    outOfStateHandler.setCorrectState(ConfiguredComponentState.STARTED);
    
    component.changeState(InitialisedState.getInstance());
    try {
      outOfStateHandler.handleOutOfState(component);
      fail("Expected an OutOfStateException");
    } catch (OutOfStateException ex) {
      //expected
    }
  }
  
  public void testHandleStopped() {
    RaiseExceptionOutOfStateHandler outOfStateHandler = new RaiseExceptionOutOfStateHandler();
    outOfStateHandler.setCorrectState(ConfiguredComponentState.STARTED);
    
    component.changeState(StoppedState.getInstance());
    try {
      outOfStateHandler.handleOutOfState(component);
      fail("Expected an OutOfStateException");
    } catch (OutOfStateException ex) {
      //expected
    }
  }
  
  public void testHandleClosed() {
    RaiseExceptionOutOfStateHandler outOfStateHandler = new RaiseExceptionOutOfStateHandler();
    outOfStateHandler.setCorrectState(ConfiguredComponentState.STARTED);
    
    component.changeState(ClosedState.getInstance());
    try {
      outOfStateHandler.handleOutOfState(component);
      fail("Expected an OutOfStateException");
    } catch (OutOfStateException ex) {
      //expected
    }
  }
  
  public void testHandleStarted() throws Exception {
    RaiseExceptionOutOfStateHandler outOfStateHandler = new RaiseExceptionOutOfStateHandler();
    outOfStateHandler.setCorrectState(ConfiguredComponentState.STARTED);
    
    component.changeState(StartedState.getInstance());
    outOfStateHandler.handleOutOfState(component);
  }
  
  
}
