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

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.mockito.Mockito;

import com.adaptris.core.Adapter;
import com.adaptris.core.DefaultMarshaller;
import com.adaptris.core.MarshallingBaseCase;
import com.adaptris.core.XStreamMarshaller;
import com.adaptris.core.management.BootstrapProperties;
import com.adaptris.util.KeyValuePairSet;


public class PreProcessingXStreamMarshallerTest extends MarshallingBaseCase {

  private static final String XSTREAM_STANDARD_XML = "xstream-standard.xml";
  private static final String XSTREAM_BEAUTIFIED_XML = "xstream-beautified.xml";

  public PreProcessingXStreamMarshallerTest(java.lang.String testName) {
    super(testName);
  }

  public void setUp() throws Exception {
    super.setUp();
  }


  @Override
  protected XStreamMarshaller createMarshaller() throws Exception {
    PreProcessingXStreamMarshaller marshaller = new PreProcessingXStreamMarshaller();
    marshaller.setPreProcessors(
        DummyConfigurationPreProcessor.class.getCanonicalName() + ":" + DummyConfigurationPreProcessor2.class.getCanonicalName());
    return marshaller;
  }

  public void testPreProcessorCalled() throws Exception {
    DefaultPreProcessorLoader mockLoader = Mockito.mock(DefaultPreProcessorLoader.class);
    DummyConfigurationPreProcessor mockPreProc = Mockito.mock(DummyConfigurationPreProcessor.class);

    Adapter adapter = createMarshallingObject();
    String xml = DefaultMarshaller.getDefaultMarshaller().marshal(adapter);


    PreProcessingXStreamMarshaller marshaller = new PreProcessingXStreamMarshaller();
    marshaller.setPreProcessors(DummyConfigurationPreProcessor.class.getCanonicalName());
    marshaller.setPreProcessorLoader(mockLoader);

    ConfigPreProcessors preProcessorsList = new ConfigPreProcessors(mockPreProc);

    when(mockLoader.load(any(String.class), any(KeyValuePairSet.class))).thenReturn(preProcessorsList);
    when(mockLoader.load(any(BootstrapProperties.class))).thenReturn(preProcessorsList);
    when(mockPreProc.process(any(String.class))).thenReturn(xml);

    Adapter unmarshalled = (Adapter) marshaller.unmarshal(xml);

    verify(mockPreProc, times(1)).process(any(String.class));
    verify(mockLoader, times(1)).load(any(String.class), any(KeyValuePairSet.class));
    verify(mockLoader, times(0)).load(any(BootstrapProperties.class));
    assertRoundtripEquality(adapter, unmarshalled);

  }


  @Override
  protected String getClasspathXmlFilename() {
    return "xstream-standalone.xml";
  }
}
