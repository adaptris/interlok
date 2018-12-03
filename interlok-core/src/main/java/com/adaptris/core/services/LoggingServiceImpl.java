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
import org.slf4j.MarkerFactory;

import com.adaptris.annotation.AutoPopulated;
import com.adaptris.annotation.InputFieldDefault;
import com.adaptris.core.CoreException;
import com.adaptris.core.ServiceImp;

/**
 * 
 */
public abstract class LoggingServiceImpl extends ServiceImp {

  public static enum LoggingLevel {
    /** Deprecated since 3.6.2 with no replacement */
    @Deprecated
    FATAL {
      @Override
      void log(Logger logger, String s) {
        logger.error(MarkerFactory.getMarker("FATAL"), s);
      }

      @Override
      boolean isEnabled(Logger logger) {
        // fatal is always enabled.
        return true;
      }

    },
    ERROR {
      @Override
      void log(Logger logger, String s) {
        logger.error(s);
      }

      @Override
      boolean isEnabled(Logger logger) {
        return logger.isErrorEnabled();
      }
    },
    WARN {
      @Override
      void log(Logger logger, String s) {
        logger.warn(s);
      }

      @Override
      boolean isEnabled(Logger logger) {
        return logger.isWarnEnabled();
      }
    },
    INFO {
      @Override
      void log(Logger logger, String s) {
        logger.info(s);
      }

      @Override
      boolean isEnabled(Logger logger) {
        return logger.isInfoEnabled();
      }
    },
    DEBUG {
      @Override
      void log(Logger logger, String s) {
        logger.debug(s);
      }

      @Override
      boolean isEnabled(Logger logger) {
        return logger.isDebugEnabled();
      }

    },
    TRACE {
      @Override
      void log(Logger logger, String s) {
        logger.trace(s);
      }

      @Override
      boolean isEnabled(Logger logger) {
        return logger.isTraceEnabled();
      }
    };
    abstract void log(Logger logger, String s);

    abstract boolean isEnabled(Logger logger);

  }

  @InputFieldDefault(value = "DEBUG")
  @AutoPopulated
  private LoggingLevel logLevel;

  public LoggingServiceImpl() {
    super();
    setLogLevel(LoggingLevel.DEBUG);
  }

  protected static LoggingLevel getLogger(LoggingLevel level) {
    return level != null ? level : LoggingLevel.DEBUG;
  }


  @Override
  protected void initService() throws CoreException {
  }

  @Override
  protected void closeService() {
  }

  @Override
  public void prepare() throws CoreException {
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

}
