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

package com.adaptris.core.jms;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import com.adaptris.core.AdaptrisComponent;
import com.adaptris.core.Channel;
import com.adaptris.core.CoreException;
import com.adaptris.core.StandaloneConsumer;
import com.adaptris.core.jms.activemq.BasicActiveMqImplementation;
import com.adaptris.core.jms.activemq.EmbeddedActiveMq;
import com.adaptris.core.stubs.MockChannel;
import com.adaptris.core.util.LifecycleHelper;
import com.adaptris.util.TimeInterval;

public abstract class FailoverJmsProducerCase
    extends com.adaptris.interlok.junit.scaffolding.jms.JmsProducerCase {

  private FailoverJmsConnection connection;

  @Test
  public void testBug1012() throws Exception {
    EmbeddedActiveMq broker = new EmbeddedActiveMq();
    FailoverJmsConnection connection = new FailoverJmsConnection();
    try {
      broker.start();
      List<JmsConnection> ptp = new ArrayList<JmsConnection>();
      ptp.add(broker.getJmsConnection());
      ptp.add(broker.getJmsConnection(new BasicActiveMqImplementation(), true));
      connection.setConnections(ptp);
      LifecycleHelper.init(connection);

      assertEquals(1, connection.currentJmsConnection().retrieveExceptionListeners().size());
      AdaptrisComponent owner = (AdaptrisComponent) connection.currentJmsConnection().retrieveExceptionListeners().toArray()[0];
      assertTrue("Owner should be failover connection", connection == owner);
      LifecycleHelper.close(connection);

      Channel channel = new MockChannel();
      connection = new FailoverJmsConnection();
      connection.setRegisterOwner(true);
      channel.setConsumeConnection(connection);
      ptp = new ArrayList<JmsConnection>();
      ptp.add(broker.getJmsConnection());
      ptp.add(broker.getJmsConnection(new BasicActiveMqImplementation(), true));
      connection.setConnections(ptp);
      LifecycleHelper.init(connection);
      //setting the consume connection no longer sets up the exception handler, so expect 0 here.
      assertEquals(0, connection.currentJmsConnection().retrieveExceptionListeners().size());
      LifecycleHelper.close(connection);
    }
    finally {
      broker.destroy();
    }
  }

  @Test
  public void testNeverConnects() throws Exception {
    EmbeddedActiveMq broker = new EmbeddedActiveMq();
    FailoverJmsConnection connection = new FailoverJmsConnection();
    connection.addConnection(new JmsConnection(new BasicActiveMqImplementation("tcp://localhost:123456")));
    connection.addConnection(new JmsConnection(new BasicActiveMqImplementation("tcp://localhost:123456")));
    connection.setConnectionRetryInterval(new TimeInterval(250L, TimeUnit.MILLISECONDS));
    connection.setConnectionAttempts(1);
    connection.addExceptionListener(new StandaloneConsumer());
    connection.setRegisterOwner(true);
    try {
      broker.start();
      LifecycleHelper.initAndStart(connection);
    }
    catch (CoreException expected) {

    }
    finally {
      broker.destroy();
      LifecycleHelper.stopAndClose(connection);
    }
  }

  @Test
  public void testEventuallyConnects() throws Exception {
    final EmbeddedActiveMq broker = new EmbeddedActiveMq();
    FailoverJmsConnection connection = new FailoverJmsConnection();
    connection.addConnection(new JmsConnection(new BasicActiveMqImplementation("tcp://localhost:123456")));
    connection.addConnection(broker.getJmsConnection(new BasicActiveMqImplementation(), true));
    connection.setConnectionRetryInterval(new TimeInterval(250L, TimeUnit.MILLISECONDS));
    connection.addExceptionListener(new StandaloneConsumer());
    connection.setRegisterOwner(true);
    ScheduledExecutorService es = Executors.newSingleThreadScheduledExecutor();
    try {
      es.schedule(new Runnable() {

        @Override
        public void run() {
          try {
            broker.start();
          }
          catch (Exception e) {
            throw new RuntimeException(e);
          }
        }

      }, 2L, TimeUnit.SECONDS);
      LifecycleHelper.initAndStart(connection);
    }
    finally {
      broker.destroy();
      LifecycleHelper.stopAndClose(connection);
      es.shutdownNow();
    }
  }

  @Test
  public void testConnectionEquals() throws Exception {
    EmbeddedActiveMq broker = new EmbeddedActiveMq();
    FailoverJmsConnection connection = new FailoverJmsConnection();
    connection.addConnection(broker.getJmsConnection(new BasicActiveMqImplementation(), true));
    connection.addConnection(broker.getJmsConnection());
    assertEquals(false, connection.connectionEquals(new JmsConnection(new BasicActiveMqImplementation("tcp://localhost:123456"))));
    try {
      broker.start();
      LifecycleHelper.initAndStart(connection);
      assertNotNull(connection.currentJmsConnection());
      assertEquals(true, connection.connectionEquals(broker.getJmsConnection(new BasicActiveMqImplementation(), true)));
    }
    finally {
      broker.destroy();
      LifecycleHelper.stopAndClose(connection);
    }
  }

  @Test
  public void testDelegatedMethods() throws Exception {
    EmbeddedActiveMq broker = new EmbeddedActiveMq();
    FailoverJmsConnection connection = new FailoverJmsConnection();
    connection.addConnection(broker.getJmsConnection(new BasicActiveMqImplementation(), true));
    connection.addConnection(broker.getJmsConnection());
    try {
      broker.start();
      LifecycleHelper.initAndStart(connection);
      assertNotNull(connection.currentJmsConnection());
      assertNotNull(connection.obtainConnectionFactory());
      connection.createConnection(connection.obtainConnectionFactory());
      assertNotNull(connection.configuredClientId());
      assertEquals(true, StringUtils.isEmpty(connection.configuredPassword()));
      assertEquals(true, StringUtils.isEmpty(connection.configuredUserName()));
      assertEquals(BasicActiveMqImplementation.class, connection.configuredVendorImplementation().getClass());
    }
    finally {
      broker.destroy();
      LifecycleHelper.stopAndClose(connection);
    }
  }
}
