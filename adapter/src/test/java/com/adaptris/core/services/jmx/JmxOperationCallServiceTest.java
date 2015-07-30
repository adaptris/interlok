package com.adaptris.core.services.jmx;

import java.util.ArrayList;
import java.util.List;

import javax.management.MBeanException;

import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.Mockito.when;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.DefaultMessageFactory;
import com.adaptris.core.ServiceCase;
import com.adaptris.core.ServiceException;

public class JmxOperationCallServiceTest extends ServiceCase {
  
  private static final String BASE_DIR_KEY = "JmxServiceExamples.baseDir";
  
  private AdaptrisMessage message;
  
  private JmxOperationCallService callService;
  
  private String originalPayload = "OriginalPayload";

  @Mock private JmxOperationInvoker mockInvoker;
  
  public JmxOperationCallServiceTest(String name) {
    super(name);
    if (PROPERTIES.getProperty(BASE_DIR_KEY) != null) {
      setBaseDir(PROPERTIES.getProperty(BASE_DIR_KEY));
    }
  }
  
  /*************************************************************************************
   * JUNIT
   *************************************************************************************/
  
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);
    
    callService = new JmxOperationCallService();
    callService.setInvoker(mockInvoker);
    callService.setOperationName("MyOperationName");
    callService.setJmxServiceUrl("jmx:jmxmp://localhost:5555");
    callService.setObjectName("com.adaptris:type=Workflow,adapter=JMS-JMS-Adapter,channel=Channel1,id=Workflow1");
    
    message = DefaultMessageFactory.getDefaultInstance().newMessage(originalPayload);
    
    callService.init();
    callService.start();
  }
  
  public void tearDown() throws Exception {
    callService.stop();
    callService.close();
  }

  /*************************************************************************************
   * TESTS
   *************************************************************************************/
  
  public void testPayloadReturn() throws Exception {
    String operationReturnValue = "NewPayloadValue";
    when(mockInvoker.invoke(anyString(), anyString(), anyString(), anyString(), anyString(), any(Object[].class), any(String[].class)))
      .thenReturn(operationReturnValue);
    
    callService.setResultValueTranslator(new PayloadValueTranslator());
    callService.doService(message);
    
    assertEquals(operationReturnValue, message.getStringPayload());
  }
  
  public void testPayloadReturnNullPayloadType() throws Exception {
    String operationReturnValue = "NewPayloadValue";
    when(mockInvoker.invoke(anyString(), anyString(), anyString(), anyString(), anyString(), any(Object[].class), any(String[].class)))
      .thenReturn(operationReturnValue);
    
    PayloadValueTranslator payloadValueTranslator = new PayloadValueTranslator();
    payloadValueTranslator.setType(null);
    callService.setResultValueTranslator(payloadValueTranslator);
    callService.doService(message);
    
    assertEquals(operationReturnValue, message.getStringPayload());
  }
  
  public void testPayloadReturnWithParams() throws Exception {
    String operationReturnValue = "NewPayloadValue";
    when(mockInvoker.invoke(anyString(), anyString(), anyString(), anyString(), anyString(), any(Object[].class), any(String[].class)))
      .thenReturn(operationReturnValue);
    
    PayloadValueTranslator translatorParam = new PayloadValueTranslator();
    List<ValueTranslator> parameters = new ArrayList<>();
    parameters.add(translatorParam);
    
    callService.setOperationParameters(parameters);
    callService.setResultValueTranslator(new PayloadValueTranslator());
    callService.doService(message);
    
    assertEquals(operationReturnValue, message.getStringPayload());
  }
  
  public void testNoReturn() throws Exception {
    String operationReturnValue = "NewPayloadValue";
    when(mockInvoker.invoke(anyString(), anyString(), anyString(), anyString(), anyString(), any(Object[].class), any(String[].class)))
      .thenReturn(operationReturnValue);
    
    callService.setResultValueTranslator(null);
    callService.doService(message);
    
    assertEquals(originalPayload, message.getStringPayload());
  }
  
  public void testInvokerException() throws Exception {
    when(mockInvoker.invoke(anyString(), anyString(), anyString(), anyString(), anyString(), any(Object[].class), any(String[].class)))
      .thenThrow(new MBeanException(new Exception(), "Expected"));
    
    callService.setResultValueTranslator(null);
    try {
      callService.doService(message);
      fail("Expect a service exception");
    } catch (ServiceException ex) {
      //expected
    }
  }
  
  public void testMetadataReturnWithObjectParams() throws Exception {
    String existingObjectMetadataKey = "ExistingObjectMetadataKey";
    String existingObjectMetadataValue = "ExistingObjectMetadataValue";
    String newMetadataKay = "NewMetadataKey";
    
    String operationReturnValue = "NewMetadataValue";
    when(mockInvoker.invoke(anyString(), anyString(), anyString(), anyString(), anyString(), any(Object[].class), any(String[].class)))
      .thenReturn(operationReturnValue);
    
    message.addObjectMetadata(existingObjectMetadataKey, existingObjectMetadataValue);
    
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
    
    MetadataValueTranslator param2 = new MetadataValueTranslator();
    param2.setMetadataKey("metadataKey");
    
    ObjectMetadataValueTranslator param3 = new ObjectMetadataValueTranslator();
    param3.setMetadataKey("objectMetadataKey");
    param3.setType("java.lang.Object");
    
    ConstantValueTranslator param4 = new ConstantValueTranslator();
    param4.setAllowOverwrite(true);
    param4.setValue("1");
    param4.setType("java.lang.Integer");
    
    List<ValueTranslator> params = new ArrayList<>();
    params.add(param1);
    params.add(param2);
    params.add(param3);
    params.add(param4);
    
    JmxOperationCallService callService = new JmxOperationCallService();
    callService.setJmxServiceUrl("service:jmx:rmi://host[:connectorPort]urlpath");
    callService.setObjectName("com.adaptris:type=Workflow,adapter=MyAdapter,channel=Channel1,id=Workflow1");
    callService.setOperationName("myMethodToInvoke");
    callService.setUsername("jmxUsername");
    callService.setPassword("jmxPassword");
    callService.setOperationParameters(params);
    callService.setResultValueTranslator(resultTranslator);
    
    return callService;
  }



}
