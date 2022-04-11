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

import static org.apache.commons.lang3.StringUtils.defaultIfEmpty;

import org.apache.commons.lang3.ObjectUtils;
import org.slf4j.Logger;

import com.adaptris.annotation.AdapterComponent;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.annotation.DisplayOrder;
import com.adaptris.annotation.InputFieldDefault;
import com.adaptris.annotation.InputFieldHint;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.MessageLogger;
import com.adaptris.core.ServiceException;
import com.adaptris.core.util.PayloadMessageLogger;
import com.thoughtworks.xstream.annotations.XStreamAlias;

import lombok.Getter;
import lombok.Setter;

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

  /**
   * Set the logging prefix to the output
   *
   * @param logPrefix
   *          the logging prefix, default ''
   * @return the logging prefix
   */
  @Getter
  @Setter
  @InputFieldHint(style="BLANKABLE")
  private String logPrefix;
  /**
   * Set the MessageLogger used to format the output logging
   *
   * @param loggingFormat
   *          MessageLogger, default '{@link PayloadMessageLogger}: <i>message-logging-with-payload</i>'
   * @return the MessageLogger used to format the logging
   */
  @Getter
  @Setter
  @InputFieldDefault(value = "message-logging-with-payload")
  private MessageLogger loggingFormat;

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
  @Override
  public void doService(AdaptrisMessage msg) throws ServiceException {
    LoggingLevel myLogger = getLogger(getLogLevel());
    Logger realLogger = slf4jLogger();
    if (myLogger.isEnabled(realLogger)) {
      myLogger.log(realLogger, defaultIfEmpty(getLogPrefix(), "") + loggingFormat().toString(msg));
    }
  }

  public MessageLogger loggingFormat() {
    return ObjectUtils.defaultIfNull(getLoggingFormat(), DEFAULT_MSG_LOGGER);
  }

}
