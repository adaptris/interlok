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

package com.adaptris.http;

import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.Hashtable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * <p>
 * This class is the basis of our simple HTTP server. For any given HTTP server
 * there are really two things that must occur, it must listen for incoming
 * requests, and then handle the requests coming on a given socket.
 * </p>
 * <p>
 * This class simply listens for incoming socket requests. Having received one,
 * it passes the socket to RequestDispatcher to delegate to any matching
 * requestProcessors for the specified URL.
 * </p>
 * 
 * @see HttpSession
 * @see RequestProcessor
 */
public class HttpListener implements Listener, Runnable {

  // Default number of Listener threads
  private static final int DEFAULT_LISTENER_POOL_SIZE = 10;
  private static final int SHUTDOWN_WAIT_TIME_MS = 60000;

  /** The logger */
  protected transient Log logR = null;
  protected int listenPort = 0;
  protected int serverSocketTimeout = Http.DEFAULT_SERVER_SOCKET_TIMEOUT;
  protected int socketTimeout = Http.DEFAULT_SOCKET_TIMEOUT;
  protected ServerSocket serverSocket;
  protected boolean initialised = false;

  private boolean started = true;
  private Hashtable requestProcessors;
  private ExecutorService dispatcherPool;
  private int poolSize;
  private Thread listenerThread;

  /** Default Constructor */
  protected HttpListener() {
    logR = LogFactory.getLog(this.getClass());
  }

  /**
   * HttpListener
   * 
   * @param listenPort the port to listen on
   */
  public HttpListener(int listenPort) {
    this(listenPort, DEFAULT_LISTENER_POOL_SIZE);
  }

  /**
   * HttpListener Constructor.
   * 
   * @param listenPort the port to listen on
   * @param poolSize the initial size of the threadpool that will service
   *          requests.
   */
  public HttpListener(int listenPort, int poolSize) {
    this();
    this.listenPort = listenPort;
    this.poolSize = poolSize;
    requestProcessors = new Hashtable();
    dispatcherPool = Executors.newCachedThreadPool();
    if (dispatcherPool instanceof ThreadPoolExecutor) {
      ((ThreadPoolExecutor) dispatcherPool).setCorePoolSize(poolSize);
    }
  }

  /**
   * Add a request processor to the list
   * 
   * @param rp the request processor.
   * @throws HttpException on error.
   */
  public synchronized void addRequestProcessor(RequestProcessor rp)
      throws HttpException {
    try {

      String uri = rp.getUri();
      LinkedBlockingQueue list = (LinkedBlockingQueue) requestProcessors
          .get(uri);
      if (list == null) {
        list = new LinkedBlockingQueue();
      }
      list.put(rp);
      requestProcessors.put(uri, list);
    }
    catch (Exception e) {
      throw new HttpException(e);
    }
  }

  /**
   * @see com.adaptris.http.Listener#isAlive()
   */
  public final boolean isAlive() {
    return started;
  }

  /**
   * @see Listener#initialise()
   */
  public void initialise() throws HttpException {
    if (initialised) {
      return;
    }
    try {
      if (serverSocket == null) {
        serverSocket = new ServerSocket(listenPort, 1024);
        if (logR.isTraceEnabled()) {
          logR.trace("Server socket timeout set to " + serverSocketTimeout
              + "ms");
        }
        serverSocket.setSoTimeout(serverSocketTimeout);
        if (logR.isInfoEnabled()) {
          logR.info("Initialised to listen for HTTP connections on port "
              + serverSocket.getLocalPort());
        }
        listenPort = serverSocket.getLocalPort();
      }
      initialised = true;
    }
    catch (Exception e) {
      throw new HttpException(e);
    }
  }

  /**
   * @see Listener#stop()
   */
  public void stop() throws HttpException {
    started = false;
    try {
      if (listenerThread.isAlive()) {
        listenerThread.join();
      }
    }
    catch (Exception e) {
      logR.trace(e.getMessage(), e);
    }
  }

  /**
   * @see Listener#start()
   */
  public void start() throws HttpException {
    if (!initialised) {
      initialise();
    }
    started = true;

    listenerThread = new Thread(this, toString());
    listenerThread.start();
  }

  /** @see Runnable#run() */
  public void run() {
    try {
      logR.trace("Accepting Connections");
      while (started) {
        try {

          Socket socket = serverSocket.accept();

          // Set the default timeout
          socket.setSoTimeout(socketTimeout);
          dispatcherPool.execute(new RequestDispatcher(socket,
              requestProcessors, this));
        }
        catch (Exception e) {
          ;
        }
      }
      try {
        logR.trace("Shutdown requested on the dispatcher pool");
        dispatcherPool.shutdown();
        boolean poolShutdown = dispatcherPool.awaitTermination(
            SHUTDOWN_WAIT_TIME_MS, TimeUnit.MILLISECONDS);
        if (!poolShutdown) {
          dispatcherPool.shutdownNow();
        }
        logR.trace("Pool is shutdown");
        serverSocket.close();
      }
      catch (Exception e) {
        ;
      }
    }
    catch (Exception e) {
      logR.error("Error occured during runtime :- ", e);
    }
    logR.trace("Run-method returns");
  }

  /**
   * Set the server socket timeout.
   * <p>
   * If the timeout period is reached while doing a
   * <code>ServerSocket.accept()</code> then a
   * <code>SocketTimeoutException</code> is thrown, forcing the listener to
   * process some other actions (like shutdown!).
   * </p>
   * 
   * @param timeout the timeout.
   */
  public void setServerSocketTimeout(int timeout) {
    serverSocketTimeout = timeout;
  }

  /**
   * Set the socket timeout for each requested socket
   * 
   * @param timeout the timeout
   */
  public void setSocketTimeout(int timeout) {
    socketTimeout = timeout;
  }

  /**
   * Get the incoming request socket.
   * <p>
   * Sub-classes should override this method to accept a connection.
   */
  private Socket getIncomingRequest() throws SocketTimeoutException,
      HttpException {

    Socket socket = null;
    try {
      socket = serverSocket.accept();
    }
    catch (SocketTimeoutException e) {
      throw e;
    }
    catch (Exception e) {
      throw new HttpException(e);
    }
    return socket;
  }

  /**
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    String className = this.getClass().getSimpleName();
    InetAddress a = null;
    try {
      a = InetAddress.getLocalHost();
    }
    catch (Exception ignoredIntentionally) {
      ;
    }
    StringBuffer sb = new StringBuffer(className);
    sb.append(" on ");
    sb.append(a != null ? a.getHostName() + ":" : "");
    sb.append(listenPort);
    return sb.toString();
  }
}
