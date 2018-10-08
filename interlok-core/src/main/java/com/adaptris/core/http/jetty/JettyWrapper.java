/*
 * Copyright 2018 Adaptris Ltd.
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
package com.adaptris.core.http.jetty;

import static com.adaptris.core.http.jetty.JettyConstants.JETTY_WRAPPER;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.util.FifoMutexLock;

/**
 * Class that contains information that about the jetty request/response.
 * 
 */
public class JettyWrapper {

  private transient JettyConsumerMonitor monitor;
  private transient HttpServletResponse response;
  private transient HttpServletRequest request;

  private transient FifoMutexLock locker;

  protected JettyWrapper() {
    locker = new FifoMutexLock();
    setMonitor(new JettyConsumerMonitor());
  }

  protected void lock() throws InterruptedException {
    locker.acquire();
  }

  protected void unlock() {
    locker.release();
  }

  /**
   * Get a wrapper with null protection.
   * <p>
   * All it means is that if the wrapper doesn't exist; then you have an empty wrapper with no monitor and no response.
   * </p>
   */
  protected static JettyWrapper unwrap(AdaptrisMessage msg) {
    JettyWrapper wrapper = (JettyWrapper) msg.getObjectHeaders().get(JETTY_WRAPPER);
    if (wrapper == null) {
      wrapper = new JettyWrapper();
    }
    return wrapper;
  }

  public JettyConsumerMonitor getMonitor() {
    return monitor;
  }

  public void setMonitor(JettyConsumerMonitor m) {
    this.monitor = m;
  }

  public JettyWrapper withMonitor(JettyConsumerMonitor b) {
    setMonitor(b);
    return this;
  }

  public HttpServletResponse getResponse() {
    return response;
  }

  public void setResponse(HttpServletResponse r) {
    this.response = r;
  }

  public JettyWrapper withResponse(HttpServletResponse b) {
    setResponse(b);
    return this;
  }

  public HttpServletRequest getRequest() {
    return request;
  }

  public void setRequest(HttpServletRequest request) {
    this.request = request;
  }

  public JettyWrapper withRequest(HttpServletRequest b) {
    setRequest(b);
    return this;
  }
}
