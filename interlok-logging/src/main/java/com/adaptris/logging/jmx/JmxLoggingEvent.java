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

package com.adaptris.logging.jmx;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import javax.management.Notification;

/**
 * A Logging event that can be published to JMX.
 * 
 * @author lchan
 *
 */
public class JmxLoggingEvent implements Serializable, Comparable<JmxLoggingEvent> {
  public static final String OBJECT_NAME_STR = "com.adaptris:type=Logging,id=%1s";
  public static final String DEFAULT_OBJECTNAME = "JmxLogAppender";
  private static final long serialVersionUID = 2015091801L;

  private Long timestamp;
  private String eventType;
  private String message;
  private String source;
  private String loggerName;
  private String level;
  private String sourceThread;


  private enum UserDataBuilder {
   
    Timestamp {
      @Override
      void put(JmxLoggingEvent event, Map<String, Object> userData) {
        userData.put(name(), event.getTimestamp());
      }
    },
    EventType {
      @Override
      void put(JmxLoggingEvent event, Map<String, Object> userData) {
        userData.put(name(), event.getEventType());
      }
    },
    Message {
      @Override
      void put(JmxLoggingEvent event, Map<String, Object> userData) {
        userData.put(name(), event.getMessage());
      }
    },
    Source {
      @Override
      void put(JmxLoggingEvent event, Map<String, Object> userData) {
        userData.put(name(), event.getSource());
      }
    },
    LoggerName {
      @Override
      void put(JmxLoggingEvent event, Map<String, Object> userData) {
        userData.put(name(), event.getLoggerName());
      }
    },
    Level {
      @Override
      void put(JmxLoggingEvent event, Map<String, Object> userData) {
        userData.put(name(), event.getLevel());
      }
    },
    SourceThread {
      @Override
      void put(JmxLoggingEvent event, Map<String, Object> userData) {
        userData.put(name(), event.getSourceThread());
      }
    };
    abstract void put(JmxLoggingEvent event, Map<String, Object> userData);
  }



  public JmxLoggingEvent() {
    setTimestamp(System.currentTimeMillis());
    setEventType("com.adaptris.logging.event");
  }

  public Long getTimestamp() {
    return timestamp;
  }

  public void setTimestamp(Long timestamp) {
    this.timestamp = timestamp;
  }

  public String getEventType() {
    return eventType;
  }

  public void setEventType(String eventType) {
    this.eventType = eventType;
  }

  public String getMessage() {
    return message;
  }

  public void setMessage(String m) {
    this.message = m;
  }

  public String getSource() {
    return source;
  }

  public void setSource(String source) {
    this.source = source;
  }

  @Override
  public int compareTo(JmxLoggingEvent o) {
    return getTimestamp().compareTo(o.getTimestamp());
  }

  public String getLoggerName() {
    return loggerName;
  }

  public void setLoggerName(String loggerName) {
    this.loggerName = loggerName;
  }

  public String getLevel() {
    return level;
  }

  public void setLevel(String level) {
    this.level = level;
  }

  public String getSourceThread() {
    return sourceThread;
  }

  public void setSourceThread(String sourceThread) {
    this.sourceThread = sourceThread;
  }

  public Notification buildDefaultNotification() {
    Notification note =
        new Notification(getEventType(), getSource(), System.currentTimeMillis(), getTimestamp(), getMessage());
    note.setUserData(buildUserData());
    return note;
  }

  private Map<String, Object> buildUserData() {
    Map<String, Object> userData = new HashMap<>();
    for (UserDataBuilder m : UserDataBuilder.values()) {
      m.put(this, userData);
    }
    return userData;
  }
}
