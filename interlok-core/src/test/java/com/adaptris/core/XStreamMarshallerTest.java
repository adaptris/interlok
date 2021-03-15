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

import com.adaptris.core.services.LogMessageService;
import com.adaptris.core.services.metadata.AddMetadataService;
import com.adaptris.core.services.metadata.XpathMetadataService;
import com.adaptris.core.services.metadata.xpath.ConfiguredXpathQuery;
import com.adaptris.core.services.metadata.xpath.XpathQuery;
import com.adaptris.core.stubs.XStreamCDataWrapper;
import com.adaptris.core.stubs.XStreamImplicitWrapper;
import com.adaptris.core.util.XmlHelper;
import com.adaptris.util.text.xml.XPath;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import java.util.Iterator;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;


@SuppressWarnings("deprecation")
public class XStreamMarshallerTest
    extends com.adaptris.interlok.junit.scaffolding.MarshallingBaseCase {

  private static final String XSTREAM_STANDARD_XML = "xstream-standard.xml";

  @Override
  protected XStreamMarshaller createMarshaller() throws Exception {
    return new XStreamMarshaller();
  }

  @Override
  protected String getClasspathXmlFilename() {
    return "xstream-standalone.xml";
  }

  @Test
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

  @Test
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
    NullMessageConsumer consumer = (NullMessageConsumer) standardWorkflow.getConsumer();
    assertNotNull(consumer);

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
      MetadataElement metadataElement = iterator.next();
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
  }

}
