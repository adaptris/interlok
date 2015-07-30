package com.adaptris.core;

import com.adaptris.core.stubs.MockConnection;


public class StartedStateTest extends ComponentStateCase {

  public StartedStateTest(java.lang.String testName) {
    super(testName);
  }

  @Override
  protected void setUp() throws Exception {
  }


  public void testInstance() throws Exception {
    StartedState one = StartedState.getInstance();
    assertTrue(one == StartedState.getInstance());
    assertEquals(StartedState.getInstance(), one);
    assertEquals(StartedState.class.getSimpleName(), one.toString());
  }

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