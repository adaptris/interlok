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
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import javax.management.Notification;
import javax.management.NotificationListener;
import org.awaitility.Awaitility;

public class SimpleNotificationListener implements NotificationListener, Serializable, SimpleNotificationListenerMBean {
  private static final long serialVersionUID = 2014031901L;

  private List<Notification> notifications = new IgnoreNulls<Notification>();

  @Override
  public void handleNotification(Notification notification, Object handback) {
    notifications.add(notification);
  }

  @Override
  public List<Notification> getNotifications() {
    return new ArrayList(notifications);
  }

  public List<Notification> notificationsSortedBySeqNo() {
    List<Notification> result = new ArrayList(notifications);
    Collections.sort(result, new SortBySeqNo());
    return result;
  }

  public void waitForMessages(int count, int maxSeconds) {
    Awaitility.await().atMost(Duration.ofSeconds(maxSeconds)).with().pollInterval(Duration.ofMillis(100))
        .until(() -> notifications.size() >= count);
  }

  private static class SortBySeqNo implements Comparator<Notification> {

    @Override
    public int compare(Notification o1, Notification o2) {
      return Long.compare(o1.getSequenceNumber(), o2.getSequenceNumber());
    }

  }

  private static class IgnoreNulls<E> extends ArrayList<E> {
    private static final long serialVersionUID = 2019071801L;
    @Override
    public boolean add(E e) {
      if (e == null) {
        return false;
      }
      return super.add(e);
    }
  }
}
