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
import java.util.EnumSet;
import java.util.List;

import javax.management.JMX;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import com.adaptris.core.Adapter;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.CoreException;
import com.adaptris.core.DefaultMarshaller;
import com.adaptris.core.DefaultSerializableMessageTranslator;
import com.adaptris.core.MetadataElement;
import com.adaptris.core.SerializableAdaptrisMessage;
import com.adaptris.core.services.jdbc.JdbcServiceList;
import com.adaptris.core.services.metadata.AddMetadataService;
import com.adaptris.core.stubs.LicenseStub;
import com.adaptris.util.license.License.LicenseType;

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

  public void testCheckInitialised_NotLicensed() throws Exception {
    String adapterName = this.getClass().getSimpleName() + "." + getName();
    Adapter adapter = createAdapter(adapterName, 2, 2);
    adapter.registerLicense(new LicenseStub(EnumSet.of(LicenseType.Basic)));
    List<BaseComponentMBean> mBeans = createJmxManagers(adapter);
    ObjectName objectName = createComponentCheckerObjectName(adapterName);
    register(mBeans);
    AdapterComponentCheckerMBean manager = JMX.newMBeanProxy(mBeanServer, objectName, AdapterComponentCheckerMBean.class);
    try {
      manager.checkInitialise(createLicensedService());
      fail();
    }
    catch (CoreException expected) {

    }
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

  public void testApplyService() throws Exception {
    String adapterName = this.getClass().getSimpleName() + "." + getName();
    Adapter adapter = createAdapter(adapterName, 2, 2);
    List<BaseComponentMBean> mBeans = createJmxManagers(adapter);
    ObjectName objectName = createComponentCheckerObjectName(adapterName);
    register(mBeans);
    AdapterComponentCheckerMBean manager = JMX.newMBeanProxy(mBeanServer, objectName, AdapterComponentCheckerMBean.class);
    AdaptrisMessage msg = new DefaultSerializableMessageTranslator().translate(manager.applyService(createServiceForTests(),
        createSerializableMessage()));
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
    SerializableAdaptrisMessage msg = createSerializableMessage();
    try {
      manager.applyService("<Document/>", msg);
      fail();
    }
    catch (CoreException expected) {

    }
  }

  public void testApplyService_NotLicensed() throws Exception {
    String adapterName = this.getClass().getSimpleName() + "." + getName();
    Adapter adapter = createAdapter(adapterName, 2, 2);
    adapter.registerLicense(new LicenseStub(EnumSet.of(LicenseType.Basic)));
    List<BaseComponentMBean> mBeans = createJmxManagers(adapter);
    ObjectName objectName = createComponentCheckerObjectName(adapterName);
    register(mBeans);
    AdapterComponentCheckerMBean manager = JMX.newMBeanProxy(mBeanServer, objectName, AdapterComponentCheckerMBean.class);
    SerializableAdaptrisMessage msg = createSerializableMessage();
    try {
      manager.applyService(createLicensedService(), msg);
      fail();
    }
    catch (CoreException expected) {

    }
  }

  private ObjectName createComponentCheckerObjectName(String adapterName) throws MalformedObjectNameException {
    return ObjectName.getInstance(COMPONENT_CHECKER_TYPE + ADAPTER_PREFIX + adapterName + ID_PREFIX
        + AdapterComponentChecker.class.getSimpleName());
  }

  private String createServiceForTests() throws Exception {
    AddMetadataService service = new AddMetadataService(new ArrayList(Arrays.asList(new MetadataElement[]
    {
      new MetadataElement("key", "value")
    })));
    return DefaultMarshaller.getDefaultMarshaller().marshal(service);
  }

  private String createLicensedService() throws Exception {
    JdbcServiceList service = new JdbcServiceList();
    return DefaultMarshaller.getDefaultMarshaller().marshal(service);
  }

  private SerializableAdaptrisMessage createSerializableMessage() throws Exception {
    return new DefaultSerializableMessageTranslator().translate(AdaptrisMessageFactory.getDefaultInstance().newMessage());
  }
}
