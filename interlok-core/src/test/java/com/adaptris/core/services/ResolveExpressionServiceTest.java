package com.adaptris.core.services;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.DefaultMessageFactory;
import com.adaptris.core.ServiceException;
import com.adaptris.core.common.ConstantDataInputParameter;
import com.adaptris.core.common.MetadataDataOutputParameter;
import com.adaptris.core.util.LifecycleHelper;

public class ResolveExpressionServiceTest {
  
  private ResolveExpressionService service;
  
  private AdaptrisMessage message;
  
  @Before
  public void setUp() throws Exception {
    message = DefaultMessageFactory.getDefaultInstance().newMessage();
    service = new ResolveExpressionService();
    
    service.setOutput(new MetadataDataOutputParameter("result"));
    
    LifecycleHelper.prepare(service);
    LifecycleHelper.initAndStart(service);
  }
  
  @After
  public void tearDown() {
    LifecycleHelper.stopAndClose(service);
  }
  
  @Test
  public void testResolveAMetadataKey() throws Exception {
    message.addMessageHeader("key", "value1");
    
    service.setInput(new ConstantDataInputParameter("%message{key}"));
    service.doService(message);
    
    assertEquals("Should be value1", "value1", message.getMetadataValue("result"));
  }
  
  @Test
  public void testResolveToAppendToMetadata() throws Exception {
    message.addMessageHeader("result", "AB");
    
    service.setInput(new ConstantDataInputParameter("%message{result}C"));
    service.doService(message);
    
    assertEquals("Should be ABC", "ABC", message.getMetadataValue("result"));
  }
  
  @Test
  public void testResolveAMetadataKeyDoesNotExist() throws Exception {    
    service.setInput(new ConstantDataInputParameter("%message{key}"));
    try {
      service.doService(message);
      fail("Should fail with no metadata found.");
    } catch (ServiceException ex) {
      // expected.
    }
  }
  
  @Test
  public void testResolveAMetadataKeyWithNoExpression() throws Exception {    
    service.setInput(new ConstantDataInputParameter("NoExpression"));
    service.doService(message);
    
    assertEquals("Should be NoExpression", "NoExpression", message.getMetadataValue("result"));
  }
}
