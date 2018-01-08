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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import javax.management.JMX;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import org.apache.commons.lang.StringUtils;

import com.adaptris.core.Adapter;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.CoreException;
import com.adaptris.core.DefaultMarshaller;
import com.adaptris.core.DefaultSerializableMessageTranslator;
import com.adaptris.core.MetadataElement;
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

public class AdapterComponentCheckerTest extends ComponentManagerCase {

  public AdapterComponentCheckerTest(String name) {
    super(name);
  }

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

  public void testCheckInitialised() throws Exception {
    String adapterName = this.getClass().getSimpleName() + "." + getName();
    Adapter adapter = createAdapter(adapterName, 2, 2);
    List<BaseComponentMBean> mBeans = createJmxManagers(adapter);
    ObjectName objectName = createComponentCheckerObjectName(adapterName);
    register(mBeans);
    AdapterComponentCheckerMBean manager = JMX.newMBeanProxy(mBeanServer, objectName, AdapterComponentCheckerMBean.class);
    manager.checkInitialise(createServiceForTests());
  }

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
    }
    catch (CoreException expected) {

    }
  }

  public void testCheckInitialised_Connection() throws Exception {
    String adapterName = this.getClass().getSimpleName() + "." + getName();
    Adapter adapter = createAdapter(adapterName, 2, 2);
    List<BaseComponentMBean> mBeans = createJmxManagers(adapter);
    ObjectName objectName = createComponentCheckerObjectName(adapterName);
    register(mBeans);
    AdapterComponentCheckerMBean manager = JMX.newMBeanProxy(mBeanServer, objectName, AdapterComponentCheckerMBean.class);
    manager.checkInitialise(DefaultMarshaller.getDefaultMarshaller().marshal(new NullConnection()));
  }

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

  @SuppressWarnings("deprecation")
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
    }
    catch (CoreException expected) {

    }
  }

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
    return ObjectName.getInstance(
        COMPONENT_CHECKER_TYPE + ADAPTER_PREFIX + adapterName + ID_PREFIX + AdapterComponentChecker.class.getSimpleName());
  }

  private String createServiceForTests() throws Exception {
    AddMetadataService service = new AddMetadataService(new ArrayList(Arrays.asList(new MetadataElement[]
    {
        new MetadataElement("key", "value")
    })));
    return DefaultMarshaller.getDefaultMarshaller().marshal(service);
  }

  private String createConnectedServices(String sharedName) throws Exception {
    ServiceList nestedList = new ServiceList();
    nestedList.add(new StandaloneProducer(new MockAllowsRetriesConnection(6), new NullMessageProducer()));
    if (!StringUtils.isEmpty(sharedName)) {
      nestedList.add(new StatelessServiceWrapper(
          new StandaloneProducer(new SharedConnection(sharedName), new NullMessageProducer())));
    }
    else {
      nestedList.add(new StatelessServiceWrapper(new StandaloneProducer()));

    }
    nestedList.add(new AddMetadataService(new ArrayList(Arrays.asList(new MetadataElement[]
    {
        new MetadataElement("key", "value")
    }))));
    ServiceList list = new ServiceList();
    list.add(nestedList);
    list.add(new JdbcServiceList());
    return DefaultMarshaller.getDefaultMarshaller().marshal(list);
  }

  private String createLicensedService() throws Exception {
    JdbcServiceList service = new JdbcServiceList();
    return DefaultMarshaller.getDefaultMarshaller().marshal(service);
  }

  private SerializableMessage createSerializableMessage() throws Exception {
    return new DefaultSerializableMessageTranslator().translate(AdaptrisMessageFactory.getDefaultInstance().newMessage());
  }
}
