package com.adaptris.logging.jmx;

import javax.management.NotificationBroadcasterSupport;

class JmxLoggingNotification extends NotificationBroadcasterSupport implements JmxLoggingNotificationMBean {

  public JmxLoggingNotification() {
  }

  public void sendNotification(JmxLoggingEvent event) {
    sendNotification(event.buildDefaultNotification());
  }

}

