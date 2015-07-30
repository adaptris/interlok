package com.adaptris.core;

import com.adaptris.core.stubs.MockConnection;


public class ClosedStateTest extends ComponentStateCase {

  public ClosedStateTest(java.lang.String testName) {
    super(testName);
  }

  @Override
  protected void setUp() throws Exception {
  }


  public void testInstance() throws Exception {
    ClosedState one = ClosedState.getInstance();
    assertTrue(one == ClosedState.getInstance());
    assertEquals(ClosedState.getInstance(), one);
    assertEquals(ClosedState.class.getSimpleName(), one.toString());

  }

  public void testClosed_To_Initialised() throws Exception {
    MockConnection channel = new MockConnection();
    ClosedState state = ClosedState.getInstance();
    state.requestInit(channel);
    assertEquals(InitialisedState.getInstance(), channel.retrieveComponentState());
    assertEquals(1, channel.getInitCount());
    assertEquals(0, channel.getStartCount());
    assertEquals(0, channel.getStopCount());
    assertEquals(0, channel.getCloseCount());
  }

  public void testClosed_To_Started() throws Exception {
    MockConnection channel = new MockConnection();
    ClosedState state = ClosedState.getInstance();
    state.requestStart(channel);
    assertEquals(StartedState.getInstance(), channel.retrieveComponentState());
    assertEquals(1, channel.getInitCount());
    assertEquals(1, channel.getStartCount());
    assertEquals(0, channel.getStopCount());
    assertEquals(0, channel.getCloseCount());
  }

  public void testClosed_To_Stopped() throws Exception {
    MockConnection channel = new MockConnection();
    ClosedState state = ClosedState.getInstance();
    state.requestStop(channel);
    // This actually should do nothing.
    assertEquals(ClosedState.getInstance(), channel.retrieveComponentState());
    assertEquals(0, channel.getInitCount());
    assertEquals(0, channel.getStartCount());
    assertEquals(0, channel.getStopCount());
    assertEquals(0, channel.getCloseCount());
  }

  public void testClosed_To_Closed() throws Exception {
    MockConnection channel = new MockConnection();
    ClosedState state = ClosedState.getInstance();
    state.requestClose(channel);
    // This actually should do nothing.
    assertEquals(ClosedState.getInstance(), channel.retrieveComponentState());
    assertEquals(0, channel.getInitCount());
    assertEquals(0, channel.getStartCount());
    assertEquals(0, channel.getStopCount());
    assertEquals(0, channel.getCloseCount());
  }

  public void testRestart() throws Exception {
    MockConnection channel = new MockConnection();
    ClosedState state = ClosedState.getInstance();
    state.requestRestart(channel);
    assertEquals(StartedState.getInstance(), channel.retrieveComponentState());
    assertEquals(1, channel.getInitCount());
    assertEquals(1, channel.getStartCount());
    assertEquals(0, channel.getStopCount());
    assertEquals(0, channel.getCloseCount());
  }

  @Override
  protected ComponentState createState() {
    return ClosedState.getInstance();
  }

  @Override
  protected ConnOperator createOperator(MockConnection c) {
    return new Closer(c);
  }

  @Override
  protected void verifyOperation(MockConnection c) {
    assertEquals(1, c.getInitCount());
    assertEquals(1, c.getStartCount());
    assertEquals(1, c.getStopCount());
    assertEquals(1, c.getCloseCount());
  }

  @Override
  protected void prepareConn(MockConnection c) throws Exception {
    c.requestStart();
  }

  private class Closer extends ConnOperator {

    protected Closer(MockConnection conn) {
      super(conn);
    }

    @Override
    public void run() {
      try {
        conn.requestClose();
      }
      catch (Exception e) {
        fail(e.getMessage());
      }
    }
  }
}