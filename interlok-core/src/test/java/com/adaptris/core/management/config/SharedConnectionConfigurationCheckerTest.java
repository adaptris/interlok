package com.adaptris.core.management.config;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import java.io.InputStream;

import org.fusesource.hawtbuf.ByteArrayInputStream;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.adaptris.core.Adapter;
import com.adaptris.core.AdaptrisConnection;
import com.adaptris.core.Channel;
import com.adaptris.core.DefaultMarshaller;
import com.adaptris.core.NullConnection;
import com.adaptris.core.NullMessageProducer;
import com.adaptris.core.SharedConnection;
import com.adaptris.core.StandaloneProducer;
import com.adaptris.core.StandardWorkflow;
import com.adaptris.core.management.BootstrapProperties;

public class SharedConnectionConfigurationCheckerTest {

  private SharedConnectionConfigurationChecker checker;
  
  private InputStream configurationStream;
  
  @Mock private BootstrapProperties mockBootProperties;
  
  @Before
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);
    checker = new SharedConnectionConfigurationChecker();
    
  }
  
  @After
  public void tearDown() throws Exception {
  }
  
  @Test
  public void testCompleteFailureBadXml() throws Exception {
    configurationStream = new ByteArrayInputStream("bad-data".getBytes());
    
    when(mockBootProperties.getConfigurationStream())
        .thenReturn(configurationStream);
    
    ConfigurationCheckReport report = checker.performConfigCheck(mockBootProperties, null);
    assertFalse(report.isCheckPassed());
    assertTrue(report.getFailureExceptions().size() > 0);
  }
  
  @Test
  public void testNoConnections() throws Exception {
    configurationStream = this.createAdapterConfig(null, null, null, null);
    
    when(mockBootProperties.getConfigurationStream())
        .thenReturn(configurationStream);
    
    ConfigurationCheckReport report = checker.performConfigCheck(mockBootProperties, null);
    assertTrue(report.isCheckPassed());
  }
  
  @Test
  public void testSharedNotUsed() throws Exception {
    configurationStream = this.createAdapterConfig(new NullConnection("SharedNullConnection"), null, null, null);
    
    when(mockBootProperties.getConfigurationStream())
        .thenReturn(configurationStream);
    
    ConfigurationCheckReport report = checker.performConfigCheck(mockBootProperties, null);
    assertFalse(report.isCheckPassed());
  }
  
  @Test
  public void testConsumeConnectionNoShared() throws Exception {
    configurationStream = this.createAdapterConfig(null, new SharedConnection("DoesNotExist"), null, null);
    
    when(mockBootProperties.getConfigurationStream())
        .thenReturn(configurationStream);
    
    ConfigurationCheckReport report = checker.performConfigCheck(mockBootProperties, null);
    assertFalse(report.isCheckPassed());
    assertTrue(report.getFailureExceptions().size() > 0);
  }
  
  @Test
  public void testProduceConnectionNoShared() throws Exception {
    configurationStream = this.createAdapterConfig(null, null, new SharedConnection("DoesNotExist"), null);
    
    when(mockBootProperties.getConfigurationStream())
        .thenReturn(configurationStream);
    
    ConfigurationCheckReport report = checker.performConfigCheck(mockBootProperties, null);
    assertFalse(report.isCheckPassed());
    assertTrue(report.getFailureExceptions().size() > 0);
  }
  
  @Test
  public void testProduceAndConsumeConnectionsExist() throws Exception {
    configurationStream = this.createAdapterConfig(new NullConnection("SharedNullConnection"), new SharedConnection("SharedNullConnection"), new SharedConnection("SharedNullConnection"), null);
    
    when(mockBootProperties.getConfigurationStream())
        .thenReturn(configurationStream);
    
    ConfigurationCheckReport report = checker.performConfigCheck(mockBootProperties, null);
    assertTrue(report.isCheckPassed());
    assertNotNull(report.toString());
  }
  
  @Test
  public void testServiceConnectionDoesNotExist() throws Exception {
    configurationStream = this.createAdapterConfig(new NullConnection("SharedNullConnection"), null, null, new SharedConnection("DoesNotExist"));
    
    when(mockBootProperties.getConfigurationStream())
        .thenReturn(configurationStream);
    
    ConfigurationCheckReport report = checker.performConfigCheck(mockBootProperties, null);
    assertFalse(report.isCheckPassed());
    assertTrue(report.getFailureExceptions().size() > 0);
    assertNotNull(report.toString());
  }
  
  private InputStream createAdapterConfig(AdaptrisConnection sharedComponent, AdaptrisConnection consumeConnection, AdaptrisConnection produceConnection, AdaptrisConnection serviceConnection) throws Exception {
    Adapter adapter = new Adapter();
    if(sharedComponent != null)
      adapter.getSharedComponents().addConnection(sharedComponent);
    
    Channel channel = new Channel();
    if(consumeConnection != null)
      channel.setConsumeConnection(consumeConnection);
    if(produceConnection != null)
      channel.setProduceConnection(produceConnection);
    if(serviceConnection != null) {
      StandardWorkflow standardWorkflow = new StandardWorkflow();
      StandaloneProducer sp = new StandaloneProducer(serviceConnection, new NullMessageProducer());
      standardWorkflow.getServiceCollection().add(sp);
      channel.getWorkflowList().add(standardWorkflow);
      
    }
    
    adapter.getChannelList().add(channel);
    String marshalledConfig = DefaultMarshaller.getDefaultMarshaller().marshal(adapter);
    return new ByteArrayInputStream(marshalledConfig.getBytes());
  }
  
}
