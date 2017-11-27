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
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.adaptris.core.AdapterXStreamMarshallerFactory.OutputMode;
import com.adaptris.core.services.LogMessageService;
import com.adaptris.core.services.metadata.AddMetadataService;
import com.adaptris.core.services.metadata.XpathMetadataService;
import com.adaptris.core.services.metadata.xpath.ConfiguredXpathQuery;
import com.adaptris.core.services.metadata.xpath.XpathQuery;
import com.adaptris.core.stubs.XStreamBeanInfoWrapper;
import com.adaptris.core.stubs.XStreamCDataWrapper;
import com.adaptris.core.stubs.XStreamImplicitWrapper;
import com.adaptris.core.util.XmlHelper;
import com.adaptris.util.text.xml.XPath;
import com.thoughtworks.xstream.XStream;


@SuppressWarnings("deprecation")
public class XStreamMarshallerTest extends MarshallingBaseCase {

  private static final String XSTREAM_STANDARD_XML = "xstream-standard.xml";
  private static final String XSTREAM_BEAUTIFIED_XML = "xstream-beautified.xml";

  public XStreamMarshallerTest(java.lang.String testName) {
    super(testName);
  }

  @Override
  protected XStreamMarshaller createMarshaller() throws Exception {
    return new XStreamMarshaller();
  }

  @Override
  protected String getClasspathXmlFilename() {
    return "xstream-standalone.xml";
  }

  public void testXStreamImplicit() throws Exception {
    XStreamMarshaller xm = createMarshaller();
    XStreamImplicitWrapper wrapper = new XStreamImplicitWrapper();
//    String id = wrapper.getMarshalledIdentity();
    wrapper.addMarshalledString("ABC");
    wrapper.addMarshalledString("DEF");
    wrapper.addParentString("123");
    wrapper.addParentString("234");
    String xml = xm.marshal(wrapper);
    Document xmlDoc = XmlHelper.createDocument(xml);
    XPath xpath = new XPath();
    assertEquals("ABC", xpath.selectSingleTextItem(xmlDoc, "/xstream-implicit-wrapper/marshalled-string[1]"));
    assertEquals("DEF", xpath.selectSingleTextItem(xmlDoc, "/xstream-implicit-wrapper/marshalled-string[2]"));
    assertEquals("123", xpath.selectSingleTextItem(xmlDoc, "/xstream-implicit-wrapper/parent-string[1]"));
    assertEquals("234", xpath.selectSingleTextItem(xmlDoc, "/xstream-implicit-wrapper/parent-string[2]"));
    XStreamImplicitWrapper roundTrip = (XStreamImplicitWrapper) xm.unmarshal(xml);
    assertEquals(2, roundTrip.getMarshalledStrings().size());
    assertEquals(2, roundTrip.getParentStrings().size());
    assertRoundtripEquality(wrapper, roundTrip);
  }

  public void testXStreamBeanInfo() throws Exception {
    XStreamMarshaller xm = createMarshaller();
    XStreamBeanInfoWrapper wrapper = new XStreamBeanInfoWrapper();
    String id = wrapper.getMarshalledIdentity();
    assertFalse(wrapper.getSetterCalled());
    String xml = xm.marshal(wrapper);
    XStreamBeanInfoWrapper roundTrip = (XStreamBeanInfoWrapper) xm.unmarshal(xml);
    assertEquals(id, roundTrip.getMarshalledIdentity());
    assertTrue(roundTrip.getSetterCalled());
  }

  public void testXStreamCdata() throws Exception {
    XStreamMarshaller xm = createMarshaller();
    XStreamCDataWrapper wrapper = new XStreamCDataWrapper();
//    String id = wrapper.getRawValue();
    String xml = xm.marshal(wrapper);
    Document xmlDoc = XmlHelper.createDocument(xml);
    XPath xpath = new XPath();
    Node rawNode = xpath.selectSingleNode(xmlDoc, "/xstream-cdata-wrapper/raw-value");
    Node parentRawNode = xpath.selectSingleNode(xmlDoc, "/xstream-cdata-wrapper/parent-raw-value");

    // How else to do this...
    // We know that what we have is an element, and the first child should be the raw CDATA...
    //
    assertEquals(Node.CDATA_SECTION_NODE, ((Element) rawNode).getFirstChild().getNodeType());
    assertEquals(Node.CDATA_SECTION_NODE, ((Element) parentRawNode).getFirstChild().getNodeType());
    XStreamCDataWrapper roundTrip = (XStreamCDataWrapper) xm.unmarshal(xml);
    assertRoundtripEquality(wrapper, roundTrip);
  }
  
  public void testBeautifiedBeanInfo() throws Exception {
    try {
      AdapterXStreamMarshallerFactory factory = AdapterXStreamMarshallerFactory.getInstance();
      factory.setMode(OutputMode.ALIASED_SUBCLASSES);
      XStream xstreamInstance = factory.createXStream();
      XStreamBeanInfoWrapper wrapper = new XStreamBeanInfoWrapper();
      String id = wrapper.getMarshalledIdentity();
      assertFalse(wrapper.getSetterCalled());
      String xml = xstreamInstance.toXML(new ServiceList(wrapper));
      XStreamBeanInfoWrapper roundTrip = (XStreamBeanInfoWrapper) ((ServiceList) xstreamInstance.fromXML(xml)).get(0);
      assertEquals(id, roundTrip.getMarshalledIdentity());
      assertTrue(roundTrip.getSetterCalled());
    }
    finally {
      AdapterXStreamMarshallerFactory.reset();
    }
  }

  // redmineID 2457 Beautifying the XStream output - Test conversion of standard config to beautified config
  public void testXStreamBeautified() throws Exception {
    try { // Create factory
    AdapterXStreamMarshallerFactory factory = AdapterXStreamMarshallerFactory.getInstance();
    factory.setMode(OutputMode.ALIASED_SUBCLASSES);
    
    // Create xstream instance
    XStream xstreamInstance = factory.createXStream();
    Adapter standardAdapter = null;
    try ( InputStream resourceAsStream = this.getClass().getClassLoader().getResourceAsStream(XSTREAM_STANDARD_XML) ) {
      standardAdapter = (Adapter) xstreamInstance.fromXML(resourceAsStream);
    }

    // Check the unmarshalled adapter
    adapterInstanceFieldChecks(standardAdapter);
    
    // Now since unmarshalling an adapter autopopulates certain fields then the
    // best way to compare is to marshal both adapters back to xml and compare
    // them that way.
    
    // Now marshal standard format adapter to xml
    String standardMarshalledXML = xstreamInstance.toXML(standardAdapter);

    // Read in the expected beautified file.
    Adapter beautifiedAdapter = null;
    try ( InputStream resourceAsStream = this.getClass().getClassLoader().getResourceAsStream(XSTREAM_BEAUTIFIED_XML) ) {
      beautifiedAdapter = (Adapter) xstreamInstance.fromXML(resourceAsStream);
    }
    // Now marshal beautified format adapter to xml
    String beautifiedMarshalledXML = xstreamInstance.toXML(beautifiedAdapter);
    
    // Ensure that the conversion from standard xml to beautified xml went as expected 
    assertEquals(beautifiedMarshalledXML, standardMarshalledXML);
    }
    finally {
      AdapterXStreamMarshallerFactory.reset();

    }
  }
  
  // redmineID 2457 Beautifying the XStream output - roundtrip check
  public void testXStreamBeautifiedUnmarshal() throws Exception {
    try { // Create factory
    assertTrue(XpathQuery.class.isAssignableFrom(ConfiguredXpathQuery.class));
    AdapterXStreamMarshallerFactory factory = AdapterXStreamMarshallerFactory.getInstance();
    factory.setMode(OutputMode.ALIASED_SUBCLASSES);
    
    // Create xstream instance
    XStream xstreamInstance = factory.createXStream();
    Adapter unmarshalledAdapter = null;
    try ( InputStream resourceAsStream = this.getClass().getClassLoader().getResourceAsStream(XSTREAM_BEAUTIFIED_XML) ) {
      unmarshalledAdapter = (Adapter) xstreamInstance.fromXML(resourceAsStream);
    }
    adapterInstanceFieldChecks(unmarshalledAdapter);
 
    String xml = xstreamInstance.toXML(unmarshalledAdapter);
    Adapter roundTripAdapter = (Adapter)xstreamInstance.fromXML(xml);
    
    assertRoundtripEquality(unmarshalledAdapter, roundTripAdapter);
    }
    finally {
      AdapterXStreamMarshallerFactory.reset();

    }
  }
  
  // redmineID 2457 - ensures that marshalling/unmarshalling the given files results in no loss of data
  public static void adapterInstanceFieldChecks(Adapter fromXML) {
    assertNotNull(fromXML);
    assertEquals("SimpleAdapterTest", fromXML.getUniqueId());
    assertTrue(fromXML.logHandler() instanceof NullLogHandler);
    
    assertTrue(fromXML.getEventHandler() instanceof DefaultEventHandler);
    assertTrue(((DefaultEventHandler)fromXML.getEventHandler()).getConnection() instanceof NullConnection);
    assertTrue(((DefaultEventHandler)fromXML.getEventHandler()).getProducer() instanceof NullMessageProducer);
    // ShutdownWaitSeconds is now null.
    assertNull(((DefaultEventHandler) fromXML.getEventHandler()).getShutdownWaitSeconds());
    
    assertTrue(fromXML.getMessageErrorHandler() instanceof NullProcessingExceptionHandler);
    
    Channel channel = fromXML.getChannelList().get(0);
    assertTrue(channel.getConsumeConnection() instanceof NullConnection);
    assertTrue(channel.getProduceConnection() instanceof NullConnection);
    
    // Check workflow
    WorkflowList workflowList = channel.getWorkflowList();
    assertNotNull(workflowList);
    assertEquals(1, workflowList.size());
    // test workflow
    StandardWorkflow standardWorkflow = (StandardWorkflow) workflowList.get(0);
    assertNotNull(standardWorkflow);
    assertEquals("workflow1", standardWorkflow.getUniqueId());
    // test workflow consumer
    AdaptrisMessageConsumer consumer = standardWorkflow.getConsumer();
    assertNotNull(consumer);
    ConsumeDestination destination = consumer.getDestination();
    assertNotNull(destination);
    assertTrue(destination instanceof ConfiguredConsumeDestination);
    assertEquals("dummy", destination.getDestination());
    
    // test services
    ServiceCollection serviceCollection = standardWorkflow.getServiceCollection();
    assertNotNull(serviceCollection);
    assertEquals("serviceListID1", serviceCollection.getUniqueId());
    assertEquals(3, serviceCollection.size());
    assertTrue(serviceCollection instanceof ServiceList);
    // test service 1
    Service service1 = serviceCollection.get(0);
    assertEquals("serviceID1", service1.getUniqueId());
    assertTrue(service1 instanceof AddMetadataService);
    Set<MetadataElement> metadataElements = ((AddMetadataService)service1).getMetadataElements();
    assertEquals(1, metadataElements.size());
    for (Iterator<MetadataElement> iterator = metadataElements.iterator(); iterator.hasNext();) {
      MetadataElement metadataElement = (MetadataElement) iterator.next();
      assertEquals("key1", metadataElement.getKey());
      assertEquals("val1", metadataElement.getValue());
      break;
    }
    
    // test service 2
    Service service2 = serviceCollection.get(1);
    assertEquals("serviceID2", service2.getUniqueId());
    assertTrue(service2 instanceof XpathMetadataService);
    List<XpathQuery> xpathQueries = ((XpathMetadataService) service2).getXpathQueries();
    assertEquals(1, xpathQueries.size());
    assertEquals(ConfiguredXpathQuery.class, xpathQueries.get(0).getClass());
    assertEquals("/a/b/c", ((ConfiguredXpathQuery)xpathQueries.get(0)).getXpathQuery());
    
    // Test service3
    Service service3 = serviceCollection.get(2);
    assertEquals("serviceID3", service3.getUniqueId());
    assertTrue(service3 instanceof LogMessageService);
    assertTrue(StringUtils.isBlank(((LogMessageService)service3).getLogPrefix()));
    assertFalse(((LogMessageService)service3).getIncludeEvents());
    assertTrue(((LogMessageService)service3).getIncludePayload());
  }
  
//	/**
//	 * Creates a file of the given name on the filesystem that contains the given text
//	 * @param filename
//	 * @param text
//	 */
//	public static void generateFile(String filename, String text) {
//		try (FileWriter fw = new FileWriter(filename)) {
//			fw.write(text);
//		} catch (IOException e) {
//			System.err.println("Failed to generate file: "+filename);
//			e.printStackTrace();
//		}
//	}
}
