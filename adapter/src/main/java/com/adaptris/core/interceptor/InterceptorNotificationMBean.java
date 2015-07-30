package com.adaptris.core.interceptor;

import java.util.Properties;

import com.adaptris.core.runtime.ChildRuntimeInfoComponentMBean;

/**
 * Management MBean definition for publishing JMX notifications.
 * 
 * 
 */
public interface InterceptorNotificationMBean extends ChildRuntimeInfoComponentMBean {

  /**
   * The notification type for interceptor notifications '{@value} '.
   * 
   */
  String NOTIF_TYPE_INTERCEPTOR = "adaptris.jmx.interceptor.notification";

  /**
   * Send a a JMX notification containing the message and associated userdata.
   * 
   * @param msg the message
   * @param userData the userdata.
   */
  void sendNotification(String msg, Properties userData);

}
