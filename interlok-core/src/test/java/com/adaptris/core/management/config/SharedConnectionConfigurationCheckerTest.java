package com.adaptris.core.management.config;

import static org.junit.Assert.assertFalse;
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
import com.adaptris.core.SharedConnection;
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
  public void testNoConnections() throws Exception {
    configurationStream = this.createAdapterConfig(null, null, null);
    
    when(mockBootProperties.getConfigurationStream())
        .thenReturn(configurationStream);
    
    ConfigurationCheckReport report = checker.performConfigCheck(mockBootProperties, null);
    assertTrue(report.isCheckPassed());
  }
  
  @Test
  public void testSharedNotUsed() throws Exception {
    configurationStream = this.createAdapterConfig(new NullConnection("SharedNullConnection"), null, null);
    
    when(mockBootProperties.getConfigurationStream())
        .thenReturn(configurationStream);
    
    ConfigurationCheckReport report = checker.performConfigCheck(mockBootProperties, null);
    assertFalse(report.isCheckPassed());
  }
  
  @Test
  public void testConsumeConnectionNoShared() throws Exception {
    configurationStream = this.createAdapterConfig(null, new SharedConnection("DoesNotExist"), null);
    
    when(mockBootProperties.getConfigurationStream())
        .thenReturn(configurationStream);
    
    ConfigurationCheckReport report = checker.performConfigCheck(mockBootProperties, null);
    assertFalse(report.isCheckPassed());

  }
  
  @Test
  public void testProduceConnectionNoShared() throws Exception {
    configurationStream = this.createAdapterConfig(null, null, new SharedConnection("DoesNotExist"));
    
    when(mockBootProperties.getConfigurationStream())
        .thenReturn(configurationStream);
    
    ConfigurationCheckReport report = checker.performConfigCheck(mockBootProperties, null);
    assertFalse(report.isCheckPassed());
  }
  
  @Test
  public void testProduceAndConsumeConnectionsExist() throws Exception {
    configurationStream = this.createAdapterConfig(new NullConnection("SharedNullConnection"), new SharedConnection("SharedNullConnection"), new SharedConnection("SharedNullConnection"));
    
    when(mockBootProperties.getConfigurationStream())
        .thenReturn(configurationStream);
    
    ConfigurationCheckReport report = checker.performConfigCheck(mockBootProperties, null);
    assertTrue(report.isCheckPassed());
  }
  
  
  private InputStream createAdapterConfig(AdaptrisConnection sharedComponent, AdaptrisConnection consumeConnection, AdaptrisConnection produceConnection) throws Exception {
    Adapter adapter = new Adapter();
    if(sharedComponent != null)
      adapter.getSharedComponents().addConnection(sharedComponent);
    
    Channel channel = new Channel();
    if(consumeConnection != null)
      channel.setConsumeConnection(consumeConnection);
    if(produceConnection != null)
      channel.setProduceConnection(produceConnection);
    
    adapter.getChannelList().add(channel);
    String marshalledConfig = DefaultMarshaller.getDefaultMarshaller().marshal(adapter);
    return new ByteArrayInputStream(marshalledConfig.getBytes());
  }
  
}
