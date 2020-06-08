package com.adaptris.core.management.config;

import static org.junit.Assert.assertEquals;
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
import com.adaptris.core.SharedConnection;
import com.adaptris.core.management.BootstrapProperties;
import com.adaptris.core.management.UnifiedBootstrap;
import com.adaptris.core.runtime.AdapterManagerMBean;

public class ConfigurationCheckRunnerTest {
  
  private static final String SHARED_CONN_TEST_FRIENDLY_NAME = "Shared connection check.";

  private ConfigurationCheckRunner checkRunner;
  
  private InputStream configurationStream;
  
  @Mock private BootstrapProperties mockBootProperties;
  
  @Mock private AdapterManagerMBean mockAdapterMBean;
  
  @Mock private UnifiedBootstrap mockUnifiedBootstrap;
  
  @Before
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);
    checkRunner = new ConfigurationCheckRunner();
    
    when(mockUnifiedBootstrap.createAdapter())
        .thenReturn(mockAdapterMBean);

    when(mockAdapterMBean.getConfiguration())
        .thenReturn(createAdapterConfig());
    
    System.setProperty("interlok.bootstrap.debug", "true");
  }
  
  @After
  public void tearDown() throws Exception {
    
  }
  
  @Test
  public void testSuccessRunner() throws Exception {
    configurationStream = this.createAdapterConfig(new NullConnection("SharedNullConnection"), new SharedConnection("SharedNullConnection"), new SharedConnection("SharedNullConnection"));
    
    when(mockBootProperties.getConfigurationStream())
        .thenReturn(configurationStream);
    
    checkRunner.runChecks(mockBootProperties, mockUnifiedBootstrap).forEach(report -> {
      // The de-dup test might actually fail, so lets test the shared connection test.
      if(report.getCheckName().equals(SHARED_CONN_TEST_FRIENDLY_NAME)) {
        assertTrue(report.isCheckPassed());
        assertEquals(0, report.getFailureExceptions().size());
      }
    });
  }
  
  @Test
  public void testFailureRunner() throws Exception {
    configurationStream = this.createAdapterConfig(new NullConnection("SharedNullConnection"), null, null);
    
    when(mockBootProperties.getConfigurationStream())
        .thenReturn(configurationStream);
    
    checkRunner.runChecks(mockBootProperties, mockUnifiedBootstrap).forEach(report -> {
      if(report.getCheckName().equals(SHARED_CONN_TEST_FRIENDLY_NAME)) {
        assertFalse(report.isCheckPassed());
        assertTrue(report.getWarnings().size() > 0);
        assertNotNull(report.toString());
      }
    });
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
  
  private String createAdapterConfig() throws Exception {
    Adapter adapter = new Adapter();
    adapter.setUniqueId("MyAdapter");
    return DefaultMarshaller.getDefaultMarshaller().marshal(adapter);
  }
}
