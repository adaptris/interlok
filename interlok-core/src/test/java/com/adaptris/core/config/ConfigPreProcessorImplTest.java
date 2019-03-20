package com.adaptris.core.config;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;

import java.util.Properties;

import org.junit.Test;

import com.adaptris.core.management.BootstrapProperties;
import com.adaptris.core.stubs.JunitBootstrapProperties;

public class ConfigPreProcessorImplTest {


  @Test
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
