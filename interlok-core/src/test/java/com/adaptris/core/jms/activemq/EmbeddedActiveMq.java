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

package com.adaptris.core.jms.activemq;

import static com.adaptris.core.PortManager.nextUnusedPort;
import static com.adaptris.core.PortManager.release;
import static com.adaptris.core.jms.JmsConfig.DEFAULT_PAYLOAD;
import static com.adaptris.core.jms.JmsConfig.DEFAULT_TTL;
import static com.adaptris.core.jms.JmsConfig.HIGHEST_PRIORITY;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import javax.jms.DeliveryMode;
import javax.jms.JMSException;
import javax.naming.Context;

import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.broker.Broker;
import org.apache.activemq.broker.BrokerService;
import org.apache.activemq.broker.region.Destination;
import org.apache.activemq.command.ActiveMQDestination;
import org.apache.activemq.jndi.ActiveMQInitialContextFactory;
import org.apache.activemq.store.memory.MemoryPersistenceAdapter;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.jms.FailoverJmsConnection;
import com.adaptris.core.jms.JmsConnection;
import com.adaptris.core.jms.JmsConstants;
import com.adaptris.core.jms.jndi.StandardJndiImplementation;
import com.adaptris.core.stubs.ExternalResourcesHelper;
import com.adaptris.util.GuidGenerator;
import com.adaptris.util.IdGenerator;
import com.adaptris.util.KeyValuePair;
import com.adaptris.util.PlainIdGenerator;

public class EmbeddedActiveMq {

  private static final String DEF_URL_PREFIX = "tcp://localhost:";
  private static Logger log = Logger.getLogger(EmbeddedActiveMq.class);
  private BrokerService broker = null;
  private String brokerName;
  private File brokerDataDir;
  private Integer port;

  private static IdGenerator nameGenerator;
  static {
    try {
      nameGenerator = new GuidGenerator();
    }
    catch (Exception e) {
      nameGenerator = new PlainIdGenerator("-");
    }
  }

  public EmbeddedActiveMq() throws Exception {
    brokerName = createSafeUniqueId(this);
    port = nextUnusedPort(61616);
  }

  public String getName() {
    return brokerName;
  }

  public void start() throws Exception {
    brokerDataDir = createTempFile(true);
    broker = createBroker();
    broker.start();
    while (!broker.isStarted()) {
      Thread.sleep(100);
    }
  }

  public BrokerService createBroker() throws Exception {
    BrokerService br = new BrokerService();
    br.setBrokerName(brokerName);
    br.addConnector(DEF_URL_PREFIX + port);
    br.setUseJmx(false);
    br.setDeleteAllMessagesOnStartup(true);
    br.setDataDirectoryFile(brokerDataDir);
    br.setPersistent(false);
    br.setPersistenceAdapter(new MemoryPersistenceAdapter());
    br.getSystemUsage().getMemoryUsage().setLimit(1024L * 1024 * 20);
    br.getSystemUsage().getTempUsage().setLimit(1024L * 1024 * 20);
    br.getSystemUsage().getStoreUsage().setLimit(1024L * 1024 * 20);
    br.getSystemUsage().getJobSchedulerUsage().setLimit(1024L * 1024 * 20);
    return br;
  }

  private File createTempFile(boolean isDir) throws IOException {
    File result = File.createTempFile("AMQ-", "");
    result.delete();
    if (isDir) {
      result.mkdirs();
    }
    return result;
  }

  public void destroy() {
    new Thread(new Runnable() {

      @Override
      public void run() {
        try {
          stop();
          release(port);
        }
        catch (Exception e) {

        }
      }
    }).start();
  }

  public void stop() throws Exception {
    if (broker != null) {
      broker.stop();
      broker.waitUntilStopped();
      FileUtils.deleteDirectory(brokerDataDir);
    }
  }

  public JmsConnection getJmsConnection() {
    return applyCfg(new JmsConnection(), new BasicActiveMqImplementation(), false);
  }

  public JmsConnection getJmsConnection(BasicActiveMqImplementation impl) {
    return applyCfg(new JmsConnection(), impl, false);
  }

  public JmsConnection getJmsConnection(BasicActiveMqImplementation impl, boolean useTcp) {
    return applyCfg(new JmsConnection(), impl, useTcp);
  }

  private JmsConnection applyCfg(JmsConnection con, BasicActiveMqImplementation impl, boolean useTcp) {
    if (useTcp) {
      impl.setBrokerUrl("tcp://localhost:" + port);
    }
    else {
      impl.setBrokerUrl("vm://" + getName());
    }
    con.setClientId(createSafeUniqueId(impl));
    con.setVendorImplementation(impl);
    return con;
  }

  public FailoverJmsConnection getFailoverJmsConnection(boolean isPtp) throws Exception {
    FailoverJmsConnection result = new FailoverJmsConnection();
    if (!isPtp) {
      JmsConnection c = new JmsConnection(new BasicActiveMqImplementation(DEF_URL_PREFIX + "9999"));
      result.addConnection(c);
      result.addConnection(getJmsConnection(new BasicActiveMqImplementation(), true));
    }
    else {
      JmsConnection c = new JmsConnection(new BasicActiveMqImplementation(DEF_URL_PREFIX + "9999"));
      result.addConnection(c);
      result.addConnection(getJmsConnection(new BasicActiveMqImplementation(), true));
    }
    return result;
  }

  public JmsConnection getJndiPasConnection(StandardJndiImplementation jv, boolean useJndiOnly, String queueName, String topicName) {
    JmsConnection result = new JmsConnection();
    StandardJndiImplementation jndi = applyCfg(jv, useJndiOnly, queueName, topicName);
    jndi.setJndiName("topicConnectionFactory");
    result.setVendorImplementation(jndi);
    result.setClientId(createSafeUniqueId(jndi));

    return result;
  }

  public JmsConnection getJndiPtpConnection(StandardJndiImplementation jv, boolean useJndiOnly, String queueName, String topicName) {
    JmsConnection result = new JmsConnection();
    StandardJndiImplementation jndi = applyCfg(jv, useJndiOnly, queueName, topicName);
    jndi.setJndiName("queueConnectionFactory");
    result.setVendorImplementation(jndi);
    result.setClientId(createSafeUniqueId(jndi));
    return result;
  }

  private StandardJndiImplementation applyCfg(StandardJndiImplementation jndi, boolean useJndiOnly, String queueName,
                                              String topicName) {
    jndi.getJndiParams().addKeyValuePair(new KeyValuePair(Context.PROVIDER_URL, DEF_URL_PREFIX + port));
    jndi.getJndiParams().addKeyValuePair(
        new KeyValuePair(Context.INITIAL_CONTEXT_FACTORY, ActiveMQInitialContextFactory.class.getName()));
    jndi.getJndiParams().addKeyValuePair(
        new KeyValuePair("connectionFactoryNames", "connectionFactory, queueConnectionFactory, topicConnectionFactory"));
    jndi.getJndiParams().addKeyValuePair(new KeyValuePair("queue." + queueName, queueName));
    jndi.getJndiParams().addKeyValuePair(new KeyValuePair("topic." + topicName, topicName));
    if (useJndiOnly) {
      jndi.setUseJndiForQueues(true);
      jndi.setUseJndiForTopics(true);
    }
    return jndi;
  }

  public static AdaptrisMessage createMessage(AdaptrisMessageFactory fact) {
    AdaptrisMessage msg = fact == null ? AdaptrisMessageFactory.getDefaultInstance().newMessage(DEFAULT_PAYLOAD) : fact
        .newMessage(DEFAULT_PAYLOAD);
    msg.addMetadata(JmsConstants.JMS_PRIORITY, String.valueOf(HIGHEST_PRIORITY));
    msg.addMetadata(JmsConstants.JMS_DELIVERY_MODE, String.valueOf(DeliveryMode.NON_PERSISTENT));
    msg.addMetadata(JmsConstants.JMS_EXPIRATION, String.valueOf(DEFAULT_TTL));
    return msg;
  }

  public static AdaptrisMessage addBlobUrlRef(AdaptrisMessage msg, String key) {
    msg.addMetadata(key, ExternalResourcesHelper.createUrl());
    return msg;
  }

  public ActiveMQConnection createConnection() throws JMSException {
    ActiveMQConnectionFactory fact = new ActiveMQConnectionFactory("vm://" + getName());
    return (ActiveMQConnection) fact.createConnection();
  }

  public long messagesOnQueue(String queueName) throws Exception {
    Broker b = broker.getBroker();

    Map<ActiveMQDestination, Destination> map = b.getDestinationMap();
    for (ActiveMQDestination dest : map.keySet()) {
      if (dest.isQueue() && dest.getPhysicalName().equals(queueName)) {
        Destination queueDest = map.get(dest);
        return queueDest.getDestinationStatistics().getMessages().getCount();
        // return queueDest.browse().length;
      }
    }
    return -1;
  }

  public static String createSafeUniqueId(Object o) {
    return nameGenerator.create(o).replaceAll(":", "").replaceAll("-", "");
  }

}
