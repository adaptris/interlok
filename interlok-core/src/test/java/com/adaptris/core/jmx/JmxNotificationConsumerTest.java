/*
 * Copyright 2015 Adaptris Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.adaptris.core.jmx;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import org.junit.Test;
import com.adaptris.core.AdaptrisConnection;
import com.adaptris.core.AdaptrisMessageConsumer;
import com.adaptris.core.AdaptrisMessageListener;
import com.adaptris.core.ConsumerCase;
import com.adaptris.core.CoreException;
import com.adaptris.core.StandaloneConsumer;
import com.adaptris.core.stubs.MockMessageListener;
import com.adaptris.core.util.JmxHelper;
import com.adaptris.core.util.LifecycleHelper;
import com.adaptris.core.util.ManagedThreadFactory;
import com.adaptris.util.TimeInterval;

public class JmxNotificationConsumerTest extends ConsumerCase {

  private static final String BASE_DIR_KEY = "JmxConsumerExamples.baseDir";

  public JmxNotificationConsumerTest() {
    if (PROPERTIES.getProperty(BASE_DIR_KEY) != null) {
      setBaseDir(PROPERTIES.getProperty(BASE_DIR_KEY));
    }
  }

  @Override
  public boolean isAnnotatedForJunit4() {
    return true;
  }

  @Test
  public void testNotFound() throws Exception {
    MBeanServer mbeanServer = JmxHelper.findMBeanServer();
    String myObjectName = "com.adaptris:type=Junit,id=" + getName();
    StubNotificationBroadcaster broadcast = new StubNotificationBroadcaster();
    MockMessageListener listener = new MockMessageListener();
    JmxNotificationConsumer consumer = new JmxNotificationConsumer();
    consumer.setObjectName(myObjectName);
    StandaloneConsumer sc = wrap(new JmxConnection(), consumer, listener);

    try {
      start(sc);
      fail();
    } catch (CoreException e) {
    } finally {
      stop(sc);
    }
  }

  @Test
  public void testConsumer_NoUserData() throws Exception {
    MBeanServer mbeanServer = JmxHelper.findMBeanServer();
    String myObjectName = "com.adaptris:type=Junit,id=" + getName();
    StubNotificationBroadcaster broadcast = new StubNotificationBroadcaster();
    MockMessageListener listener = new MockMessageListener();
    JmxNotificationConsumer consumer = new JmxNotificationConsumer();
    consumer.setObjectName(myObjectName);
    StandaloneConsumer sc = wrap(new JmxConnection(), consumer, listener);

    try {
      mbeanServer.registerMBean(broadcast, ObjectName.getInstance(myObjectName));
      start(sc);
      broadcast.sendNotification(getName(), null);
      waitForMessages(listener, 1);
      assertEquals(1, listener.messageCount());
      assertNull(listener.getMessages().get(0).getObjectHeaders().get(NotificationSerializer.OBJ_METADATA_USERDATA));
    } finally {
      stop(sc);
    }
  }

  @Test
  public void testConsumer() throws Exception {
    MBeanServer mbeanServer = JmxHelper.findMBeanServer();
    String myObjectName = "com.adaptris:type=Junit,id=" + getName();
    StubNotificationBroadcaster broadcast = new StubNotificationBroadcaster();
    MockMessageListener listener = new MockMessageListener();
    JmxNotificationConsumer consumer = new JmxNotificationConsumer();
    consumer.setObjectName(myObjectName);
    StandaloneConsumer sc = wrap(new JmxConnection(), consumer, listener);

    try {
      mbeanServer.registerMBean(broadcast, ObjectName.getInstance(myObjectName));
      // INTERLOK-3329 For coverage so the prepare() warning is executed 2x
      LifecycleHelper.prepare(sc);
      start(sc);
      broadcast.sendNotification(getName(), new Object());
      waitForMessages(listener, 1);
      assertEquals(1, listener.messageCount());
      assertNotNull(listener.getMessages().get(0).getObjectHeaders().get(NotificationSerializer.OBJ_METADATA_USERDATA));
    } finally {
      stop(sc);
    }
  }

  @Test
  public void testNotFound_Retry() throws Exception {
    final MBeanServer mbeanServer = JmxHelper.findMBeanServer();
    final String myObjectName = "com.adaptris:type=Junit,id=" + getName();
    final StubNotificationBroadcaster broadcast = new StubNotificationBroadcaster();
    MockMessageListener listener = new MockMessageListener();
    JmxNotificationConsumer consumer = new JmxNotificationConsumer();
    consumer.setFailIfNotFound(false);
    consumer.setRetryInterval(new TimeInterval(1L, TimeUnit.SECONDS));
    consumer.setObjectName(myObjectName);
    StandaloneConsumer sc = wrap(new JmxConnection(), consumer, listener);
    ScheduledExecutorService scheduler = Executors
        .newSingleThreadScheduledExecutor(new ManagedThreadFactory(getClass().getSimpleName()));
    try {
      start(sc);
      scheduler.scheduleWithFixedDelay(new Runnable() {
        @Override
        public void run() {
          try {
            if (!mbeanServer.isRegistered(ObjectName.getInstance(myObjectName))) {
              mbeanServer.registerMBean(broadcast, ObjectName.getInstance(myObjectName));
            }
            broadcast.sendNotification(getName(), new Object());
          } catch (Exception e) {
            throw new RuntimeException();
          }
        }
      }, 3, 1, TimeUnit.SECONDS);
      waitForMessages(listener, 1);
      assertTrue(listener.messageCount() >= 1);
      assertNotNull(listener.getMessages().get(0).getObjectHeaders().get(NotificationSerializer.OBJ_METADATA_USERDATA));
    } catch (CoreException e) {
    } finally {
      stop(sc);
      ManagedThreadFactory.shutdownQuietly(scheduler, 100L);
    }
  }

  @Override
  protected StandaloneConsumer retrieveObjectForSampleConfig() {
    JmxNotificationConsumer consumer = new JmxNotificationConsumer();
    JmxConnection conn = new JmxConnection();
    conn.setJmxServiceUrl("service:jmx:jmxmp://localhost:5555");
    conn.setUsername("jmxUsername");
    conn.setPassword("jmxPassword");
    consumer.setObjectName(
        "com.adaptris:type=Notifications,adapter=MyAdapter,channel=C1,workflow=W1,id=Name");
    consumer.setSerializer(new XmlNotificationSerializer());
    return new StandaloneConsumer(conn, consumer);
  }

  private StandaloneConsumer wrap(AdaptrisConnection conn, AdaptrisMessageConsumer consumer, AdaptrisMessageListener listener)
      throws Exception {
    StandaloneConsumer sc = new StandaloneConsumer(conn, consumer);
    sc.registerAdaptrisMessageListener(listener);
    sc.prepare();
    return sc;
  }
}
