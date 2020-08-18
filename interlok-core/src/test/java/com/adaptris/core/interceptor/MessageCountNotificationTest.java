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
import static org.junit.Assert.assertTrue;
import java.util.List;
import java.util.concurrent.TimeUnit;
import javax.management.Notification;
import javax.management.ObjectName;
import org.junit.Test;
import com.adaptris.core.Adapter;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.StandardWorkflow;
import com.adaptris.core.Workflow;
import com.adaptris.core.runtime.BaseComponentMBean;
import com.adaptris.core.runtime.SimpleNotificationListener;
import com.adaptris.core.util.LifecycleHelper;
import com.adaptris.util.TimeInterval;

public class MessageCountNotificationTest extends MessageNotificationCase {


  @Test
  public void testNotifyThreshold() throws Exception {
    MessageCountNotification notif = new MessageCountNotification();
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
    MessageCountNotification notif = new MessageCountNotification(getName(), new TimeInterval(10L, TimeUnit.SECONDS));
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
      submitWithDelay(msg, workflow, 5, 5);
    } finally {
      stop(adapter);
    }
    assertEquals(0, listener.getNotifications().size());
  }

  @Test
  public void testNotification_AboveThreshold() throws Exception {
    MessageCountNotification notif = new MessageCountNotification(getName(), new TimeInterval(200L, TimeUnit.MILLISECONDS));
    notif.setMessageCount(1);
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
      submitWithDelay(msg, workflow, 6, 5);
      LifecycleHelper.waitQuietly(200);
      submitWithDelay(msg, workflow, 1, 5);
      listener.waitForMessages(1);
      assertEquals(1, listener.getNotifications().size());
      Notification notification = listener.getNotifications().get(0);
      assertTrue(notification.getMessage().startsWith(MessageCountNotification.NOTIF_MESSAGE_ABOVE_THRESHOLD));
    } finally {
      stop(adapter);
    }
  }

  @Test
  public void testNotification_AboveThreshold_MaxExceeded() throws Exception {
    MessageCountNotification notif = new MessageCountNotification(getName(), new TimeInterval(200L, TimeUnit.MILLISECONDS));
    notif.setMessageCount(1);
    notif.setMaxNotifications(1);
    StandardWorkflow workflow = createWorkflow(getName() + "_Workflow", notif);
    Adapter adapter = createAdapter(getName(), workflow);
    List<BaseComponentMBean> mBeans = createJmxManagers(adapter);
    BaseComponentMBean notifier = getFirstImpl(mBeans, InterceptorNotificationMBean.class);
    assertNotNull(notifier);
    ObjectName notifObjName = notifier.createObjectName();
    System.err.println(notifObjName);
    SimpleNotificationListener listener = new SimpleNotificationListener();
    try {
      start(adapter);
      register(mBeans);
      mBeanServer.addNotificationListener(notifObjName, listener, null, null);
      AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage("Hello World");
      submitWithDelay(msg, workflow, 10, 5);
      LifecycleHelper.waitQuietly(200);
      submitWithDelay(msg, workflow, 6, 5);
      LifecycleHelper.waitQuietly(200);
      submitWithDelay(msg, workflow, 1, 0);
      listener.waitForMessages(1);
      assertEquals(1, listener.getNotifications().size());
      Notification notification = listener.getNotifications().get(0);
      assertTrue(notification.getMessage().startsWith(MessageCountNotification.NOTIF_MESSAGE_ABOVE_THRESHOLD));
    } finally {
      stop(adapter);
    }
  }

  @Test
  public void testNotification_StaysBelowThreshold() throws Exception {
    MessageCountNotification notif = new MessageCountNotification(getName(), new TimeInterval(1L, TimeUnit.SECONDS));
    notif.setMessageCount(5);
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
      submitWithDelay(msg, workflow, 1, 5);
      LifecycleHelper.waitQuietly(200);
      submitWithDelay(msg, workflow, 1, 5);
      LifecycleHelper.waitQuietly(200);
      submitWithDelay(msg, workflow, 1, 5);
      LifecycleHelper.waitQuietly(200);
      submitWithDelay(msg, workflow, 1, 5);
    } finally {
      stop(adapter);
    }
    assertEquals(0, listener.getNotifications().size());
  }

  @Test
  public void testNotification_BelowThreshold() throws Exception {
    MessageCountNotification notif = new MessageCountNotification(getName(), new TimeInterval(200L, TimeUnit.MILLISECONDS));
    notif.setMessageCount(2);
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
      submitWithDelay(msg, workflow, 6, 5);
      LifecycleHelper.waitQuietly(200);
      submitWithDelay(msg, workflow, 1, 0);
      LifecycleHelper.waitQuietly(200);
      submitWithDelay(msg, workflow, 1, 0);
      LifecycleHelper.waitQuietly(200);
      submitWithDelay(msg, workflow, 1, 0);
      listener.waitForMessages(3);
      assertEquals(3, listener.getNotifications().size());
      Notification n1 = listener.getNotifications().get(0);
      Notification n2 = listener.getNotifications().get(1);
      assertTrue(n1.getMessage().startsWith(MessageCountNotification.NOTIF_MESSAGE_ABOVE_THRESHOLD));
      assertTrue(n2.getMessage().startsWith(MessageCountNotification.NOTIF_MESSAGE_BELOW_THRESHOLD));
    } finally {
      stop(adapter);
    }
  }

  @Test
  public void testNotification_BelowThreshold_MaxExceeded() throws Exception {
    MessageCountNotification notif = new MessageCountNotification(getName(), new TimeInterval(200L, TimeUnit.MILLISECONDS));
    notif.setMessageCount(2);
    notif.setMaxNotifications(1);
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
      submitWithDelay(msg, workflow, 6, 5);
      LifecycleHelper.waitQuietly(200);
      submitWithDelay(msg, workflow, 1, 0);
      LifecycleHelper.waitQuietly(200);
      submitWithDelay(msg, workflow, 1, 0);
      LifecycleHelper.waitQuietly(200);
      submitWithDelay(msg, workflow, 1, 0);
      listener.waitForMessages(2);
      assertEquals(2, listener.getNotifications().size());
      Notification n1 = listener.getNotifications().get(0);
      Notification n2 = listener.getNotifications().get(1);
      assertTrue(n1.getMessage().startsWith(MessageCountNotification.NOTIF_MESSAGE_ABOVE_THRESHOLD));
      assertTrue(n2.getMessage().startsWith(MessageCountNotification.NOTIF_MESSAGE_BELOW_THRESHOLD));
    } finally {
      stop(adapter);
    }
  }



  private void submitWithDelay(AdaptrisMessage msg, Workflow w, int count, long delay) {
    for (int i = 0; i < count; i++) {
      w.onAdaptrisMessage(msg);
      LifecycleHelper.waitQuietly(delay);
    }
  }
}
