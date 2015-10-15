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

package com.adaptris.transport;

import static com.adaptris.transport.JunitSocketServer.SOCKET_PROPERTIES;
import static org.junit.Assert.assertEquals;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.After;
import org.junit.Test;

import com.adaptris.transport.ppp.PppSocketTransport;
import com.adaptris.util.text.HexDump;

/**
 * Test the PppSocket implementation.
 * <p>
 * This is a self test, such that it is used to test against A running "server"
 * example.
 * <p>
 * Because the transport layer is essentially devoid of comms protocol, a fixed
 * length string is sent and received, and compared.
 * <p>
 * This expects a dial-up-connection to exist called "junit"
 * </p>
 *
 * @author lchan
 */
public final class TestPpp {
  private static Log logR = LogFactory.getLog(TestPpp.class);


  @After
  public void tearDown() throws Exception {
    System.gc();
  }

  @Test
  public void testPppConnect() throws Exception {
    JunitSocketServer server = new JunitSocketServer();
    server.startThreads();
    Transport transport = null;
    TransportLayer tLayer = null;
    try {
      String receive = SOCKET_PROPERTIES
          .getProperty("simplesocketserver.server.string");
      String sendString = SOCKET_PROPERTIES
          .getProperty("simplesocketserver.client.string");
      transport = createTransport(server);
      tLayer = transport.connect();
      tLayer.send(sendString.getBytes());
      byte[] bytes = tLayer.receive();
      logR.debug("Read:-\n" + HexDump.parse(bytes));
      String compare = new String(bytes);
      assertEquals(receive, compare);

      tLayer.close();
      transport.close();
    }
    finally {
      if (tLayer != null) {
        tLayer.close();
      }
      if (transport != null) {
        transport.close();
      }
      server.stopThreads();
    }
  }

  private Transport createTransport(JunitSocketServer server) throws Exception {
    PppSocketTransport ppp = new PppSocketTransport();
    ppp.setHost(SOCKET_PROPERTIES.getProperty(SocketConstants.CONFIG_HOST));
    ppp.setPort(server.getTcpPort());

    ppp.setConnectTimeout(Integer.parseInt(SOCKET_PROPERTIES
        .getProperty(SocketConstants.CONFIG_TIMEOUT)));
    ppp.setBlockSize(Integer.parseInt(SOCKET_PROPERTIES
        .getProperty(SocketConstants.CONFIG_BLOCKSIZE)));
    ppp.setCheckConnectionCommand("echo CHECK");
    ppp.setNoConnectionFilter("CHECK");
    ppp.setConnectCommand("echo CONNECT");
    ppp.setDisconnectCommand("echo DISCONNECT");

    // ppp.setCheckConnectionCommand("rasdial");
    // ppp.setNoConnectionFilter("No connections");
    // ppp.setConnectCommand("rasdial junit");
    // ppp.setDisconnectCommand("rasdial /disconnect");
    logR.debug(ppp);
    return ppp;
  }
}
