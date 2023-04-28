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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.management.MBeanException;
import javax.management.MBeanServerConnection;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.DefaultMessageFactory;
import com.adaptris.core.ServiceException;
import com.adaptris.core.jmx.JmxConnection;

public class JmxOperationCallServiceTest
    extends com.adaptris.interlok.junit.scaffolding.services.ExampleServiceCase {

  private static final String BASE_DIR_KEY = "JmxServiceExamples.baseDir";

  private AdaptrisMessage message;

  private JmxOperationCallService callService;

  private String originalPayload = "OriginalPayload";

  @Mock
  private JmxOperationInvoker<Object> mockInvoker;
  @Mock
  private JmxConnection mockConnection;

  public JmxOperationCallServiceTest() {
    if (PROPERTIES.getProperty(BASE_DIR_KEY) != null) {
      setBaseDir(PROPERTIES.getProperty(BASE_DIR_KEY));
    }
  }

  @BeforeEach
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);

    callService = new JmxOperationCallService();
    callService.setInvoker(mockInvoker);
    callService.setOperationName("MyOperationName");
    callService.setObjectName("com.adaptris:id=xxx");

    message = DefaultMessageFactory.getDefaultInstance().newMessage(originalPayload);

    callService.init();
    callService.start();
  }

  @AfterEach
  public void tearDown() throws Exception {
    callService.stop();
    callService.close();
  }

  /*************************************************************************************
   * TESTS
   *************************************************************************************/

  @Test
  public void testPayloadReturn() throws Exception {
    String operationReturnValue = "NewPayloadValue";

    when(mockInvoker.invoke((MBeanServerConnection) any(), anyString(), anyString(), any(Object[].class), any(String[].class)))
      .thenReturn(operationReturnValue);

    callService.setResultValueTranslator(new PayloadValueTranslator());
    callService.doService(message);

    assertEquals(operationReturnValue, message.getContent());
  }

  @Test
  public void testPayloadReturnWithParams() throws Exception {
    String operationReturnValue = "NewPayloadValue";
    when(mockInvoker.invoke((MBeanServerConnection) any(), anyString(), anyString(), any(Object[].class), any(String[].class)))
      .thenReturn(operationReturnValue);

    PayloadValueTranslator translatorParam = new PayloadValueTranslator();
    List<ValueTranslator> parameters = new ArrayList<>();
    parameters.add(translatorParam);

    callService.setOperationParameters(parameters);
    callService.setResultValueTranslator(new PayloadValueTranslator());
    callService.doService(message);

    assertEquals(operationReturnValue, message.getContent());
  }

  @Test
  public void testNoReturn() throws Exception {
    String operationReturnValue = "NewPayloadValue";
    when(mockInvoker.invoke((MBeanServerConnection) any(), anyString(), anyString(), any(Object[].class), any(String[].class)))
      .thenReturn(operationReturnValue);

    callService.setResultValueTranslator(null);
    callService.doService(message);

    assertEquals(originalPayload, message.getContent());
  }

  @Test
  public void testInvokerException() throws Exception {
    when(mockInvoker.invoke((MBeanServerConnection) any(), anyString(), anyString(), any(Object[].class), any(String[].class)))
      .thenThrow(new MBeanException(new Exception(), "Expected"));

    callService.setResultValueTranslator(null);
    try {
      callService.doService(message);
      fail("Expect a service exception");
    } catch (ServiceException ex) {
      //expected
    }
  }

  @Test
  public void testMetadataReturnWithObjectParams() throws Exception {
    String existingObjectMetadataKey = "ExistingObjectMetadataKey";
    String existingObjectMetadataValue = "ExistingObjectMetadataValue";
    String newMetadataKay = "NewMetadataKey";

    String operationReturnValue = "NewMetadataValue";
    when(mockInvoker.invoke((MBeanServerConnection) any(), anyString(), anyString(), any(Object[].class), any(String[].class)))
      .thenReturn(operationReturnValue);

    message.addObjectHeader(existingObjectMetadataKey, existingObjectMetadataValue);

    ObjectMetadataValueTranslator translatorParam = new ObjectMetadataValueTranslator();
    translatorParam.setMetadataKey("ExistingObjectMetadataKey");
    translatorParam.setType("java.lang.String");

    List<ValueTranslator> parameters = new ArrayList<>();
    parameters.add(translatorParam);

    MetadataValueTranslator returnTranslator = new MetadataValueTranslator();
    returnTranslator.setMetadataKey(newMetadataKay);

    callService.setOperationParameters(parameters);
    callService.setResultValueTranslator(returnTranslator);

    assertNull(message.getMetadataValue(newMetadataKay));
    callService.doService(message);
    assertEquals(operationReturnValue, message.getMetadataValue(newMetadataKay));
  }

  /*************************************************************************************
   * SAMPLE CONFIG
   *************************************************************************************/

  @Override
  protected Object retrieveObjectForSampleConfig() {
    PayloadValueTranslator resultTranslator = new PayloadValueTranslator();

    PayloadValueTranslator param1 = new PayloadValueTranslator();
    MetadataValueTranslator param2 = new MetadataValueTranslator("metadataKey", "java.lang.String");
    ObjectMetadataValueTranslator param3 = new ObjectMetadataValueTranslator("objectMetadataKey", "java.lang.Object");
    ConstantValueTranslator param4 = new ConstantValueTranslator("1", "java.lang.Integer");
    List<ValueTranslator> params = new ArrayList<>(Arrays.asList(new ValueTranslator[]
    {
        param1, param2, param3, param4
    }));

    JmxOperationCallService callService = new JmxOperationCallService();
    JmxConnection conn = new JmxConnection();
    conn.setJmxServiceUrl("service:jmx:jmxmp://localhost:5555");
    conn.setUsername("jmxUsername");
    conn.setPassword("jmxPassword");
    callService.setConnection(conn);
    callService.setObjectName("com.adaptris:type=Workflow,adapter=MyAdapter,channel=Channel1,id=Workflow1");
    callService.setOperationName("myMethodToInvoke");
    callService.setOperationParameters(params);
    callService.setResultValueTranslator(resultTranslator);

    return callService;
  }

  @Override
  protected Object retrieveObjectForCastorRoundTrip() {
    PayloadValueTranslator resultTranslator = new PayloadValueTranslator();
    PayloadValueTranslator param1 = new PayloadValueTranslator();
    MetadataValueTranslator param2 = new MetadataValueTranslator("metadataKey", "java.lang.String");
    ObjectMetadataValueTranslator param3 = new ObjectMetadataValueTranslator("objectMetadataKey", "java.lang.Object");
    ConstantValueTranslator param4 = new ConstantValueTranslator("1", "java.lang.Integer");
    List<ValueTranslator> params = new ArrayList<>(Arrays.asList(new ValueTranslator[]
    {
        param1, param2, param3, param4
    }));

    JmxOperationCallService callService = new JmxOperationCallService();
    callService.setConnection(new JmxConnection());
    callService.setObjectName("com.adaptris:type=Workflow,adapter=MyAdapter,channel=Channel1,id=Workflow1");
    callService.setOperationName("myMethodToInvoke");
    callService.setOperationParameters(params);
    callService.setResultValueTranslator(resultTranslator);

    return callService;
  }



}
