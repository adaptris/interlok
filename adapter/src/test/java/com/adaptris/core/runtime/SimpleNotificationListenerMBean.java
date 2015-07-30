package com.adaptris.core.runtime;

import java.util.List;

import javax.management.Notification;

public interface SimpleNotificationListenerMBean {

  List<Notification> getNotifications();

}
