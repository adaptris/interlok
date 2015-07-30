package com.adaptris.transport;

import static com.adaptris.transport.JunitSocketServer.SOCKET_PROPERTIES;
import static org.junit.Assert.assertEquals;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.After;
import org.junit.Test;

import com.adaptris.util.text.HexDump;

/**
 * Test the SslSocketTransport implementation.
 * <p>
 * This is a self test, such that it is used to test against A running "server"
 * example.
 * <p>
 * Because the transport layer is essentially devoid of comms protocol, a fixed
 * length string is sent and received, and compared.
 *
 * @author lchan
 */
public final class TestSecureSocketTransport {
  private static Log logR = LogFactory.getLog(TestSecureSocketTransport.class);

  @After
  public void tearDown() throws Exception {
    System.gc();
  }

  @Test
  public void testSslSendAndReceive() throws Exception {
    JunitSocketServer server = new JunitSocketServer();
    server.startThreads();
    Transport transport = null;
    TransportLayer tLayer = null;
    try {
      String receive = SOCKET_PROPERTIES.getProperty("simplesocketserver.server.string");
      String sendString = SOCKET_PROPERTIES.getProperty("simplesocketserver.client.string");

      transport = createTransport(server);
      tLayer = transport.connect();
      logR.debug(tLayer + " connected ok");
      tLayer.send(sendString.getBytes());
      byte[] bytes = tLayer.receive();
      logR.debug("Read:-\n" + HexDump.parse(bytes));
      String compare = new String(bytes);
      logR.debug("Comparing [" + receive + "] and [" + compare + "]");
      assertEquals(receive, compare);
      tLayer.close();
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
    SSLSocketTransport ssl = new SSLSocketTransport();
    ssl.setHost(SOCKET_PROPERTIES.getProperty(SocketConstants.CONFIG_HOST));
    ssl.setPort(server.getSslPort());

    String url = SOCKET_PROPERTIES.getProperty(TransportConstants.CONFIG_KEYSTORE_FILE);
    ssl.setKeystoreUrl(url);
    ssl.setKeystorePassword(SOCKET_PROPERTIES.getProperty(TransportConstants.CONFIG_KEYSTORE_PW));
    ssl.setPrivateKeyPassword(SOCKET_PROPERTIES.getProperty(TransportConstants.CONFIG_PRIVATE_KEY_PW));
    ssl.setAlwaysTrust(Boolean.valueOf(SOCKET_PROPERTIES.getProperty(SocketConstants.CONFIG_ALWAYS_TRUST, "false")).booleanValue());
    ssl.setRequireClientAuth(Boolean.valueOf(SOCKET_PROPERTIES.getProperty(SocketConstants.CONFIG_CLIENT_AUTH, "true"))
        .booleanValue());
    ssl.setConnectTimeout(Integer.parseInt(SOCKET_PROPERTIES.getProperty(SocketConstants.CONFIG_TIMEOUT)));
    ssl.setBlockSize(Integer.parseInt(SOCKET_PROPERTIES.getProperty(SocketConstants.CONFIG_BLOCKSIZE)));
    logR.debug(ssl);

    return ssl;
  }
}
