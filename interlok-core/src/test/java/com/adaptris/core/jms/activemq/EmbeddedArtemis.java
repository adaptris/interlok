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
import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import org.apache.activemq.artemis.core.config.Configuration;
import org.apache.activemq.artemis.core.config.impl.ConfigurationImpl;
import org.apache.activemq.artemis.core.server.embedded.EmbeddedActiveMQ;
import org.apache.commons.io.FileUtils;
import org.junit.Assume;
import com.adaptris.core.jms.JmsConfig;
import com.adaptris.core.jms.JmsConnection;
import com.adaptris.core.jms.jndi.StandardJndiImplementation;
import com.adaptris.core.util.JmxHelper;
import com.adaptris.util.GuidGenerator;
import com.adaptris.util.IdGenerator;
import com.adaptris.util.KeyValuePair;
import com.adaptris.util.PlainIdGenerator;

public class EmbeddedArtemis {

//Found in the src/test/resources/broker.xml
 private static final String ARTEMIS_BROKER_NAME = "artemis-embedded-junit";
 
 private static final String QUEUE_OBJECT_NAME = "org.apache.activemq.artemis:broker=\"" + ARTEMIS_BROKER_NAME + "\",component=addresses,address=";

 private File brokerDataDir;
 private EmbeddedActiveMQ embeddedJMS;
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

 public EmbeddedArtemis() throws Exception {
    Assume.assumeTrue(JmsConfig.jmsTestsEnabled());
    port = nextUnusedPort(61616);
 }

 public String getName() {
   return ARTEMIS_BROKER_NAME;
 }

 public void start() throws Exception {
   brokerDataDir = createTempFile(true);
   embeddedJMS = createBroker();
   try {
     embeddedJMS.start();
   } catch (Throwable t) {
     throw new Exception(t);
   }
   embeddedJMS.waitClusterForming(1l, TimeUnit.SECONDS, 60, 1);
 }
 
 private File createTempFile(boolean isDir) throws IOException {
   File result = File.createTempFile("ARTEMIS-", "");
   result.delete();
   if (isDir) {
     result.mkdirs();
   }
   return result;
 }

 public EmbeddedActiveMQ createBroker() throws Exception {
   Configuration config = new ConfigurationImpl();

   config.setName(ARTEMIS_BROKER_NAME);
   config.setSecurityEnabled(false);
   config.addAcceptorConfiguration("in-vm", "vm://0");
   config.addAcceptorConfiguration("tcp", "tcp://127.0.0.1:" + port);
   config.setPersistenceEnabled(false);
   config.setBrokerInstance(brokerDataDir);
   
   EmbeddedActiveMQ embeddedActiveMQ = new EmbeddedActiveMQ();
   embeddedActiveMQ.setConfiguration(config);
//   embeddedActiveMQ.setConfigResourcePath("junit-broker.xml");
   return embeddedActiveMQ;
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
   if (embeddedJMS != null) {
     embeddedJMS.stop();
     FileUtils.deleteDirectory(brokerDataDir);
   }
 }

 public JmsConnection getJmsConnection() {
   StandardJndiImplementation standardJndiImplementation = new StandardJndiImplementation("ConnectionFactory");
   standardJndiImplementation.getJndiParams().addKeyValuePair(new KeyValuePair("java.naming.factory.initial", "org.apache.activemq.artemis.jndi.ActiveMQInitialContextFactory"));
   standardJndiImplementation.getJndiParams().addKeyValuePair(new KeyValuePair("java.naming.provider.url", "vm:/1"));
   
   return applyCfg(new JmsConnection(), standardJndiImplementation, false);
 }
 
 private JmsConnection applyCfg(JmsConnection con, StandardJndiImplementation impl, boolean useTcp) {
   con.setClientId(createSafeUniqueId(impl));
   con.setVendorImplementation(impl);
   return con;
 }
 
 public long messagesOnQueue(String queueName) throws Exception {
   String fullQueueObjectName = QUEUE_OBJECT_NAME + "\"" + queueName + "\"";
   MBeanServer mBeanServer = JmxHelper.findMBeanServer();
   
   return (long) mBeanServer.getAttribute(new ObjectName(fullQueueObjectName), "NumberOfMessages");
 }

 static String createSafeUniqueId(Object o) {
   return nameGenerator.create(o).replaceAll(":", "").replaceAll("-", "");
 }

}
