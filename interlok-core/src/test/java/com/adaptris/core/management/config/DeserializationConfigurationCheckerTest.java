package com.adaptris.core.management.config;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import com.adaptris.core.Adapter;
import com.adaptris.core.DefaultMarshaller;
import com.adaptris.core.management.BootstrapProperties;

public class DeserializationConfigurationCheckerTest {

  private DeserializationConfigurationChecker checker;

  @Before
  public void setUp() throws Exception {
    checker = new DeserializationConfigurationChecker();
  }

  @Test
  public void testUnmarshallSuccess() throws Exception {
    BootstrapProperties mockBootProperties = new MockBootProperties(createAdapterConfig());
    ConfigurationCheckReport report = checker.performConfigCheck(mockBootProperties);
    assertTrue(report.isCheckPassed());
  }

  @Test
  public void testUnmarshallFailureBadXml() throws Exception {
    BootstrapProperties mockBootProperties = new MockBootProperties("xxx");
    ConfigurationCheckReport report = checker.performConfigCheck(mockBootProperties);
    assertFalse(report.isCheckPassed());
  }

  private String createAdapterConfig() throws Exception {
    Adapter adapter = new Adapter();
    adapter.setUniqueId("MyAdapter");
    return DefaultMarshaller.getDefaultMarshaller().marshal(adapter);
  }
}
