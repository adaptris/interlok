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
import org.junit.Test;
import com.adaptris.core.AdapterMarshallerFactory.MarshallingOutput;
import com.thoughtworks.xstream.XStream;

public class XStreamJsonMarshallerTest extends MarshallingBaseCase {

  private static final String XSTREAM_STANDARD_JSON   = "xstream-standard.json";
  private static final String XSTREAM_STANDARD_XML    = "xstream-standard.xml";
  
  AdapterMarshallerFactory marshallerFactory = AdapterXStreamMarshallerFactory.getInstance();
  
  @Override
  protected XStreamJsonMarshaller createMarshaller() throws Exception {
    return (XStreamJsonMarshaller)marshallerFactory.createMarshaller(MarshallingOutput.JSON);
  }

  @Override
  protected String getClasspathXmlFilename() {
    return "xstream.json";
  }
  
  
  @Test
  public void testXStreamFullConfigMarshall() throws Exception {
    try {
      // Create factory
      AdapterXStreamMarshallerFactory factory = AdapterXStreamMarshallerFactory.getInstance();

      // Create xstream instance
      XStream xstreamInstance = factory.createXStream(MarshallingOutput.JSON);
      Adapter standardAdapter = null;
      try (InputStream resourceAsStream = this.getClass().getClassLoader().getResourceAsStream(XSTREAM_STANDARD_JSON)) {
        standardAdapter = (Adapter) xstreamInstance.fromXML(resourceAsStream);
      }

      // Check that the Adapter instance contains all fields
      XStreamMarshallerTest.adapterInstanceFieldChecks(standardAdapter);

      // Now since unmarshalling an adapter autopopulates certain fields then the
      // best way to compare is to marshal both adapters back to xml and compare
      // them that way.

      // Now marshal and then unmarshal to check that it works as intended
      String standardMarshalledXML = xstreamInstance.toXML(standardAdapter);
      Adapter roundTripAdapter = (Adapter) xstreamInstance.fromXML(standardMarshalledXML);

      assertRoundtripEquality(standardAdapter, roundTripAdapter);
    }
    finally {
      AdapterXStreamMarshallerFactory.reset();
    }
  }
  
}

