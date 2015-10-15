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

import java.io.InputStream;
import java.io.InterruptedIOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.adaptris.core.PortManager;
import com.adaptris.util.text.HexDump;

/**
 * A simple Socket server
 * <p>
 * It just reads in some bytes, and echos them to the log and writes back a
 * string to the remote client
 * <p>
 * The configuration file is a file called "SimpleSocketServer.properties"
 * somewhere in the classpath.
 * <p>
 * The server will stop if a file called "SimpleSocketServer.stop" is found in
 * the current working directory.
 */
public class JunitSocketServer {
  private boolean stopNow = false;
  private static final String CFG_FNAME = "transport-test.properties";

  private static Log logR = null;
  private SocketServer tcp = null;
  private SocketServer ssl = null;
  protected static final Properties SOCKET_PROPERTIES;

  private Thread tcpThread, sslThread;

  static {
    SOCKET_PROPERTIES = new Properties();
    try {
      InputStream in = JunitSocketServer.class.getClassLoader().getResourceAsStream(CFG_FNAME);
      if (in != null) {
        SOCKET_PROPERTIES.load(in);
        in.close();
      }
    }
    catch (Exception e) {
      ;
    }
  }

  public JunitSocketServer() throws Exception {
    if (logR == null) {
      logR = LogFactory.getLog(JunitSocketServer.class);
    }
    if (SOCKET_PROPERTIES.isEmpty()) {
      throw new Exception("No Properties");
    }
    tcp = new SocketServer(SOCKET_PROPERTIES, "tcp");
    ssl = new SocketServer(SOCKET_PROPERTIES, "ssl");
  }

  public int getTcpPort() {
    return tcp.getListenPort();
  }

  public int getSslPort() {
    return ssl.getListenPort();
  }

  public void startThreads() {
    tcpThread = new Thread(tcp, "TCP-Server");
    tcpThread.start();
    sslThread = new Thread(ssl, "SSL-Server");
    sslThread.start();
    while (ssl.nowStarted == false || tcp.nowStarted == false) {
      try {
        Thread.sleep(500);
      }
      catch (Exception e) {
        ;
      }
    }
  }

  public void stopThreads() {
    stopNow = true;
    join(tcpThread);
    join(sslThread);
    logR.info("tcpThread + sslThread dead");
  }

  private static void join(Thread t) {
    try {
      if (t.isAlive()) {
        t.join();
      }
    }
    catch (InterruptedException ignored) {
      logR.warn("Interrupted");
    }
  }

  private class SocketServer implements Runnable {
    private Properties config;
    private Transport transport;
    private String myType = "";
    private int listenPort = 0;
    boolean nowStarted = false;

    SocketServer(Properties props, String type) throws TransportException {
      config = (Properties) props.clone();
      myType = type;
      transport = Transport.create(type);
      int offset = Integer.parseInt(config.getProperty(SocketConstants.CONFIG_LISTEN));
      listenPort = PortManager.nextUnusedPort(offset);
      config.setProperty(SocketConstants.CONFIG_LISTEN, String.valueOf(listenPort));
      transport.setConfiguration(config);
    }

    public int getListenPort() {
      return listenPort;
    }

    @Override
    public void run() {
      List<Thread> tList = new ArrayList<Thread>();
      while (!stopNow) {
        nowStarted = true;
        try {
          TransportLayer tl = transport.listen(1000);
          SocketAction s = new SocketAction(tl);
          Thread t = new Thread(s);
          tList.add(t);
          t.start();
        }
        catch (InterruptedIOException e) {
          continue;
        }
        catch (Exception e) {
          logR.error(myType + ": Exception in run", e);
        }
        for (Thread t : tList) {
          join(t);
        }
      }
      try {
        transport.close();
        logR.info(myType + " : listener on " + listenPort + " closed");
      }
      catch (TransportException e) {
        ;
      }
    }
  }

  private class SocketAction implements Runnable {
    TransportLayer comms;

    SocketAction(TransportLayer t) {
      comms = t;
    }

    @Override
    public void run() {
      try {
        byte[] bytes = comms.receive();
        logR.debug("Read :\n" + HexDump.parse(bytes));
        String send = SOCKET_PROPERTIES.getProperty("simplesocketserver.server.string");
        comms.send(send.getBytes());
        comms.close();
      }
      catch (Exception e) {
        logR.error("SocketAction Error", e);
      }
    }
  }

}
