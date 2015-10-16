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

package com.adaptris.core.runtime;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.management.Notification;
import javax.management.NotificationListener;

public class SimpleNotificationListener implements NotificationListener, Serializable, SimpleNotificationListenerMBean {
  private static final long MAX_WAIT = 65000;
  private static final int DEFAULT_WAIT_INTERVAL = 100;
  private static final long serialVersionUID = 2014031901L;

  private List<Notification> notifications = new ArrayList<Notification>();

  @Override
  public void handleNotification(Notification notification, Object handback) {
    notifications.add(notification);
  }

  public List<Notification> getNotifications() {
    return new ArrayList(notifications);
  }

  public List<Notification> notificationsSortedBySeqNo() {
    List<Notification> result = new ArrayList(notifications);
    Collections.sort(result, new SortBySeqNo());
    return result;
  }

  public void waitForMessages(int count) throws Exception {
    long totalWaitTime = 0;
    while (notifications.size() < count && totalWaitTime < MAX_WAIT) {
      Thread.sleep(DEFAULT_WAIT_INTERVAL);
      totalWaitTime += DEFAULT_WAIT_INTERVAL;
    }
  }

  private static class SortBySeqNo implements Comparator<Notification> {

    @Override
    public int compare(Notification o1, Notification o2) {
      return Long.compare(o1.getSequenceNumber(), o2.getSequenceNumber());
    }

  }
}
