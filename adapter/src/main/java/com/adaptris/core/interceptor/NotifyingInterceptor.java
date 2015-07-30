package com.adaptris.core.interceptor;

import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @since 3.0.4
 */
public abstract class NotifyingInterceptor extends WorkflowInterceptorImpl {

  private transient InterceptorNotificationMBean notifier;
  protected transient Logger log = LoggerFactory.getLogger(this.getClass());

  void registerNotificationEmitter(InterceptorNotificationMBean notifier) {
    this.notifier = notifier;
  }

  void sendNotification(String msg, Properties userData) {
    if (notifier != null) {
      notifier.sendNotification(msg, userData);
    }
  }

}
