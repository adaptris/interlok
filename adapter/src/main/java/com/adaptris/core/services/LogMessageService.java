package com.adaptris.core.services;

import static org.apache.commons.lang.StringUtils.defaultIfEmpty;

import org.slf4j.Logger;
import org.slf4j.MarkerFactory;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreException;
import com.adaptris.core.ServiceException;
import com.adaptris.core.ServiceImp;
import com.adaptris.util.license.License;
import com.adaptris.util.license.License.LicenseType;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * <p>
 * Service which logs the <code>AdaptrisMessage</code> to the configured logger for debugging purposes.
 * </p>
 * 
 * @config log-message-service
 * @license BASIC
 */
@XStreamAlias("log-message-service")
public class LogMessageService extends ServiceImp {

  public static enum LoggingLevel {
    FATAL {
      @Override
      void log(Logger logger, String s) {
        logger.error(MarkerFactory.getMarker("FATAL"), s);
      }
    },
    ERROR {
      @Override
      void log(Logger logger, String s) {
        logger.error(s);
      }
    },
    WARN {
      @Override
      void log(Logger logger, String s) {
        logger.warn(s);
      }
    },
    INFO {
      @Override
      void log(Logger logger, String s) {
        logger.info(s);
      }
    },
    DEBUG {
      @Override
      void log(Logger logger, String s) {
        logger.debug(s);
      }
    },
    TRACE {
      @Override
      void log(Logger logger, String s) {
        logger.trace(s);
      }
    };
    abstract void log(Logger logger, String s);
  }

  private String logPrefix;
  private Boolean includePayload;
  private Boolean includeEvents;
  private LoggingLevel logLevel;

  public LogMessageService() {
    super();
    setLogLevel(LoggingLevel.DEBUG);
  }

  public LogMessageService(LoggingLevel level) {
    this();
    setLogLevel(level);
  }

  public LogMessageService(LoggingLevel level, String loggingPrefix) {
    this();
    setLogLevel(level);
    setLogPrefix(loggingPrefix);
  }

  public LogMessageService(String uniqueId) {
    this();
    setUniqueId(uniqueId);
  }

  private static LoggingLevel getLogger(LoggingLevel level) {
    return level != null ? level : LoggingLevel.DEBUG;
  }

  /**
   * @see com.adaptris.core.Service #doService(com.adaptris.core.AdaptrisMessage)
   */
  public void doService(AdaptrisMessage msg) throws ServiceException {
    getLogger(getLogLevel()).log(log, defaultIfEmpty(getLogPrefix(), "") + msg.toString(includePayload(), includeEvents()));
  }

  /** @see com.adaptris.core.AdaptrisComponent#init() */
  public void init() throws CoreException {
    // na
  }

  /** @see com.adaptris.core.AdaptrisComponent#close() */
  public void close() {
    // na
  }

  public String getLogPrefix() {
    return logPrefix;
  }

  /**
   * Set the logging prefix to the output
   *
   * @param s the logging prefix, default ''
   */
  public void setLogPrefix(String s) {
    logPrefix = s;
  }

  public Boolean getIncludePayload() {
    return includePayload;
  }

  /**
   * Whether or not to include the payload in the logging.
   *
   * @param b true to include the payload, default is true.
   */
  public void setIncludePayload(Boolean b) {
    includePayload = b;
  }

  boolean includePayload() {
    return getIncludePayload() != null ? getIncludePayload().booleanValue() : true;
  }

  public Boolean getIncludeEvents() {
    return includeEvents;
  }

  /**
   * Whether or not to include the events in the logging.
   *
   * @param b true to include events, default is false.
   */
  public void setIncludeEvents(Boolean b) {
    includeEvents = b;
  }

  boolean includeEvents() {
    return getIncludeEvents() != null ? getIncludeEvents().booleanValue() : false;
  }

  public LoggingLevel getLogLevel() {
    return logLevel;
  }

  /**
   * Set the log level for logging.
   * 
   * @param level the log level, default is DEBUG but can be any one of ERROR, WARN, INFO, DEBUG, TRACE
   * @see LoggingLevel
   */
  public void setLogLevel(LoggingLevel level) {
    logLevel = level;
  }

  @Override
  public boolean isEnabled(License license) throws CoreException {
    return license.isEnabled(LicenseType.Basic);
  }
}
