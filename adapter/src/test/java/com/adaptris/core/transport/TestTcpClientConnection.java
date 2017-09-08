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

public class TestTcpClientConnection {

  @Test
  public void testBlockSize() throws Exception {
    TcpClientConnection conn = new TcpClientConnection();
    assertEquals(8192, conn.getBlockSize());
    conn.setBlockSize(1024);
    assertEquals(1024, conn.getBlockSize());
  }

  @Test
  public void testHost() throws Exception {
    TcpClientConnection conn = new TcpClientConnection();
    assertNull(conn.getHost());
    conn.setHost("localhost");
    assertEquals("localhost", conn.getHost());
  }

  @Test
  public void testPort() throws Exception {
    TcpClientConnection conn = new TcpClientConnection();
    assertEquals(0, conn.getPort());
    conn.setPort(443);
    assertEquals(443, conn.getPort());
  }

  @Test
  public void testTimeoutMs() throws Exception {
    TcpClientConnection conn = new TcpClientConnection();
    assertEquals(60000, conn.getTimeoutMs());
    conn.setTimeoutMs(443);
    assertEquals(443, conn.getTimeoutMs());
  }

  @Test
  public void testCreateTransport() throws Exception {
    TcpClientConnection conn = new TcpClientConnection("localhost", 80);
    assertNotNull(conn.createTransport());
  }

}
