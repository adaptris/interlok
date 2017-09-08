/*
 * Copyright 2017 Adaptris Ltd.
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
package com.adaptris.core.transport;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.junit.Test;

public class TestPppTcpClientConnection {

  @Test
  public void testConnectCommand() throws Exception {
    PppTcpClientConnection conn = new PppTcpClientConnection();
    assertNull(conn.getConnectCommand());
    conn.setConnectCommand("true");
    assertEquals("true", conn.getConnectCommand());
  }

  @Test
  public void testCheckConnectCommand() throws Exception {
    PppTcpClientConnection conn = new PppTcpClientConnection();
    assertNull(conn.getCheckConnectionCommand());
    conn.setCheckConnectionCommand("true");
    assertEquals("true", conn.getCheckConnectionCommand());
  }

  @Test
  public void testDisconnectCommand() throws Exception {
    PppTcpClientConnection conn = new PppTcpClientConnection();
    assertNull(conn.getDisconnectCommand());
    conn.setDisconnectCommand("true");
    assertEquals("true", conn.getDisconnectCommand());
  }

  @Test
  public void testNoConnectionFilter() throws Exception {
    PppTcpClientConnection conn = new PppTcpClientConnection();
    assertNull(conn.getNoConnectionFilter());
    conn.setNoConnectionFilter("true");
    assertEquals("true", conn.getNoConnectionFilter());
  }


  @Test
  public void testCreateTransport() throws Exception {
    PppTcpClientConnection conn = new PppTcpClientConnection("localhost", 80, "/bin/true", "/bin/false");
    assertNotNull(conn.createTransport());
  }

}
