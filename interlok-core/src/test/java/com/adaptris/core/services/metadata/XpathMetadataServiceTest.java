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

package com.adaptris.core.services.metadata;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.CoreException;
import com.adaptris.core.ServiceException;
import com.adaptris.core.services.metadata.xpath.ConfiguredXpathQuery;
import com.adaptris.core.services.metadata.xpath.MetadataXpathQuery;
import com.adaptris.core.services.metadata.xpath.MultiItemConfiguredXpathQuery;
import com.adaptris.core.services.metadata.xpath.MultiItemMetadataXpathQuery;
import com.adaptris.core.services.metadata.xpath.XpathQuery;
import com.adaptris.core.util.DocumentBuilderFactoryBuilder;
import com.adaptris.util.KeyValuePair;
import com.adaptris.util.KeyValuePairSet;

@SuppressWarnings("deprecation")
public class XpathMetadataServiceTest extends MetadataServiceExample {

  public static final String XML = "<?xml version=\"1.0\"?><message><message-type>order"
      + "</message-type><source-id>partnera</source-id><destination-id>" + "partnerb</destination-id><body>...</body>"
      + "<extra att=\"att\">one</extra><extra att=\"two\">two</extra>" + "<extra att=\"two\">three</extra></message>";

  public static final String XML_WITH_NAMESPACE = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n"
      + "<svrl:schematron-output xmlns:svrl=\"http://purl.oclc.org/dsdl/svrl\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:xs=\"http://www.w3.org/2001/XMLSchema\" xmlns:sch=\"http://www.ascc.net/xml/schematron\" xmlns:iso=\"http://purl.oclc.org/dsdl/schematron\" xmlns:dp=\"http://www.dpawson.co.uk/ns#\" title=\"Anglia Farmers AF xml Invoice Schematron File\" schemaVersion=\"ISO19757-3\">\n"
      + "<svrl:ns-prefix-in-attribute-values uri=\"http://www.dpawson.co.uk/ns#\" prefix=\"dp\"/>\n" + "<svrl:active-pattern/>\n"
      + "<svrl:fired-rule context=\"SageData/JoinedData\"/>\n"
      + "<svrl:failed-assert test=\"STOCK_CODE != ''\" location=\"/SageData[1]/JoinedData[1]\">\n"
      + "<svrl:text>Error: Product Code must be present.</svrl:text>\n" + "</svrl:failed-assert>\n"
      + "<svrl:fired-rule context=\"SageData/JoinedData/CUST_ORDER_NUMBER[. != '']\"/>\n"
      + "<svrl:fired-rule context=\"SageData/JoinedData/AF_NUMBER[. != '']\"/>\n"
      + "<svrl:failed-assert test=\"string-length(.) = 5\" location=\"/SageData[1]/JoinedData[1]/AF_NUMBER[1]\">\n"
      + "<svrl:text>Error: Anglia Farmer's Supplier Number must be 5 digits long. (Current Value: 62826123)</svrl:text>\n"
      + "</svrl:failed-assert>\n" + "</svrl:schematron-output>";

  public static final String XML_WITH_DOCTYPE = "<?xml version=\"1.0\"?>\n" + "<!DOCTYPE document [\n"
      + "<!ENTITY LOCAL_ENTITY 'entity'>\n" + "<!ENTITY % StandardInfo SYSTEM \"../StandardInfo.dtd\">\n" + "%StandardInfo;\n"
      + "]>\n" + "<document>\n" + "</document>\n";

  public XpathMetadataServiceTest(String name) {
    super(name);
  }

  @Override
  protected void setUp() throws Exception {
    super.setUp();
  }

  public void testSetNamespaceContext() {
    XpathMetadataService obj = new XpathMetadataService();
    assertNull(obj.getNamespaceContext());
    KeyValuePairSet kvps = new KeyValuePairSet();
    kvps.add(new KeyValuePair("hello", "world"));
    obj.setNamespaceContext(kvps);
    assertEquals(kvps, obj.getNamespaceContext());
    obj.setNamespaceContext(null);
    assertNull(obj.getNamespaceContext());
  }

  public void testDoService_NotXML() throws Exception {
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage("ABCDEFG");
    XpathMetadataService service = new XpathMetadataService();
    service.setXpathQueries(new ArrayList<XpathQuery>(Arrays.asList(new ConfiguredXpathQuery("source",
        "//source-id"), new ConfiguredXpathQuery("destination", "//destination-id"))));
    try {
      execute(service, msg);
      fail();
    }
    catch (ServiceException expected) {

    }
  }

  public void testDoService_UsingXpathQuery() throws Exception {
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(XML);
    XpathMetadataService service = new XpathMetadataService();
    service.setXpathQueries(new ArrayList<XpathQuery>(Arrays.asList(new ConfiguredXpathQuery("source",
        "//source-id"), new ConfiguredXpathQuery("destination", "//destination-id"))));
    execute(service, msg);
    assertEquals("partnera", msg.getMetadataValue("source"));
    assertEquals("partnerb", msg.getMetadataValue("destination"));
  }

  public void testDoService_UsingXpathQuery_WithNamespaceContext() throws CoreException {
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(XML_WITH_NAMESPACE);
    XpathMetadataService service = new XpathMetadataService();
    service.setXpathQueries(new ArrayList<XpathQuery>(Arrays.asList(

    new ConfiguredXpathQuery("failureCount", "count(/svrl:schematron-output/svrl:failed-assert)"))));
    service.setNamespaceContext(createContextEntries());

    execute(service, msg);

    assertTrue(msg.containsKey("failureCount"));
    assertEquals("2", msg.getMetadataValue("failureCount"));
  }

  public void testDoService_DisableDocType() throws CoreException {
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(XML_WITH_DOCTYPE);
    XpathMetadataService service = new XpathMetadataService();
    // Shouldn't matter what the query actually is.
    service.setXpathQueries(new ArrayList<XpathQuery>(Arrays.asList(new ConfiguredXpathQuery("source",
        "//source-id"), new ConfiguredXpathQuery("destination", "//destination-id"))));
    DocumentBuilderFactoryBuilder builder = new DocumentBuilderFactoryBuilder();
    builder.getFeatures().add(new KeyValuePair("http://apache.org/xml/features/disallow-doctype-decl", "true"));
    service.setXmlDocumentFactoryConfig(builder);
    try {
      execute(service, msg);
      fail();
    } catch (ServiceException expected) {
      assertTrue(expected.getMessage().contains("DOCTYPE is disallowed"));
    }
  }
  public void testSetXpathQueryList() {
    XpathMetadataService service = new XpathMetadataService();
    assertEquals(0, service.getXpathQueries().size());
    XpathQuery query = new ConfiguredXpathQuery("metadataKey", "//root");
    List<XpathQuery> list = new ArrayList<>();
    list.add(query);
    service.setXpathQueries(list);
    assertEquals(list, service.getXpathQueries());
    try {
      service.setXpathQueries(null);
      fail();
    }
    catch (IllegalArgumentException e) {

    }
    assertEquals(list, service.getXpathQueries());
  }

  public void testAddXpathQuery() {
    XpathMetadataService service = new XpathMetadataService();
    ConfiguredXpathQuery query = new ConfiguredXpathQuery("failureCount", "count(/svrl:schematron-output/svrl:failed-assert)");
    service.addXpathQuery(query);
    assertEquals(1, service.getXpathQueries().size());
    assertEquals(query, service.getXpathQueries().get(0));
    try {
      service.addXpathQuery(null);
      fail();
    }
    catch (IllegalArgumentException expected) {

    }
    assertEquals(1, service.getXpathQueries().size());
    assertEquals(query, service.getXpathQueries().get(0));

  }

  public void testDoService_UsingXpathQuery_WithNamespaceContext_NotNamespaceAware() throws CoreException {
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(XML_WITH_NAMESPACE);
    XpathMetadataService service = new XpathMetadataService();
    service.setXpathQueries(new ArrayList<XpathQuery>(Arrays.asList(
        new ConfiguredXpathQuery("failureCount", "count(/schematron-output/failed-assert)"))));
    execute(service, msg);

    // count(/schematron-output/failed-assert) will return 0; becaue Saxon failed
    // like the mostly amusing thing that it is.
    assertEquals("0", msg.getMetadataValue("failureCount"));

    service.setXmlDocumentFactoryConfig(new DocumentBuilderFactoryBuilder().withNamespaceAware(false));
    execute(service, msg);
    assertTrue(msg.containsKey("failureCount"));
    assertEquals("2", msg.getMetadataValue("failureCount"));
  }

  @Override
  protected Object retrieveObjectForSampleConfig() {
    XpathMetadataService service = new XpathMetadataService();
    service.setXpathQueries(new ArrayList<XpathQuery>(Arrays.asList(
        new ConfiguredXpathQuery("key1", "//source-id"), new ConfiguredXpathQuery("key2", "//destination-id"),
        new MetadataXpathQuery("key3", "metadataKey_containing_an_XPath"), new MultiItemConfiguredXpathQuery("key4",
            "//xpath/that/resolves/to/multiple/items"), new MultiItemMetadataXpathQuery("key5",
            "metadata_containing_an_Xpath_that_resolves_to_multiple_items"), new ConfiguredXpathQuery("key6",
            "/svrl:output/svrl:value"))));
    service.setNamespaceContext(createContextEntries());
    return service;
  }

  @Override
  protected String getExampleCommentHeader(Object object) {
    return super.getExampleCommentHeader(object) + "\n<!--"
        + "\nIf you wish to have namespace support then you will need to explicitly map the "
        + "\nnamespace prefixes against the namespace URI using the namespace-context field."
        + "\nThis is a set of key value pairs, where the key is the prefix, and the value" + "\nis the namespace URI" + "\n-->\n\n";
  }

  public static KeyValuePairSet createContextEntries() {
    KeyValuePairSet contextEntries = new KeyValuePairSet();
    contextEntries.add(new KeyValuePair("svrl", "http://purl.oclc.org/dsdl/svrl"));
    contextEntries.add(new KeyValuePair("xsd", "http://www.w3.org/2001/XMLSchema"));
    contextEntries.add(new KeyValuePair("xs", "http://www.w3.org/2001/XMLSchema"));
    contextEntries.add(new KeyValuePair("sch", "http://www.ascc.net/xml/schematron"));
    contextEntries.add(new KeyValuePair("iso", "http://purl.oclc.org/dsdl/schematron"));
    contextEntries.add(new KeyValuePair("dp", "http://www.dpawson.co.uk/ns#"));
    return contextEntries;
  }
}
