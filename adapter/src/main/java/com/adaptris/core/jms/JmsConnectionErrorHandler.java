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

package com.adaptris.core.jms;

import javax.jms.ExceptionListener;
import javax.jms.JMSException;

import com.adaptris.core.CoreException;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Standard implementation of ConnectionErrorHandler which implements {@link ExceptionListener}.
 * 
 * @config jms-connection-error-handler
 */
@XStreamAlias("jms-connection-error-handler")
public class JmsConnectionErrorHandler extends JmsConnectionErrorHandlerImpl implements ExceptionListener {


  @Override
  public void init() throws CoreException {
    super.init();
    try {
      retrieveConnection(JmsConnection.class).currentConnection().setExceptionListener(this);
    }
    catch (JMSException e) {
      throw new CoreException(e);
    }
  }

  @Override
  public void start() throws CoreException {
  }

  @Override
  public void stop() {
  }

  @Override
  public void close() {
  }

  @Override
  public void onException(JMSException e) {
    try {
      Thread.currentThread().setName("JMSExceptionListener for " + idForLogging);
      log.error("JMS connection exception", e);
      if (e.getLinkedException() != null) {
        log.debug("JMS Linked Exception ", e.getLinkedException());
      }
      handleConnectionException();
    }
    catch (Exception x) {
      log.error("Unexpected Exception thrown back to onException", x);
    }
  }
}
