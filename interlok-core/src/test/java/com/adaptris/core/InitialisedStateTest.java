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

public class InitialisedStateTest extends ComponentStateCase {

  public InitialisedStateTest() {
  }

  @Test
  public void testInstance() throws Exception {
    InitialisedState one = InitialisedState.getInstance();
    assertTrue(one == InitialisedState.getInstance());
    assertEquals(InitialisedState.getInstance(), one);
    assertEquals(InitialisedState.class.getSimpleName(), one.toString());
  }

  @Test
  public void testInitialised_To_Initialised() throws Exception {
    MockConnection component = new MockConnection();
    component.requestInit();
    InitialisedState state = InitialisedState.getInstance();
    state.requestInit(component);
    assertEquals(InitialisedState.getInstance(), component.retrieveComponentState());
    assertEquals(1, component.getInitCount());
  }

  @Test
  public void testInitialised_To_Started() throws Exception {
    MockConnection component = new MockConnection();
    component.requestInit();

    InitialisedState state = InitialisedState.getInstance();
    state.requestStart(component);
    assertEquals(StartedState.getInstance(), component.retrieveComponentState());
    assertEquals(1, component.getInitCount());
    assertEquals(1, component.getStartCount());

  }

  @Test
  public void testInitialised_To_Stopped() throws Exception {
    MockConnection component = new MockConnection();
    component.requestInit();

    InitialisedState state = InitialisedState.getInstance();
    state.requestStop(component);
    // This actually should do nothing.
    assertEquals(InitialisedState.getInstance(), component.retrieveComponentState());
    assertEquals(1, component.getInitCount());
    assertEquals(0, component.getStopCount());
  }

  @Test
  public void testInitialised_To_Closed() throws Exception {
    MockConnection component = new MockConnection();
    component.requestInit();
    InitialisedState state = InitialisedState.getInstance();
    state.requestClose(component);
    assertEquals(ClosedState.getInstance(), component.retrieveComponentState());
    assertEquals(1, component.getInitCount());
    assertEquals(0, component.getStartCount());
    assertEquals(0, component.getStopCount());
    assertEquals(1, component.getCloseCount());
  }

  @Test
  public void testRestart() throws Exception {
    MockConnection component = new MockConnection();
    component.requestInit();
    InitialisedState state = InitialisedState.getInstance();
    state.requestRestart(component);
    assertEquals(StartedState.getInstance(), component.retrieveComponentState());
    assertEquals(2, component.getInitCount());
    assertEquals(1, component.getStartCount());
    assertEquals(0, component.getStopCount());
    assertEquals(1, component.getCloseCount());
  }

  @Override
  protected ComponentState createState() {
    return InitialisedState.getInstance();
  }

  @Override
  protected ConnOperator createOperator(MockConnection c) {
    return new Initialiser(c);
  }

  @Override
  protected void verifyOperation(MockConnection c) {
    assertEquals(1, c.getInitCount());
    assertEquals(0, c.getStartCount());
    assertEquals(0, c.getStopCount());
    assertEquals(0, c.getCloseCount());
  }

  @Override
  protected void prepareConn(MockConnection c) throws Exception {
    // nothing to do.
  }

  private class Initialiser extends ConnOperator {

    protected Initialiser(MockConnection conn) {
      super(conn);
    }

    @Override
    public void run() {
      try {
        conn.requestInit();
      }
      catch (Exception e) {
        fail(e.getMessage());
      }
    }

  }

}
