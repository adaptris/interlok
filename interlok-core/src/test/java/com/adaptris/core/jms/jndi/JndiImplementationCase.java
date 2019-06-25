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

package com.adaptris.core.jms.jndi;

import java.util.concurrent.TimeUnit;

import com.adaptris.core.BaseCase;
import com.adaptris.core.ConfiguredProduceDestination;
import com.adaptris.core.StandaloneProducer;
import com.adaptris.core.jms.JmsConnection;
import com.adaptris.core.jms.JmsConnectionConfig;
import com.adaptris.core.jms.PasProducer;
import com.adaptris.core.jms.VendorImplementation;
import com.adaptris.core.jms.activemq.EmbeddedActiveMq;
import com.adaptris.core.jms.activemq.EmbeddedArtemis;
import com.adaptris.core.jms.activemq.RequiresCredentialsBroker;
import com.adaptris.core.util.LifecycleHelper;
import com.adaptris.security.password.Password;
import com.adaptris.util.KeyValuePair;
import com.adaptris.util.KeyValuePairSet;
import com.adaptris.util.TimeInterval;

public abstract class JndiImplementationCase extends BaseCase {

  public JndiImplementationCase(String name) {
    super(name);
  }

  protected abstract StandardJndiImplementation createVendorImplementation();

  public void testSetEnableJndiForQueues() throws Exception {
    BaseJndiImplementation vendorImp = createVendorImplementation();
    assertNull(vendorImp.getUseJndiForQueues());
    assertFalse(vendorImp.useJndiForQueues());
    vendorImp.setUseJndiForQueues(Boolean.TRUE);
    assertNotNull(vendorImp.getUseJndiForQueues());
    assertTrue(vendorImp.useJndiForQueues());
    vendorImp.setUseJndiForQueues(null);
    assertNull(vendorImp.getUseJndiForQueues());
    assertFalse(vendorImp.useJndiForQueues());
  }

  public void testSetEnableJndiForTopics() throws Exception {
    BaseJndiImplementation vendorImp = createVendorImplementation();
    assertNull(vendorImp.getUseJndiForTopics());
    assertFalse(vendorImp.useJndiForTopics());
    vendorImp.setUseJndiForTopics(Boolean.TRUE);
    assertNotNull(vendorImp.getUseJndiForTopics());
    assertTrue(vendorImp.useJndiForTopics());
    vendorImp.setUseJndiForTopics(null);
    assertNull(vendorImp.getUseJndiForTopics());
    assertFalse(vendorImp.useJndiForTopics());

  }

  public void testSetEnableEncodedPasswords() throws Exception {
    BaseJndiImplementation vendorImp = createVendorImplementation();
    assertNull(vendorImp.getEnableEncodedPasswords());
    assertFalse(vendorImp.enableEncodedPasswords());
    vendorImp.setEnableEncodedPasswords(Boolean.TRUE);
    assertNotNull(vendorImp.getEnableEncodedPasswords());
    assertTrue(vendorImp.enableEncodedPasswords());
    vendorImp.setEnableEncodedPasswords(null);
    assertNull(vendorImp.getEnableEncodedPasswords());
    assertFalse(vendorImp.enableEncodedPasswords());
  }

  public void testSetNewContextOnException() throws Exception {
    BaseJndiImplementation jv = createVendorImplementation();
    assertNull(jv.getNewContextOnException());
    assertFalse(jv.newContextOnException());
    jv.setNewContextOnException(Boolean.TRUE);
    assertEquals(Boolean.TRUE, jv.getNewContextOnException());
    assertTrue(jv.newContextOnException());

    jv.setNewContextOnException(null);
    assertNull(jv.getNewContextOnException());
    assertFalse(jv.newContextOnException());
  }

  public void testSetJndiName() throws Exception {
    BaseJndiImplementation jv = createVendorImplementation();
    jv.setJndiName("ABCDE");
    assertEquals("ABCDE", jv.getJndiName());
    try {
      jv.setJndiName("");
      fail();
    }
    catch (IllegalArgumentException expected) {

    }
    assertEquals("ABCDE", jv.getJndiName());
    try {
      jv.setJndiName(null);
      fail();
    }
    catch (IllegalArgumentException expected) {
    }
    assertEquals("ABCDE", jv.getJndiName());
  }

  public void testSetExtraConfiguration() throws Exception {
    BaseJndiImplementation jv = createVendorImplementation();
    assertEquals(NoOpFactoryConfiguration.class, jv.getExtraFactoryConfiguration().getClass());
    jv.setExtraFactoryConfiguration(new SimpleFactoryConfiguration());
    assertEquals(SimpleFactoryConfiguration.class, jv.getExtraFactoryConfiguration().getClass());
    try {
      jv.setExtraFactoryConfiguration(null);
      fail();
    }
    catch (IllegalArgumentException expected) {

    }
    assertEquals(SimpleFactoryConfiguration.class, jv.getExtraFactoryConfiguration().getClass());
  }


  public void testSetJndiParams() throws Exception {
    BaseJndiImplementation jv = createVendorImplementation();
    KeyValuePairSet set = new KeyValuePairSet();
    jv.setJndiParams(set);
    assertEquals(set, jv.getJndiParams());
    try {
      jv.setJndiParams(null);
      fail();
    }
    catch (IllegalArgumentException expected) {
    }
    assertEquals(set, jv.getJndiParams());
  }
  
  public void testInitialiseDefaultArtemisBroker() throws Exception {
    EmbeddedArtemis broker = new EmbeddedArtemis();
    String topicName = getName() + "_topic";
    
    PasProducer producer = new PasProducer(new ConfiguredProduceDestination(topicName));
    JmsConnection c = broker.getJmsConnection();
    StandaloneProducer standaloneProducer = new StandaloneProducer(c, producer);
    
    try {
      broker.start();
      LifecycleHelper.init(standaloneProducer);
    }
    finally {
      LifecycleHelper.close(standaloneProducer);
      broker.destroy();
    }
  }

  public void testInitialiseWithCredentials() throws Exception {
    RequiresCredentialsBroker broker = new RequiresCredentialsBroker();
    String queueName = getName() + "_queue";
    String topicName = getName() + "_topic";
    PasProducer producer = new PasProducer(new ConfiguredProduceDestination(topicName));
    StandardJndiImplementation jv = createVendorImplementation();
    JmsConnection c = broker.getJndiPasConnection(jv, false, queueName, topicName);

    jv.getJndiParams().addKeyValuePair(new KeyValuePair("UserName", RequiresCredentialsBroker.DEFAULT_USERNAME));
    jv.getJndiParams().addKeyValuePair(new KeyValuePair("Password", RequiresCredentialsBroker.DEFAULT_PASSWORD));
    StandaloneProducer standaloneProducer = new StandaloneProducer(c, producer);
    try {
      broker.start();
      LifecycleHelper.init(standaloneProducer);
    }
    finally {
      LifecycleHelper.close(standaloneProducer);
      broker.destroy();
    }
  }

  public void testInitialiseWithEncryptedPassword_viaEncodedPasswordKeys() throws Exception {
    RequiresCredentialsBroker broker = new RequiresCredentialsBroker();
    String queueName = getName() + "_queue";
    String topicName = getName() + "_topic";
    PasProducer producer = new PasProducer(new ConfiguredProduceDestination(queueName));
    StandardJndiImplementation jv = createVendorImplementation();
    JmsConnection c = broker.getJndiPasConnection(jv, false, queueName, topicName);

    jv.getJndiParams().addKeyValuePair(new KeyValuePair("UserName", RequiresCredentialsBroker.DEFAULT_USERNAME));
    jv.getJndiParams().addKeyValuePair(
        new KeyValuePair("Password", Password.encode(RequiresCredentialsBroker.DEFAULT_PASSWORD, Password.NON_PORTABLE_PASSWORD)));
    jv.setEncodedPasswordKeys("Password");
    StandaloneProducer standaloneProducer = new StandaloneProducer(c, producer);
    try {
      broker.start();
      LifecycleHelper.init(standaloneProducer);
    }
    finally {
      LifecycleHelper.close(standaloneProducer);
      broker.destroy();
    }
  }

  public void testInitialiseWithEncryptedPassword_withEnableEncodedPasswords() throws Exception {
    String queueName = getName() + "_queue";
    String topicName = getName() + "_topic";
    RequiresCredentialsBroker broker = new RequiresCredentialsBroker();
    PasProducer producer = new PasProducer(new ConfiguredProduceDestination(queueName));
    StandardJndiImplementation jv = createVendorImplementation();
    JmsConnection c = broker.getJndiPasConnection(jv, false, queueName, topicName);

    jv.getJndiParams().addKeyValuePair(new KeyValuePair("UserName", RequiresCredentialsBroker.DEFAULT_USERNAME));
    jv.getJndiParams().addKeyValuePair(
        new KeyValuePair("Password", Password.encode(RequiresCredentialsBroker.DEFAULT_PASSWORD, Password.NON_PORTABLE_PASSWORD)));
    jv.setEncodedPasswordKeys("Password");
    jv.setEnableEncodedPasswords(true);
    StandaloneProducer standaloneProducer = new StandaloneProducer(c, producer);
    try {
      broker.start();
      LifecycleHelper.init(standaloneProducer);
    }
    finally {
      LifecycleHelper.close(standaloneProducer);
      broker.destroy();
    }
  }

  public void testInitialiseWithEncryptedPasswordNotSupported() throws Exception {
    RequiresCredentialsBroker broker = new RequiresCredentialsBroker();
    String queueName = getName() + "_queue";
    String topicName = getName() + "_topic";
    PasProducer producer = new PasProducer(new ConfiguredProduceDestination(queueName));
    StandardJndiImplementation jv = createVendorImplementation();
    JmsConnection c = broker.getJndiPasConnection(jv, false, queueName, topicName);
    jv.getJndiParams().addKeyValuePair(new KeyValuePair("UserName", RequiresCredentialsBroker.DEFAULT_USERNAME));
    jv.getJndiParams().addKeyValuePair(
        new KeyValuePair("Password", Password.encode(RequiresCredentialsBroker.DEFAULT_PASSWORD, Password.NON_PORTABLE_PASSWORD)));
    StandaloneProducer standaloneProducer = new StandaloneProducer(c, producer);
    try {
      broker.start();
      LifecycleHelper.init(standaloneProducer);
      fail("Encrypted password should not be supported, as not explicitly configured");
    }
    catch (Exception e) {
      // expected
    }
    finally {
      broker.destroy();
    }
  }

  public void testInitialiseWithTopicConnectionFactoryNotFound() throws Exception {
    EmbeddedActiveMq broker = new EmbeddedActiveMq();
    String queueName = getName() + "_queue";
    String topicName = getName() + "_topic";
    PasProducer producer = new PasProducer(new ConfiguredProduceDestination(queueName));
    StandardJndiImplementation jv = createVendorImplementation();
    JmsConnection c = broker.getJndiPasConnection(jv, false, queueName, topicName);
    c.setConnectionAttempts(1);
    c.setConnectionRetryInterval(new TimeInterval(100L, TimeUnit.MILLISECONDS.name()));
    jv.setJndiName("testInitialiseWithTopicConnectionFactoryNotFound");
    StandaloneProducer standaloneProducer = new StandaloneProducer(c, producer);
    try {
      broker.start();
      LifecycleHelper.init(standaloneProducer);
      fail("Should Fail to lookup 'testInitialiseWithTopicConnectionFactoryNotFound'");
    }
    catch (Exception e) {
      // expected
    }
    finally {
      broker.destroy();
    }
  }

  public void testInitialiseWithQueueConnectionFactoryNotFound() throws Exception {
    EmbeddedActiveMq broker = new EmbeddedActiveMq();
    String queueName = getName() + "_queue";
    String topicName = getName() + "_topic";
    PasProducer producer = new PasProducer(new ConfiguredProduceDestination(topicName));
    StandardJndiImplementation jv = createVendorImplementation();
    JmsConnection c = broker.getJndiPtpConnection(jv, false, queueName, topicName);
    c.setConnectionAttempts(1);
    c.setConnectionRetryInterval(new TimeInterval(100L, TimeUnit.MILLISECONDS));
    jv.setJndiName("testInitialiseWithQueueConnectionFactoryNotFound");
    StandaloneProducer standaloneProducer = new StandaloneProducer(c, producer);
    try {
      broker.start();
      LifecycleHelper.init(standaloneProducer);
      fail("Should Fail to lookup 'testInitialiseWithQueueConnectionFactoryNotFound'");
    }
    catch (Exception e) {
      // expected
    }
    finally {
      broker.destroy();
    }
  }

  protected class StubJndiJmsConnectionConfig implements JmsConnectionConfig {

    public StubJndiJmsConnectionConfig() {

    }

    @Override
    public String configuredClientId() {
      return "StubJndiJmsConnectionConfig";
    }

    @Override
    public String configuredPassword() {
      return null;
    }

    @Override
    public String configuredUserName() {
      return null;
    }

    @Override
    public VendorImplementation configuredVendorImplementation() {
      return new StandardJndiImplementation();
    }
  }
}
