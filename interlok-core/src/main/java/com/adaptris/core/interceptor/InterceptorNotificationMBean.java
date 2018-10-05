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
