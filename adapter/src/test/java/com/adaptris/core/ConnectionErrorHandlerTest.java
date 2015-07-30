/*
 * $RCSfile: ConnectionErrorHandlerTest.java,v $
 * $Revision: 1.2 $
 * $Date: 2008/07/14 09:57:10 $
 * $Author: lchan $
 */
package com.adaptris.core;

import com.adaptris.core.stubs.MockChannel;
import com.adaptris.core.stubs.MockMessageConsumer;
import com.adaptris.core.stubs.MockMessageListener;
import com.adaptris.core.stubs.MockStandaloneConsumer;
import com.adaptris.core.stubs.StateManagedStandaloneConsumer;
import com.adaptris.util.license.License;

public class ConnectionErrorHandlerTest extends BaseCase {

  public ConnectionErrorHandlerTest(java.lang.String testName) {
    super(testName);
  }

  @Override
  protected void setUp() throws Exception {

  }

  @Override
  protected void tearDown() throws Exception {

  }

  public void testAllowedInConjunctionWith() throws Exception {
    ChannelCloseErrorHandler a = new ChannelCloseErrorHandler();
    ChannelCloseErrorHandler b = new ChannelCloseErrorHandler();
    assertTrue(a.allowedInConjunctionWith(b));
  }

  public void testAdaptrisComponentConnectionErrorHandler() throws Exception {
    MockStandaloneConsumer c = new MockStandaloneConsumer(new TriggeredFailingConnection(), new MockMessageConsumer(
        new MockMessageListener()));

    start(c);
    ((TriggeredFailingConnection) c.getConnection()).triggerError();
    assertEquals(2, c.getInitCount());
    assertEquals(2, c.getStartCount());
    assertEquals(1, c.getStopCount());
    assertEquals(1, c.getCloseCount());
    stop(c);
  }

  public void testStateManagedComponentConnectionErrorHandler() throws Exception {
    MockStandaloneConsumer c = new StateManagedStandaloneConsumer(new TriggeredFailingConnection(), new MockMessageConsumer(
        new MockMessageListener()));
    start(c);
    ((TriggeredFailingConnection) c.getConnection()).triggerError();
    assertEquals(2, c.getInitCount());
    assertEquals(2, c.getStartCount());
    assertEquals(1, c.getStopCount());
    assertEquals(1, c.getCloseCount());
    stop(c);
  }

  public void testChannelConnectionErrorHandler() throws Exception {
    MockChannel c = new MockChannel();
    TriggeredFailingConnection con = new TriggeredFailingConnection();
    c.setConsumeConnection(con);
    start(c);
    con.triggerError();
    assertEquals(2, c.getInitCount());
    assertEquals(2, c.getStartCount());
    assertEquals(1, c.getStopCount());
    assertEquals(1, c.getCloseCount());
    stop(c);
  }

  public void testChannelClosed_ConnectionErrorHandler() throws Exception {
    MockChannel started = new MockChannel();
    MockChannel neverStarted = new MockChannel();
    TriggeredFailingConnection con = new TriggeredFailingConnection();
    started.setConsumeConnection(con);
    neverStarted.setConsumeConnection(con);
    start(started);
    con.triggerError();
    assertEquals(2, started.getInitCount());
    assertEquals(2, started.getStartCount());
    assertEquals(1, started.getStopCount());
    assertEquals(1, started.getCloseCount());
    // ID 167 - Previously neverStarted would have been started as the CEH would always restart all exception listeners.
    assertEquals(0, neverStarted.getInitCount());
    assertEquals(0, neverStarted.getStartCount());
    assertEquals(0, neverStarted.getStopCount());
    assertEquals(0, neverStarted.getCloseCount());
    stop(started);
  }

  public void testChannelStopped_ConnectionErrorHandler() throws Exception {
    MockChannel started = new MockChannel();
    MockChannel stopped = new MockChannel();
    TriggeredFailingConnection con = new TriggeredFailingConnection();
    started.setConsumeConnection(con);
    stopped.setConsumeConnection(con);
    start(started);
    start(stopped);
    stopped.requestStop();
    con.triggerError();
    assertEquals(2, started.getInitCount());
    assertEquals(2, started.getStartCount());
    assertEquals(1, started.getStopCount());
    assertEquals(1, started.getCloseCount());

    assertEquals(1, stopped.getInitCount());
    assertEquals(1, stopped.getStartCount());
    assertEquals(1, stopped.getStopCount());
    assertEquals(1, stopped.getCloseCount());
    assertEquals(ClosedState.getInstance(), stopped.retrieveComponentState());
    stop(started);
  }

  public void testChannelInitialised_ConnectionErrorHandler() throws Exception {
    MockChannel started = new MockChannel();
    MockChannel initOnly = new MockChannel();
    TriggeredFailingConnection con = new TriggeredFailingConnection();
    started.setConsumeConnection(con);
    initOnly.setConsumeConnection(con);
    start(started);
    initOnly.requestInit();
    con.triggerError();
    assertEquals(2, started.getInitCount());
    assertEquals(2, started.getStartCount());
    assertEquals(1, started.getStopCount());
    assertEquals(1, started.getCloseCount());

    assertEquals(1, initOnly.getInitCount());
    assertEquals(0, initOnly.getStartCount());
    assertEquals(0, initOnly.getStopCount());
    assertEquals(1, initOnly.getCloseCount());
    assertEquals(ClosedState.getInstance(), initOnly.retrieveComponentState());
    stop(started);
  }

  public void testAdaptrisComponentNullConnectionErrorHandler() throws Exception {
    MockStandaloneConsumer c = new MockStandaloneConsumer(new TriggeredFailingConnection(new NullConnectionErrorHandler()),
        new MockMessageConsumer(
        new MockMessageListener()));

    start(c);
    ((TriggeredFailingConnection) c.getConnection()).triggerError();
    assertEquals(1, c.getInitCount());
    assertEquals(1, c.getStartCount());
    assertEquals(0, c.getStopCount());
    assertEquals(0, c.getCloseCount());
    stop(c);
  }

  public void testStateManagedComponentNullConnectionErrorHandler() throws Exception {
    MockStandaloneConsumer c = new StateManagedStandaloneConsumer(new TriggeredFailingConnection(new NullConnectionErrorHandler()),
        new MockMessageConsumer(
        new MockMessageListener()));
    start(c);
    ((TriggeredFailingConnection) c.getConnection()).triggerError();
    assertEquals(1, c.getInitCount());
    assertEquals(1, c.getStartCount());
    assertEquals(0, c.getStopCount());
    assertEquals(0, c.getCloseCount());
    stop(c);
  }

  public void testChannelNullConnectionErrorHandler() throws Exception {
    MockChannel c = new MockChannel();
    TriggeredFailingConnection con = new TriggeredFailingConnection(new NullConnectionErrorHandler());
    c.setConsumeConnection(con);
    start(c);
    con.triggerError();
    assertEquals(1, c.getInitCount());
    assertEquals(1, c.getStartCount());
    assertEquals(0, c.getStopCount());
    assertEquals(0, c.getCloseCount());
    stop(c);
  }

  public void testChannelChannelCloseConnectionErrorHandler() throws Exception {
    MockChannel c = new MockChannel();
    TriggeredFailingConnection con = new TriggeredFailingConnection(new ChannelCloseErrorHandler());
    c.setConsumeConnection(con);
    start(c);
    con.triggerError();
    assertEquals(1, c.getInitCount());
    assertEquals(1, c.getStartCount());
    assertEquals(1, c.getStopCount());
    assertEquals(1, c.getCloseCount());
    assertEquals(ClosedState.getInstance(), c.retrieveComponentState());
    stop(c);
  }

  private class TriggeredFailingConnection extends AdaptrisConnectionImp {
    public TriggeredFailingConnection() {
      this(new ConnectionErrorHandlerImp() {
        @Override
        public void handleConnectionException() {
          super.restartAffectedComponents();
        };

        @Override
        public void init() throws CoreException {

        }
        @Override
        public void start() throws CoreException {
        }

        @Override
        public void stop() {
        }

        @Override
        public void close() {
        }
      });
    }

    public TriggeredFailingConnection(ConnectionErrorHandler c) {
      super();
      setConnectionErrorHandler(c);
    }

    public void triggerError() {
      getConnectionErrorHandler().handleConnectionException();
    }

    @Override
    protected void initConnection() throws CoreException {
      ;
    }

    /**
     * 
     * @see com.adaptris.core.AdaptrisConnectionImp#startConnection()
     */
    @Override
    protected void startConnection() throws CoreException {
      ;
    }

    /**
     * 
     * @see com.adaptris.core.AdaptrisConnectionImp#stopConnection()
     */
    @Override
    protected void stopConnection() {
      ;
    }

    /**
     * 
     * @see com.adaptris.core.AdaptrisConnectionImp#closeConnection()
     */
    @Override
    protected void closeConnection() {
      ;
    }

    /** @see AdaptrisComponent */
    public boolean isEnabled(License license) throws CoreException {
      return true; // always enabled
    }

  }

}