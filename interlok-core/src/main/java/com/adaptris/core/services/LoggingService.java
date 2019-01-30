/*
 * Copyright 2018 Adaptris Ltd.
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

import org.slf4j.Logger;

import com.adaptris.annotation.AdapterComponent;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.annotation.DisplayOrder;
import com.adaptris.annotation.InputFieldDefault;
import com.adaptris.annotation.InputFieldHint;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.ServiceException;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Log some arbitrary information.
 * 
 * @config logging-service
 * 
 */
@XStreamAlias("logging-service")
@AdapterComponent
@ComponentProfile(summary = "Log a message to the log file; useful for debugging", tag = "service,logging,debug")
@DisplayOrder(order = {"logLevel", "text", "logCategory"})
public class LoggingService extends LoggingServiceImpl {

  @InputFieldHint(expression = true, style = "BLANKABLE")
  @InputFieldDefault(value = "")
  private String text;

  public LoggingService() {
    super();
    setLogLevel(LoggingLevel.DEBUG);
  }

  public LoggingService(LoggingLevel level, String text) {
    this();
    setLogLevel(level);
    setText(text);
  }

  /**
   * @see com.adaptris.core.Service #doService(com.adaptris.core.AdaptrisMessage)
   */
  public void doService(AdaptrisMessage msg) throws ServiceException {
    LoggingLevel myLogger = getLogger(getLogLevel());
    Logger realLogger = slf4jLogger();
    if (myLogger.isEnabled(realLogger)) {
      myLogger.log(realLogger, msg.resolve(getText()));
    }
  }

  public String getText() {
    return text;
  }

  public void setText(String s) {
    this.text = s;
  }

}
