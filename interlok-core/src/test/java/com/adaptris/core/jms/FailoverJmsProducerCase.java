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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

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

  private static EmbeddedActiveMq activeMqBroker;

  @BeforeAll
  public static void setUpAll() throws Exception {
    activeMqBroker = new EmbeddedActiveMq();
    activeMqBroker.start();
  }
  
  @AfterAll
  public static void tearDownAll() throws Exception {
    if(activeMqBroker != null)
      activeMqBroker.destroy();
  }
  
  @Test
  public void testBug1012() throws Exception {
    FailoverJmsConnection connection = new FailoverJmsConnection();
    List<JmsConnection> ptp = new ArrayList<JmsConnection>();
    ptp.add(activeMqBroker.getJmsConnection());
    ptp.add(activeMqBroker.getJmsConnection(new BasicActiveMqImplementation(), true));
    connection.setConnections(ptp);
    LifecycleHelper.init(connection);

    assertEquals(1, connection.currentJmsConnection().retrieveExceptionListeners().size());
    AdaptrisComponent owner = (AdaptrisComponent) connection.currentJmsConnection().retrieveExceptionListeners().toArray()[0];
    assertTrue(connection == owner);
    LifecycleHelper.close(connection);

    Channel channel = new MockChannel();
    connection = new FailoverJmsConnection();
    connection.setRegisterOwner(true);
    channel.setConsumeConnection(connection);
    ptp = new ArrayList<JmsConnection>();
    ptp.add(activeMqBroker.getJmsConnection());
    ptp.add(activeMqBroker.getJmsConnection(new BasicActiveMqImplementation(), true));
    connection.setConnections(ptp);
    LifecycleHelper.init(connection);
    //setting the consume connection no longer sets up the exception handler, so expect 0 here.
    assertEquals(0, connection.currentJmsConnection().retrieveExceptionListeners().size());
    LifecycleHelper.close(connection);
    }

  @Test
  public void testNeverConnects() throws Exception {
    FailoverJmsConnection connection = new FailoverJmsConnection();
    connection.addConnection(new JmsConnection(new BasicActiveMqImplementation("tcp://localhost:123456")));
    connection.addConnection(new JmsConnection(new BasicActiveMqImplementation("tcp://localhost:123456")));
    connection.setConnectionRetryInterval(new TimeInterval(250L, TimeUnit.MILLISECONDS));
    connection.setConnectionAttempts(1);
    connection.addExceptionListener(new StandaloneConsumer());
    connection.setRegisterOwner(true);
    try {
      LifecycleHelper.initAndStart(connection);
    }
    catch (CoreException expected) {

    }
    finally {
      LifecycleHelper.stopAndClose(connection);
    }
  }

  @Test
  public void testEventuallyConnects() throws Exception {
    FailoverJmsConnection connection = new FailoverJmsConnection();
    connection.addConnection(new JmsConnection(new BasicActiveMqImplementation("tcp://localhost:123456")));
    connection.addConnection(activeMqBroker.getJmsConnection(new BasicActiveMqImplementation(), true));
    connection.setConnectionRetryInterval(new TimeInterval(250L, TimeUnit.MILLISECONDS));
    connection.addExceptionListener(new StandaloneConsumer());
    connection.setRegisterOwner(true);
    ScheduledExecutorService es = Executors.newSingleThreadScheduledExecutor();
    try {
      es.schedule(new Runnable() {

        @Override
        public void run() {
          try {
            activeMqBroker.start();
          }
          catch (Exception e) {
            throw new RuntimeException(e);
          }
        }

      }, 2L, TimeUnit.SECONDS);
      LifecycleHelper.initAndStart(connection);
    }
    finally {
      LifecycleHelper.stopAndClose(connection);
      es.shutdownNow();
    }
  }

  @Test
  public void testConnectionEquals() throws Exception {
    FailoverJmsConnection connection = new FailoverJmsConnection();
    connection.addConnection(activeMqBroker.getJmsConnection(new BasicActiveMqImplementation(), true));
    connection.addConnection(activeMqBroker.getJmsConnection());
    assertEquals(false, connection.connectionEquals(new JmsConnection(new BasicActiveMqImplementation("tcp://localhost:123456"))));
    try {
      LifecycleHelper.initAndStart(connection);
      assertNotNull(connection.currentJmsConnection());
      assertEquals(true, connection.connectionEquals(activeMqBroker.getJmsConnection(new BasicActiveMqImplementation(), true)));
    }
    finally {
      LifecycleHelper.stopAndClose(connection);
    }
  }

  @Test
  public void testDelegatedMethods() throws Exception {
    FailoverJmsConnection connection = new FailoverJmsConnection();
    connection.addConnection(activeMqBroker.getJmsConnection(new BasicActiveMqImplementation(), true));
    connection.addConnection(activeMqBroker.getJmsConnection());
    try {
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
      LifecycleHelper.stopAndClose(connection);
    }
  }
}
