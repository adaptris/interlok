package com.adaptris.core.management.config;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.adaptris.core.Adapter;
import com.adaptris.core.DefaultMarshaller;
import com.adaptris.core.management.BootstrapProperties;
import com.adaptris.core.management.UnifiedBootstrap;
import com.adaptris.core.runtime.AdapterManagerMBean;
import com.adaptris.interlok.util.Closer;

public class DeserializationConfigurationCheckerTest {

  private DeserializationConfigurationChecker checker;

  @Mock private AdapterManagerMBean mockAdapterMBean;

  @Mock private UnifiedBootstrap mockUnifiedBootstrap;

  private AutoCloseable openMocks;

  @Before
  public void setUp() throws Exception {
    openMocks = MockitoAnnotations.openMocks(this);

    checker = new DeserializationConfigurationChecker();
    when(mockUnifiedBootstrap.createAdapter()).thenReturn(mockAdapterMBean);
    when(mockAdapterMBean.getConfiguration()).thenReturn(createAdapterConfig());
  }

  @After
  public void tearDown() throws Exception {
    Closer.closeQuietly(openMocks);
  }

  @Test
  public void testUnmarshallSuccess() throws Exception {
    BootstrapProperties mockBootProperties = new MockBootProperties(createAdapterConfig());
    ConfigurationCheckReport report = checker.performConfigCheck(mockBootProperties, mockUnifiedBootstrap);
    assertTrue(report.isCheckPassed());
  }

  @Test
  public void testUnmarshallFailureBadXml() throws Exception {
    BootstrapProperties mockBootProperties = new MockBootProperties("xxx");
    ConfigurationCheckReport report = checker.performConfigCheck(mockBootProperties, mockUnifiedBootstrap);
    assertFalse(report.isCheckPassed());
  }

  private String createAdapterConfig() throws Exception {
    Adapter adapter = new Adapter();
    adapter.setUniqueId("MyAdapter");
    return DefaultMarshaller.getDefaultMarshaller().marshal(adapter);
  }
}
