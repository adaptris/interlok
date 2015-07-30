package com.adaptris.core.jms;

import javax.jms.Session;

import org.slf4j.Logger;

import com.adaptris.core.AdaptrisMessageListener;

/**
 * Interface specifying common configuration for JMS Workers
 *
 * @author lchan
 *
 */
public interface JmsActorConfig {

  /**
   * Return the currently configured messageTranslator.
   *
   * @return the MessageTypeTranslator instance.
   */
  MessageTypeTranslator configuredMessageTranslator();

  /**
   * Return the current acknowledge mode.
   *
   * @return the session acknowledge mode.
   */
  int configuredAcknowledgeMode();

  /**
   * Return the current correlation id source.
   *
   * @return the correlation id source
   */
  CorrelationIdSource configuredCorrelationIdSource();

  /**
   * return the current configured MessageListener.
   *
   * @return the adaptris message listener instance.
   */
  AdaptrisMessageListener configuredMessageListener();

  /**
   * Return the current jms session.
   *
   * @return the current javax.jms.Session
   */
  Session currentSession();

  /**
   * Return the configured logger.
   *
   * @return the logger.
   */
  Logger currentLogger();

  /**
   * How long we should wait after a rollback before continuing on with
   * processing
   *
   * @return the timeout in ms.
   */
  long rollbackTimeout();

}
