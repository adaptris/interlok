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
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import com.adaptris.core.stubs.MockConnection;

public class ConnectionStateHandlerImpTest {

  /**
   * Concrete test implementation of abstract ConnectionStateHandlerImp.
   */
  private static class TestConnectionStateHandler extends ConnectionStateHandlerImp {
  }

  @Test
  public void testRegisterConnection() throws Exception {
    ConnectionStateHandler handler = new TestConnectionStateHandler();
    MockConnection connection = new MockConnection();

    handler.registerConnection(connection);

    assertEquals(connection, handler.retrieveConnection(AdaptrisConnection.class));
  }

  @Test
  public void testRegisterConnectionNull() throws Exception {
    ConnectionStateHandler handler = new TestConnectionStateHandler();

    assertThrows(IllegalArgumentException.class, () -> handler.registerConnection(null),
        "registerConnection() should reject null connection");
  }

  @Test
  public void testRetrieveConnectionBeforeRegistration() throws Exception {
    ConnectionStateHandler handler = new TestConnectionStateHandler();

    assertNull(handler.retrieveConnection(AdaptrisConnection.class),
        "retrieveConnection() should return null before registerConnection() is called");
  }

  @Test
  public void testRetrieveConnectionWithCasting() throws Exception {
    ConnectionStateHandler handler = new TestConnectionStateHandler();
    MockConnection connection = new MockConnection();
    handler.registerConnection(connection);

    MockConnection retrieved = handler.retrieveConnection(MockConnection.class);

    assertEquals(connection, retrieved);
    assertEquals(MockConnection.class, retrieved.getClass());
  }

  @Test
  public void testRegisterConnectionMultipleTimes() throws Exception {
    ConnectionStateHandler handler = new TestConnectionStateHandler();
    MockConnection connection1 = new MockConnection();
    MockConnection connection2 = new MockConnection();

    handler.registerConnection(connection1);
    assertEquals(connection1, handler.retrieveConnection(AdaptrisConnection.class));

    // Subsequent registration overwrites the previous one
    handler.registerConnection(connection2);
    assertEquals(connection2, handler.retrieveConnection(AdaptrisConnection.class));
  }

}
