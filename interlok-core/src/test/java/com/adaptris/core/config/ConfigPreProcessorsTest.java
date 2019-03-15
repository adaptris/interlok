package com.adaptris.core.config;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.net.URL;

import org.junit.Test;

import com.adaptris.core.CoreException;
import com.adaptris.util.KeyValuePairSet;

public class ConfigPreProcessorsTest {

  @Test
  public void testSize() {
    ConfigPreProcessors p = new ConfigPreProcessors();
    assertEquals(0, p.size());
  }

  @Test
  public void testClear() {
    ConfigPreProcessors p =
        new ConfigPreProcessors(new DummyConfigurationPreProcessor(new KeyValuePairSet()),
            new DummyConfigurationPreProcessor(new KeyValuePairSet()));
    assertEquals(2, p.size());
    p.clear();
    assertEquals(0, p.size());
  }

  @Test
  public void testProcessURL() throws Exception {
    ConfigPreProcessors p =
        new ConfigPreProcessors(new DummyConfigurationPreProcessor(new KeyValuePairSet()),
            new DummyConfigurationPreProcessor(new KeyValuePairSet()));
    URL onClasspath = this.getClass().getClassLoader().getResource("xstream-standalone.xml");
    assertNotNull(p.process(onClasspath));
    try {
      p.process(new URL("file:///./does/not/exist"));
      fail();
    } catch (CoreException expected) {
      
    }
  }

  @Test
  public void testProcessString() throws Exception {
    ConfigPreProcessors p =
        new ConfigPreProcessors(new DummyConfigurationPreProcessor(new KeyValuePairSet()));
    assertNotNull(p.process("<xml/>"));
  }

  @Test
  public void testIterator() {
    ConfigPreProcessors p =
        new ConfigPreProcessors(new DummyConfigurationPreProcessor(new KeyValuePairSet()));
    assertNotNull(p.iterator());
  }

  @Test
  public void testAddConfigPreProcessor() {
    ConfigPreProcessors p =
        new ConfigPreProcessors(new DummyConfigurationPreProcessor(new KeyValuePairSet()));
    p.add(new DummyConfigurationPreProcessor(new KeyValuePairSet()));
    assertEquals(2, p.size());
  }

  @Test
  public void testGetPreProcessors() {
    ConfigPreProcessors p =
        new ConfigPreProcessors(new DummyConfigurationPreProcessor(new KeyValuePairSet()),
            new DummyConfigurationPreProcessor(new KeyValuePairSet()));
    assertEquals(2, p.getPreProcessors().size());
  }

}
