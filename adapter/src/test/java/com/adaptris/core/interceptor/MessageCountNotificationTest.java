package com.adaptris.core.interceptor;

import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.management.Notification;
import javax.management.ObjectName;

import com.adaptris.core.Adapter;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.StandardWorkflow;
import com.adaptris.core.Workflow;
import com.adaptris.core.runtime.BaseComponentMBean;
import com.adaptris.core.runtime.SimpleNotificationListener;
import com.adaptris.util.TimeInterval;

public class MessageCountNotificationTest extends MessageNotificationCase {

  public MessageCountNotificationTest(String name) {
    super(name);
  }

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


  public void testNotification_AboveThreshold() throws Exception {
    MessageCountNotification notif = new MessageCountNotification(getName(), new TimeInterval(1L, TimeUnit.SECONDS));
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
      waitQuietly(1000);
      submitWithDelay(msg, workflow, 1, 5);
      listener.waitForMessages(1);
      assertEquals(1, listener.getNotifications().size());
      Notification notification = listener.getNotifications().get(0);
      assertTrue(notification.getMessage().startsWith(MessageCountNotification.NOTIF_MESSAGE_ABOVE_THRESHOLD));
    } finally {
      stop(adapter);
    }
  }

  public void testNotification_AboveThreshold_MaxExceeded() throws Exception {
    MessageCountNotification notif = new MessageCountNotification(getName(), new TimeInterval(1L, TimeUnit.SECONDS));
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
      waitQuietly(1000);
      submitWithDelay(msg, workflow, 6, 5);
      waitQuietly(1000);
      submitWithDelay(msg, workflow, 1, 0);
      listener.waitForMessages(1);
      assertEquals(1, listener.getNotifications().size());
      Notification notification = listener.getNotifications().get(0);
      assertTrue(notification.getMessage().startsWith(MessageCountNotification.NOTIF_MESSAGE_ABOVE_THRESHOLD));
    } finally {
      stop(adapter);
    }
  }


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
      waitQuietly(1000);
      submitWithDelay(msg, workflow, 1, 5);
      waitQuietly(1000);
      submitWithDelay(msg, workflow, 1, 5);
      waitQuietly(1000);
      submitWithDelay(msg, workflow, 1, 5);
    } finally {
      stop(adapter);
    }
    assertEquals(0, listener.getNotifications().size());
  }


  public void testNotification_BelowThreshold() throws Exception {
    MessageCountNotification notif = new MessageCountNotification(getName(), new TimeInterval(1L, TimeUnit.SECONDS));
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
      waitQuietly(1000);
      submitWithDelay(msg, workflow, 1, 0);
      waitQuietly(1000);
      submitWithDelay(msg, workflow, 1, 0);
      waitQuietly(1000);
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


  public void testNotification_BelowThreshold_MaxExceeded() throws Exception {
    MessageCountNotification notif = new MessageCountNotification(getName(), new TimeInterval(1L, TimeUnit.SECONDS));
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
      waitQuietly(1000);
      submitWithDelay(msg, workflow, 1, 0);
      waitQuietly(1000);
      submitWithDelay(msg, workflow, 1, 0);
      waitQuietly(1000);
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
      waitQuietly(delay);
    }
  }

  private void waitQuietly(long sleepTime) {
    if (sleepTime > 0) {
      try {
        Thread.sleep(sleepTime);
      } catch (InterruptedException e) {

      }
    }
  }
}
