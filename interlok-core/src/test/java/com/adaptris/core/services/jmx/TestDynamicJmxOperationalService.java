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

package com.adaptris.core.services.jmx;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import java.util.ArrayList;
import java.util.Properties;
import javax.management.MBeanException;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.CoreException;
import com.adaptris.core.PortManager;
import com.adaptris.core.ServiceCase;
import com.adaptris.core.ServiceException;
import com.adaptris.core.management.Constants;
import com.adaptris.core.management.jmx.JmxRemoteComponent;
import com.adaptris.core.runtime.AdapterComponentMBean;
import com.adaptris.core.util.LifecycleHelper;
import com.adaptris.util.GuidGenerator;

public class TestDynamicJmxOperationalService extends ServiceCase {

  private static GuidGenerator guid = new GuidGenerator();

  private static final String BASE_DIR_KEY = "JmxServiceExamples.baseDir";
  private static final String JMXMP_PREFIX = "service:jmx:jmxmp://localhost:";

  private static final String PAYLOAD = "OriginalPayload";
  private static final String DEFAULT_OBJECTNAME = "com.adaptris:type=Adapter,id=MyInterlokInstance";
  private static final String DEFAULT_OPERATION = "requestRestart";
  private static final String DEFAULT_JMX_SERVICEURL = "service:jmx:jmxmp://localhost:5555";

  @Mock
  private JmxOperationInvoker<Object> mockInvoker;

  public TestDynamicJmxOperationalService() {
    if (PROPERTIES.getProperty(BASE_DIR_KEY) != null) {
      setBaseDir(PROPERTIES.getProperty(BASE_DIR_KEY));
    }
  }

  @Override
  public boolean isAnnotatedForJunit4() {
    return true;
  }

  @Before
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);
  }

  @Test
  public void testMaxCache() throws Exception {
    DynamicJmxOperationService service = new DynamicJmxOperationService();
    assertNull(service.getMaxJmxConnectionCache());
    assertEquals(16, service.maxCache());
    service.withMaxJmxConnectionCache(10);
    assertEquals(Integer.valueOf(10), service.getMaxJmxConnectionCache());
    assertEquals(10, service.maxCache());
  }

  @Test
  public void testLifecycle() throws Exception {
    DynamicJmxOperationService service = new DynamicJmxOperationService();
    try {
      LifecycleHelper.prepare(service);
      LifecycleHelper.init(service);
      fail();
    }
    catch (CoreException expected) {

    }
    service.setObjectName(DEFAULT_OBJECTNAME);
    service.setOperationName(DEFAULT_OPERATION);
    LifecycleHelper.prepare(service);
    LifecycleHelper.init(service);
  }

  @Test
  public void testService() throws Exception {
    ObjectName objName = ObjectName.getInstance("com.adaptris.junit:testname=" + getName());
    HelloWorld hb = new HelloWorld(objName);
    hb.register();
    try {
      DynamicJmxOperationService service = new DynamicJmxOperationService().withJmxServiceUrl("").withMaxJmxConnectionCache(1)
          .withObjectName(objName.toString()).withOperationName("hello").withResultValueTranslator(new PayloadValueTranslator());
      AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(PAYLOAD);
      execute(service, msg);
      assertEquals(hb.hello(), msg.getContent());
    } finally {
      hb.unregister();
    }
  }

  @Test
  public void testService_Mocked() throws Exception {
    String operationReturnValue = "NewPayloadValue";
    when(mockInvoker.invoke((MBeanServerConnection) any(), anyString(), anyString(), any(Object[].class), any(String[].class)))
        .thenReturn(operationReturnValue);
    DynamicJmxOperationService service = new DynamicJmxOperationService().withJmxServiceUrl("").withMaxJmxConnectionCache(1)
        .withObjectName(DEFAULT_OBJECTNAME).withOperationName(DEFAULT_OPERATION)
        .withOperationParameters(new ArrayList<ValueTranslator>());
    service.setInvoker(mockInvoker);
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(PAYLOAD);
    execute(service, msg);
    assertEquals(PAYLOAD, msg.getContent());
  }

  @Test
  public void testService_Mocked_ReturnValue() throws Exception {
    String operationReturnValue = "NewPayloadValue";
    when(mockInvoker.invoke((MBeanServerConnection) any(), anyString(), anyString(), any(Object[].class), any(String[].class)))
        .thenReturn(operationReturnValue);
    DynamicJmxOperationService service = new DynamicJmxOperationService().withJmxServiceUrl("").withMaxJmxConnectionCache(1)
        .withObjectName(DEFAULT_OBJECTNAME).withOperationName(DEFAULT_OPERATION)
        .withOperationParameters(new ArrayList<ValueTranslator>()).withResultValueTranslator(new PayloadValueTranslator());
    service.setInvoker(mockInvoker);
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(PAYLOAD);
    execute(service, msg);
    assertEquals(operationReturnValue, msg.getContent());

  }

  @Test
  public void testService_Mocked_Cache() throws Exception {
    JmxComponentWrapper jmx1 = new JmxComponentWrapper().start();
    JmxComponentWrapper jmx2 = new JmxComponentWrapper().start();

    String operationReturnValue = "NewPayloadValue";
    when(mockInvoker.invoke((MBeanServerConnection) any(), anyString(), anyString(), any(Object[].class), any(String[].class)))
        .thenReturn(operationReturnValue);
    DynamicJmxOperationService service = new DynamicJmxOperationService().withJmxServiceUrl("%message{jmxServiceUrl}")
        .withMaxJmxConnectionCache(1)
        .withObjectName(DEFAULT_OBJECTNAME).withOperationName(DEFAULT_OPERATION)
        .withResultValueTranslator(new PayloadValueTranslator());

    service.setInvoker(mockInvoker);

    try {
      AdaptrisMessage msg1 = AdaptrisMessageFactory.getDefaultInstance().newMessage(PAYLOAD);
      msg1.addMetadata("jmxServiceUrl", jmx1.jmxServiceUrl());

      AdaptrisMessage msg2 = AdaptrisMessageFactory.getDefaultInstance().newMessage(PAYLOAD);
      msg2.addMetadata("jmxServiceUrl", jmx2.jmxServiceUrl());

      LifecycleHelper.initAndStart(service);
      service.doService(msg1);
      assertEquals(operationReturnValue, msg1.getContent());
      service.doService(msg1);
      assertEquals(operationReturnValue, msg1.getContent());
      service.doService(msg2);
      assertEquals(operationReturnValue, msg2.getContent());
      service.doService(msg2);
      assertEquals(operationReturnValue, msg2.getContent());
    }
    finally {
      LifecycleHelper.stopAndClose(service);
      jmx1.destroy();
      jmx2.destroy();
    }

  }

  @Test
  public void testService_Mocked_InvokerException() throws Exception {
    when(mockInvoker.invoke((MBeanServerConnection) any(), anyString(), anyString(), any(Object[].class), any(String[].class)))
      .thenThrow(new MBeanException(new Exception(), "Expected"));

    DynamicJmxOperationService service = new DynamicJmxOperationService();
    service.setInvoker(mockInvoker);
    service.setObjectName(DEFAULT_OBJECTNAME);
    service.setOperationName(DEFAULT_OPERATION);
    try {
      AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(PAYLOAD);
      execute(service, msg);
      fail("Expect a service exception");
    } catch (ServiceException ex) {
      //expected
    }
  }

  @Override
  protected Object retrieveObjectForSampleConfig() {

    DynamicJmxOperationService service = new DynamicJmxOperationService();
    service.setJmxServiceUrl(DEFAULT_JMX_SERVICEURL);
    service.setObjectName(DEFAULT_OBJECTNAME);
    service.setOperationName("myMethodToInvoke");

    return service;
  }

  private static class JmxComponentWrapper {
    JmxRemoteComponent jmx;
    Integer port;

    JmxComponentWrapper() {
      port = PortManager.nextUnusedPort(5555);
    }

    JmxComponentWrapper start() throws Exception {
      jmx = new JmxRemoteComponent();
      jmx.init(createProperties(port));
      jmx.start();
      return this;
    }

    String jmxServiceUrl() {
      return JMXMP_PREFIX + port;
    }

    void destroy() throws Exception {
      try {
        jmx.stop();
      }
      finally {
        jmx.destroy();
      }
      PortManager.release(port);
    }

    private Properties createProperties(int unusedPort) {
      Properties result = new Properties();
      result.put(Constants.CFG_KEY_JMX_SERVICE_URL_KEY, JMXMP_PREFIX + unusedPort);
      result.put(JmxRemoteComponent.CFG_KEY_JMX_SERVICE_URL_OBJECT_NAME,
          AdapterComponentMBean.JMX_DOMAIN_NAME + ":type=" + guid.safeUUID());
      return result;
    }
  }

}
