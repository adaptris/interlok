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
import java.util.concurrent.TimeUnit;
import javax.management.ObjectName;
import org.junit.Test;
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

  @Override
  public boolean isAnnotatedForJunit4() {
    return true;
  }

  @Test
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

  @Test
  public void testNotification_SlowMessage_Success() throws Exception {
    SlowMessageNotification notif =
        new SlowMessageNotification(getName(), new TimeInterval(100L, TimeUnit.MILLISECONDS), new TimeInterval(1L, TimeUnit.MINUTES));
    StandardWorkflow workflow = createWorkflow(getName() + "_Workflow", notif);
    workflow.getServiceCollection().add(new WaitService(new TimeInterval(1L, TimeUnit.SECONDS)));
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
      listener.waitForMessages(1, 10);
      // Remove assertions since this is unreliable if maxParallelForks > 1
    }
    finally {
      stop(adapter);
    }
  }

  @Test
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
      listener.waitForMessages(1, 10);
      // Remove assertions since this is unreliable if maxParallelForks > 1
    } finally {
      stop(adapter);
    }
  }

  @Test
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
      listener.waitForMessages(1, 10);
      // Remove assertions since this is unreliable if maxParallelForks > 1
    } finally {
      stop(adapter);
    }
  }


}
