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
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import org.junit.Test;
import com.adaptris.core.stubs.MockConnection;


public class StartedStateTest extends ComponentStateCase {

  public StartedStateTest() {
  }

  @Override
  public boolean isAnnotatedForJunit4() {
    return true;
  }

  @Test
  public void testInstance() throws Exception {
    StartedState one = StartedState.getInstance();
    assertTrue(one == StartedState.getInstance());
    assertEquals(StartedState.getInstance(), one);
    assertEquals(StartedState.class.getSimpleName(), one.toString());
  }

  @Test
  public void testStarted_To_Initialised() throws Exception {
    MockConnection component = new MockConnection();
    component.requestStart();
    StartedState state = StartedState.getInstance();
    state.requestInit(component);
    // redmineID #4453 - Started won't let you goto Initialised.
    assertEquals(StartedState.getInstance(), component.retrieveComponentState());
    assertEquals(1, component.getInitCount());
    assertEquals(1, component.getStartCount());
    assertEquals(0, component.getStopCount());
    assertEquals(0, component.getCloseCount());
  }

  @Test
  public void testStarted_To_Started() throws Exception {
    MockConnection component = new MockConnection();
    component.requestStart();

    StartedState state = StartedState.getInstance();
    state.requestStart(component);
    // This will do nothing.
    assertEquals(StartedState.getInstance(), component.retrieveComponentState());
    assertEquals(1, component.getInitCount());
    assertEquals(1, component.getStartCount());
    assertEquals(0, component.getStopCount());
    assertEquals(0, component.getCloseCount());
  }

  @Test
  public void testStarted_To_Stopped() throws Exception {
    MockConnection component = new MockConnection();
    component.requestStart();

    StartedState state = StartedState.getInstance();
    state.requestStop(component);
    assertEquals(StoppedState.getInstance(), component.retrieveComponentState());
    assertEquals(1, component.getInitCount());
    assertEquals(1, component.getStartCount());
    assertEquals(1, component.getStopCount());
    assertEquals(0, component.getCloseCount());
  }

  @Test
  public void testStarted_To_Closed() throws Exception {
    MockConnection component = new MockConnection();
    component.requestStart();

    StartedState state = StartedState.getInstance();
    state.requestClose(component);
    assertEquals(ClosedState.getInstance(), component.retrieveComponentState());
    assertEquals(1, component.getInitCount());
    assertEquals(1, component.getStartCount());
    assertEquals(1, component.getStopCount());
    assertEquals(1, component.getCloseCount());
  }

  @Test
  public void testRestart() throws Exception {
    MockConnection component = new MockConnection();
    component.requestStart();
    StartedState state = StartedState.getInstance();
    state.requestRestart(component);
    assertEquals(StartedState.getInstance(), component.retrieveComponentState());
    assertEquals(2, component.getInitCount());
    assertEquals(2, component.getStartCount());
    assertEquals(1, component.getStopCount());
    assertEquals(1, component.getCloseCount());
  }

  @Override
  protected ComponentState createState() {
    return StartedState.getInstance();
  }

  @Override
  protected ConnOperator createOperator(MockConnection c) {
    return new Starter(c);
  }

  @Override
  protected void verifyOperation(MockConnection c) {
    assertEquals(1, c.getInitCount());
    assertEquals(1, c.getStartCount());
    assertEquals(0, c.getStopCount());
    assertEquals(0, c.getCloseCount());
  }

  @Override
  protected void prepareConn(MockConnection c) throws Exception {
    // nothing to do.
  }

  private class Starter extends ConnOperator {

    protected Starter(MockConnection conn) {
      super(conn);
    }

    @Override
    public void run() {
      try {
        conn.requestStart();
      }
      catch (Exception e) {
        fail(e.getMessage());
      }
    }

  }
}
