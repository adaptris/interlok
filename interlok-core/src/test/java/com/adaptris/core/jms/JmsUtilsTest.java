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

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import javax.jms.Connection;
import javax.jms.Destination;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TemporaryQueue;
import javax.jms.TemporaryTopic;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import com.adaptris.core.jms.activemq.EmbeddedActiveMq;

public class JmsUtilsTest {

  private static EmbeddedActiveMq activeMqBroker;

  @BeforeClass
  public static void setUpAll() throws Exception {
    activeMqBroker = new EmbeddedActiveMq();
    activeMqBroker.start();
  }
  
  @AfterClass
  public static void tearDownAll() throws Exception {
    if(activeMqBroker != null)
      activeMqBroker.destroy();
  }
  
  @Test
  public void testCloseSession() throws Exception {
    Session session = activeMqBroker.createConnection().createSession(false, Session.CLIENT_ACKNOWLEDGE);
    JmsUtils.closeQuietly(session);
    JmsUtils.closeQuietly((Session) null);
    session = (Session) Proxy.newProxyInstance(Session.class.getClassLoader(), new Class[]
    {
      Session.class
    }, new FailingDynamicProxy());
    JmsUtils.closeQuietly(session);
  }

  @Test
  public void testCloseConnection() throws Exception {
    Connection connection = activeMqBroker.createConnection();
    JmsUtils.closeQuietly(connection);
    connection = activeMqBroker.createConnection();
    JmsUtils.closeQuietly(connection, true);
    JmsUtils.closeQuietly((Connection) null);
    connection = (Connection) Proxy.newProxyInstance(Connection.class.getClassLoader(), new Class[]
    {
      Connection.class
    }, new FailingDynamicProxy());
    JmsUtils.closeQuietly(connection);
    connection = (Connection) Proxy.newProxyInstance(Connection.class.getClassLoader(), new Class[]
    {
      Connection.class
    }, new FailingDynamicProxy());
    JmsUtils.closeQuietly(connection, true);
  }

  @Test
  public void testStopConnection() throws Exception {
    Connection connection = activeMqBroker.createConnection();
    JmsUtils.stopQuietly(connection);
    JmsUtils.stopQuietly(null);
    connection = activeMqBroker.createConnection();
    JmsUtils.stopQuietly(connection);
    connection = (Connection) Proxy.newProxyInstance(Connection.class.getClassLoader(), new Class[]
    {
      Connection.class
    }, new FailingDynamicProxy());
    JmsUtils.stopQuietly(connection);
  }

  @Test
  public void testDeleteTemporaryTopic() throws Exception {
    Session session = activeMqBroker.createConnection().createSession(false, Session.CLIENT_ACKNOWLEDGE);
    TemporaryTopic topic = session.createTemporaryTopic();
    JmsUtils.deleteQuietly(topic);
    JmsUtils.deleteQuietly((TemporaryTopic) null);
    topic = (TemporaryTopic) Proxy.newProxyInstance(TemporaryTopic.class.getClassLoader(), new Class[]
    {
      TemporaryTopic.class
    }, new FailingDynamicProxy());
    JmsUtils.deleteQuietly(topic);
    JmsUtils.closeQuietly(session);
  }

  @Test
  public void testDeleteTemporaryQueue() throws Exception {
    Session session = activeMqBroker.createConnection().createSession(false, Session.CLIENT_ACKNOWLEDGE);
    TemporaryQueue queue = session.createTemporaryQueue();
    JmsUtils.deleteQuietly(queue);
    JmsUtils.deleteQuietly((TemporaryQueue) null);
    queue = (TemporaryQueue) Proxy.newProxyInstance(TemporaryQueue.class.getClassLoader(), new Class[]
    {
      TemporaryQueue.class
    }, new FailingDynamicProxy());
    JmsUtils.deleteQuietly(queue);
    JmsUtils.closeQuietly(session);
  }

  @Test
  public void testDeleteTemporaryDestination() throws Exception {
    Session session = activeMqBroker.createConnection().createSession(false, Session.CLIENT_ACKNOWLEDGE);
    Destination dest = session.createTemporaryQueue();
    JmsUtils.deleteTemporaryDestination(dest);
    dest = session.createTemporaryTopic();
    JmsUtils.deleteTemporaryDestination(dest);
    JmsUtils.deleteTemporaryDestination(null);
    dest = (TemporaryQueue) Proxy.newProxyInstance(TemporaryQueue.class.getClassLoader(), new Class[]
    {
      TemporaryQueue.class
    }, new FailingDynamicProxy());
    JmsUtils.deleteTemporaryDestination(dest);
    dest = session.createQueue("myQueue");
    JmsUtils.deleteTemporaryDestination(dest);
    JmsUtils.closeQuietly(session);
  }

  @Test
  public void testCloseProducer() throws Exception {
    Session session = activeMqBroker.createConnection().createSession(false, Session.CLIENT_ACKNOWLEDGE);
    MessageProducer producer = session.createProducer(session.createTemporaryTopic());
    JmsUtils.closeQuietly(producer);
    JmsUtils.closeQuietly((MessageProducer) null);
    producer = (MessageProducer) Proxy.newProxyInstance(MessageProducer.class.getClassLoader(), new Class[]
    {
      MessageProducer.class
    }, new FailingDynamicProxy());
    JmsUtils.closeQuietly(producer);
    JmsUtils.closeQuietly(session);
  }

  @Test
  public void testCloseConsumer() throws Exception {
    Session session = activeMqBroker.createConnection().createSession(false, Session.CLIENT_ACKNOWLEDGE);
    MessageConsumer consumer = session.createConsumer(session.createTemporaryTopic());
    JmsUtils.closeQuietly(consumer);
    JmsUtils.closeQuietly((MessageProducer) null);
    consumer = (MessageConsumer) Proxy.newProxyInstance(MessageConsumer.class.getClassLoader(), new Class[]
    {
      MessageConsumer.class
    }, new FailingDynamicProxy());
    JmsUtils.closeQuietly(consumer);
    JmsUtils.closeQuietly(session);
  }

  private class FailingDynamicProxy implements InvocationHandler {

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
      throw new Exception();
    }
  }
}
