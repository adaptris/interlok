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

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import javax.jms.DeliveryMode;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TemporaryTopic;

import com.adaptris.core.ConnectionErrorHandler;
import com.adaptris.core.CoreException;
import com.adaptris.core.util.ExceptionHelper;
import com.adaptris.core.util.ManagedThreadFactory;
import com.adaptris.util.TimeInterval;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * {@link ConnectionErrorHandler} implementation that actively attempts messages via JMS to detect outages.
 * 
 * <p>
 * Implementation of {@link ConnectionErrorHandler} which tests the connection every {@link #getCheckInterval()} interval (default
 * is 5 second) and if the test fails then restarts the Connection's owner and stops the testing thread.
 * </p>
 * 
 * @config active-jms-connection-error-handler
 */
@XStreamAlias("active-jms-connection-error-handler")
public class ActiveJmsConnectionErrorHandler extends JmsConnectionErrorHandlerImpl {

  private static final TimeInterval DEFAULT_CHECK_INTERVAL = new TimeInterval(5L, TimeUnit.SECONDS);
  private static final TimeInterval DEFAULT_MAX_WAIT_FOR_START = new TimeInterval(5L, TimeUnit.MINUTES);
  private Boolean additionalLogging;
  private TimeInterval checkInterval;
  private transient JmsConnectionVerifier verifier;

  @Override
  public void init() throws CoreException {
    super.init();
    try {
      MyExceptionHandler handler = new MyExceptionHandler();
      CountDownLatch verifierThreadGate = new CountDownLatch(1);
      verifier = new JmsConnectionVerifier(idForLogging, verifierThreadGate);
      Thread verifierThread = new ManagedThreadFactory().newThread(verifier);
      verifierThread.setName("JmsConnectionErrorHandler for " + idForLogging);
      verifierThread.setUncaughtExceptionHandler(handler);
      verifierThread.start();
      boolean actuallyStarted = verifierThreadGate.await(DEFAULT_MAX_WAIT_FOR_START.toMilliseconds(), TimeUnit.MILLISECONDS);
      if (!actuallyStarted) {
        if (handler.hasError()) {
          ExceptionHelper.rethrowCoreException(handler.lastCaughtException);
        }
        else {
          throw new CoreException("Failed to start connection error handler");
        }
      }
      if (additionalLogging()) {
        log.debug("ActiveJmsConnectionErrorHandler for " + idForLogging + " started");
      }
    }
    catch (Exception e) {
      ExceptionHelper.rethrowCoreException(e);
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
    if (verifier != null) {
      verifier.finish();
      verifier = null;
    }
  }

  public Boolean getAdditionalLogging() {
    return additionalLogging;
  }

  /**
   * Whether or not to log each attempt at verifying the connection.
   * 
   * @param b true to enable trace logging of every attempt to verify the connection status, default is null (false)
   */
  public void setAdditionalLogging(Boolean b) {
    additionalLogging = b;
  }

  public boolean additionalLogging() {
    return getAdditionalLogging() != null ? getAdditionalLogging().booleanValue() : false;
  }

  long retryInterval() {
    long period = 0;
    if (getCheckInterval() != null) {
      period = getCheckInterval().toMilliseconds();
    }
    if (period <= 0) {
      period = DEFAULT_CHECK_INTERVAL.toMilliseconds();
    }
    return period;
  }

  public TimeInterval getCheckInterval() {
    return checkInterval;
  }

  /**
   * Set interval between each attempt to veri
   * 
   * @param checkInterval the retry interval, if <=0 then the default is assumed (5 seconds).
   */
  public void setCheckInterval(TimeInterval checkInterval) {
    this.checkInterval = checkInterval;
  }

  protected class JmsConnectionVerifier implements Runnable {
    private String idForLogging;
    private JmsTemporaryDestination temporaryDest;
    private CountDownLatch startLatch;
    private boolean isActive;

    public JmsConnectionVerifier(String logString, CountDownLatch marker) {
      idForLogging = logString;
      startLatch = marker;
    }

    @Override
    public void run() {
      try {
        isActive = true;
        JmsConnection conn = retrieveConnection(JmsConnection.class);
        Session session = conn.createSession(false, Session.AUTO_ACKNOWLEDGE);
        temporaryDest = new JmsTemporaryDestination(session);
        startLatch.countDown();
        while (isActive) {
          try {
            if (additionalLogging()) {
              log.trace("Attempting to verify " + idForLogging + " using " + temporaryDest.getDestination());
            }
            MessageProducer messageProducer = session.createProducer(temporaryDest.getDestination());
            Message message = session.createMessage();
            messageProducer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
            messageProducer.setTimeToLive(5000L);
            messageProducer.send(message);
            JmsUtils.closeQuietly(messageProducer);
          }
          catch (Exception ex) {
            if (isActive) {
              handleConnectionException();
              finish();
            }
            break;
          }
          TimeUnit.MILLISECONDS.sleep(retryInterval());
        }
        temporaryDest.deleteQuietly();
        JmsUtils.closeQuietly(session);
      }
      catch (JMSException | InterruptedException ex) {
        Thread.currentThread().getUncaughtExceptionHandler().uncaughtException(Thread.currentThread(), ex);
      }
    }

    protected void finish() {
      isActive = false;
    }

    protected JmsTemporaryDestination getTemporaryDestination() {
      return temporaryDest;
    }
  }

  private class MyExceptionHandler implements Thread.UncaughtExceptionHandler {
    private Throwable lastCaughtException = null;

    /**
     * @see java.lang.Thread.UncaughtExceptionHandler#uncaughtException(java.lang.Thread, java.lang.Throwable)
     */
    @Override
    public void uncaughtException(Thread t, Throwable e) {
      log.warn("UnhandledException from " + t.getName(), e);
      lastCaughtException = e;
    }

    public boolean hasError() {
      return lastCaughtException != null;
    }

    public Throwable lastException() {
      return lastCaughtException;
    }
  }

  protected class JmsTemporaryDestination {
    private TemporaryTopic tmpTopic;

    JmsTemporaryDestination() {

    }

    JmsTemporaryDestination(Session session) throws JMSException {
      tmpTopic = session.createTemporaryTopic();
    }

    public Destination getDestination() {
      return tmpTopic;
    }

    public void deleteQuietly() {
      JmsUtils.deleteQuietly(tmpTopic);
    }
  }

}
