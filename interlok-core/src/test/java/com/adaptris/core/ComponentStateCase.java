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
import org.junit.Test;
import com.adaptris.core.stubs.MockConnection;

public abstract class ComponentStateCase extends BaseCase {

  public ComponentStateCase() {
  }

  // This is to check that we don't trigger the requestXXX operation multiple times.
  //
  @Test
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
