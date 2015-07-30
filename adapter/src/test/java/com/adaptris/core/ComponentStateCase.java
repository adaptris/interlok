package com.adaptris.core;

import com.adaptris.core.stubs.MockConnection;

public abstract class ComponentStateCase extends BaseCase {

  public ComponentStateCase(String name) {
    super(name);
  }

  // This is to check that we don't trigger the requestXXX operation multiple times.
  //
  public void testStateChange_MultiThreaded() throws Exception {
    MockConnection connection = new MockConnection(1000);
    ComponentState expected = createState();
    prepareConn(connection);
    new Thread(createOperator(connection)).start();
    new Thread(createOperator(connection)).start();
    new Thread(createOperator(connection)).start();
    new Thread(createOperator(connection)).start();
    new Thread(createOperator(connection)).start();
    waitFor(connection, expected);
    assertEquals(expected, connection.retrieveComponentState());
    verifyOperation(connection);
  }
  

  protected abstract ComponentState createState();

  protected abstract void prepareConn(MockConnection c) throws Exception;

  protected abstract ConnOperator createOperator(MockConnection c);

  protected abstract void verifyOperation(MockConnection c);

  protected abstract class ConnOperator implements Runnable {
    protected MockConnection conn;

    protected ConnOperator(MockConnection conn) {
      this.conn = conn;
    }
  }

}
