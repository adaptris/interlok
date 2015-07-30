package com.adaptris.core.jms;

import static org.apache.commons.lang.StringUtils.abbreviate;
import static org.apache.commons.lang.StringUtils.isEmpty;

import java.util.concurrent.CountDownLatch;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Session;

import com.adaptris.core.AdaptrisConnection;
import com.adaptris.core.CoreException;

/**
 * Extension of ActiveJmsConnectionErrorHandler for test purposes.
 */
public class MockJmsConnectionErrorHandler extends ActiveJmsConnectionErrorHandler {

  private transient JmsConnectionCallback callback = null;
  private transient MockJmsConnectionVerifier verifier = null;
  @Override
  public void init() throws CoreException {
    String loggingId = retrieveConnection(AdaptrisConnection.class).getUniqueId();
    if (!isEmpty(loggingId)) {
      idForLogging = loggingId;
    }
    else {
      idForLogging = abbreviate(retrieveConnection(JmsConnection.class).getBrokerDetailsForLogging(), 20);
    }
    try {
      String s = "MockJmsConnectionErrorHandler";
      final String idForLogging = abbreviate(s, 20);
      MyExceptionHandler handler = new MyExceptionHandler();
      verifier = new MockJmsConnectionVerifier(idForLogging, new CountDownLatch(1));
      if (callback != null) {
        callback.register(verifier);
      }
      Thread verifierThread = new Thread(verifier);
      verifierThread.setUncaughtExceptionHandler(handler);
      verifierThread.start();
      if (additionalLogging()) {
        log.debug("ActiveJmsConnectionErrorHandler for " + idForLogging + " started");
      }
    }
    catch (Exception e) {
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
    if (verifier != null) verifier.finish();
  }

  public void registerCallback(JmsConnectionCallback c) {
    callback = c;
  }

  public interface JmsConnectionCallback {
    void register(MockJmsConnectionVerifier c);
  }

  public class MockJmsConnectionVerifier extends JmsConnectionVerifier {

    public MockJmsConnectionVerifier(String logString, CountDownLatch latch) {
      super(logString, latch);
    }

    @Override
    public JmsTemporaryDestination getTemporaryDestination() {
      return new MockJmsTemporaryDestination(super.getTemporaryDestination());
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

  public class MockJmsTemporaryDestination extends JmsTemporaryDestination {
    private JmsTemporaryDestination wrapped = null;
    public MockJmsTemporaryDestination(Session session) throws JMSException {
      super(session);
    }

    public MockJmsTemporaryDestination(JmsTemporaryDestination jmsD) {
      wrapped = jmsD;
    }

    @Override
    public Destination getDestination() {
      return wrapped == null ? super.getDestination() : wrapped.getDestination();
    }

    @Override
    public void deleteQuietly() {
      if (wrapped != null) {
        wrapped.deleteQuietly();
      }
      else {
        super.deleteQuietly();
      }
    }
  }
}