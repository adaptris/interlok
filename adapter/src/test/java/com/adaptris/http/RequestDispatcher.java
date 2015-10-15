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

import java.net.HttpURLConnection;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author lchan
 * @author $Author: lchan $
 */
final class RequestDispatcher implements Runnable {
  private Listener parent;
  /** some generic response codes to be stored in a hashmap.
   */
  private static final String[] COMMON_RESPONSE_CODES =
    {
      Integer.toString(HttpURLConnection.HTTP_OK),
      Integer.toString(HttpURLConnection.HTTP_BAD_METHOD),
      Integer.toString(HttpURLConnection.HTTP_FORBIDDEN),
      Integer.toString(HttpURLConnection.HTTP_LENGTH_REQUIRED),
      Integer.toString(HttpURLConnection.HTTP_INTERNAL_ERROR),
      Integer.toString(HttpURLConnection.HTTP_NOT_IMPLEMENTED),
      Integer.toString(HttpURLConnection.HTTP_NOT_FOUND),
      Integer.toString(HttpURLConnection.HTTP_VERSION)};
  /** some generic response messages to be stored in a hashmap.
   */
  private static final String[] COMMON_RESPONSE_MESSAGES =
    {
      "OK",
      "Bad Method",
      "Bad username/password",
      "Length Required",
      "Internal Server Error",
      "Not implemented",
      "Workflow Not Found",
      "HTTP Version not supported" };
  /** The generic response hashmap.
   */
  private static HashMap commonResponseHashMap;
  // Default poll for the LinkedQueue timeout. 100 ms
  private static final int DEFAULT_QUEUE_POLL_TIMEOUT = 100;

  static {
    commonResponseHashMap = new HashMap();
    for (int i = 0; i < COMMON_RESPONSE_CODES.length; i++) {
      commonResponseHashMap.put(
        COMMON_RESPONSE_CODES[i],
        COMMON_RESPONSE_MESSAGES[i]);
    }
  }

  private Socket socket;
  private transient Log logR;
  private Map requestProcessors;
  private String threadName;
  
  private RequestDispatcher() {
    logR = LogFactory.getLog(this.getClass());
    threadName = getThreadName();
  }

  RequestDispatcher(Socket s, Map requests, Listener parent) {
    this();
    socket = s;
    requestProcessors = requests;
    this.parent = parent;
  }

  public void run() {
    RequestProcessor rp = null;
    HttpSession session = null;
    LinkedBlockingQueue queue = null;
    String oldName = Thread.currentThread().getName();
    Thread.currentThread().setName(threadName);
    do {
      try {
        if (logR.isTraceEnabled()) {
          logR.trace("Reading HTTP Request");
        }

        session = new ServerSession();
        session.setSocket(socket);
//        String uri = session.getRequestLine().getURI();

        String file = session.getRequestLine().getFile();
        queue = (LinkedBlockingQueue) requestProcessors.get(file);

        if (queue == null) {
          // Get a default one, if any
          queue = (LinkedBlockingQueue) requestProcessors.get("*");
          if (queue == null) {
            doResponse(session, HttpURLConnection.HTTP_NOT_FOUND);
            break;
          }
        }
        rp = waitForRequestProcessor(queue);
        if (!parent.isAlive()) {
          doResponse(session, HttpURLConnection.HTTP_INTERNAL_ERROR);
          break;
        }
        rp.processRequest(session);
        session.commit();
      } catch (Exception e) {
        // if an exception occurs, then it's pretty much a fatal error for 
        // this session
        // we ignore any output that it might have setup, and use our own
        try {
          logR.error(e.getMessage(), e);
          if (session != null) {
            doResponse(session, HttpURLConnection.HTTP_INTERNAL_ERROR);
          }
        } catch (Exception e2) {
          ;
        }
      }
    } while (false);
    try {
      if (rp != null && queue != null) {
        queue.put(rp);
        logR.trace(rp + " put back on to queue");
      }
    } catch (Exception e) {
      ;
    }
    session = null;
    queue = null;
    rp = null;
    Thread.currentThread().setName(oldName);
    return;
  }

  // Block on a request processor for ever, or until we are are shutdown.
  //
  private RequestProcessor waitForRequestProcessor(LinkedBlockingQueue queue)
    throws HttpException {
    RequestProcessor rp = null;
    do {
      if (logR.isTraceEnabled()) {
        logR.trace("Waiting for an available processor from " + queue);
      }
      try {
        rp = (RequestProcessor) queue.poll(DEFAULT_QUEUE_POLL_TIMEOUT, TimeUnit.MILLISECONDS);
        if (rp != null) {
          if (logR.isTraceEnabled()) {
            logR.trace("Got RequestProcessor " + rp);
          }
          break;
        }
      } catch (InterruptedException e) {
        ;
      }
    } while (rp == null && parent.isAlive());
    return rp;
  }

  /** Perform some generic response to the remote party
   */
  private static void doResponse(HttpSession currentSession, int code)
    throws HttpException {

    HttpResponse response = currentSession.getResponseLine();
    response.setResponseCode(code);
    response.setResponseMessage(
      (String) commonResponseHashMap.get(Integer.toString(code)));
    HttpMessage msg = currentSession.getResponseMessage();
    msg.getHeaders().put(Http.CONNECTION, Http.CLOSE);
    currentSession.commit();
  }
  
  private String getThreadName() {
    String className = this.getClass().getName();
    int dot = className.lastIndexOf(".");
    if (dot > 0) {
      className = className.substring(dot + 1);
    }
    return className + "@" + hashCode();
  }


}
