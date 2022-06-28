/*
 * Copyright 2015 Adaptris Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.adaptris.logging.log4j2;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.appender.AppenderLoggingException;
import org.apache.logging.log4j.core.config.Node;
import org.apache.logging.log4j.core.config.Property;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginElement;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;
import org.apache.logging.log4j.core.layout.AbstractStringLayout;
import org.apache.logging.log4j.core.layout.PatternLayout;

import com.adaptris.logging.jmx.JmxLogger;
import com.adaptris.logging.jmx.JmxLoggingEvent;
import com.adaptris.logging.jmx.JmxLoggingNotificationMBean;

@Plugin(name = "JmxLogAppender", category = Node.CATEGORY, elementType = Appender.ELEMENT_TYPE, printObject = true)
public final class JmxLogAppender extends AbstractAppender {

  private final ReadWriteLock rwLock = new ReentrantReadWriteLock();
  private final Lock readLock = rwLock.readLock();
  private ObjectName objectName;
  private JmxLogger logger;
  private final int linesOfContext;
  private final int errors;

  protected JmxLogAppender(String name, Filter filter, AbstractStringLayout layout, final boolean ignoreExceptions, int lines,
      int errs) {
    super(name, filter, layout, ignoreExceptions, Property.EMPTY_ARRAY);
    this.linesOfContext = lines != 0 ? lines : JmxLoggingNotificationMBean.DEFAULT_LOGMSG_COUNT;
    this.errors = errs != 0 ? errs : JmxLoggingNotificationMBean.DEFAULT_MAX_ERRORS_COUNT;
  }

  @Override
  public void append(LogEvent event) {
    readLock.lock();
    try {
      logger.log(wrap(event));
    } catch (Exception ex) {
      if (!ignoreExceptions()) {
        throw new AppenderLoggingException(ex);
      }
    } finally {
      readLock.unlock();
    }
  }

  @PluginFactory
  public static JmxLogAppender createAppender(@PluginAttribute("name") String name, @PluginAttribute("lines") int lines,
      @PluginAttribute("errors") int errors, @PluginElement("Layout") AbstractStringLayout layout,
      @PluginElement("Filter") final Filter filter) {
    if (name == null) {
      LOGGER.error("No name provided for JmxLogAppender");
      return null;
    }
    if (layout == null) {
      layout = PatternLayout.createDefaultLayout();
    }
    return new JmxLogAppender(name, filter, layout, true, lines, errors);
  }

  private ObjectName buildObjectName() throws MalformedObjectNameException {
    String name = String.format(JmxLoggingEvent.OBJECT_NAME_STR, getName());
    return ObjectName.getInstance(name);
  }

  private JmxLoggingEvent wrap(LogEvent log4jEvent) {
    JmxLoggingEvent event = new JmxLoggingEvent();
    event.setSource(objectName.toString());
    event.setLevel(log4jEvent.getLevel().toString());
    event.setLoggerName(log4jEvent.getLoggerName());
    // Should be ok to cast to a string, because the factory forces a AbstractStringLayout.
    event.setMessage((String) getLayout().toSerializable(log4jEvent));
    event.setSourceThread(log4jEvent.getThreadName());
    return event;
  }

  @Override
  public void start() {
    try {
      objectName = buildObjectName();
      logger = new JmxLogger(objectName, linesOfContext, errors);
      if (!logger.isStarted()) {
        logger.start();
      }
    } catch (Exception ignore) {
      //
    }
    super.start();
  }


  @Override
  public void stop() {
    super.stop();
    try {
      logger.stop();
    } catch (Exception ignored) {

    }
  }

}
