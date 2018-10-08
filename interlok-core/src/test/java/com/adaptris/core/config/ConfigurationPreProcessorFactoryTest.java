/*
 * Copyright 2015 Adaptris Ltd.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/

package com.adaptris.core.config;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

import java.util.Properties;

import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.adaptris.core.config.DefaultPreProcessorLoader.PropertyLoader;
import com.adaptris.core.stubs.JunitBootstrapProperties;

import junit.framework.TestCase;

public class ConfigurationPreProcessorFactoryTest extends TestCase {
  
  private DefaultPreProcessorLoader preProcessorFactory;
  
  @Mock
  private PropertyLoader mockPropertyLoader;
  
  private Properties sampleProperties;
  
  public void tearDown() throws Exception {
    
  }
  
  public void setUp() throws Exception {
    preProcessorFactory = new DefaultPreProcessorLoader();
    sampleProperties = new Properties();
    sampleProperties.put("name", "testPreProcessor");
    sampleProperties.put("class", DummyConfigurationPreProcessor.class.getCanonicalName());
    MockitoAnnotations.initMocks(this);
  }
  
  public void testNoConfiguredPreProcessors() throws Exception {
    ConfigPreProcessors loaded = preProcessorFactory.load(new JunitBootstrapProperties(new Properties()));
    
    assertEquals(0, loaded.size());
  }
  
  public void testEmptyConfiguredPreProcessors() throws Exception {
    Properties props = new Properties();
    props.put("preProcessors", "");
    
    ConfigPreProcessors loaded = preProcessorFactory.load(new JunitBootstrapProperties(props));
    
    assertEquals(0, loaded.size());
  }
  
  public void testSingleConfiguredPreProcessors() throws Exception {
    //Bypass the searching for meta-inf property files
    when(mockPropertyLoader.loadPropertyFile(anyString())).thenReturn(sampleProperties);
    preProcessorFactory.setPropertyLoader(mockPropertyLoader);
    
    Properties props = new Properties();
    props.put("preProcessors", "testPreProcessor");
    
    ConfigPreProcessors loaded = preProcessorFactory.load(new JunitBootstrapProperties(props));
    
    assertEquals(1, loaded.size());
    assertEquals(DummyConfigurationPreProcessor.class.getCanonicalName(), loaded.toArray()[0].getClass().getName());
  }
  
  
  public void testMisConfiguredPreProcessorNoClassName() throws Exception {
    when(mockPropertyLoader.loadPropertyFile(anyString())).thenReturn(new Properties());
    preProcessorFactory.setPropertyLoader(mockPropertyLoader);
    
    Properties props = new Properties();
    props.put("preProcessors", "testPreProcessor");
    assertEquals(0, preProcessorFactory.load(new JunitBootstrapProperties(props)).size());
  }
  
  public void testMultipleExternalPropertyFilesOnly1Configured() throws Exception {
    Properties sampleProperties2 = new Properties();
    sampleProperties2.put("name", "testPreProcessor2");
    sampleProperties2.put("class", DummyConfigurationPreProcessor2.class.getCanonicalName());
    
    when(mockPropertyLoader.loadPropertyFile("testPreProcessor")).thenReturn(sampleProperties);
    when(mockPropertyLoader.loadPropertyFile("testPreProcessor2")).thenReturn(sampleProperties2);
    preProcessorFactory.setPropertyLoader(mockPropertyLoader);
    
    Properties props = new Properties();
    props.put("preProcessors", "testPreProcessor");
    
    ConfigPreProcessors processorsList = preProcessorFactory.load(new JunitBootstrapProperties(props));
    assertEquals(1, processorsList.size());
    assertEquals(DummyConfigurationPreProcessor.class.getCanonicalName(), processorsList.toArray()[0].getClass().getName());
  }
  
  public void testMultipleExternalPropertyFiles2Configured() throws Exception {
    Properties sampleProperties2 = new Properties();
    sampleProperties2.put("name", "testPreProcessor2");
    sampleProperties2.put("class", DummyConfigurationPreProcessor2.class.getCanonicalName());
    
    when(mockPropertyLoader.loadPropertyFile("testPreProcessor")).thenReturn(sampleProperties);
    when(mockPropertyLoader.loadPropertyFile("testPreProcessor2")).thenReturn(sampleProperties2);
    preProcessorFactory.setPropertyLoader(mockPropertyLoader);
    
    Properties props = new Properties();
    props.put("preProcessors", "testPreProcessor:testPreProcessor2");
    
    ConfigPreProcessors processorsList = preProcessorFactory.load(new JunitBootstrapProperties(props));
    assertEquals(2, processorsList.size());
    assertEquals(DummyConfigurationPreProcessor.class.getCanonicalName(), processorsList.toArray()[0].getClass().getName());
    assertEquals(DummyConfigurationPreProcessor2.class.getCanonicalName(), processorsList.toArray()[1].getClass().getName());
  }

}
