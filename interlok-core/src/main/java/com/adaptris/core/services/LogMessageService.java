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

package com.adaptris.core.services;

import static org.apache.commons.lang.StringUtils.defaultIfEmpty;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.slf4j.Logger;

import com.adaptris.annotation.AdapterComponent;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.annotation.DisplayOrder;
import com.adaptris.annotation.InputFieldDefault;
import com.adaptris.annotation.InputFieldHint;
import com.adaptris.annotation.Removal;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.MessageLogger;
import com.adaptris.core.ServiceException;
import com.adaptris.core.util.LoggingHelper;
import com.adaptris.core.util.PayloadMessageLogger;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * <p>
 * Service which logs the <code>AdaptrisMessage</code> to the configured logger for debugging purposes.
 * </p>
 * 
 * @config log-message-service
 * 
 */
@XStreamAlias("log-message-service")
@AdapterComponent
@ComponentProfile(summary = "Log the message to the log file; useful for debugging", tag = "service,logging,debug")
@DisplayOrder(order = {"logLevel", "logPrefix", "logCategory", "loggingFormat", "includeEvents",
    "includePayload"})
public class LogMessageService extends LoggingServiceImpl {

  private static final MessageLogger DEFAULT_MSG_LOGGER = new PayloadMessageLogger();

  @InputFieldHint(style="BLANKABLE")
  private String logPrefix;
  @Deprecated
  @Removal(version = "3.11.0", message = "Use a logging-format instead")
  private Boolean includePayload;
  @Deprecated
  @Removal(version = "3.11.0", message = "Use a logging-format instead")
  private Boolean includeEvents;
  @InputFieldDefault(value = "message-logging-with-payload")
  private MessageLogger loggingFormat;

  private transient boolean warningLogged;

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

  /**
   * @see com.adaptris.core.Service #doService(com.adaptris.core.AdaptrisMessage)
   */
  public void doService(AdaptrisMessage msg) throws ServiceException {
    LoggingLevel myLogger = getLogger(getLogLevel());
    Logger realLogger = slf4jLogger();
    if (myLogger.isEnabled(realLogger)) {
      myLogger.log(realLogger, defaultIfEmpty(getLogPrefix(), "") + loggingFormat().toString(msg));
    }
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

  @Deprecated
  @Removal(version = "3.11.0", message = "Use a logging-format instead")
  public Boolean getIncludePayload() {
    return includePayload;
  }

  /**
   * Whether or not to include the payload in the logging.
   *
   * @param b true to include the payload, default is true.
   * @deprecated since 3.8.4 use {@link #setLoggingFormat(MessageLogger)} instead.
   */
  @Deprecated
  @Removal(version = "3.11.0", message = "Use a logging-format instead")
  public void setIncludePayload(Boolean b) {
    includePayload = b;
  }

  @Deprecated
  @Removal(version = "3.11.0", message = "Use a logging-format instead")
  public Boolean getIncludeEvents() {
    return includeEvents;
  }

  /**
   * Whether or not to include the events in the logging.
   *
   * @param b true to include events, default is false.
   * @deprecated since 3.8.4 use {@link #setLoggingFormat(MessageLogger)} instead.
   */
  @Deprecated
  @Removal(version = "3.11.0", message = "Use a logging-format instead")
  public void setIncludeEvents(Boolean b) {
    includeEvents = b;
  }

  public MessageLogger getLoggingFormat() {
    return loggingFormat;
  }

  public void setLoggingFormat(MessageLogger ml) {
    this.loggingFormat = ml;
  }

  @SuppressWarnings("deprecation")
  public MessageLogger loggingFormat() {
    if (getIncludePayload() != null || getIncludeEvents() != null) {
      LoggingHelper.logWarning(warningLogged, () -> {
        warningLogged = true;
      }, "Use logging-format instead of include-payload/include-events");
      return (m) -> {
        return m.toString(BooleanUtils.toBooleanDefaultIfNull(getIncludePayload(), true), 
            BooleanUtils.toBooleanDefaultIfNull(getIncludeEvents(), false));
      };
    }
    return ObjectUtils.defaultIfNull(getLoggingFormat(), DEFAULT_MSG_LOGGER);
  }

}
