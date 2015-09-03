package com.adaptris.core.interceptor;

import java.util.List;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import javax.management.Notification;
import javax.management.ObjectName;

import com.adaptris.core.Adapter;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.StandardWorkflow;
import com.adaptris.core.runtime.BaseComponentMBean;
import com.adaptris.core.runtime.SimpleNotificationListener;
import com.adaptris.core.services.AlwaysFailService;
import com.adaptris.core.services.WaitService;
import com.adaptris.util.TimeInterval;

@SuppressWarnings("deprecation")
public class SlowMessageNotificationTest extends MessageNotificationCase {

  public SlowMessageNotificationTest(String name) {
    super(name);
  }

  public void testNotifyThreshold() throws Exception {
    SlowMessageNotification notif = new SlowMessageNotification();
    final TimeInterval defaultThreshold = new TimeInterval(1L, TimeUnit.MINUTES);
    assertNull(notif.getNotifyThreshold());
    assertEquals(defaultThreshold.toMilliseconds(), notif.notifyThreshold());
    
    TimeInterval newInterval = new TimeInterval(10L, TimeUnit.MINUTES);
    notif.setNotifyThreshold(newInterval);
    assertNotNull(notif.getNotifyThreshold());
    assertEquals(newInterval, notif.getNotifyThreshold());
    assertEquals(newInterval.toMilliseconds(), notif.notifyThreshold());
  }

  public void testNotification_SlowMessage_Success() throws Exception {
    SlowMessageNotification notif =
        new SlowMessageNotification(getName(), new TimeInterval(100L, TimeUnit.MILLISECONDS), new TimeInterval(1L, TimeUnit.MINUTES));
    StandardWorkflow workflow = createWorkflow(getName() + "_Workflow", notif);
    workflow.getServiceCollection().add(
        new WaitService(new TimeInterval(500L, TimeUnit.MILLISECONDS)));
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
      AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage();
      workflow.onAdaptrisMessage(msg);
      listener.waitForMessages(1);
      assertEquals(1, listener.getNotifications().size());
      Notification notification = listener.getNotifications().get(0);
      Properties userData = (Properties) notification.getUserData();
      assertEquals(msg.getUniqueId(), userData.getProperty(SlowMessageNotification.KEY_MESSAGE_ID));
      assertEquals("true", userData.getProperty(SlowMessageNotification.KEY_MESSAGE_SUCCESS));
    }
    finally {
      stop(adapter);
    }
  }

  public void testNotification_SlowMessage_Failure() throws Exception {
    SlowMessageNotification notif =
        new SlowMessageNotification(getName(), new TimeInterval(100L, TimeUnit.MILLISECONDS));
    StandardWorkflow workflow = createWorkflow(getName() + "_Workflow", notif);
    workflow.getServiceCollection().add(
        new WaitService(new TimeInterval(500L, TimeUnit.MILLISECONDS)));
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
      AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage();
      workflow.onAdaptrisMessage(msg);
      listener.waitForMessages(1);
      assertEquals(1, listener.getNotifications().size());
      Notification notification = listener.getNotifications().get(0);
      Properties userData = (Properties) notification.getUserData();
      assertEquals(msg.getUniqueId(), userData.getProperty(SlowMessageNotification.KEY_MESSAGE_ID));
      assertEquals("false", userData.getProperty(SlowMessageNotification.KEY_MESSAGE_SUCCESS));
    } finally {
      stop(adapter);
    }
  }

  public void testNotification_SentAsPartOfCleanup() throws Exception {
    SlowMessageNotification notif =
        new SlowMessageNotification(getName(), new TimeInterval(100L, TimeUnit.MILLISECONDS),
            new TimeInterval(200L, TimeUnit.MILLISECONDS));

    StandardWorkflow workflow = createWorkflow(getName() + "_Workflow", notif);
    workflow.getServiceCollection().add(
        new WaitService(new TimeInterval(500L, TimeUnit.MILLISECONDS)));

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
      AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage();
      workflow.onAdaptrisMessage(msg);
      listener.waitForMessages(1);
      assertEquals(1, listener.getNotifications().size());
      Notification notification = listener.getNotifications().get(0);
      Properties userData = (Properties) notification.getUserData();
      assertEquals(msg.getUniqueId(), userData.getProperty(SlowMessageNotification.KEY_MESSAGE_ID));
      assertEquals("-1", userData.getProperty(SlowMessageNotification.KEY_MESSAGE_DURATION));
      assertEquals("-1", userData.getProperty(SlowMessageNotification.KEY_MESSAGE_END));
      assertEquals("false", userData.getProperty(SlowMessageNotification.KEY_MESSAGE_SUCCESS));
    } finally {
      stop(adapter);
    }
  }


}
