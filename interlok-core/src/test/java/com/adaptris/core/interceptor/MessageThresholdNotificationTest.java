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
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import javax.management.Notification;
import javax.management.ObjectName;
import org.junit.Test;
import com.adaptris.core.Adapter;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.StandardWorkflow;
import com.adaptris.core.runtime.BaseComponentMBean;
import com.adaptris.core.runtime.SimpleNotificationListener;
import com.adaptris.core.services.AlwaysFailService;
import com.adaptris.util.TimeInterval;

@SuppressWarnings("deprecation")
public class MessageThresholdNotificationTest extends MessageNotificationCase {


  @Test
  public void testNotifyThreshold() throws Exception {
    MessageThresholdNotification notif = new MessageThresholdNotification();
    final TimeInterval defaultThreshold = new TimeInterval(1L, TimeUnit.MINUTES);
    assertNull(notif.getTimesliceDuration());
    assertEquals(defaultThreshold.toMilliseconds(), notif.timesliceDurationMs());

    TimeInterval newInterval = new TimeInterval(10L, TimeUnit.MINUTES);
    notif.setTimesliceDuration(newInterval);
    assertNotNull(notif.getTimesliceDuration());
    assertEquals(newInterval, notif.getTimesliceDuration());
    assertEquals(newInterval.toMilliseconds(), notif.timesliceDurationMs());
  }

  @Test
  public void testNotification_NoNotifications() throws Exception {
    MessageThresholdNotification notif = new MessageThresholdNotification(getName());
    StandardWorkflow workflow = createWorkflow(getName() + "_Workflow", notif);
    Adapter adapter = createAdapter(getName(), workflow);
    List<BaseComponentMBean> mBeans = createJmxManagers(adapter);
    BaseComponentMBean notifier = getFirstImpl(mBeans, InterceptorNotificationMBean.class);
    assertNotNull(notifier);
    ObjectName notifObjName = notifier.createObjectName();
    SimpleNotificationListener listener = new SimpleNotificationListener();
    try {
      start(adapter);
      register(mBeans);
      mBeanServer.addNotificationListener(notifObjName, listener, null, null);
      AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage("Hello World");
      workflow.onAdaptrisMessage(msg);
      workflow.onAdaptrisMessage(msg);
      workflow.onAdaptrisMessage(msg);
      workflow.onAdaptrisMessage(msg);
      workflow.onAdaptrisMessage(msg);
      workflow.onAdaptrisMessage(msg);
    } finally {
      stop(adapter);
    }
    assertEquals(0, listener.getNotifications().size());
  }

  @Test
  public void testNotification_SizeThresholdExceeded() throws Exception {
    MessageThresholdNotification notif = new MessageThresholdNotification(getName());
    notif.setSizeThreshold(1L);
    StandardWorkflow workflow = createWorkflow(getName() + "_Workflow", notif);
    Adapter adapter = createAdapter(getName(), workflow);
    List<BaseComponentMBean> mBeans = createJmxManagers(adapter);
    BaseComponentMBean notifier = getFirstImpl(mBeans, InterceptorNotificationMBean.class);
    assertNotNull(notifier);
    ObjectName notifObjName = notifier.createObjectName();
    SimpleNotificationListener listener = new SimpleNotificationListener();
    try {
      start(adapter);
      register(mBeans);
      mBeanServer.addNotificationListener(notifObjName, listener, null, null);
      AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage("Hello World");
      workflow.onAdaptrisMessage(msg);
      listener.waitForMessages(1);
      assertEquals(1, listener.getNotifications().size());
      Notification notification = listener.getNotifications().get(0);
      Properties userData = (Properties) notification.getUserData();
      assertEquals("11", userData.getProperty(MessageThresholdNotification.KEY_MESSAGE_SIZE));
      assertEquals("1", userData.getProperty(MessageThresholdNotification.KEY_MESSAGE_COUNT));
      assertEquals("0", userData.getProperty(MessageThresholdNotification.KEY_MESSAGE_ERROR));
    } finally {
      stop(adapter);
    }
  }

  @Test
  public void testNotification_SizeThresholdNotExceeded() throws Exception {
    MessageThresholdNotification notif = new MessageThresholdNotification(getName());
    notif.setSizeThreshold(100L);
    StandardWorkflow workflow = createWorkflow(getName() + "_Workflow", notif);
    Adapter adapter = createAdapter(getName(), workflow);
    List<BaseComponentMBean> mBeans = createJmxManagers(adapter);
    BaseComponentMBean notifier = getFirstImpl(mBeans, InterceptorNotificationMBean.class);
    assertNotNull(notifier);
    ObjectName notifObjName = notifier.createObjectName();
    SimpleNotificationListener listener = new SimpleNotificationListener();
    try {
      start(adapter);
      register(mBeans);
      mBeanServer.addNotificationListener(notifObjName, listener, null, null);
      AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage("Hello World");
      workflow.onAdaptrisMessage(msg);
      workflow.onAdaptrisMessage(msg);
      workflow.onAdaptrisMessage(msg);
      workflow.onAdaptrisMessage(msg);
      workflow.onAdaptrisMessage(msg);
      workflow.onAdaptrisMessage(msg);
    } finally {
      stop(adapter);
    }
    assertEquals(0, listener.getNotifications().size());
  }

  @Test
  public void testNotification_CountThresholdExceeded() throws Exception {
    MessageThresholdNotification notif = new MessageThresholdNotification(getName());
    notif.setCountThreshold(0L);
    StandardWorkflow workflow = createWorkflow(getName() + "_Workflow", notif);
    Adapter adapter = createAdapter(getName(), workflow);
    List<BaseComponentMBean> mBeans = createJmxManagers(adapter);
    BaseComponentMBean notifier = getFirstImpl(mBeans, InterceptorNotificationMBean.class);
    assertNotNull(notifier);
    ObjectName notifObjName = notifier.createObjectName();
    SimpleNotificationListener listener = new SimpleNotificationListener();
    try {
      start(adapter);
      register(mBeans);
      mBeanServer.addNotificationListener(notifObjName, listener, null, null);
      AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage("Hello World");
      workflow.onAdaptrisMessage(msg);
      listener.waitForMessages(1);
      assertEquals(1, listener.getNotifications().size());
      Notification notification = listener.getNotifications().get(0);
      Properties userData = (Properties) notification.getUserData();
      assertEquals("11", userData.getProperty(MessageThresholdNotification.KEY_MESSAGE_SIZE));
      assertEquals("1", userData.getProperty(MessageThresholdNotification.KEY_MESSAGE_COUNT));
      assertEquals("0", userData.getProperty(MessageThresholdNotification.KEY_MESSAGE_ERROR));
    } finally {
      stop(adapter);
    }
  }

  @Test
  public void testNotification_CountThresholdNotExceeded() throws Exception {
    MessageThresholdNotification notif = new MessageThresholdNotification(getName());
    notif.setCountThreshold(100L);
    StandardWorkflow workflow = createWorkflow(getName() + "_Workflow", notif);
    Adapter adapter = createAdapter(getName(), workflow);
    List<BaseComponentMBean> mBeans = createJmxManagers(adapter);
    BaseComponentMBean notifier = getFirstImpl(mBeans, InterceptorNotificationMBean.class);
    assertNotNull(notifier);
    ObjectName notifObjName = notifier.createObjectName();
    SimpleNotificationListener listener = new SimpleNotificationListener();
    try {
      start(adapter);
      register(mBeans);
      mBeanServer.addNotificationListener(notifObjName, listener, null, null);
      AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage("Hello World");
      workflow.onAdaptrisMessage(msg);
      workflow.onAdaptrisMessage(msg);
      workflow.onAdaptrisMessage(msg);
      workflow.onAdaptrisMessage(msg);
      workflow.onAdaptrisMessage(msg);
      workflow.onAdaptrisMessage(msg);
      workflow.onAdaptrisMessage(msg);
    } finally {
      stop(adapter);
    }
    assertEquals(0, listener.getNotifications().size());
  }

  @Test
  public void testNotification_ErrorThresholdExceeded() throws Exception {
    MessageThresholdNotification notif = new MessageThresholdNotification(getName());
    notif.setErrorThreshold(0L);
    StandardWorkflow workflow = createWorkflow(getName() + "_Workflow", notif);
    workflow.getServiceCollection().add(new AlwaysFailService());
    Adapter adapter = createAdapter(getName(), workflow);
    List<BaseComponentMBean> mBeans = createJmxManagers(adapter);
    BaseComponentMBean notifier = getFirstImpl(mBeans, InterceptorNotificationMBean.class);
    assertNotNull(notifier);
    ObjectName notifObjName = notifier.createObjectName();
    SimpleNotificationListener listener = new SimpleNotificationListener();
    try {
      start(adapter);
      register(mBeans);
      mBeanServer.addNotificationListener(notifObjName, listener, null, null);
      AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage("Hello World");
      workflow.onAdaptrisMessage(msg);
      listener.waitForMessages(1);
      assertEquals(1, listener.getNotifications().size());
      Notification notification = listener.getNotifications().get(0);
      Properties userData = (Properties) notification.getUserData();
      assertEquals("11", userData.getProperty(MessageThresholdNotification.KEY_MESSAGE_SIZE));
      assertEquals("1", userData.getProperty(MessageThresholdNotification.KEY_MESSAGE_COUNT));
      assertEquals("1", userData.getProperty(MessageThresholdNotification.KEY_MESSAGE_ERROR));
    } finally {
      stop(adapter);
    }
  }

  @Test
  public void testNotification_ErrorThresholdNotExceeded() throws Exception {
    MessageThresholdNotification notif = new MessageThresholdNotification(getName());
    notif.setErrorThreshold(100L);
    StandardWorkflow workflow = createWorkflow(getName() + "_Workflow", notif);
    workflow.getServiceCollection().add(new AlwaysFailService());
    Adapter adapter = createAdapter(getName(), workflow);
    List<BaseComponentMBean> mBeans = createJmxManagers(adapter);
    BaseComponentMBean notifier = getFirstImpl(mBeans, InterceptorNotificationMBean.class);
    assertNotNull(notifier);
    ObjectName notifObjName = notifier.createObjectName();
    SimpleNotificationListener listener = new SimpleNotificationListener();
    try {
      start(adapter);
      register(mBeans);
      mBeanServer.addNotificationListener(notifObjName, listener, null, null);
      AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage("Hello World");
      workflow.onAdaptrisMessage(msg);
      workflow.onAdaptrisMessage(msg);
      workflow.onAdaptrisMessage(msg);
      workflow.onAdaptrisMessage(msg);
      workflow.onAdaptrisMessage(msg);
      workflow.onAdaptrisMessage(msg);
      workflow.onAdaptrisMessage(msg);
    } finally {
      stop(adapter);
    }
    assertEquals(0, listener.getNotifications().size());
  }

}
