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

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import javax.management.NotificationBroadcasterSupport;

class JmxLoggingNotification extends NotificationBroadcasterSupport implements JmxLoggingNotificationMBean {

  private List<List<String>> errorLogs;
  private FixedSizeList<String> currentLog;
  private int linesOfContext;
  private int errors;

  JmxLoggingNotification(int lines, int errors) {
    this.errors = errors;
    this.linesOfContext = lines;
    errorLogs = Collections.synchronizedList(new FixedSizeList<List<String>>(errors));
    currentLog = new FixedSizeList<>(linesOfContext);
  }

  private void sendNotification(JmxLoggingEvent event) {
    sendNotification(event.buildDefaultNotification());
  }

  public void handle(JmxLoggingEvent event) {
    sendNotification(event);   
    currentLog.add(event.getMessage());
    LoggingLevel level = LoggingLevel.getLevel(event.getLevel());
    if (level.compareTo(LoggingLevel.ERROR) >=0) {
      // It's an error, let's store the current logging into the error logs buffer.
      // and swap things up.
      errorLogs.add(currentLog);
      currentLog = new FixedSizeList(linesOfContext);
    }
  }

  public List<String> getErrorLog(int index) {
    if (index > errorLogs.size() || errorLogs.isEmpty()) {
      return Collections.EMPTY_LIST;
    }
    return new ArrayList<String>(errorLogs.get(index));
  }

  public int errorCount() {
    return errorLogs.size();
  }

  public List<String> remove(int index) {
    if (index > errorLogs.size() || errorLogs.isEmpty()) {
      return Collections.EMPTY_LIST;
    }
    return new ArrayList<String>(errorLogs.remove(index));
  }

  private class FixedSizeList<K> extends LinkedList<K> {
    private int maxSize;

    public FixedSizeList(int size) {
      this.maxSize = size;
    }

    public boolean add(K k) {
      while (size() > maxSize) {
        super.remove();
      }
      return super.add(k);
    }
  }
}

