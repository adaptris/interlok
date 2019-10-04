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
import org.junit.Test;
import com.adaptris.core.jms.activemq.EmbeddedActiveMq;

public class JmsUtilsTest {

  @Test
  public void testCloseSession() throws Exception {

    EmbeddedActiveMq broker = new EmbeddedActiveMq();
    try {
      broker.start();
      Session session = broker.createConnection().createSession(false, Session.CLIENT_ACKNOWLEDGE);
      JmsUtils.closeQuietly(session);
      JmsUtils.closeQuietly((Session) null);
      session = (Session) Proxy.newProxyInstance(Session.class.getClassLoader(), new Class[]
      {
        Session.class
      }, new FailingDynamicProxy());
      JmsUtils.closeQuietly(session);
    }
    finally {
      broker.destroy();

    }
  }

  @Test
  public void testCloseConnection() throws Exception {

    EmbeddedActiveMq broker = new EmbeddedActiveMq();
    try {
      broker.start();
      Connection connection = broker.createConnection();
      JmsUtils.closeQuietly(connection);
      connection = broker.createConnection();
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
    finally {
      broker.destroy();

    }
  }

  @Test
  public void testStopConnection() throws Exception {

    EmbeddedActiveMq broker = new EmbeddedActiveMq();
    try {
      broker.start();
      Connection connection = broker.createConnection();
      JmsUtils.stopQuietly(connection);
      JmsUtils.stopQuietly(null);
      connection = broker.createConnection();
      JmsUtils.stopQuietly(connection);
      connection = (Connection) Proxy.newProxyInstance(Connection.class.getClassLoader(), new Class[]
      {
        Connection.class
      }, new FailingDynamicProxy());
      JmsUtils.stopQuietly(connection);
    }
    finally {
      broker.destroy();

    }
  }

  @Test
  public void testDeleteTemporaryTopic() throws Exception {

    EmbeddedActiveMq broker = new EmbeddedActiveMq();
    try {
      broker.start();
      Session session = broker.createConnection().createSession(false, Session.CLIENT_ACKNOWLEDGE);
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
    finally {
      broker.destroy();

    }
  }

  @Test
  public void testDeleteTemporaryQueue() throws Exception {

    EmbeddedActiveMq broker = new EmbeddedActiveMq();
    try {
      broker.start();
      Session session = broker.createConnection().createSession(false, Session.CLIENT_ACKNOWLEDGE);
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
    finally {
      broker.destroy();

    }
  }

  @Test
  public void testDeleteTemporaryDestination() throws Exception {

    EmbeddedActiveMq broker = new EmbeddedActiveMq();
    try {
      broker.start();
      Session session = broker.createConnection().createSession(false, Session.CLIENT_ACKNOWLEDGE);
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
    finally {
      broker.destroy();
    }
  }

  @Test
  public void testCloseProducer() throws Exception {

    EmbeddedActiveMq broker = new EmbeddedActiveMq();
    try {
      broker.start();
      Session session = broker.createConnection().createSession(false, Session.CLIENT_ACKNOWLEDGE);
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
    finally {
      broker.destroy();

    }
  }

  @Test
  public void testCloseConsumer() throws Exception {

    EmbeddedActiveMq broker = new EmbeddedActiveMq();
    try {
      broker.start();
      Session session = broker.createConnection().createSession(false, Session.CLIENT_ACKNOWLEDGE);
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
    finally {
      broker.destroy();

    }
  }

  private class FailingDynamicProxy implements InvocationHandler {

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
      throw new Exception();
    }
  }
}
