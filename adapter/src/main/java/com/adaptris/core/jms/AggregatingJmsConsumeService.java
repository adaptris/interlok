package com.adaptris.core.jms;

import javax.jms.JMSException;
import javax.jms.Session;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adaptris.core.AdaptrisConnection;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageListener;
import com.adaptris.core.CoreException;
import com.adaptris.core.ServiceException;
import com.adaptris.core.services.aggregator.AggregatingConsumeService;
import com.adaptris.core.services.aggregator.AggregatingConsumeServiceImpl;
import com.adaptris.core.util.LifecycleHelper;
import com.adaptris.util.license.License;
import com.adaptris.util.license.License.LicenseType;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Implentation of {@link AggregatingConsumeService} that allows you to consume a related message from a queue based on some
 * criteria.
 * 
 * @config aggregating-jms-consume-service
 * @license STANDARD
 */
@XStreamAlias("aggregating-jms-consume-service")
public class AggregatingJmsConsumeService extends AggregatingConsumeServiceImpl<JmsConnection> implements JmsActorConfig {

  @NotNull
  @Valid
  private AggregatingJmsConsumer jmsConsumer;
  @NotNull
  @Valid
  private AdaptrisConnection connection;
  private transient Session session;

  private transient Logger myLogger = LoggerFactory.getLogger(this.getClass());

  public AggregatingJmsConsumeService() {
  }

  @Override
  public void init() throws CoreException {
    if (connection == null) {
      throw new CoreException("Null Connection");
    }
    if (jmsConsumer == null) {
      throw new CoreException("Null consumer");
    }
    LifecycleHelper.init(connection);
    try {
      session = getConnection().retrieveConnection(JmsConnection.class).createSession(false, configuredAcknowledgeMode());
    }
    catch (JMSException e) {
      throw new CoreException(e);
    }
  }

  @Override
  public void start() throws CoreException {
    super.start();
    LifecycleHelper.start(connection);
  }

  @Override
  public void stop() {
    super.stop();
    LifecycleHelper.stop(connection);
  }

  @Override
  public void close() {
    LifecycleHelper.close(connection);
  }

  @Override
  public boolean isEnabled(License l) throws CoreException {
    return l.isEnabled(LicenseType.Standard) && (getConnection() != null ? getConnection().isEnabled(l) : true)
        && (getJmsConsumer() != null ? getJmsConsumer().isEnabled(l) : true);
  }

  @Override
  public void doService(AdaptrisMessage msg) throws ServiceException {
    try {
      start(jmsConsumer);
      jmsConsumer.aggregateMessages(msg, this);
    }
    finally {
      stop(jmsConsumer);
    }
  }

  // Not used, only really used internally by OracleAqImplementation do handle some
  // oracle specifics.
  @Override
  public MessageTypeTranslator configuredMessageTranslator() {
    return null;
  }

  @Override
  public int configuredAcknowledgeMode() {
    return AcknowledgeMode.getMode(AcknowledgeMode.Mode.AUTO_ACKNOWLEDGE.name());
  }

  // Not used, only really used internally.
  @Override
  public CorrelationIdSource configuredCorrelationIdSource() {
    return null;
  }

  // Not used, only really used internally.
  @Override
  public AdaptrisMessageListener configuredMessageListener() {
    return null;
  }

  @Override
  public Session currentSession() {
    return session;
  }

  // Not used, only really used internally.
  @Override
  public Logger currentLogger() {
    return myLogger;
  }

  // Not used, only really used internally.
  @Override
  public long rollbackTimeout() {
    return 0;
  }

  public AggregatingJmsConsumer getJmsConsumer() {
    return jmsConsumer;
  }

  public void setJmsConsumer(AggregatingJmsConsumer consumer) {
    this.jmsConsumer = consumer;
  }

  public AdaptrisConnection getConnection() {
    return connection;
  }

  public void setConnection(AdaptrisConnection connection) {
    this.connection = connection;
  }

}
