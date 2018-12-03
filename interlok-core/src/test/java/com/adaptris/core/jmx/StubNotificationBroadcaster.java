package com.adaptris.core.jmx;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import javax.management.Notification;
import javax.management.NotificationBroadcasterSupport;
import javax.management.NotificationFilter;
import javax.management.NotificationListener;

public class StubNotificationBroadcaster extends NotificationBroadcasterSupport implements StubNotificationBroadcasterMBean {

  private transient AtomicInteger sequenceNumber = new AtomicInteger();

  private transient List<NotificationListener> listeners = Collections.synchronizedList(new ArrayList<NotificationListener>());

  public StubNotificationBroadcaster() {

  }
  public void sendNotification(String msg, Object userData) {
    Notification n = new Notification(msg, msg, sequenceNumber.getAndIncrement(), msg);
    n.setUserData(userData);
    super.sendNotification(n);
  }


  public void addNotificationListener(NotificationListener listener, NotificationFilter filter, Object handback)
      throws java.lang.IllegalArgumentException {
    listeners.add(listener);
    super.addNotificationListener(listener, filter, handback);
  }

  public boolean hasListeners() {
    return listeners.size() > 0;
  }
}
