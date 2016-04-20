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

package com.adaptris.util.stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MarkerFactory;

import com.adaptris.core.util.Args;

/**
 * An OutputStream that flushes out to a slf4j logger.
 * <p>
 * Note that no data is written out to the Category until the stream is flushed or closed.
 * </p>
 * <br/>
 * Example:
 * 
 * <pre>
 * {@code 
 * // make sure everything sent to System.err is logged
 * System.setErr(new PrintStream(new LoggingOutputStream(LogHandler.WARN));
 * // make sure everything sent to System.out is also logged
 * System.setOut(new PrintStream(new LoggingOutputStream(LogHandler.INFO));
 * }
 * </pre>
 * 
 */
public class Slf4jLoggingOutputStream extends LoggingOutputStreamImpl {

  private enum Lumberjack {
    FATAL(LogLevel.FATAL) {
      @Override
      void log(Logger logger, String s) {
        logger.error(MarkerFactory.getMarker("FATAL"), s);
      }
    },
    ERROR(LogLevel.ERROR) {
      @Override
      void log(Logger logger, String s) {
        logger.error(s);
      }
    },
    WARN(LogLevel.WARN) {
      @Override
      void log(Logger logger, String s) {
        logger.warn(s);
      }
    },
    INFO(LogLevel.INFO) {
      @Override
      void log(Logger logger, String s) {
        logger.info(s);
      }
    },
    DEBUG(LogLevel.DEBUG) {
      @Override
      void log(Logger logger, String s) {
        logger.debug(s);
      }
    },
    TRACE(LogLevel.TRACE) {
      @Override
      void log(Logger logger, String s) {
        logger.trace(s);
      }
    };
    private LogLevel lev;

    Lumberjack(LogLevel level) {
      lev = level;
    }

    boolean matches(LogLevel level) {
      return lev.equals(level);
    }

    abstract void log(Logger logger, String s);
  }

  private transient Logger logger;

  public Slf4jLoggingOutputStream(LogLevel level) {
    this(LoggerFactory.getLogger(Slf4jLoggingOutputStream.class), level);
  }

  public Slf4jLoggingOutputStream(String level) {
    this(LoggerFactory.getLogger(Slf4jLoggingOutputStream.class), level);
  }


  /**
   * Creates the LoggingOutputStream to flush to the given LogLevel.
   *
   * @param log the Logger to write to
   * @param level the Level to use when writing to the Logger
   * @throws IllegalArgumentException if log == null or level == null
   */
  public Slf4jLoggingOutputStream(Logger log, LogLevel level) throws IllegalArgumentException {
    super(level);
    logger = Args.notNull(log, "Logger");
    reset();
  }

  /**
   * Creates the LoggingOutputStream to flush to the given LogLevel.
   *
   * @param log the Logger to write to
   * @param level the Level to use when writing to the Logger
   * @throws IllegalArgumentException if log == null or level == null
   */
  public Slf4jLoggingOutputStream(Logger log, String level) throws IllegalArgumentException {
    super(LogLevel.valueOf(level));
    logger = Args.notNull(log, "Logger");
    reset();
  }


  @Override
  protected void log(LogLevel level, String s) {
    getLumberjack(level).log(logger, s);
  }

  private Lumberjack getLumberjack(LogLevel level) {
    Lumberjack handler = Lumberjack.INFO;
    for (Lumberjack h : Lumberjack.values()) {
      if (h.matches(level)) {
        handler = h;
        break;
      }
    }
    return handler;
  }
}
