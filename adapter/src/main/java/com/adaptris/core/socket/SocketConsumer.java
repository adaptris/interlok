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

package com.adaptris.core.socket;

import static com.adaptris.core.AdaptrisMessageFactory.defaultIfNull;
import static org.apache.commons.lang.StringUtils.isEmpty;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.hibernate.validator.constraints.NotBlank;

import com.adaptris.annotation.AdapterComponent;
import com.adaptris.annotation.AdvancedConfig;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.annotation.DisplayOrder;
import com.adaptris.annotation.InputFieldDefault;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageConsumerImp;
import com.adaptris.core.AdaptrisMessageListener;
import com.adaptris.core.ClosedState;
import com.adaptris.core.CoreConstants;
import com.adaptris.core.CoreException;
import com.adaptris.core.util.ManagedThreadFactory;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Consumer for the generic Socket Adapter.
 * 
 * @config socket-consumer
 * 
 * 
 */
@XStreamAlias("socket-consumer")
@AdapterComponent
@ComponentProfile(summary = "Process messages arriving on a socket", tag = "consumer,socket,tcp",
    recommended = {ConsumeConnection.class})
@DisplayOrder(order = {"destination", "protocolImplementation"})
public class SocketConsumer extends AdaptrisMessageConsumerImp {
  @AdvancedConfig
  @InputFieldDefault(value = "true")
  private Boolean sendImmediateReply;
  @NotBlank
  private String protocolImplementation;
  private transient Listener listener;
  private transient Thread listenThread;
  @AdvancedConfig
  private int socketTimeout;
  private transient ManagedThreadFactory threadFactory = new ManagedThreadFactory();

  public SocketConsumer() {
    socketTimeout = 60000;
    changeState(ClosedState.getInstance());
  }

  /**
   * @see AdaptrisMessageConsumerImp#close()
   */
  @Override
  public void close() {
    stopListeners();
  }

  @Override
  public void prepare() throws CoreException {
  }

  /**
   * @see AdaptrisMessageConsumerImp#init()
   */
  @Override
  public void init() throws CoreException {
    if (isEmpty(protocolImplementation)) {
      throw new CoreException("Null or empty protocol implementation");
    }
    try {
      listener = new Listener();
    }
    catch (Exception e) {
      throw new CoreException(e);
    }
  }

  /**
   * @see AdaptrisMessageConsumerImp#start()
   */
  @Override
  public void start() throws CoreException {
    listenThread = threadFactory.newThread(listener);
    listenThread.setName("SocketListener@" + hashCode());
    listenThread.start();
  }

  /**
   * @see AdaptrisMessageConsumerImp#stop()
   */
  @Override
  public void stop() {
    stopListeners();
  }

  private void stopListeners() {
    listener.stop();
    if (listenThread != null && listenThread.isAlive()) {
      try {
        listenThread.join();
      }
      catch (Exception e) {
        log.trace("Failed to stop component cleanly, logging exception for informational purposes only", e);
      }
    }
  }

  /**
   * Get the protocol implementation.
   *
   * @return the protocol implementation
   * @see Protocol
   */
  public String getProtocolImplementation() {
    return protocolImplementation;
  }

  /**
   * Set the protocol implementation.
   *
   * @param string the protocol implementation
   * @see Protocol
   */
  public void setProtocolImplementation(String string) {
    protocolImplementation = string;
  }

  /**
   * Specify whether to send an immediate reply.
   * <p>
   * If set to true, the method <code>Protocol.receiveDocumentError()</code> or
   * <code>Protocol.receiveDocumentSuccess()</code> is immediately used to
   * register the receipt of a document. In this instance, the temporary socket
   * endpoint between the two parties is closed, otherwise it is stored as
   * object metadata against the resulting AdaptrisMessage.
   *
   * @param b true to send a reply immediately upon receipt of data.
   * @see AdaptrisMessage#addObjectHeader(Object, Object)
   * @see CoreConstants#SOCKET_OBJECT_KEY
   */
  public void setSendImmediateReply(Boolean b) {
    sendImmediateReply = b;
  }

  /**
   * Get the immediate reply flag.
   * 
   * @return true or false.
   * @see #setSendImmediateReply(Boolean)
   */
  public Boolean getSendImmediateReply() {
    return sendImmediateReply;
  }

  boolean sendImmediateReply() {
    return getSendImmediateReply() != null ? getSendImmediateReply().booleanValue() : true;
  }

  /**
   * Get the configured socket timeout in milliseconds.
   *
   * @return the socket timeout in ms.
   */
  public int getSocketTimeout() {
    return socketTimeout;
  }

  /**
   * Set the socket timeout in milliseconds.
   *
   * @param i the timeout.
   */
  public void setSocketTimeout(int i) {
    socketTimeout = i;
  }

  /**
   * <p>
   * Comment required...
   * </p>
   */
  private class Listener implements Runnable {

    private boolean isStopped = false;
    private ServerSocket serverSocket = null;
    private int timeout = 6000;

    Listener() throws IOException {
      serverSocket = retrieveConnection(ConsumeConnection.class)
          .createServerSocket();
      timeout = retrieveConnection(ConsumeConnection.class)
          .getServerSocketTimeout();
    }

    @Override
    public void run() {
      ArrayList<Thread> threadList = new ArrayList<Thread>();
      log.trace("Started Listening on " + serverSocket.getLocalPort());
      while (!isStopped) {
        try {
          serverSocket.setSoTimeout(timeout);
          Socket s = serverSocket.accept();
          s.setSoTimeout(getSocketTimeout());
          Worker w = new Worker(s);
          String threadName = retrieveAdaptrisMessageListener().friendlyName() + "@" + w.hashCode();

          Thread t = threadFactory.newThread(w);
          t.setName(threadName);
          threadList.add(t);
          t.start();
        }
        catch (SocketTimeoutException e) {
          filterDeadThreads(threadList);
          continue;
        }
        catch (Exception e) {
          log.error("Fatal Listen error, listener is stopped.");
          isStopped = true;
        }
      }
      log.trace("Processing stop request, waiting for threads to terminate");
      for (Iterator i = threadList.iterator(); i.hasNext();) {
        Thread t = (Thread) i.next();
        if (t.isAlive()) {
          try {
            t.join();
          }
          catch (InterruptedException ignored) {
            ;
          }
        }
      }
      log.trace("Listener stopped");
      return;
    }

    public void stop() {
      log.trace("Stop Requested");
      isStopped = true;
    }

    private void filterDeadThreads(List<Thread> threadList) {
      ArrayList<Thread> dead = new ArrayList<Thread>();
      for (Iterator i = threadList.iterator(); i.hasNext();) {
        Thread t = (Thread) i.next();
        if (!t.isAlive()) {
          dead.add(t);
        }
      }
      threadList.removeAll(dead);
      log.trace("Currently " + threadList.size() + " threads active");
    }
  }

  /**
   * <p>
   * Comment required...
   * </p>
   */
  private class Worker implements Runnable {
    private Socket sock;

    Worker(Socket t) {
      sock = t;
    }

    @Override
    public void run() {
      Protocol p = null;
      try {
        p = (Protocol) Class.forName(protocolImplementation).newInstance();
        p.setSocket(sock);
        p.receiveDocument();
        try {
          processDocument(p.getReceivedAsBytes());
          if (sendImmediateReply()) {
            p.receiveDocumentSuccess();
          }
        }
        catch (Exception e) {
          if (sendImmediateReply()) {
            p.receiveDocumentError();
          }
        }
      }
      catch (Exception e) {
        log.error("ProtocolHandler exception ", e);
      }
      finally {
        if (sendImmediateReply()) {
          try {
            sock.close();
          }
          catch (Exception ignored) {
            ;
          }
        }
      }
    }

    private void processDocument(byte[] b) {
      AdaptrisMessage msg = defaultIfNull(getMessageFactory()).newMessage(b);
      AdaptrisMessageListener l = retrieveAdaptrisMessageListener();
      if (!sendImmediateReply()) {
        msg.addObjectHeader(CoreConstants.SOCKET_OBJECT_KEY, sock);
      }
      synchronized (l) {
        l.onAdaptrisMessage(msg);
      }
    }
  }

}
