package com.adaptris.core.config;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;

import java.util.Properties;

import org.junit.jupiter.api.Test;

import com.adaptris.core.management.BootstrapProperties;
import com.adaptris.core.stubs.JunitBootstrapProperties;

public class ConfigPreProcessorImplTest {


  @Test
  @SuppressWarnings("deprecation")
  public void testConfiguration() {
    BootstrapProperties props = new JunitBootstrapProperties(new Properties());
    DummyConfigurationPreProcessor p = new DummyConfigurationPreProcessor(props);
    assertNotSame(props, p.getBootstrapProperties());
    assertNotNull(p.getConfiguration());
    assertEquals(0, p.getConfiguration().size());
  }

  @Test
  public void testProperties() {
    BootstrapProperties props = new JunitBootstrapProperties(new Properties());
    DummyConfigurationPreProcessor p = new DummyConfigurationPreProcessor(props);
    p.setProperties(new Properties());
    assertNotNull(p.getProperties());
  }

}
