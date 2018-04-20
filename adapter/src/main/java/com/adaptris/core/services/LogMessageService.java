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

import com.adaptris.annotation.AdapterComponent;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.annotation.DisplayOrder;
import com.adaptris.annotation.InputFieldDefault;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.ServiceException;
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
@DisplayOrder(order = {"logLevel", "logPrefix", "includeEvents", "includePayload"})
public class LogMessageService extends LoggingServiceImpl {


  private String logPrefix;
  @InputFieldDefault(value = "true")
  private Boolean includePayload;
  @InputFieldDefault(value = "false")
  private Boolean includeEvents;

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
    if (myLogger.isEnabled(log)) {
      myLogger.log(log, defaultIfEmpty(getLogPrefix(), "") + msg.toString(includePayload(), includeEvents()));
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
}
