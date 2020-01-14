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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import org.junit.Before;
import org.junit.Test;

public class RaiseExceptionOutOfStateHandlerTest {
  
  private DummyComponent component;

  @Before
  public void setUp() throws Exception {
    component = new DummyComponent();
  }


  @Test
  public void testExpectedStateOnInit() {
    RaiseExceptionOutOfStateHandler outOfStateHandler = new RaiseExceptionOutOfStateHandler();
    
    assertEquals(ConfiguredComponentState.STARTED, outOfStateHandler.getCorrectState());
  }

  @Test
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

  @Test
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

  @Test
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

  @Test
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

  @Test
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

  @Test
  public void testHandleStarted() throws Exception {
    RaiseExceptionOutOfStateHandler outOfStateHandler = new RaiseExceptionOutOfStateHandler();
    outOfStateHandler.setCorrectState(ConfiguredComponentState.STARTED);
    
    component.changeState(StartedState.getInstance());
    outOfStateHandler.handleOutOfState(component);
  }
  
  
}
