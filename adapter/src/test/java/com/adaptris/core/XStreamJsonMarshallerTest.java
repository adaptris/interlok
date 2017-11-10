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

package com.adaptris.core;

import java.io.InputStream;

import com.adaptris.core.AdapterMarshallerFactory.MarshallingOutput;
import com.adaptris.core.AdapterXStreamMarshallerFactory.OutputMode;
import com.adaptris.core.runtime.NullMessageErrorDigester;
import com.thoughtworks.xstream.XStream;

public class XStreamJsonMarshallerTest extends MarshallingBaseCase {

  private static final String XSTREAM_STANDARD_JSON   = "xstream-standard.json";
  private static final String XSTREAM_BEAUTIFIED_JSON = "xstream-beautified.json";
  private static final String XSTREAM_STANDARD_XML    = "xstream-standard.xml";
  
  AdapterMarshallerFactory marshallerFactory = AdapterXStreamMarshallerFactory.getInstance();
  
  public XStreamJsonMarshallerTest(java.lang.String testName) {
    super(testName);
  }

  @Override
  protected XStreamJsonMarshaller createMarshaller() throws Exception {
    return (XStreamJsonMarshaller)marshallerFactory.createMarshaller(MarshallingOutput.JSON);
  }

  @Override
  protected String getClasspathXmlFilename() {
    return "xstream.json";
  }
  
  // Reads in a standard xml adapter config and then generates a json based config
  public void testXStreamConfigXMLtoJSON() throws Exception {
    // Create factory
    AdapterXStreamMarshallerFactory factory = AdapterXStreamMarshallerFactory.getInstance();
    
    // Create xstream instance
    XStream xstreamInstance = factory.createXStream(MarshallingOutput.XML);
    Adapter standardAdapter = null;
    try ( InputStream resourceAsStream = this.getClass().getClassLoader().getResourceAsStream(XSTREAM_STANDARD_XML) ) {
      standardAdapter = (Adapter) xstreamInstance.fromXML(resourceAsStream);
      // Default has changed, so set the adapter to have a null-message-error-digestor INTERLOK-506
      standardAdapter.setMessageErrorDigester(new NullMessageErrorDigester());
    }

    // Check that the Adapter instance contains all fields
    XStreamMarshallerTest.adapterInstanceFieldChecks(standardAdapter);
    
    // Now create JSON based XStream instance for output
    factory.setMode(OutputMode.ALIASED_SUBCLASSES);
    xstreamInstance = factory.createXStream(MarshallingOutput.JSON);
    
    // Now since unmarshalling an adapter autopopulates certain fields then the
    // best way to compare is to marshal both adapters back to xml and compare
    // them that way.
    
    // Now marshal standard format adapter to xml
    String standardMarshalledXML = xstreamInstance.toXML(standardAdapter);

    // Read in the expected beautified file.
    Adapter beautifiedAdapter = null;
    try ( InputStream resourceAsStream = this.getClass().getClassLoader().getResourceAsStream(XSTREAM_BEAUTIFIED_JSON) ) {
      beautifiedAdapter = (Adapter) xstreamInstance.fromXML(resourceAsStream);
      // Default has changed, so set the adapter to have a null-message-error-digestor INTERLOK-506
      beautifiedAdapter.setMessageErrorDigester(new NullMessageErrorDigester());
    }
    
    // Check that the Adapter instance contains all fields
    XStreamMarshallerTest.adapterInstanceFieldChecks(beautifiedAdapter);
    
    // Now marshal beautified format adapter to xml
    // String beautifiedMarshalledXML = xstreamInstance.toXML(beautifiedAdapter);
    
    // Ensure that the conversion from standard xml to beautified xml went as expected 
    // assertEquals(beautifiedMarshalledXML, standardMarshalledXML);
    assertRoundtripEquality(standardAdapter, beautifiedAdapter);

  }
  
  // Standard JSON roundtrip test
  public void testXStreamFullConfigMarshall() throws Exception {
    // Create factory
    AdapterXStreamMarshallerFactory factory = AdapterXStreamMarshallerFactory.getInstance();
    factory.setMode(OutputMode.STANDARD);

    // Create xstream instance
    XStream xstreamInstance = factory.createXStream(MarshallingOutput.JSON);
    Adapter standardAdapter = null;
    try ( InputStream resourceAsStream = this.getClass().getClassLoader().getResourceAsStream(XSTREAM_STANDARD_JSON) ) {
      standardAdapter = (Adapter) xstreamInstance.fromXML(resourceAsStream);
    }

    // Check that the Adapter instance contains all fields
    XStreamMarshallerTest.adapterInstanceFieldChecks(standardAdapter);
    
    // Now since unmarshalling an adapter autopopulates certain fields then the
    // best way to compare is to marshal both adapters back to xml and compare
    // them that way.
    
    // Now marshal and then unmarshal to check that it works as intended
    String standardMarshalledXML = xstreamInstance.toXML(standardAdapter);
    Adapter roundTripAdapter = (Adapter)xstreamInstance.fromXML(standardMarshalledXML);
    
    assertRoundtripEquality(standardAdapter, roundTripAdapter);
  }
  
  // JSON beautified round trip test
  public void testXStreamBeautified() throws Exception {
    // Create factory
    AdapterXStreamMarshallerFactory factory = AdapterXStreamMarshallerFactory.getInstance();
    factory.setMode(OutputMode.ALIASED_SUBCLASSES);

    // Create xstream instance
    XStream xstreamInstance = factory.createXStream(MarshallingOutput.JSON);
    Adapter beautifiedAdapter = null;
    try ( InputStream resourceAsStream = this.getClass().getClassLoader().getResourceAsStream(XSTREAM_BEAUTIFIED_JSON) ) {
      beautifiedAdapter = (Adapter) xstreamInstance.fromXML(resourceAsStream);
    }

    // Check that the Adapter instance contains all fields
    XStreamMarshallerTest.adapterInstanceFieldChecks(beautifiedAdapter);
    
    // Now since unmarshalling an adapter autopopulates certain fields then the
    // best way to compare is to marshal both adapters back to xml and compare
    // them that way.
    
    // Now marshal and then unmarshal to check that it works as intended
    String standardMarshalledXML = xstreamInstance.toXML(beautifiedAdapter);
    Adapter roundTripAdapter = (Adapter)xstreamInstance.fromXML(standardMarshalledXML);
    
    assertRoundtripEquality(beautifiedAdapter, roundTripAdapter);
  }
}

