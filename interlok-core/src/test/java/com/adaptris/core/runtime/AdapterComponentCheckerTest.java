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

package com.adaptris.core.runtime;

import static com.adaptris.core.runtime.AdapterComponentCheckerMBean.COMPONENT_CHECKER_TYPE;
import static com.adaptris.core.runtime.AdapterComponentMBean.ADAPTER_PREFIX;
import static com.adaptris.core.runtime.AdapterComponentMBean.ID_PREFIX;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import javax.management.JMX;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;

import com.adaptris.core.Adapter;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.CoreException;
import com.adaptris.core.DefaultMarshaller;
import com.adaptris.core.DefaultMessageFactory;
import com.adaptris.core.DefaultSerializableMessageTranslator;
import com.adaptris.core.MetadataElement;
import com.adaptris.core.MultiPayloadAdaptrisMessage;
import com.adaptris.core.MultiPayloadAdaptrisMessageImp;
import com.adaptris.core.MultiPayloadMessageMimeEncoder;
import com.adaptris.core.NullConnection;
import com.adaptris.core.NullMessageProducer;
import com.adaptris.core.ServiceList;
import com.adaptris.core.SharedConnection;
import com.adaptris.core.StandaloneProducer;
import com.adaptris.core.services.StatelessServiceWrapper;
import com.adaptris.core.services.jdbc.JdbcServiceList;
import com.adaptris.core.services.metadata.AddMetadataService;
import com.adaptris.core.stubs.MockAllowsRetriesConnection;
import com.adaptris.core.util.LifecycleHelper;
import com.adaptris.interlok.types.SerializableMessage;
import com.adaptris.util.GuidGenerator;

@SuppressWarnings("deprecation")
public class AdapterComponentCheckerTest extends ComponentManagerCase {

  public AdapterComponentCheckerTest() {
  }

  @Test
  public void testRegistered() throws Exception {
    String adapterName = this.getClass().getSimpleName() + "." + getName();
    Adapter adapter = createAdapter(adapterName, 2, 2);
    List<BaseComponentMBean> mBeans = createJmxManagers(adapter);
    ObjectName objectName = createComponentCheckerObjectName(adapterName);
    register(mBeans);
    ObjectName adapterObj = createAdapterObjectName(adapterName);
    AdapterManagerMBean manager = JMX.newMBeanProxy(mBeanServer, adapterObj, AdapterManagerMBean.class);
    Collection<ObjectName> names = manager.getChildRuntimeInfoComponents();
    assertTrue(names.contains(objectName));
  }

  @Test
  public void testCheckInitialised() throws Exception {
    String adapterName = this.getClass().getSimpleName() + "." + getName();
    Adapter adapter = createAdapter(adapterName, 2, 2);
    List<BaseComponentMBean> mBeans = createJmxManagers(adapter);
    ObjectName objectName = createComponentCheckerObjectName(adapterName);
    register(mBeans);
    AdapterComponentCheckerMBean manager = JMX.newMBeanProxy(mBeanServer, objectName, AdapterComponentCheckerMBean.class);
    manager.checkInitialise(createServiceForTests());
  }

  @Test
  public void testCheckInitialised_NotComponent() throws Exception {
    String adapterName = this.getClass().getSimpleName() + "." + getName();
    Adapter adapter = createAdapter(adapterName, 2, 2);
    List<BaseComponentMBean> mBeans = createJmxManagers(adapter);
    ObjectName objectName = createComponentCheckerObjectName(adapterName);
    register(mBeans);
    AdapterComponentCheckerMBean manager = JMX.newMBeanProxy(mBeanServer, objectName, AdapterComponentCheckerMBean.class);
    try {
      manager.checkInitialise("<Document/>");
      fail();
    } catch (CoreException expected) {

    }
  }

  @Test
  public void testCheckInitialised_Connection() throws Exception {
    String adapterName = this.getClass().getSimpleName() + "." + getName();
    Adapter adapter = createAdapter(adapterName, 2, 2);
    List<BaseComponentMBean> mBeans = createJmxManagers(adapter);
    ObjectName objectName = createComponentCheckerObjectName(adapterName);
    register(mBeans);
    AdapterComponentCheckerMBean manager = JMX.newMBeanProxy(mBeanServer, objectName, AdapterComponentCheckerMBean.class);
    manager.checkInitialise(DefaultMarshaller.getDefaultMarshaller().marshal(new NullConnection()));
  }

  @Test
  public void testCheckInitialised_RetriesConnection() throws Exception {
    String adapterName = this.getClass().getSimpleName() + "." + getName();
    Adapter adapter = createAdapter(adapterName, 2, 2);
    List<BaseComponentMBean> mBeans = createJmxManagers(adapter);
    ObjectName objectName = createComponentCheckerObjectName(adapterName);
    register(mBeans);
    AdapterComponentCheckerMBean manager = JMX.newMBeanProxy(mBeanServer, objectName, AdapterComponentCheckerMBean.class);
    manager.checkInitialise(DefaultMarshaller.getDefaultMarshaller().marshal(new MockAllowsRetriesConnection()));
    manager.checkInitialise(DefaultMarshaller.getDefaultMarshaller().marshal(new MockAllowsRetriesConnection(1)));
  }

  @Test
  public void testApplyService() throws Exception {
    String adapterName = this.getClass().getSimpleName() + "." + getName();
    Adapter adapter = createAdapter(adapterName, 2, 2);
    List<BaseComponentMBean> mBeans = createJmxManagers(adapter);
    ObjectName objectName = createComponentCheckerObjectName(adapterName);
    register(mBeans);
    AdapterComponentCheckerMBean manager = JMX.newMBeanProxy(mBeanServer, objectName, AdapterComponentCheckerMBean.class);
    AdaptrisMessage msg = new DefaultSerializableMessageTranslator()
        .translate(manager.applyService(createServiceForTests(), createSerializableMessage()));
    assertTrue(msg.containsKey("key"));
    assertEquals("value", msg.getMetadataValue("key"));
  }

  @Test
  public void testApplyService_WithMimeMessage() throws Exception {
    String adapterName = this.getClass().getSimpleName() + "." + getName();
    Adapter adapter = createAdapter(adapterName, 2, 2);
    List<BaseComponentMBean> mBeans = createJmxManagers(adapter);
    ObjectName objectName = createComponentCheckerObjectName(adapterName);
    register(mBeans);
    AdapterComponentCheckerMBean manager = JMX.newMBeanProxy(mBeanServer, objectName, AdapterComponentCheckerMBean.class);

    String mimeMessage = createMultiPayloadMimeMessage();

    String resultMsg = manager.applyService(createServiceForTests(), mimeMessage, true);

    System.out.println(resultMsg);

    MultiPayloadAdaptrisMessage msg = readMultiPayloadMimeMessage(resultMsg);

    assertTrue(msg.headersContainsKey("key"));
    assertEquals("value", msg.getMetadataValue("key"));
    assertEquals(2, msg.getPayloadCount());
    assertEquals("payload-1", msg.getContent("payload-1"));
    assertEquals("payload-2", msg.getContent("payload-2"));
    assertFalse(msg.hasPayloadId(MultiPayloadAdaptrisMessage.DEFAULT_PAYLOAD_ID));
  }

  @Test
  public void testApplyService_NotService() throws Exception {
    String adapterName = this.getClass().getSimpleName() + "." + getName();
    Adapter adapter = createAdapter(adapterName, 2, 2);
    List<BaseComponentMBean> mBeans = createJmxManagers(adapter);
    ObjectName objectName = createComponentCheckerObjectName(adapterName);
    register(mBeans);
    AdapterComponentCheckerMBean manager = JMX.newMBeanProxy(mBeanServer, objectName, AdapterComponentCheckerMBean.class);
    SerializableMessage msg = createSerializableMessage();
    try {
      manager.applyService("<Document/>", msg, false);
      fail();
    } catch (CoreException expected) {

    }
  }

  @Test
  public void testApplyService_WithConnections_Rewrite() throws Exception {
    String adapterName = this.getClass().getSimpleName() + "." + getName();
    Adapter adapter = createAdapter(adapterName, 2, 2);
    adapter.getSharedComponents().addConnection(new NullConnection(getName()));
    List<BaseComponentMBean> mBeans = createJmxManagers(adapter);
    ObjectName objectName = createComponentCheckerObjectName(adapterName);
    LifecycleHelper.initAndStart(adapter);
    register(mBeans);
    // Must init & start because of shared connections.
    LifecycleHelper.initAndStart(adapter);
    AdapterComponentCheckerMBean manager = JMX.newMBeanProxy(mBeanServer, objectName, AdapterComponentCheckerMBean.class);
    AdaptrisMessage msg = new DefaultSerializableMessageTranslator()
        .translate(manager.applyService(createConnectedServices(getName()), createSerializableMessage(), true));
    assertTrue(msg.containsKey("key"));
    assertEquals("value", msg.getMetadataValue("key"));
  }

  @Test
  public void testApplyService_WithConnections_NoRewrite() throws Exception {
    String adapterName = this.getClass().getSimpleName() + "." + getName();
    Adapter adapter = createAdapter(adapterName, 2, 2);
    adapter.getSharedComponents().addConnection(new NullConnection(getName()));
    List<BaseComponentMBean> mBeans = createJmxManagers(adapter);
    ObjectName objectName = createComponentCheckerObjectName(adapterName);
    register(mBeans);
    AdapterComponentCheckerMBean manager = JMX.newMBeanProxy(mBeanServer, objectName, AdapterComponentCheckerMBean.class);
    AdaptrisMessage msg = new DefaultSerializableMessageTranslator()
        .translate(manager.applyService(createConnectedServices(null), createSerializableMessage(), false));
    assertTrue(msg.containsKey("key"));
    assertEquals("value", msg.getMetadataValue("key"));
  }

  private ObjectName createComponentCheckerObjectName(String adapterName) throws MalformedObjectNameException {
    return ObjectName
        .getInstance(COMPONENT_CHECKER_TYPE + ADAPTER_PREFIX + adapterName + ID_PREFIX + AdapterComponentChecker.class.getSimpleName());
  }

  private String createServiceForTests() throws Exception {
    AddMetadataService service = new AddMetadataService(new ArrayList(Arrays.asList(new MetadataElement("key", "value"))));
    return DefaultMarshaller.getDefaultMarshaller().marshal(service);
  }

  private String createConnectedServices(String sharedName) throws Exception {
    ServiceList nestedList = new ServiceList();
    nestedList.add(new StandaloneProducer(new MockAllowsRetriesConnection(6), new NullMessageProducer()));
    if (!StringUtils.isEmpty(sharedName)) {
      nestedList.add(new StatelessServiceWrapper(new StandaloneProducer(new SharedConnection(sharedName), new NullMessageProducer())));
    } else {
      nestedList.add(new StatelessServiceWrapper(new StandaloneProducer()));

    }
    nestedList.add(new AddMetadataService(new ArrayList(Arrays.asList(new MetadataElement("key", "value")))));
    ServiceList list = new ServiceList();
    list.add(nestedList);
    list.add(new JdbcServiceList());
    return DefaultMarshaller.getDefaultMarshaller().marshal(list);
  }

  private SerializableMessage createSerializableMessage() throws Exception {
    return new DefaultSerializableMessageTranslator().translate(AdaptrisMessageFactory.getDefaultInstance().newMessage());
  }

  private String createMultiPayloadMimeMessage() throws Exception {
    MultiPayloadAdaptrisMessage message = new MultiPayloadAdaptrisMessageImp("payload-1", new GuidGenerator(),
        DefaultMessageFactory.getDefaultInstance(), "payload-1".getBytes());
    message.addContent("payload-2", "payload-2");
    message.addMetadata("key", "value");

    message.setNextServiceId("nextServiceId");

    MultiPayloadMessageMimeEncoder mimeEncoder = new MultiPayloadMessageMimeEncoder();
    mimeEncoder.setRetainUniqueId(true);
    mimeEncoder.setRetainNextServiceId(true);

    ByteArrayOutputStream bo = new ByteArrayOutputStream();
    mimeEncoder.writeMessage(message, bo);
    return new String(bo.toByteArray(), StandardCharsets.UTF_8);
  }

  private MultiPayloadAdaptrisMessage readMultiPayloadMimeMessage(String mimeEncodedMsg) throws Exception {
    try (ByteArrayInputStream in = new ByteArrayInputStream(mimeEncodedMsg.getBytes())) {
      return (MultiPayloadAdaptrisMessage) new MultiPayloadMessageMimeEncoder().readMessage(in);
    }
  }
}
