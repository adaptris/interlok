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
package com.adaptris.logging.jmx;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

class LoggingLevel implements Comparable<LoggingLevel> {

  static final Comparator<LoggingLevel> COMPARATOR = new Comparator<LoggingLevel>() {
    public int compare(LoggingLevel left, LoggingLevel right) {
      if (null == left) {
        return (null == right ? 0 : -1);
      } else if (null == right) {
        return 1;
      } else if (left == right) {
        return 0;
      } else {
        return (left.precedence - right.precedence);
      }
    }
  };

  static final LoggingLevel UNKNOWN = new LoggingLevel("UNKNOWN", 00);
  static final LoggingLevel TRACE = new LoggingLevel("TRACE", 10);
  static final LoggingLevel DEBUG = new LoggingLevel("DEBUG", 20);
  static final LoggingLevel INFO = new LoggingLevel("INFO", 30);
  static final LoggingLevel WARN = new LoggingLevel("WARN", 40);
  static final LoggingLevel ERROR = new LoggingLevel("ERROR", 50);
  static final LoggingLevel FATAL = new LoggingLevel("FATAL", 60);

  static final List<LoggingLevel> levels = Arrays.asList(UNKNOWN, TRACE, DEBUG, INFO, WARN, ERROR, FATAL);

  public int compareTo(LoggingLevel other) {
    return COMPARATOR.compare(this, other);
  }

  private final int precedence;
  private final String name;

  private LoggingLevel(String name, int precedence) {
    this.name = name;
    this.precedence = precedence;
  }

  @Override
  public String toString() {
    return name;
  }

  static LoggingLevel getLevel(String levelName) {
    for (LoggingLevel l : levels) {
      if (l.name.equalsIgnoreCase(levelName)) {
        return l;
      }
    }
    return UNKNOWN;
  }
}

