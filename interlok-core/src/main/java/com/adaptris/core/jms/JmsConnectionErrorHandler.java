/*
 * Copyright 2015 Adaptris Ltd.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.adaptris.core.jms;

import java.util.concurrent.atomic.AtomicBoolean;
import javax.jms.ExceptionListener;
import javax.jms.JMSException;
import org.apache.commons.lang3.BooleanUtils;
import com.adaptris.annotation.AdvancedConfig;
import com.adaptris.annotation.InputFieldDefault;
import com.adaptris.core.CoreException;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Standard implementation of ConnectionErrorHandler which implements {@link ExceptionListener}.
 * 
 * @config jms-connection-error-handler
 */
@XStreamAlias("jms-connection-error-handler")
public class JmsConnectionErrorHandler extends JmsConnectionErrorHandlerImpl implements ExceptionListener {

  @AdvancedConfig(rare = true)
  @InputFieldDefault(value = "true")
  private Boolean singleExecution;
  private transient AtomicBoolean inOnException = new AtomicBoolean(false);


  public JmsConnectionErrorHandler() {

  }

  public JmsConnectionErrorHandler(Boolean singleExecution) {
    this();
    setSingleExecution(singleExecution);
  }

  @Override
  public void init() throws CoreException {
    super.init();
    try {
      retrieveConnection(JmsConnection.class).currentConnection().setExceptionListener(this);
      inOnException.set(false);
    } catch (JMSException e) {
      throw new CoreException(e);
    }
  }

  @Override
  public void start() throws CoreException {}

  @Override
  public void stop() {}

  @Override
  public void close() {}

  @Override
  public void onException(JMSException e) {
    try {
      Thread.currentThread().setName("JMSExceptionListener for " + idForLogging);
      if (singleExecution()) {
        if (inOnException.compareAndSet(false, true)) {
          try {
            retrieveConnection(JmsConnection.class).currentConnection().setExceptionListener(null);
          } catch (JMSException ignore) {

          }
          handleOnException(e);
        } else {
          log.debug("Already handling an exception; ignoring additional onException()");
        }
      } else {
        handleOnException(e);
      }
    } finally {
      inOnException.set(false);
    }
  }

  private void handleOnException(JMSException e) {
    try {
      log.error("JMS connection exception", e);
      if (e.getLinkedException() != null) {
        log.debug("JMS Linked Exception ", e.getLinkedException());
      }
      handleConnectionException();
    } catch (Exception x) {
      log.error("Unexpected Exception thrown back to onException", x);
    }
  }

  /**
   * @return the singleExecution value.
   */
  public Boolean getSingleExecution() {
    return singleExecution;
  }

  /**
   * Ignore multiple invocations of the {@link ExceptionListener#onException(JMSException)} method.
   * <p>
   * There is no JMS specification guarantee that a provider will not issue multiple calls to
   * {@link ExceptionListener#onException(JMSException)} in short order. In some cases this does happen, which can cause subequent
   * problems based on timing around when the connections re-initialise themselves.
   * </p>
   * 
   * @param b whether or not to filter multiple executions; defaults to true if not specified.
   */
  public void setSingleExecution(Boolean b) {
    this.singleExecution = b;
  }

  boolean singleExecution() {
    return BooleanUtils.toBooleanDefaultIfNull(getSingleExecution(), true);
  }
}
