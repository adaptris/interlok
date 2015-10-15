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

package com.adaptris.logging.jmx.log4j12;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.Layout;
import org.apache.log4j.Level;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.Priority;
import org.apache.log4j.spi.ErrorCode;
import org.apache.log4j.spi.LoggingEvent;

import com.adaptris.logging.jmx.JmxLogger;
import com.adaptris.logging.jmx.JmxLoggingEvent;

public class JmxLogAppender extends AppenderSkeleton {

  private JmxLogger logger;
  private Layout defaultLayout = new PatternLayout("%d{ISO8601} %-5p [%t] [%c] %m%n");
  private static final String OBJECT_NAME_STR = "com.adaptris:type=Logging,id=%1s";
  private static final String DEFAULT_OBJECTNAME = JmxLogAppender.class.getSimpleName();

  private String loggerId;
  private ObjectName objectName;

  public JmxLogAppender() {}

  @Override
  public void setThreshold(Priority level) {
    super.setThreshold(level);
  }

  public void setLoggerId(String objName) {
    loggerId = objName;
  }

  public String getLoggerId() {
    return loggerId;
  }

  @Override
  public void activateOptions() {
    if (getLayout() == null) {
      setLayout(defaultLayout);
    }
    if (getThreshold() == null) {
      setThreshold(Level.INFO);
    }
    try {
      objectName = buildObjectName();
      logger = new JmxLogger(objectName);
      if (!logger.isStarted()) {
        logger.start();
      }
    } catch (Exception ignore) {
      //
    }
  }

  @Override
  protected void append(LoggingEvent log4jEvent) {
    if (!ready())
      return;
    if (!shouldLog(log4jEvent))
      return;

    try {
      logger.log(wrap(log4jEvent));
    } catch (Exception ex) {
      errorHandler.error("Unable to send logging event to JMX", ex, ErrorCode.WRITE_FAILURE);
    }
  }

  private boolean ready() {
    return logger != null && logger.isStarted();
  }

  @Override
  public synchronized void close() {
    try {
      logger.stop();
    } catch (Exception ignored) {

    }
    this.closed = true;
  }


  @Override
  public boolean requiresLayout() {
    return true;
  }

  private boolean shouldLog(LoggingEvent event) {
    return event.getLevel().isGreaterOrEqual(this.getThreshold());
  }

  private JmxLoggingEvent wrap(LoggingEvent log4jEvent) {
    JmxLoggingEvent event = new JmxLoggingEvent();
    event.setSource(objectName.toString());
    event.setLevel(log4jEvent.getLevel().toString());
    event.setLoggerName(log4jEvent.getLoggerName());
    event.setMessage(layout.format(log4jEvent));
    event.setSourceThread(log4jEvent.getThreadName());
    return event;
  }

  private ObjectName buildObjectName() throws MalformedObjectNameException {
    String name = String.format(OBJECT_NAME_STR, getLoggerId() != null ? getLoggerId() : DEFAULT_OBJECTNAME);
    return ObjectName.getInstance(name);
  }

}

