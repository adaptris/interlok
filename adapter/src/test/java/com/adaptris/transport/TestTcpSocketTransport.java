/*
 * $Id: TestTcpSocketTransport.java,v 1.7 2006/07/19 11:02:11 lchan Exp $
 */

package com.adaptris.transport;

import static com.adaptris.transport.JunitSocketServer.SOCKET_PROPERTIES;
import static org.junit.Assert.assertEquals;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.After;
import org.junit.Test;

import com.adaptris.util.text.HexDump;

/**
 * Test the TcpSocketTransport implementation.
 * <p>
 * This is a self test, such that it is used to test against A running "server"
 * example.
 * <p>
 * Because the transport layer is essentially devoid of comms protocol, a fixed
 * length string is sent and received, and compared.
 *
 * @author lchan
 */
public final class TestTcpSocketTransport {
  private static Log logR = LogFactory.getLog(TestTcpSocketTransport.class);
  @After
  public void tearDown() throws Exception {
    System.gc();
  }


  @Test
  public void testTcpSendAndReceive() throws Exception {
    JunitSocketServer server = new JunitSocketServer();
    server.startThreads();
    Transport transport = null;
    TransportLayer tLayer = null;
    try {
      transport = createTransport(server);
      String receive = SOCKET_PROPERTIES.getProperty("simplesocketserver.server.string");
      String sendString = SOCKET_PROPERTIES.getProperty("simplesocketserver.client.string");
      tLayer = transport.connect();
      tLayer.send(sendString.getBytes());
      byte[] bytes = tLayer.receive();
      logR.debug("Read:-\n" + HexDump.parse(bytes));
      String compare = new String(bytes);
      assertEquals(receive, compare);
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

  // THis test is causing a OOM error. Happens now that JunitSocketServer
  // is executed inline in the test as a thread... It used to be forked
  // in a <parallel> task just before the test.
  // Probably dodgy code in there.
  @Test
  public void testTcpSendReceiveRewindReceive() throws Exception {
    JunitSocketServer server = new JunitSocketServer();
    server.startThreads();
    Transport transport = null;
    TransportLayer tLayer = null;
    try {
      transport = createTransport(server);
      String receive = SOCKET_PROPERTIES.getProperty("simplesocketserver.server.string");
      String sendString = SOCKET_PROPERTIES.getProperty("simplesocketserver.client.string");
      tLayer = transport.connect();
      tLayer.send(sendString.getBytes());
      byte[] bytes = tLayer.receive();
      logR.debug("Read:-\n" + HexDump.parse(bytes));
      String compare = new String(bytes);
      assertEquals("Received data equal", receive, compare);

      tLayer.rewind(receive.length());
      bytes = tLayer.receive();
      logR.debug("Read:-\n" + HexDump.parse(bytes));
      compare = new String(bytes);
      assertEquals("Received (rewound) data equal", receive, compare);
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
    TcpSocketTransport tcp = new TcpSocketTransport();
    tcp.setHost(SOCKET_PROPERTIES.getProperty(SocketConstants.CONFIG_HOST));
    tcp.setPort(server.getTcpPort());
    tcp.setConnectTimeout(Integer.parseInt(SOCKET_PROPERTIES.getProperty(SocketConstants.CONFIG_TIMEOUT)));
    tcp.setBlockSize(Integer.parseInt(SOCKET_PROPERTIES.getProperty(SocketConstants.CONFIG_BLOCKSIZE)));
    logR.debug(tcp);
    return tcp;
  }
}
