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
