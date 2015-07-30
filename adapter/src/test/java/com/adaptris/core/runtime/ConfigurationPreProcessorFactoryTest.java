package com.adaptris.core.runtime;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

import java.util.Properties;

import junit.framework.TestCase;

import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.adaptris.core.runtime.ConfigurationPreProcessorFactory.ExternalPreProcessorPropertyLoader;
import com.adaptris.core.stubs.JunitBootstrapProperties;

public class ConfigurationPreProcessorFactoryTest extends TestCase {
  
  private ConfigurationPreProcessorFactory preProcessorFactory;
  
  @Mock
  private ExternalPreProcessorPropertyLoader mockPropertyLoader;
  
  private Properties sampleProperties;
  
  public void tearDown() throws Exception {
    
  }
  
  public void setUp() throws Exception {
    preProcessorFactory = new ConfigurationPreProcessorFactory();
    sampleProperties = new Properties();
    sampleProperties.put("name", "testPreProcessor");
    sampleProperties.put("class", "com.adaptris.core.runtime.DummyConfigurationPreProcessor");
    MockitoAnnotations.initMocks(this);
  }
  
  public void testNoConfiguredPreProcessors() throws Exception {
    PreProcessorsList loaded = preProcessorFactory.load(new JunitBootstrapProperties(new Properties()));
    
    assertEquals(0, loaded.size());
  }
  
  public void testEmptyConfiguredPreProcessors() throws Exception {
    Properties props = new Properties();
    props.put("preProcessors", "");
    
    PreProcessorsList loaded = preProcessorFactory.load(new JunitBootstrapProperties(props));
    
    assertEquals(0, loaded.size());
  }
  
  public void testSingleConfiguredPreProcessors() throws Exception {
    //Bypass the searching for meta-inf property files
    when(mockPropertyLoader.loadPropertyFile(anyString())).thenReturn(sampleProperties);
    preProcessorFactory.setExternalPreProcessorPropertyLoader(mockPropertyLoader);
    
    Properties props = new Properties();
    props.put("preProcessors", "testPreProcessor");
    
    PreProcessorsList loaded = preProcessorFactory.load(new JunitBootstrapProperties(props));
    
    assertEquals(1, loaded.size());
    assertEquals("com.adaptris.core.runtime.DummyConfigurationPreProcessor", loaded.toArray()[0].getClass().getName());
  }
  
  
  public void testMisConfiguredPreProcessorNoClassName() throws Exception {
    when(mockPropertyLoader.loadPropertyFile(anyString())).thenReturn(new Properties());
    preProcessorFactory.setExternalPreProcessorPropertyLoader(mockPropertyLoader);
    
    Properties props = new Properties();
    props.put("preProcessors", "testPreProcessor");
    assertEquals(0, preProcessorFactory.load(new JunitBootstrapProperties(props)).size());
  }
  
  public void testMultipleExternalPropertyFilesOnly1Configured() throws Exception {
    Properties sampleProperties2 = new Properties();
    sampleProperties2.put("name", "testPreProcessor2");
    sampleProperties2.put("class", "com.adaptris.core.runtime.DummyConfigurationPreProcessor2");
    
    when(mockPropertyLoader.loadPropertyFile("testPreProcessor")).thenReturn(sampleProperties);
    when(mockPropertyLoader.loadPropertyFile("testPreProcessor2")).thenReturn(sampleProperties2);
    preProcessorFactory.setExternalPreProcessorPropertyLoader(mockPropertyLoader);
    
    Properties props = new Properties();
    props.put("preProcessors", "testPreProcessor");
    
    PreProcessorsList processorsList = preProcessorFactory.load(new JunitBootstrapProperties(props));
    assertEquals(1, processorsList.size());
    assertEquals("com.adaptris.core.runtime.DummyConfigurationPreProcessor", processorsList.toArray()[0].getClass().getName());
  }
  
  public void testMultipleExternalPropertyFiles2Configured() throws Exception {
    Properties sampleProperties2 = new Properties();
    sampleProperties2.put("name", "testPreProcessor2");
    sampleProperties2.put("class", "com.adaptris.core.runtime.DummyConfigurationPreProcessor2");
    
    when(mockPropertyLoader.loadPropertyFile("testPreProcessor")).thenReturn(sampleProperties);
    when(mockPropertyLoader.loadPropertyFile("testPreProcessor2")).thenReturn(sampleProperties2);
    preProcessorFactory.setExternalPreProcessorPropertyLoader(mockPropertyLoader);
    
    Properties props = new Properties();
    props.put("preProcessors", "testPreProcessor:testPreProcessor2");
    
    PreProcessorsList processorsList = preProcessorFactory.load(new JunitBootstrapProperties(props));
    assertEquals(2, processorsList.size());
    assertEquals("com.adaptris.core.runtime.DummyConfigurationPreProcessor", processorsList.toArray()[0].getClass().getName());
    assertEquals("com.adaptris.core.runtime.DummyConfigurationPreProcessor2", processorsList.toArray()[1].getClass().getName());
  }

}
