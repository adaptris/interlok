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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.adaptris.util.TimeInterval;

public class WaitingOutOfStateHandlerTest {
  
  private DummyComponent component;
  
  @BeforeEach
  public void setUp() throws Exception {
    component = new DummyComponent();
  }

  @Test
  public void testExpectedStateOnInit() {
    WaitingOutOfStateHandler outOfStateHandler = new WaitingOutOfStateHandler();
    
    assertEquals(StartedState.getInstance(), outOfStateHandler.getCorrectState().getComponentState());
  }

  @Test
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

  @Test
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

  @Test
  public void testAlreadyInCorrectState() throws Exception {
    WaitingOutOfStateHandler outOfStateHandler = new WaitingOutOfStateHandler();
    outOfStateHandler.setCorrectState(ConfiguredComponentState.STARTED);
    
    component.changeState(StartedState.getInstance());
    outOfStateHandler.handleOutOfState(component);
  }

  @Test
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

  @Test
  public void testNoExceptionNoTimeout() throws Exception {
    WaitingOutOfStateHandler outOfStateHandler = new WaitingOutOfStateHandler();
    outOfStateHandler.setCorrectState(ConfiguredComponentState.STARTED);
    outOfStateHandler.setMaximumWaitTime(new TimeInterval(3L, TimeUnit.SECONDS));
    outOfStateHandler.setIntervalToCheck(new TimeInterval(1L, TimeUnit.SECONDS));

    component.changeState(InitialisedState.getInstance());
    
    // update the state to the correct one before the max time runs out.
    new Thread() {
      @Override
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
